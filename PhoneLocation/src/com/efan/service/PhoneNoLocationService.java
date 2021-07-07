/*
 * 系统名称: 
 * 模块名称: 
 * 类  名   称: 
 * 软件版权: 
 * 开发人员: 
 * 开发时间: 2010-8-29
 * 审核人员:
 * 相关文档:
 * 修改记录: 修改日期 修改人员 修改说明
 */
package com.efan.service;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import com.efan.model.Location;
import com.efan.model.NoRecordEntity;
import com.efan.util.BytesUtil;
import com.efan.util.Constants;
import com.efan.util.FileUtil;

/**
 * @author feelow
 * 电话归属地查询服务
 */
public class PhoneNoLocationService extends LocationService {
	private static LocationService instance = null;
	private static short phoneType = Constants.PHONE_TYPE_TEL;
	private static RandomAccessFile dbFile = null;

//	private static MappedByteBuffer mbb = null;


	//首号段记录偏移地址
	private static Long numStartOffset = null;
	//末号段记录偏移地址
	private static Long numEndOffset = null;
	//区号起始偏移地址
	private static Long zoneStartOffset = null;
	//市级城市区号起始偏移地址
	private static Long cityZoneStartOffset = null;
	private static Long zoneEndOffset = null;
	//当前文件偏移地址
	private static Long currOffset = null;
	//版本号
	private static String version = "0";

	protected PhoneNoLocationService() {}

	/* 
	 * 根据电话号码取出归属地信息
	 */
	@Override
	public Location getLocation(String phoneNo) throws IOException {
		Location location = null;
		//对电话号码进行处理,若长度大于返回11位串
		phoneNo = processPhoneNo(phoneNo);
		
		//判断电话号码是固话还是手机
		phoneType = getPhoneType(phoneNo);

		switch (phoneType) {
		case Constants.PHONE_TYPE_MOB :
			//查手机号码归属地数据库
			location = getLocationFromMobileNo(phoneNo);
			break;
		case Constants.PHONE_TYPE_TEL:
			//查区号表
			location = getLocationFromZoneTable(phoneNo);
			break;
		default:
		}

		return location;
	}

	public static LocationService getInstance(File file) {
		try {
			if (instance == null) {

				instance = new PhoneNoLocationService();
				init(file);
			}
		} catch (Exception e) {
			System.out.println("LocationService initialation failed");
			instance = null;
			e.printStackTrace();
		}

		return instance;
	}

	public static LocationService getInstance(String datFilename) {
		File file = new File(datFilename, "r");
		
		return getInstance(file);

	}
	


	/**
	 * @param phoneNo
	 * @return
	 */
	private String processPhoneNo(String phoneNo) {
		String ret = phoneNo;
		
		if (phoneNo.length() > 11) {
			ret = phoneNo.substring(phoneNo.length() - 11, phoneNo.length());
		}
		return ret;
	}

	@Override
	public void destroy() {
		try {
			dbFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		instance = null;
	}
	
	public String getDbFileVersion() {
		return version;
	}
	
	/**
	 * 根据电话号码区号查出对应归属地
	 * 取区号查表
	 * @param phoneType
	 * @return
	 * @throws IOException 
	 */
	private Location getLocationFromZoneTable(String phoneNo) throws IOException {
		Location location = null;
		/*
		 *  先取前三位，若查找失败再取前四位,因为有四位区号也有3位区号,如:028,0817
		 */
		String zoneCode = phoneNo.substring(0, 3);
		location = seekByZoneCode(zoneCode);
		if (location == null) {
			zoneCode = phoneNo.substring(0, 4);
			location = seekByZoneCode(zoneCode);
		}

		return location;
	}

	/**
	 * 根据城市索引查出对应省对象,返回location对象(不含区号)
	 * @param cityOffset
	 * @return
	 * @throws IOException 
	 */
	private Location getLocationByCityOffset(Long cityOffset) throws IOException {
		Long provinceOffset = null;
		String city = "";
		String province = "";

		dbFile.seek(cityOffset);
//		mbb.position(cityOffset.intValue());
		//读出城市名称和所属省索引
		city = getString();
		provinceOffset = FileUtil.read3Long(dbFile);

//		city = getStringFromMappedBytes();
//		provinceOffset = read3LongFromMappedBytes();

		if (provinceOffset < Constants.MAX_OFFSET) {
			dbFile.seek(provinceOffset);
			province = getString();
//			mbb.position(provinceOffset.intValue());
//			province = getStringFromMappedBytes();
		}

		return new Location("", province, city, "");
	}
	/**
	 * @param zoneCode
	 * @return
	 * @throws IOException 
	 */
	private Location seekByZoneCode(String code) throws IOException {
		Location location = null;
		String city = "";
		short seekCount = 0;
		Long provinceOffset = null;
		String provinceName = "";
		Long begin = cityZoneStartOffset;
		Long mid = null;
		Long end = zoneEndOffset;
		List<Long> offsets = new ArrayList<Long>();
		ZoneRecord zoneRecord = new ZoneRecord();
		byte[] zoneBytes = new byte[Constants.ZONE_RECORD_LENGTH];

		short key = Short.parseShort(code);

		//二分法查找区号索引
		while (begin < end) {
			mid = getMidOffset(begin, end, Constants.ZONE_RECORD_LENGTH);
			dbFile.seek(mid);
			dbFile.read(zoneBytes);
			zoneRecord.parseBytes(zoneBytes);

			if (zoneRecord.zoneCode > key) {
				if (end.equals(mid)) {
					end -= Constants.ZONE_RECORD_LENGTH;
				} else {
					end = mid;
				}
			} else if (zoneRecord.zoneCode < key) {
				begin = mid;
			} else {
				break;
			}
		}

		//找到区号索引
		if (begin < end) {
			offsets.add(zoneRecord.cityOffset);
			dbFile.seek(dbFile.getFilePointer() - Constants.ZONE_RECORD_LENGTH);
			currOffset = dbFile.getFilePointer();

			//向前查找是否仍有相同区号的
			while (dbFile.getFilePointer() < numStartOffset) {
				dbFile.seek(dbFile.getFilePointer() + Constants.ZONE_RECORD_LENGTH);
				seekCount++;
				dbFile.read(zoneBytes);
				zoneRecord.parseBytes(zoneBytes);
				if (zoneRecord.zoneCode == key) {
					offsets.add(zoneRecord.cityOffset);
				} else {
					break;
				}
			}
			//向后查找,先恢复到原来的位置
			dbFile.seek(currOffset);
			while(dbFile.getFilePointer() > zoneStartOffset) {
				dbFile.seek(dbFile.getFilePointer() - Constants.ZONE_RECORD_LENGTH);
				dbFile.read(zoneBytes);
				dbFile.seek(dbFile.getFilePointer() - Constants.ZONE_RECORD_LENGTH);
				zoneRecord.parseBytes(zoneBytes);

				if (zoneRecord.zoneCode == key) {
					offsets.add(zoneRecord.cityOffset);
				} else {
					break;
				}
			}

			for (Long cityOffset : offsets) {
				dbFile.seek(cityOffset);
//				mbb.position(cityOffset.intValue());

				if (city.length() == 0) { //查找所属省
					city = getString();
					provinceOffset = FileUtil.read3Long(dbFile);
//					city = getStringFromMappedBytes();
//					provinceOffset = read3LongFromMappedBytes();
					if (provinceOffset < Constants.MAX_OFFSET) {
						dbFile.seek(provinceOffset);
						provinceName = getString();
//						mbb.position(provinceOffset.intValue());
//						provinceName = getStringFromMappedBytes();
					} 
				}else {
					city += "/" + getString();
//					city += "/" + getStringFromMappedBytes();

				}
			}
		}

		if (city.length() > 0) {
			location = new Location(code, provinceName, city, 
					Constants.AGENT_NAME_CT);
		}

		return location;
	}

	/**
	 * @return
	 * @throws IOException 
	 * @throws UnsupportedEncodingException 
	 */
	private String getString() throws UnsupportedEncodingException, IOException {
		String str = dbFile.readLine();
		return new String(str.getBytes("iso8859-1"), "gb2312");
	}

/*	private String getStringFromMappedBytes() throws UnsupportedEncodingException {
		StringBuffer input = new StringBuffer();
		
		int c = -1;
		boolean eol = false;

		while (!eol) {
			switch (c = mbb.get()) {
			case -1:
			case '\n':
				eol = true;
				break;
			case '\r':
				eol = true;
				long cur = mbb.position();
				if ((mbb.get()) != '\n') {
					mbb.position((int)cur);
				}
				break;
			default:
				input.append((char)(c & 0xff));
				break;
			}
		}

		if ((c == -1) && (input.length() == 0)) {
			return null;
		}
		return new String((input.toString()).getBytes("iso8859-1"), "gb2312");
	}*/

	class ZoneRecord {
		public short zoneCode;//区号,两位
		public Long cityOffset;//城市索引地址,3位

		public ZoneRecord() {}

		public ZoneRecord(byte[] bytes) {
			parseBytes(bytes);
		}

		public void parseBytes(byte[] bytes) {
			zoneCode = BytesUtil.readShort(bytes, 0, Constants.ZONE_RECORD_ZONECODE_LENGTH);
			cityOffset = BytesUtil.readLong(bytes, Constants.ZONE_RECORD_ZONECODE_LENGTH, 
					Constants.ZONE_RECORD_ZONECODE_LENGTH + Constants.ZONE_RECORD_CITYOFFSET_LENGTH);
		}

	}


	/**
	 * 根据手机号码查出对应对属地
	 * 取号码前7位
	 * @param phoneType
	 * @return
	 * @throws IOException 
	 */
	private Location getLocationFromMobileNo(String phoneNo) throws IOException {
		Location location = null;
		Long upOffset = numEndOffset;
		Long downOffset = numStartOffset;
		Long midOffset = null;
		byte[] noBytes = new byte[Constants.NO_RECORD_LENGTH];
		byte[] zoneBytes = new byte[Constants.ZONE_RECORD_LENGTH];
		NoRecordEntity noRecord = new NoRecordEntity();
		ZoneRecord zoneRecord = new ZoneRecord();

		//待查找的key
		int key = Integer.parseInt(phoneNo.substring(0, 7));

		//二分法查找归属地
		while (downOffset < upOffset) {
			midOffset = getMidOffset(downOffset, upOffset, Constants.NO_RECORD_LENGTH);
			//读取开始号段,结束号段
			dbFile.seek(midOffset);
			dbFile.read(noBytes);
			noRecord.parseBytes(noBytes);

			if (key < noRecord.getCompleteNoStart()) { //开始号段
				if (midOffset.equals(upOffset)) {
					upOffset -= Constants.NO_RECORD_LENGTH;
				} else {
					upOffset = midOffset;
				}
			} else if (key > noRecord.getCompleteNoEnd()) {//结束号段
				downOffset = midOffset;
			} else {
				//找到
				break;
			}
		}

		if (downOffset < upOffset) {
			//跳到区号索引处
			dbFile.seek(zoneStartOffset + noRecord.getZoneOffset() *
					Constants.ZONE_RECORD_LENGTH);

			dbFile.read(zoneBytes);
			zoneRecord.parseBytes(zoneBytes);
			//读出区号,城市索引偏移
			location = getLocationByCityOffset(zoneRecord.cityOffset);
			location.setZoneCode( "0" + zoneRecord.zoneCode);
			location.setAgentName(getAgentName(phoneNo));
		}

		return location;
	}

	/**
	 * @param phoneNo
	 * @return
	 */
	private String getAgentName(String phoneNo) {
		String first3no = phoneNo.substring(0, 3);
		if (first3no.equals("134") || 
				first3no.equals("135") ||
				first3no.equals("136") ||
				first3no.equals("137") ||
				first3no.equals("138") ||
				first3no.equals("139") ||
				first3no.equals("145") ||
				first3no.equals("147") ||
				first3no.equals("150") ||				
				first3no.equals("151") ||
				first3no.equals("152") ||
				first3no.equals("154") ||
				first3no.equals("157") ||
				first3no.equals("158") ||
				first3no.equals("159") ||
				first3no.equals("187") ||
				first3no.equals("188")) {
			return Constants.AGENT_NAME_CM;
		} else if (first3no.equals("133") ||
				first3no.equals("153") ||
				first3no.equals("180") ||
				first3no.equals("189")) {
			return Constants.AGENT_NAME_CT;
		} else if (first3no.equals("130") ||
				first3no.equals("131") ||
				first3no.equals("132") ||
				first3no.equals("133") ||
				first3no.equals("155") ||
				first3no.equals("156") ||
				first3no.equals("185") ||
				first3no.equals("186") ) {
			return Constants.AGENT_NAME_CU;
		}
		return Constants.AGENT_NAME_UKN;
	}

	/**
	 * 获取down 与 up中间记录偏移地址
	 * @param downOffset
	 * @param upOffset
	 * @return
	 */
	private Long getMidOffset(Long downOffset, Long upOffset, short length) {
		long records = (upOffset - downOffset) / length ;
		records >>= 1;
		if (0 == records) {
			records = 1;
		}

		return downOffset + records * length;
	}

	/**
	 * 返回电话号码类型
	 * @param phoneNo
	 * @return
	 */
	private short getPhoneType(String phoneNo) {	
		if (phoneNo.length() != 11) {
			return Constants.PHONE_TYPE_OTHER;
		}
		
		if ( phoneNo.startsWith("0")) {
			return Constants.PHONE_TYPE_TEL;
		}
		
		if (phoneNo.startsWith("1")) {
			return Constants.PHONE_TYPE_MOB;
		}

		return Constants.PHONE_TYPE_OTHER;
	}

	private static void init(File file) throws IOException {
		dbFile = new RandomAccessFile(file, "r");

		byte[] headBytes = new byte[Constants.HEAD_LENGTH];
		dbFile.read(headBytes);

		//初始化各个偏移地址 
		version = BytesUtil.readString(headBytes, 0, Constants.HEAD_VERSION_LENGTH);
		
		numStartOffset = BytesUtil.readLong(headBytes, Constants.HEAD_VERSION_LENGTH + 0 * Constants.HEAD_FILED_LENGTH, 
				Constants.HEAD_VERSION_LENGTH + 1 * Constants.HEAD_FILED_LENGTH);
		numEndOffset =BytesUtil.readLong(headBytes, Constants.HEAD_VERSION_LENGTH + 1 * Constants.HEAD_FILED_LENGTH, 
				Constants.HEAD_VERSION_LENGTH + 2 * Constants.HEAD_FILED_LENGTH);
		zoneStartOffset = BytesUtil.readLong(headBytes, Constants.HEAD_VERSION_LENGTH + 2 * Constants.HEAD_FILED_LENGTH, 
				Constants.HEAD_VERSION_LENGTH + 3 * Constants.HEAD_FILED_LENGTH);
		cityZoneStartOffset = BytesUtil.readLong(headBytes, Constants.HEAD_VERSION_LENGTH + 3 * Constants.HEAD_FILED_LENGTH, 
				Constants.HEAD_VERSION_LENGTH + 4 * Constants.HEAD_FILED_LENGTH);
		zoneEndOffset = numStartOffset - Constants.ZONE_RECORD_LENGTH;
		//将省份数据和城市数据映射到内存,以加快查询速度
//		mbb = dbFile.getChannel().map(
//				FileChannel.MapMode.READ_ONLY, 0, numStartOffset);
//		mbb.order(ByteOrder.LITTLE_ENDIAN);

	}
/*
	private long read3LongFromMappedBytes() {
		byte[] bytes = new byte[3];
		mbb.get(bytes);

		return (long)((bytes[0] << 16 & 0xff0000) | 
				(bytes[1] << 8 & 0xff00) |
				(bytes[2] & 0xff)) ;
	}*/

}
