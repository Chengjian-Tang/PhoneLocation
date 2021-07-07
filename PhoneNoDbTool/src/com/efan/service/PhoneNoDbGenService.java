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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.efan.model.NoRecordEntity;
import com.efan.util.Constants;

/**
 * @author feelow
 *
 */
public class PhoneNoDbGenService {
	private String provinceFilename = null;
	private String cityFilename = null;
	private String phonenoFilename = null;
	private String outputFilename = null;
	private RandomAccessFile outputFile = null;
	private NoRecordEntity noRecordEntity = new NoRecordEntity();
	//版本号
	private String version = "20100905";

	//当前号段记录起始偏移地址
	private Long currRecordOffset;

	private HashMap<String, Long> provinceMap = new HashMap<String, Long>(); 
//	private HashMap<String, Long> headCityMap = new HashMap<String, Long>();
	private List<FieldEntity> headCities = new ArrayList<FieldEntity>();
//	private List<FieldEntity> headZoneCodes = new ArrayList<FieldEntity>();
	private ArrayList<String> headZoneCodes = new ArrayList<String>();
	private List<CityEntity> citiesIndex = new ArrayList<CityEntity>();
	private List<CityEntity> areasIndex = new ArrayList<CityEntity>();
	//尾部城市表，主要用于直辖市，如:重庆
	private HashMap<String, NeedTailCity> nedTailCities = new HashMap<String, NeedTailCity>();

	private static final int HEAD_FIELD_LENGTH = 3;

	private static final short STATUS_START = 0;
	private static final short STATUS_NOSTART = (short) (STATUS_START + 1);
	private static final short STATUS_NOMID = (short) (STATUS_START + 2);
	private static final short STATUS_NOEND = (short) (STATUS_START + 3);

	private short currStatus = STATUS_START;
	
	class CityEntity {
		//两个字节长度
		public int zoneCode ;
		//对应城市的偏移量 3字节
		public Long cityOffset;
		
		public String cityName;
		/**
		 * @param zoneCode
		 * @param cityOffset
		 */
		public CityEntity(int zoneCode, Long cityOffset, String cityName) {
			super();
			this.zoneCode = zoneCode;
			this.cityOffset = cityOffset;
			this.cityName = cityName;
		}
		
		
	}

	class NeedTailCity {
		//需要修改记录
		public ArrayList<Long> recordOffsets = new ArrayList<Long>();

		public String cityName = "";

		/**
		 * @param recordOffset
		 * @param cityName
		 */
		public NeedTailCity(Long recordOffset, String cityName) {
			super();
			recordOffsets.add(recordOffset);
			this.cityName = cityName;
		} 


	}

	class Location {
		public short pid = 0;
		public short phoneno = 0;
		public String city = "";

		public Location(String phoneno, String city) {
			pid = Short.parseShort(phoneno.substring(0, 3));
			this.phoneno = Short.parseShort(phoneno.substring(3));
			this.city = city;
		}

	}

	/**
	 * @param provincFilename
	 * @param cityFilename
	 * @param phonenoFilename
	 * @param outputFilename
	 */
	public PhoneNoDbGenService(String provincFilename, String cityFilename,
			String phonenoFilename, String outputFilename, String version) {
		super();
		this.provinceFilename = provincFilename;
		this.cityFilename = cityFilename;
		this.phonenoFilename = phonenoFilename;
		this.outputFilename = outputFilename;
		this.version = version;
	}

	/**
	 * @param provinceFilename
	 * @param cityFilename
	 * @param phonenoFilename
	 * @param outputFilename
	 */
	public PhoneNoDbGenService(String provinceFilename, String cityFilename,
			String phonenoFilename, String outputFilename) {
		super();
		this.provinceFilename = provinceFilename;
		this.cityFilename = cityFilename;
		this.phonenoFilename = phonenoFilename;
		this.outputFilename = outputFilename;
	}



	public boolean createDatfile() throws IOException {
		long currOffset = 0;

		if (isEmpty(outputFilename)) {
			System.out.println("输出文件不能为空");
			return false;
		}
		outputFile = new RandomAccessFile(outputFilename, "rw");
		//清空
		outputFile.setLength(0);
		//写入版本号
		System.out.println("数据版本号:" + version);
		outputFile.write(version.getBytes());
		//首先空出文件头
		outputFile.seek(Constants.HEAD_LENGTH);

		//写入省份数据
		if (isEmpty(provinceFilename)) {
			System.out.println("省份数据文件不能为空");
			return false;
		}
		writeProvinceData();
		//写入城市数据
		//将城市数据起始偏移地址写入文件头最后一个字节
		currOffset = outputFile.length();
		System.out.println("城市数据起始偏移地址为: " + currOffset);
		outputFile.seek(Constants.HEAD_LENGTH - 1 * HEAD_FIELD_LENGTH);
		writeLong3(currOffset);
		outputFile.seek(currOffset);

		if (isEmpty(cityFilename)) {
			return false;
		}
		/*
		 * 先写城市数据,然后写城市索引表
		 */
		writeCityData();
		writeCityIndex();

		//写入号码段数据
		if (isEmpty(phonenoFilename)) {
			return false;
		}
		writePhonenoData();

		//写入尾部直辖市表
		if (nedTailCities.size() > 0) {
			System.out.println("开始创建尾部城市表");
			writeTailCities();
		}
		
		outputFile.close();
		return true;
	}

	/**
	 * @return
	 * @throws IOException 
	 */
	private void writePhonenoData() throws IOException {

		File file = new File(phonenoFilename);
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(file), "gb2312"));
		String lineStr = null;
		Location prevLocation = null;
		Location location = null;

		//在文件头写入记录起始偏移量
		Long currOffset = outputFile.length();
		outputFile.seek(0 + Constants.HEAD_VERSION_LENGTH);
		writeLong3(currOffset);
		outputFile.seek(currOffset);

		/*使用状态机进行号码段写入, 格式为:
		 * 号段起始值3字节　号段结束值3字节　索引模式1字节 索引偏移地址3字节
		 * 其中索引模式取值1 为头部城市表查找，值2尾部直辖市表查找
		 */
		while ((lineStr = br.readLine()) != null) {
			if (lineStr.trim().length() != 0) {
				prevLocation = location;
				location = new Location(lineStr.split(" ")[1],
						lineStr.split(" ")[2]);

				switch (currStatus) {
				case STATUS_START:
					gotoNOSTART(prevLocation, location);
					break;
				case STATUS_NOSTART:
					if (location.city.equals(prevLocation.city)) {
						gotoNOMID(prevLocation, location);
					} else {
						gotoNOEND(prevLocation, location);
						gotoNOSTART(prevLocation, location);
					}
					break;
				case STATUS_NOMID:
					if (!location.city.equals(prevLocation.city)) {
						gotoNOEND(prevLocation, location);
						gotoNOSTART(prevLocation, location);
					}
					break;
				case STATUS_NOEND:
					gotoNOSTART(prevLocation, location);
					break;
				default:
				}
			}
		}
		//文件结束处理
		if (currStatus != STATUS_NOEND) {
			gotoNOEND(prevLocation, location);
		}

		//写入文件头末记录偏移地址
		currOffset = outputFile.length();
		outputFile.seek(0 + Constants.HEAD_VERSION_LENGTH + Constants.HEAD_FILED_LENGTH);
		writeLong3(currOffset - Constants.NO_RECORD_LENGTH);
		outputFile.seek(currOffset);
		
	}

	/**
	 * 字段之间已 \0 分隔，记录之间以\n 分隔
	 * 如
	 *　区位　名称　对应省偏移地址
	 * xx\0南充\0xxx\n
	 */
	private void writeCityData() throws IOException {
		File file = new File(cityFilename);

		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(file), "gb2312"));
		String lineStr = null;
		String province = null;
		Long provinceOffset = 0L;
		String[] fields = null;
		String city = null;
		char cityType = 1;
		int zoneCode = 0;

		while ((lineStr = br.readLine()) != null) {
			if (lineStr.trim().length() != 0) { //不位空白行
//				System.out.println("该行为:" + lineStr);

				if (provinceStrUniprocess(lineStr).length() < 5) {//该行为省名称
					province = provinceStrUniprocess(lineStr);
					provinceOffset = provinceMap.get(province);

					//未找到　则地址为0x0000000000ffffff, 表明该行为直辖市
					if (provinceOffset == null) {
						provinceOffset = (long) 0x0000000000ffffff;
					}

					System.out.print("省:" + province + "   ");
					System.out.println("偏移地址:" + provinceOffset);
				} else { //该行为城市名称和区号,空格分隔
					fields = lineStr.split(" ");
					cityType = (char) (fields[0].charAt(0) - '0');
					city = cityStrUniprocess(fields[1]);
					zoneCode = Integer.parseInt(fields[2].trim());

					System.out.println("城市类型" + ((int)cityType) + "  市:" + city + " 区号:" + zoneCode + " 所属省偏移地址:" +
							provinceOffset + "   偏移地址:" + outputFile.length());

//					headCityMap.put(city, outputFile.length());
					headCities.add(new FieldEntity(outputFile.length(), city));
					
					if (cityType == 0) {
						areasIndex.add(new CityEntity(zoneCode, 0x0000000000ffffffl,
								city));
					} else if (cityType == 1) {
						citiesIndex.add(new CityEntity(zoneCode, 0x0000000000ffffffl,
								city));
					}
					
//					outputFile.writeByte(cityType);
//					writeInt2(zoneCode);
					outputFile.write((city + "\n").getBytes("gb2312"));
					writeLong3(provinceOffset);
//					outputFile.write("\n".getBytes());

				}
			}
		}
	}
	
	/**
	 * 写入城市索引信息
	 * @throws IOException
	 */
	private void writeCityIndex() throws IOException {
		//设置索引信息中的偏移地址
		setIndexOffset(areasIndex);
		setIndexOffset(citiesIndex);
		
		//对市级城市索引按区号进行排序
        Comparator comp = new CityIndexComparator(); 
        Collections.sort(citiesIndex, comp);
        //将区号信息索引数据起始地址写入文件头
        Long currOffset = outputFile.length();
        outputFile.seek(Constants.HEAD_LENGTH - 2 * HEAD_FIELD_LENGTH); 
        writeLong3(currOffset);
        outputFile.seek(currOffset);
		//写入非市级城市信息
		writeIndex(areasIndex);
        //将市级城市区号索引起始地址写入文件头
        currOffset = outputFile.length();
        outputFile.seek(Constants.HEAD_LENGTH - HEAD_FIELD_LENGTH); 
        writeLong3(currOffset);
        outputFile.seek(currOffset);
        //写入市级城市索引地址
        writeIndex(citiesIndex);
	}
	
	class CityIndexComparator implements Comparator {

		@Override
		public int compare(Object arg0, Object arg1) {
			if (((CityEntity)arg0).zoneCode > ((CityEntity)arg1).zoneCode) {
				return 1;
			} else if (((CityEntity)arg0).zoneCode < ((CityEntity)arg1).zoneCode) {
				return -1;
			}
			return 0;
		}
		
	}

	/**
	 * @param areasIndex2
	 * @throws IOException 
	 */
	private void writeIndex(List<CityEntity> index) throws IOException {
		for (CityEntity city : index) {
			System.out.println("城市名称: " + city.cityName + " 偏移地址:" + 
					outputFile.getFilePointer());
			headZoneCodes.add(city.cityName);
			writeInt2(city.zoneCode);
			writeLong3(city.cityOffset);
		}
	}

	/**
	 * @param citiesIndex2
	 */
	private void setIndexOffset(List<CityEntity> index) {
		for (CityEntity city : index) {
			city.cityOffset = getOffset(headCities, city.cityName);
		}
	}

	/**
	 * 记录之间以\n分隔
	 * 如 四川\n广州
	 * @throws IOException 
	 */
	private void writeProvinceData() throws IOException {
		File file = new File(provinceFilename);

		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(file), "gb2312"));
		String lineStr = null;

		while ((lineStr = br.readLine()) != null) {
			provinceMap.put(provinceStrUniprocess(lineStr), outputFile.getFilePointer());
			outputFile.write((provinceStrUniprocess(lineStr) + "\n").getBytes("gb2312"));
		}
	}

	/**
	 * 将Long型的偏移量低3字节写入文件
	 * @param num
	 * @throws IOException 
	 */
	private void writeLong3(Long num) throws IOException {
		//高1字节
		outputFile.writeByte((num.intValue() >> 16) & 0x000000ff);
		//高2字节
		outputFile.writeByte((num.intValue() >> 8) & 0x000000ff);
		//高3字节
		outputFile.writeByte(num.intValue() & 0x000000ff);
	}

	/**
	 * 状态机迁移至NOSTART状态
	 * @param prevLocation
	 * @param location
	 * @throws IOException 
	 */
	private void gotoNOSTART(Location prevLocation, Location location) throws IOException {
		currRecordOffset = outputFile.length();
		//写入起始号段起始
//		writeLong3(location.phoneno);
		noRecordEntity.setPidStart(location.pid);
		noRecordEntity.setNoStart(location.phoneno);
		noRecordEntity.setCity(location.city);
		System.out.print("起始号段:" + noRecordEntity.getCompleteNoStart() + "       ");
		//状态变为NOSTART;
		currStatus = STATUS_NOSTART;
	}

	/**
	 * 状态机迁移至NOEND状态
	 * @param prevLocation
	 * @param location
	 * @throws IOException 
	 */
	private void gotoNOEND(Location prevLocation, Location location) throws IOException {
		Long cityOffset = 0x0000000000fffffffl;
		String[] strs = prevLocation.city.split(">");
		String city = "";

		//写入结束号段以及索引模式，若能写入城市偏移地址则写入
//		writeLong3(prevLocation.phoneno);
		noRecordEntity.setPidEnd(prevLocation.pid);
		noRecordEntity.setNoEnd(prevLocation.phoneno);
		System.out.print("结束号段:" + noRecordEntity.getCompleteNoEnd() + "    ");

		if (strs.length == 2) {
			city = strs[1];
		} else {
			city = strs[0];
		}
		city = cityStrUniprocess(city);

		System.out.print("城市:" + city + "      ");

		if (headCities.size() > 0) {
			cityOffset = getOffset(headZoneCodes, city);
		}

		if (cityOffset != null) {
			System.out.print("索引模式:" + 0 + "    ");
//			outputFile.writeByte(0);
			System.out.print("首部区号表索引:" + cityOffset + "\n");
//			writeLong3(cityOffset);
			noRecordEntity.setZoneOffset(cityOffset.shortValue());
			outputFile.write(noRecordEntity.getBytes());
		} else {
//			outputFile.writeByte(1);
			System.out.print("索引模式:" + 1 + "   尾部城市索引\n");
			NeedTailCity needTailcity = nedTailCities.get(city);

			if (needTailcity != null) {
				needTailcity.recordOffsets.add(currRecordOffset);
			} else {
				needTailcity = new NeedTailCity(currRecordOffset, 
						prevLocation.city);
				nedTailCities.put(city, needTailcity);
			}
		}

		currStatus = STATUS_NOEND;
	}

	/**
	 * 从map中取出最匹配的偏移量,即长度最短的匹配量,该数据结构可使用List
	 * @param map
	 * @param city
	 * @return
	 */
	private Long getOffset(List<FieldEntity> lists, String key) {
		Long offset = 0x0000000000fffffffl;
		String name = "it'atoolongstringthatislaggerthananyouthers";

		for (FieldEntity entity : lists) {
			if (entity.name.contains(key)) {
				if (name.length() > entity.name.length()) {
					name = entity.name;
					offset = entity.offset;
				}
			}
		}
		
//		System.out.print("匹配城市名:" + name + "   ");
		return offset;
	}
	
	/**
	 * 找出key在表中的位置
	 * @param lists
	 * @param key
	 * @return
	 */
	private Long getOffset(ArrayList<String> lists, String key) {
		String name = "it'atoolongstringthatislaggerthananyouthers";
		Long ret = 0l;
		int i = 0;
		
		for (i = 0; i < lists.size(); i++) {
			if (lists.get(i).contains(key)) {
				if (name.length() > lists.get(i).length()) {
					name = lists.get(i);
					ret = (long) i;
				}
			}		
		}
		
		if (ret == 0 || ret > lists.size()) { //未找到
			ret = null;
		}
		
		return ret;
	}

	public class FieldEntity {
		public Long offset = null;
		public String name = null;
		/**
		 * @param offset
		 * @param name
		 */
		public FieldEntity(Long offset, String name) {
			super();
			this.offset = offset;
			this.name = name;
		}


	}

	/**
	 * 状态机迁移至NOMID状态
	 * @param prevLocation
	 * @param location
	 */
	private void gotoNOMID(Location prevLocation, Location location) {
		currStatus = STATUS_NOMID;
	}
	/**
	 * 将int的低两位字节写文件
	 * @param num
	 * @throws IOException 
	 */
	private void writeInt2(int num) throws IOException {
		outputFile.writeByte((num >> 8) & 0x000000ff);
		outputFile.writeByte(num & 0x000000ff);
	}

	private boolean isEmpty(String str) {
		if (str == null || str.trim().equals("")) {
			return true;
		}
		return false;
	}

	/**
	 * 创建尾部城市表
	 * @throws IOException 
	 */
	private void writeTailCities() throws IOException {
		Collection collect = nedTailCities.values();
		Iterator it = collect.iterator();
		NeedTailCity nedTailCity = null;
		Long headCityOffset = 0x0000000000ffffffl;
		String city = "";
		String area = "";
		String[] fields = null;
		

		while (it.hasNext()) {
			nedTailCity = (NeedTailCity) it.next();
			if (nedTailCity.recordOffsets.size() > 0) {
				//修改原号段记录中城市偏移地址字段
				modifyRecordOffsetFiled(nedTailCity);

				if (nedTailCity.cityName.contains(">")) {
					fields = nedTailCity.cityName.split(">");
					city = cityStrUniprocess(fields[0]);

					area = fields[1]; 
//					headCityOffset = headCityMap.get(city);
					if (headCities.size() > 0) {
						headCityOffset = getOffset(headCities, city);
					}

					if (0x0000000000ffffffl == headCityOffset) {
						writeLong3(headCityOffset);
						outputFile.write((city + area + "\n").getBytes("gb2312"));
					} else {
						writeLong3(headCityOffset);
						outputFile.write((area + "\n").getBytes());
					}
					System.out.println("城市:" + city + "  区域: " + area +
							" 　偏移地址: " + headCityOffset);
				} else {
					writeLong3(headCityOffset);
					outputFile.write((nedTailCity.cityName + "\n").getBytes("gb2312"));
					System.out.println("城市:" +nedTailCity.cityName  +
							" 　偏移地址: " + headCityOffset);
				}

			}
		}
	}

	/**
	 * 修改记录城市偏移量字段
	 * @param nedTailCity 
	 * @throws IOException 
	 */
	private void modifyRecordOffsetFiled(NeedTailCity nedTailCity) throws IOException {
		final int cityOffset = 7;
		Long currOffset = outputFile.length();

		for (Long offset : nedTailCity.recordOffsets) {
			System.out.println("修改偏移地址为:" + offset);
			outputFile.seek(offset + cityOffset);
			writeLong3(currOffset);
		}

		outputFile.seek(currOffset);
	}

	private String  provinceStrUniprocess(String province) {
		String retStr = "";

		//去掉空格
		retStr = province.replace(" ", "");

		//去掉省
		if (retStr.length() > 2 && (retStr.indexOf("省") == 
			(retStr.length() -1))) {
			retStr = retStr.substring(0, retStr.length() - 1);
		}

		return retStr;	
	}

	private String  cityStrUniprocess(String city) {
		String retStr = "";

		//去掉空格
		retStr = city.replace(" ", "");

		if (retStr.contains("/")) {
			return retStr;
		}

		//去掉市, 州
		if (retStr.length() > 2 && 
				(retStr.indexOf("市") == (retStr.length() -1))) {
			retStr = retStr.substring(0, retStr.length() - 1);
		}
		if (retStr.length() > 2 && 
				(retStr.indexOf("州") == (retStr.length() -1))) {
			retStr = retStr.substring(0, retStr.length() - 1);
		}

		return retStr;	
	}
}
