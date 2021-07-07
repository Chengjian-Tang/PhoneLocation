/*
 * 系统名称: 
 * 模块名称: 自定义归属地数据库操作适配器
 * 类  名   称: 
 * 软件版权: 
 * 开发人员: 
 * 开发时间: 2010-10-17
 * 审核人员:
 * 相关文档:
 * 修改记录: 修改日期 修改人员 修改说明
 */
package com.efan.phonelocation;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * @author feelow
 *
 */
public class CustlocDbAdapter {

	public static final String KEY_PHONENO = "phoneno";
	public static final String KEY_LOCATION = "location";
	public static final String KEY_ROWID = "_id";

	private static final String TAG = "CustLocDbAdapter";
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	/**
	 * Database creation sql statement
	 */
	private static final String DATABASE_CREATE =
		"create table custloc (_id integer primary key autoincrement, "
		+ "phoneno text not null, location text not null);";

	private static final String DATABASE_NAME = "data";
	private static final String DATABASE_TABLE = "custloc";
	private static final int DATABASE_VERSION = 2;

	private final Context mCtx;

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {

			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS notes");
			onCreate(db);
		}
	}

	public CustlocDbAdapter(Context ctx) {
		this.mCtx = ctx;
	}

	public CustlocDbAdapter open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		mDbHelper.close();
	}

	/**
	 * 新建归属地信息
	 * @param phoneno
	 * @param location
	 * @return
	 */
	public long createCustloc(String phoneno, String location) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_PHONENO, phoneno);
		initialValues.put(KEY_LOCATION, location);

		return mDb.insert(DATABASE_TABLE, null, initialValues);
	}

	/**
	 * 删除归属地信息
	 * @param rowId
	 * @return
	 */ 
	public boolean deleteCustloc(long rowId) {

		return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	}

	/**
	 * 取出所有归属地信息
	 * @return
	 */
	public Cursor fetchAllCustloc() {

		return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_PHONENO,
				KEY_LOCATION}, null, null, null, null, null);
	}

	/**
	 * 取出指定归属地信息
	 * @param rowId
	 * @return
	 * @throws SQLException
	 */
	public Cursor fetchCustloc(long rowId) throws SQLException {

		Cursor mCursor =

			mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
					KEY_PHONENO, KEY_LOCATION}, KEY_ROWID + "=" + rowId, null,
					null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;

	}

	public String fetchCursorloc(String phoneno) throws SQLException {
		String retStr = null;
		
		Cursor mCursor =

			mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
					KEY_PHONENO, KEY_LOCATION}, KEY_PHONENO + "=" + phoneno, null,
					null, null, null, null);
		//取查询结果的第一个值
		if (mCursor != null && mCursor.getCount() > 0) {
			mCursor.moveToFirst();
			retStr = mCursor.getString(mCursor.getColumnIndex(KEY_LOCATION));
		}
		mCursor.close();
		
		return retStr;
	}

	/**
	 * 指定的phoneno是否已经存在
	 * @param phoneno
	 * @return
	 */
	public boolean custlocIsExist(String phoneno) {
		boolean ret = false;
		Cursor mCursor =

			mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
					KEY_PHONENO, KEY_LOCATION}, KEY_PHONENO + "=" + phoneno, null,
					null, null, null, null);
		if (mCursor != null && mCursor.getCount() > 0) {
			ret = true;
		}
		mCursor.close();

		return ret;    	
	}

	/**
	 * 更新归属地信息
	 * @param rowId
	 * @param phoneno
	 * @param location
	 * @return
	 */
	public boolean updateCustloc(long rowId, String phoneno, String location) {
		ContentValues args = new ContentValues();
		args.put(KEY_PHONENO, phoneno);
		args.put(KEY_LOCATION, location);

		return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
	}
}
