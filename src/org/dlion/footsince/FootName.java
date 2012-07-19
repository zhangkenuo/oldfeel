package org.dlion.footsince;

import java.io.File;

import org.dlion.oldfeel.DBHelper;
import org.dlion.oldfeel.FileBrowser;
import org.dlion.oldfeel.FileUtil;
import org.dlion.oldfeel.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class FootName extends Activity {
	private static final String TAG = "DFootSince";
	protected static final String path = Environment
			.getExternalStorageDirectory().getAbsolutePath()
			+ "/oldfeel/footsince";
	ListView footNameListView;
	EditText etFootName;
	Button btnSubmit;
	SQLiteDatabase db;
	protected int lat;
	protected int lon;
	protected String footName;
	private SimpleCursorAdapter footNameAdapter;
	private int _id;
	private Cursor footnameCursor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.footsince_footname);
		db = DBHelper.openOldfeelDb(this);
		footNameListView = (ListView) findViewById(R.id.d_footsince_footname_listView);
		etFootName = (EditText) findViewById(R.id.d_footsince_footname_et);
		btnSubmit = (Button) findViewById(R.id.d_footsince_footname_btnSubmit);

		footName = getIntent().getStringExtra("footName");
		etFootName.setText(footName);

		// 显示足迹到listview
		footnameCursor = DBHelper.getAllCursor(db, "footsince");
		startManagingCursor(footnameCursor);
		footNameAdapter = new SimpleCursorAdapter(this,
				android.R.layout.simple_list_item_2, footnameCursor,
				new String[] { "footName", "date" }, new int[] {
						android.R.id.text1, android.R.id.text2 });
		footNameListView.setAdapter(footNameAdapter);
		footNameListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				footnameCursor.moveToPosition(arg2);
				footName = footnameCursor.getString(footnameCursor
						.getColumnIndex("footName"));
				lat = Integer.valueOf(footnameCursor.getString(footnameCursor
						.getColumnIndex("latitude")));
				lon = Integer.valueOf(footnameCursor.getString(footnameCursor
						.getColumnIndex("longitude")));
				etFootName.setText(footName);
			}
		});
		registerForContextMenu(footNameListView); // 注册listview的上下文菜单监听

		// 选择足迹后返回
		btnSubmit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				footName = etFootName.getText().toString().trim();
				Intent data = new Intent();
				data.putExtra("footName", footName);
				data.putExtra("lat", lat);
				data.putExtra("lon", lon);
				setResult(1, data);
				db.close();
				finish();
			}
		});
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		getMenuInflater().inflate(R.menu.footname_context_menu, menu);
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	/**
	 * 上下文菜单监听
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		Cursor c = footNameAdapter.getCursor();
		c.moveToPosition(info.position);
		_id = c.getInt(c.getColumnIndex("_id"));
		footName = c.getString(c.getColumnIndex("footName"));
		showLog(String.valueOf(info.id));
		switch (item.getItemId()) {
		case R.id.footname_context_menu_browseImg:
			Log.d(TAG, "browse img");
			browseFootFile("img");
			break;
		case R.id.footname_context_menu_browseVideo:
			Log.d(TAG, "browse video");
			browseFootFile("video");
			break;
		case R.id.footname_context_menu_delete:
			Log.d(TAG, "footsince delete");
			footNameDelete();
			break;
		case R.id.footname_context_menu_edit:
			Log.d(TAG, "footsince edit");
			footNameEdit();
			break;
		default:
			break;
		}
		return super.onContextItemSelected(item);
	}

	/**
	 * 编辑足迹
	 */
	private void footNameEdit() {
		View view = getLayoutInflater().inflate(
				R.layout.footsince_footname_edit, null);
		new AlertDialog.Builder(this).setTitle("编辑").setView(view)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				}).setNegativeButton("取消", null).show();
	}

	/**
	 * 删除足迹
	 */
	private void footNameDelete() {
		new AlertDialog.Builder(this).setMessage("删除足迹:" + footName + "?")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String footFilePath = path + "/" + footName;
						File file = new File(footFilePath);
						if (file.exists()) {
							FileUtil.deleteFile(file);
						}
						showLog(String.valueOf(_id));
						showLog(footName);
						db.delete("footsince", "_id = " + _id, null);
						footnameCursor = DBHelper.getAllCursor(db, "footsince");
						startManagingCursor(footnameCursor);
						footNameAdapter = new SimpleCursorAdapter(
								FootName.this,
								android.R.layout.simple_list_item_2,
								footnameCursor, new String[] { "footName",
										"date" },
								new int[] { android.R.id.text1,
										android.R.id.text2 });
						footNameListView.setAdapter(footNameAdapter);
					}
				}).setNegativeButton("取消", null).show();
	}

	/**
	 * 浏览
	 */
	private void browseFootFile(String type) {
		String footFilePath = path + "/" + footName + "/" + type;
		Intent intent = new Intent();
		intent.setClass(this, FileBrowser.class);
		intent.putExtra("path", footFilePath);
		startActivity(intent);
	}

	/**
	 * 显示Log
	 */
	public void showLog(String str) {
		Log.d("DFootName", str);
	}

}
