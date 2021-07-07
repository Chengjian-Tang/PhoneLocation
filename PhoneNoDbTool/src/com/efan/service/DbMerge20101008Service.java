/*
 * 系统名称: 
 * 模块名称: 合并数据 Mobile_20101008.txt
 * 类  名   称: 
 * 软件版权: 
 * 开发人员: 
 * 开发时间: 2010-10-8
 * 审核人员:
 * 相关文档:
 * 修改记录: 修改日期 修改人员 修改说明
 */
package com.efan.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @author feelow
 *
 */
public class DbMerge20101008Service {
	long count = 0; //合并条数
	BufferedReader oldReader = null;
	BufferedReader newReader = null;
	BufferedWriter writer = null;
	ByteArrayOutputStream bos = null;
	String unkonwnlocationStr = "未知"; //未知归属地字符串值
	String splitStr = ">"; //归属地字符串分隔符

	public class OldRecord implements Comparable<OldRecord> {
		String linenum;
		String phoneno;
		String locationStr; //归属地串

		public OldRecord(String str) throws Exception {
			parseStr(str);
		}

		public OldRecord(NewRecord record) {
			phoneno = record.phoneno;
			locationStr = record.province + splitStr + record.city;
		}


		protected void parseStr(String str) throws Exception {
			String[] felds = str.split(" ");

			if (felds.length != 3) {
				throw new Exception("解析记录串失败.原串:" + str);
			}

			linenum = felds[0];
			phoneno = felds[1];
			locationStr = felds[2];
		}

		/* 
		 * 
		 */
		@Override
		public String toString() {
			return phoneno + " " + locationStr;
		}

		/* 
		 * 
		 */
		@Override
		public int compareTo(OldRecord o) {
			if (Integer.parseInt(phoneno) > Integer.parseInt(o.phoneno)) {
				return 1;
			} else if (Integer.parseInt(phoneno) < Integer.parseInt(o.phoneno)) {
				return -1;
			}
			return 0;
		}

	}

	public class NewRecord {
		String phoneno;
		String province; //省
		String city; //城市
		String code; //区号
		String postcode;//邮编
		String agentname;//运营商

		public NewRecord(String str) throws Exception {
			parseStr(str);
		}

		protected void parseStr(String str) throws Exception {
			String[] felds = str.split("\t");

			if (felds.length != 7) {
				throw new Exception("解析记录串失败.原串:" + str);
			}

			phoneno = felds[1];
			province = felds[2];
			city = felds[3];
			code = felds[4];
			postcode = felds[5];
			agentname = felds[6];
		}
	}

	HashMap<String, OldRecord> oldRecords = new HashMap<String, OldRecord>();
	HashMap<String, NewRecord> newRecords = new HashMap<String, NewRecord>();


	/**
	 * 将原始数据库文件与新数据库文件合并，新数据库文件名为oldfilename_merged
	 * @param oldfilename  原始文件文件
	 * @param newfilename 新文件名
	 * @throws IOException 
	 */
	public void merge(String oldfilename, String newfilename) throws Exception {		
		oldReader = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File(oldfilename)), "GB2312"));
		newReader = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File(newfilename)), "GB2312"));		
		bos = new ByteArrayOutputStream();
		writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
				new File(oldfilename + "_merged")), "gb2312"));
		String lineStr;

		OldRecord oldRecord = null; 
		NewRecord newRecord = null; 
		//读出老的数据库记录
		count = 0;
		while ((lineStr = oldReader.readLine()) != null) {
			OldRecord record = new OldRecord(lineStr);
			OldRecord tmpRecord = oldRecords.get(record.phoneno);
			if (tmpRecord != null) {
				System.out.println("相同记录:1:" + record + " 2:" + tmpRecord);
			} else {
				oldRecords.put(record.phoneno, record);
			}
			count++;
		}
		System.out.println("处理老数据记录数:" + count);

		//读出新的数据库记录 
		while ((lineStr = newReader.readLine()) != null) {
			NewRecord record = new NewRecord(lineStr);
			newRecords.put(record.phoneno, record);
		}		
		//将老的记录中归属地为未知的记录来查找新数据库的记录，查到后修改归属地记录
		Collection<OldRecord> oldRecordsColl = oldRecords.values();
		Iterator<OldRecord> oldRecordIterator = oldRecordsColl.iterator();

		while (oldRecordIterator.hasNext()) {
			oldRecord = oldRecordIterator.next();
			newRecord = newRecords.get(oldRecord.phoneno);
			if (newRecord != null) {
				if (oldRecord.locationStr.equals(unkonwnlocationStr)) {
					newRecord = newRecords.get(oldRecord.phoneno);
					if (newRecord != null) {
						System.out.println("合并前:" + oldRecord);
						oldRecord.locationStr = newRecord.province + 
						splitStr + newRecord.city;
						System.out.println("合并后:" + oldRecord);
						count++;
					}
				}
			}
		}

		System.out.println("总合并条数:" + count);

		//将新数据库的记录加入老的记录中
		Collection<NewRecord> newRecordsColl = newRecords.values();
		Iterator<NewRecord> newRecordIterator = newRecordsColl.iterator();

		count = 0;

		while (newRecordIterator.hasNext()) {
			newRecord = newRecordIterator.next();
			if (oldRecords.get(newRecord.phoneno) == null) {
				OldRecord record = new OldRecord(newRecord);
				oldRecords.put(newRecord.phoneno, record);
				System.out.println("新增:" + record);
				count++;
			}
		}

		System.out.println("新增记录数:" + count);

		//将元素复制到arraylist中排序
		oldRecordIterator = oldRecordsColl.iterator();
		ArrayList<OldRecord> recordList = new ArrayList<OldRecord>();
		while (oldRecordIterator.hasNext()) {
			recordList.add(oldRecordIterator.next());
		}

		Collections.sort(recordList);

		//写入文件
		for (int i = 0; i < recordList.size(); i++) {
			writer.write((i + 1) + " " + recordList.get(i) + "\n");
		}

		oldReader.close();
		newReader.close();
		writer.close();
	}

}
