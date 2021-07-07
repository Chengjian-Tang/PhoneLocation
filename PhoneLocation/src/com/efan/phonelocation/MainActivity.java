/*
 * 系统名称: 
 * 模块名称: 
 * 类  名   称: 
 * 软件版权: 
 * 开发人员: 
 * 开发时间: 2010-9-3
 * 审核人员:
 * 相关文档:
 * 修改记录: 修改日期 修改人员 修改说明
 */
package com.efan.phonelocation;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TabHost;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.efan.model.Location;
import com.efan.util.StringUtil;
/**
 * @author feelow
 *
 */
public class MainActivity extends TabActivity
implements OnClickListener, OnTabChangeListener, OnItemClickListener
{
	private static final int ACTIVITY_CREATE=0;
	private static final int ACTIVITY_EDIT=1;

	public static String MY_PREFS = "PHONELOCATION_PRES";
	public static String XOFFSET = "xoffset";
	public static String YOFFSET = "yoffset";
	public static String CUSTLOCATION_TABTAG = "tab_custlocation";

	private TabHost mTabHost;
	private Toast testToast = null;
	private int xOffset = 0;
	private int yOffset = 0;

	private Button searchButton = null;
	private Button testButton = null;
	private Button saveButton = null;
	private EditText phoneNoEdit = null;
	private EditText xOffsetEdit = null;
	private EditText yOffsetEdit = null;
	private TextView city = null;
	private TextView province = null;
	private TextView agentName = null;
	private TextView zoneCode = null;
	private TextView dbVersion = null;
	private SharedPreferences prefs = null;
	private TextView msgTextView = null;
	private ListView custlocListView = null;

	private CustlocDbAdapter mDbHelper;
	private Cursor custlocCursor;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

//		startService(new Intent(this, PhoneLocationService.class));

		setContentView(R.layout.main);

		mTabHost = getTabHost();
		mTabHost.setOnTabChangedListener(this);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		loadPrefs();

		mDbHelper = new CustlocDbAdapter(this);
		mDbHelper.open();

		setupUI();
		setupButtons();
	}

	/* 
	 * 设置对应tab的菜单
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		MenuInflater inflater = getMenuInflater();

		if (mTabHost.getCurrentTabTag().equals(CUSTLOCATION_TABTAG)) {
			inflater.inflate(R.menu.custlocmenu, menu);
		}

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.contextcustlocmenu, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {

		// TODO: fill in rest of method
		switch(item.getItemId()) {
		case R.id.delcustloc:
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
			mDbHelper.deleteCustloc(info.id);
			fillListViewData();
			return true;
		}
		return super.onContextItemSelected(item);
	}

	/* 
	 * tab为自定义归属地时,载入数
	 */
	@Override
	public void onTabChanged(String tabId) {
		if (mTabHost.getCurrentTabTag().equals(CUSTLOCATION_TABTAG)) {
			fillListViewData();
		}
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.addcustloc : 
			createCustloc();
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
		Cursor c = custlocCursor;
		c.moveToPosition(position);
		Intent i = new Intent(this, CustlocEdit.class);
		
		i.putExtra(CustlocDbAdapter.KEY_ROWID, id);
		i.putExtra(CustlocDbAdapter.KEY_PHONENO, c.getString(
				c.getColumnIndexOrThrow(CustlocDbAdapter.KEY_PHONENO)));
		i.putExtra(CustlocDbAdapter.KEY_LOCATION, c.getString(
				c.getColumnIndexOrThrow(CustlocDbAdapter.KEY_LOCATION)));
		startActivityForResult(i, ACTIVITY_EDIT);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		
		fillListViewData();
	}

	/**
	 * 获取ui上各个组件
	 */
	private void setupUI() {
		mTabHost.addTab(mTabHost.newTabSpec("tab_phonelocation").setIndicator(
				getApplicationContext().getResources().getText(R.string.phonelocation)).setContent(R.id.locationView));
		mTabHost.addTab(mTabHost.newTabSpec(CUSTLOCATION_TABTAG).setIndicator(
				getApplicationContext().getResources().getText(R.string.custlocation)).setContent(R.id.custlocationView));

		mTabHost.addTab(mTabHost.newTabSpec("tab_preference").setIndicator(
				getApplicationContext().getResources().getText(R.string.preference)).setContent(R.id.preferenceView));
		mTabHost.addTab(mTabHost.newTabSpec("tab_about").setIndicator(
				getApplicationContext().getResources().getText(R.string.about)).setContent(R.id.aboutView));

		mTabHost.setCurrentTab(0);

		searchButton = (Button) findViewById(R.id.search);
		testButton = (Button) findViewById(R.id.test);
		saveButton = (Button) findViewById(R.id.save);
		phoneNoEdit = (EditText) findViewById(R.id.phoneNo);;
		xOffsetEdit = (EditText) findViewById(R.id.xoffset);
		yOffsetEdit = (EditText) findViewById(R.id.yoffset);

		xOffsetEdit.setText("" + xOffset);
		yOffsetEdit.setText("" + yOffset);

		province = (TextView) findViewById(R.id.province);
		city = (TextView) findViewById(R.id.city);
		agentName = (TextView) findViewById(R.id.agentname);
		zoneCode = (TextView) findViewById(R.id.zonecode);
		dbVersion = (TextView) findViewById(R.id.dbversion);
		dbVersion.setText(PhoneLocationService.getDbVersion(this));

		custlocListView = (ListView) findViewById(R.id.custloclist);
		//添加自定义归属地列表的环境菜单
		registerForContextMenu(custlocListView);
		//设置点击列表监听器
		custlocListView.setOnItemClickListener(this);
	}

	/**
	 * 打开新增归属地信息Acitivity
	 */
	private void createCustloc() {
		Intent i = new Intent(this, CustlocEdit.class);
		startActivityForResult(i, ACTIVITY_CREATE);
	}

	/**
	 * 载入归属地信息数据
	 */
	private void fillListViewData() {
		custlocCursor = mDbHelper.fetchAllCustloc();
		startManagingCursor(custlocCursor);

		// Create an array to specify the fields we want to display in the list (only TITLE)
		String[] from = new String[]{CustlocDbAdapter.KEY_PHONENO, 
				CustlocDbAdapter.KEY_LOCATION};

		// and an array of the fields we want to bind those fields to (in this case just text1)
		int[] to = new int[]{R.id.custphoneno, R.id.custlocation};

		// Now create a simple cursor adapter and set it to display
		SimpleCursorAdapter notes = 
			new SimpleCursorAdapter(this, R.layout.custloc_row, custlocCursor,
					from, to);
		custlocListView.setAdapter(notes);
	}

	private void loadPrefs() {
		xOffset = prefs.getInt(MainActivity.XOFFSET, 0);
		yOffset = prefs.getInt(MainActivity.YOFFSET, 0);
	}

	/**
	 * 设置搜索按钮
	 */
	private void setupButtons() {
		searchButton.setOnClickListener(this);
		testButton.setOnClickListener(this);
		saveButton.setOnClickListener(this);
	}

	/* 
	 * 按钮事件响应
	 */
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.search : //搜索按钮
			doSearch();
			break;
		case R.id.test : //测试按钮
			doTest();
			break;
		case R.id.save : //保存按钮
			doSave();
			break;
		default:
		}

	}

	/**
	 * 保存设置
	 */
	private void doSave() {
		getValuesFromView();

		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(XOFFSET, xOffset);
		editor.putInt(YOFFSET, yOffset);
		editor.commit();
	}

	/**
	 * 从ui中取值
	 */
	private void getValuesFromView() {
		String str = xOffsetEdit.getText().toString();

		if (!StringUtil.isEmpty(str)) {
			xOffset = Integer.parseInt(str);
		}
		str = yOffsetEdit.getText().toString();
		if (!StringUtil.isEmpty(str)) {
			yOffset = Integer.parseInt(str);
		}
	}

	/**
	 * 测试设置
	 */
	private void doTest() {
		getValuesFromView();
		if (msgTextView == null) {
			msgTextView = new TextView(this);
			msgTextView.setTextSize(20);
		}
		msgTextView.setText(getResources().getText(R.string.testinfo));
		if (testToast == null) {
			testToast = Toast.makeText(this, "", 
					Toast.LENGTH_LONG);
		}
		testToast.setView(msgTextView);
		testToast.cancel();
		testToast.setGravity(Gravity.BOTTOM, xOffset, yOffset);
		testToast.show();
	}

	/**
	 * 搜索归属地
	 */
	private void doSearch() {

		String phoneNo = phoneNoEdit.getText().toString();

		Location location = PhoneLocationService.getLocation(phoneNo, this);
		if (location != null) {
			province.setText(location.getProvince());
			city.setText(location.getCity());
			agentName.setText(location.getAgentName());
			zoneCode.setText(location.getZoneCode());
		}
	}

}
