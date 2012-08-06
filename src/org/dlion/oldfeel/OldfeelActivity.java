﻿package org.dlion.oldfeel;

import java.io.File;

import org.dlion.footsince.FootSince;
import org.dlion.mybook.MyBook;
import org.dlion.schedule.Schedule;
import org.dlion.timer.Timer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class OldfeelActivity extends Activity {
<<<<<<< HEAD
	String[] dTools = { "清除数据", "足迹", "日程", "秒表" };
	Class<?>[] dActivities = { null, FootSince.class, Schedule.class,
			Timer.class };
	int[] dIcons = { R.drawable.icon_deletedb, R.drawable.icon_footsince,
			R.drawable.icon_schedule, R.drawable.icon_timer };
=======
	private static final int DIALOG_DB_DELETE = 0;
	String[] dTools = { "足迹", "日程", "秒表", "书库" };
	Class<?>[] dActivities = { FootSince.class, Schedule.class, Timer.class,
			MyBook.class };
	int[] dIcons = { R.drawable.icon_footsince, R.drawable.icon_schedule,
			R.drawable.icon_timer, R.drawable.icon_book };
>>>>>>> 添加了一个电子书阅读器

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.oldfeel_main);
		GridView dToolsGridView = (GridView) findViewById(R.id.dTools_GridView);
		dToolsGridView.setAdapter(adapter);
		dToolsGridView.setOnItemClickListener(listener);
		ImageView dbDelete = (ImageView) findViewById(R.id.db_delete);
		dbDelete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showDialog(DIALOG_DB_DELETE);
			}
		});
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_DB_DELETE:
			Dialog dialogDbDelete = new AlertDialog.Builder(this)
					.setTitle("删除数据库")
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									String dbPath = Environment
											.getExternalStorageDirectory()
											.getAbsolutePath().toString()
											+ "/oldfeel/database/oldfeel.db";
									File dbFile = new File(dbPath);
									if (!dbFile.delete()) {
										Log.d("DeleteDB",
												"delete db file failed");
									}
								}
							}).setNegativeButton("取消", null).create();
			return dialogDbDelete;
		default:
			break;
		}
		return super.onCreateDialog(id);
	}

	private BaseAdapter adapter = new BaseAdapter() {

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			Context context = getApplicationContext();
			LinearLayout view = new LinearLayout(context);
			ImageView img = new ImageView(context);
			img.setImageResource(dIcons[position]);
			TextView textView = new TextView(context);
			textView.setText(dTools[position]);
			textView.setGravity(Gravity.CENTER);
			view.setOrientation(LinearLayout.VERTICAL);
			view.addView(img, LayoutParams.FILL_PARENT,
					LayoutParams.WRAP_CONTENT);
			view.addView(textView, LayoutParams.FILL_PARENT,
					LayoutParams.WRAP_CONTENT);
			return view;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public int getCount() {
			return dTools.length;
		}
	};
	private OnItemClickListener listener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			if (arg2 == 0) {
				String dbPath = Environment.getExternalStorageDirectory()
						.getAbsolutePath().toString()
						+ "/oldfeel/database/oldfeel.db";
				File dbFile = new File(dbPath);
				if (dbFile.delete()) {
					Log.d("DeleteDB", "delete db file success");
				}
			} else
				startActivity(new Intent(getApplicationContext(),
						dActivities[arg2]));
		}
	};
}