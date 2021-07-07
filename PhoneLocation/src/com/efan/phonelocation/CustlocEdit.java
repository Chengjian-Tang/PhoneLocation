/*
 * 系统名称: 
 * 模块名称: 
 * 类  名   称: 
 * 软件版权: 
 * 开发人员: 
 * 开发时间: 2010-10-17
 * 审核人员:
 * 相关文档:
 * 修改记录: 修改日期 修改人员 修改说明
 */
package com.efan.phonelocation;

import android.app.Activity;
import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * @author feelow
 *
 */
public class CustlocEdit extends Activity {
	private EditText phonenoEdit;
	private EditText locationEdit;
	private Long rowId;
	private CustlocDbAdapter mDbHelper = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.custloc_edit);

		phonenoEdit = (EditText) findViewById(R.id.custphonenoEdit);
		locationEdit = (EditText) findViewById(R.id.custlocationEdit);      
		Button confirmButton = (Button) findViewById(R.id.confirm);
		mDbHelper = new CustlocDbAdapter(this);
		rowId = savedInstanceState != null ? savedInstanceState.getLong(CustlocDbAdapter.KEY_ROWID)
				: null;

		mDbHelper.open();
		if (rowId == null) {
			Bundle extras = getIntent().getExtras();
			rowId = extras != null ?extras.getLong(CustlocDbAdapter.KEY_ROWID)
					: null;

		}

		populateFields();

		confirmButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				setResult(RESULT_OK);
				finish();
			}
		});
	}

	/**
	 * populate the fields based on the mRowId 
	 */
	private void populateFields() {
		// TODO Auto-generated method stub
		if (rowId != null) {
			Cursor custlocation = mDbHelper.fetchCustloc(rowId);
			startManagingCursor(custlocation);
			phonenoEdit.setText(custlocation.getString(
					custlocation.getColumnIndexOrThrow(CustlocDbAdapter.KEY_PHONENO)));
			locationEdit.setText(custlocation.getString(
					custlocation.getColumnIndexOrThrow(CustlocDbAdapter.KEY_LOCATION)));
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		saveState();
	}

	private void saveState() {
		// TODO Auto-generated method stub
		String phoneno = phonenoEdit.getText().toString();
		String location = locationEdit.getText().toString();
		long id = 0;

		if (rowId == null) {
			if (!phoneno.equals("") && mDbHelper.custlocIsExist(phoneno)) {
				AlertDialog dlg = new AlertDialog.Builder(this).create();
				dlg.setMessage(this.getResources().getText(R.string.custlocexist));
				dlg.show();
			} else if (!phoneno.equals("") || !location.equals("")){
				id = mDbHelper.createCustloc(phoneno, location);
			}
			if (id > 0) {
				rowId = id;
			} 
		} else {
			mDbHelper.updateCustloc(rowId, phoneno, location);
		}

	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		populateFields();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);

		if (rowId != null) {
			outState.putLong(CustlocDbAdapter.KEY_ROWID, rowId);
		}
	}
}
