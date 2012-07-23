package org.dlion.schedule;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;

import org.dlion.oldfeel.DBHelper;
import org.dlion.oldfeel.R;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

public class Schedule extends Activity {
	SQLiteDatabase db;
	Spinner eachWeek;
	ListView scheduleList;
	Calendar calendar;
	String[] weekNames = new String[] { "自定义", "星期日", "星期一", "星期二", "星期三",
			"星期四", "星期五", "星期六" };

	protected int weekDay;
	/**
	 * 选择星期几监听
	 */
	private OnItemSelectedListener spinnerListener = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			weekDay = arg2;
			initScheduleList();
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
		}
	};
	/**
	 * 课程表监听
	 */
	private OnItemClickListener listViewListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			Intent intent = new Intent();
			intent.setClass(Schedule.this, ScheduleSetting.class);
			ScheduleInfo extraInfo = list.get(arg2);
			intent.putExtra("_id", extraInfo._id);
			intent.putExtra("enable", extraInfo.enable);
			intent.putExtra("weekDay", weekDay);
			intent.putExtra("lessonTime", extraInfo.scheduleTime);
			intent.putExtra("lessonName", extraInfo.scheduleName);
			intent.putExtra("ringTime", extraInfo.ringTime);
			intent.putExtra("ringName", extraInfo.ringName);
			intent.putExtra("classRoom", extraInfo.scheduleRemark);
			intent.putExtra("teacherName", extraInfo.scheduleContent);
			startActivity(intent);
		}
	};
	private ArrayList<ScheduleInfo> list;
	private scheduleAdapter mScheduleAdapter;

	/**
	 * 课程表内容显示
	 */
	class scheduleAdapter extends BaseAdapter {

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			ScheduleInfo schedule = list.get(position);
			convertView = getLayoutInflater().inflate(
					R.layout.schedule_list_item, null);
			TextView tvLessonName = (TextView) convertView
					.findViewById(R.id.schedule_lessonName);
			TextView tvLessonTime = (TextView) convertView
					.findViewById(R.id.schedule_lessonTime);
			final ImageView ivAlarmEnable = (ImageView) convertView
					.findViewById(R.id.schedule_alarmEnable);
			ivAlarmEnable.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					ScheduleInfo tempSchedule = list.get(position);
					if (tempSchedule.enable == 1) {
						tempSchedule.enable = 0;
					} else {
						tempSchedule.enable = 1;
					}
					ivAlarmEnable
							.setImageResource((tempSchedule.enable == 1) ? R.drawable.d_alarm_enable
									: R.drawable.d_alarm_disable);
					new ScheduleUtil(Schedule.this, db).setEnable(tempSchedule);
				}
			});
			tvLessonName.setText(schedule.scheduleName);
			tvLessonTime.setText(schedule.scheduleTime);
			ivAlarmEnable
					.setImageResource((schedule.enable == 1) ? R.drawable.d_alarm_enable
							: R.drawable.d_alarm_disable);
			return convertView;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public int getCount() {
			return list.size();
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("日程");
		setContentView(R.layout.schedule);
		db = DBHelper.openDb(Schedule.this);
		String ringPath = DATABASE_PATH + "/ring/";
		File ringDir = new File(ringPath);
		if (!ringDir.exists()) {
			ringDir.mkdir();
		}
		String ring_01 = ringDir + "/ooo.mp3";
		loadRingFile(ring_01);
		initEachWeek();
		initScheduleList();
	}

	/**
	 * 获取课程表信息
	 */
	protected void getScheduleInfo() {
		list = new ArrayList<ScheduleInfo>();
		try {
			Cursor c = db.rawQuery("select * from schedule where weekDay = ?",
					new String[] { String.valueOf(weekDay) });
			while (c.moveToNext()) {
				int _id = c.getInt(c.getColumnIndex("_id"));
				int enable = c.getInt(c.getColumnIndex("enable"));
				String lessonTime = c.getString(c.getColumnIndex("lessonTime"));
				String lessonName = c.getString(c.getColumnIndex("lessonName"));
				String ringTime = c.getString(c.getColumnIndex("ringTime"));
				String ringName = c.getString(c.getColumnIndex("ringName"));
				String classRoom = c.getString(c.getColumnIndex("classRoom"));
				String teacherName = c.getString(c
						.getColumnIndex("teacherName"));
				ScheduleInfo schedule = new ScheduleInfo(_id, enable, weekDay,
						lessonTime, lessonName, ringTime, ringName, classRoom,
						teacherName);
				list.add(schedule);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 初始化当前星期课程表
	 */
	private void initScheduleList() {
		scheduleList = (ListView) findViewById(R.id.schedule_list);
		getScheduleInfo();
		mScheduleAdapter = new scheduleAdapter();
		scheduleList.setAdapter(mScheduleAdapter);
		scheduleList.setOnItemClickListener(listViewListener);
		scheduleList.setOnCreateContextMenuListener(this);
		// 添加课程表按钮监听
		Button btnAddSchedule = (Button) findViewById(R.id.schedule_btnAddSchedule);
		btnAddSchedule.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (db == null || !db.isOpen()) {
					db = DBHelper.openDb(Schedule.this);
				}
				Cursor c = Schedule.this.db.rawQuery(
						"select MAX(_id) from schedule", null);
				int _id = 0;
				if (c.moveToNext()) {
					_id = c.getInt(0) + 1;
					ContentValues values = new ContentValues();
					values.put("_id", _id);
					values.put("weekDay", weekDay);
					db.insert("schedule", "_id", values);
				}
				Intent intent = new Intent();
				intent.setClass(Schedule.this, ScheduleSetting.class);
				intent.putExtra("_id", _id);
				intent.putExtra("weekDay", weekDay);
				startActivity(intent);
			}
		});
	}

	/**
	 * 初始化星期
	 */
	private void initEachWeek() {
		calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		weekDay = calendar.get(Calendar.DAY_OF_WEEK);
		eachWeek = (Spinner) findViewById(R.id.schedule_spinnerWeek);
		ArrayAdapter<String> weekAdapter = new ArrayAdapter<String>(this,
				R.layout.schedule_week_day, weekNames);
		weekAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		eachWeek.setAdapter(weekAdapter);
		eachWeek.setOnItemSelectedListener(spinnerListener);
		eachWeek.setSelection(weekDay);
	}

	private final String DATABASE_PATH = android.os.Environment
			.getExternalStorageDirectory().getAbsolutePath()
			+ "/oldfeel/database";

	/**
	 * 下载铃声到SD卡
	 */
	private void loadRingFile(String ringFileName) {
		showLog("load ring file");
		try {
			if (!(new File(ringFileName)).exists()) {
				InputStream is = getResources().openRawResource(R.raw.ooo);
				FileOutputStream fos = new FileOutputStream(ringFileName);
				byte[] buffer = new byte[8192];
				int count = 0;
				while ((count = is.read(buffer)) > 0) {
					fos.write(buffer, 0, count);
				}
				fos.close();
				is.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 创建上下文菜单
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		getMenuInflater().inflate(R.menu.schedule_context_menu, menu);
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	/**
	 * 上下文菜单监听
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		ScheduleInfo tempSchedule = list.get(info.position); // 当前操作的课程表
		switch (item.getItemId()) {
		case R.id.schedule_context_menu_enable: // 启用闹钟
			tempSchedule.enable = 1;
			new ScheduleUtil(Schedule.this, db).setEnable(tempSchedule);
			mScheduleAdapter.notifyDataSetChanged();
			return true;
		case R.id.schedule_context_menu_disenable: // 禁用闹钟
			tempSchedule.enable = 0;
			new ScheduleUtil(Schedule.this, db).setEnable(tempSchedule);
			mScheduleAdapter.notifyDataSetChanged();
			return true;
		case R.id.schedule_context_menu_delete: // 删除课程表
			new ScheduleUtil(Schedule.this, db).scheduleDel(tempSchedule);
			list.remove(info.position);
			mScheduleAdapter.notifyDataSetChanged();
			return true;
		default:
			break;
		}
		return super.onContextItemSelected(item);
	}

	/**
	 * 显示Log
	 */
	private void showLog(String log) {
		Log.d("Schedule", log);
	}

	@Override
	protected void onResume() {
		if (db == null || !db.isOpen()) {
			db = DBHelper.openDb(this);
		}
		getScheduleInfo();
		mScheduleAdapter.notifyDataSetChanged();
		super.onResume();
	}

	@Override
	protected void onPause() {
		db.close();
		super.onPause();
	}
}