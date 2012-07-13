package org.dlion.schedule;

import java.util.Calendar;

import org.dlion.alarm.DAlarmReceiver;
import org.dlion.oldfeel.OldfeelDBManager;
import org.dlion.oldfeel.R;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

public class DScheduleSetting extends Activity {
	int _id;
	int enable;
	int weekDay;
	String ringTime;
	String lessonName;
	String lessonTime;
	String teacherName;
	String classRoom;
	String ringName;
	public String[] infoNames = new String[] { "响铃时间", "课程名称", "课程时间", "课程教师",
			"教室名称", "选择铃声" };
	/**
	 * 设置信息的值{0:响铃时间，1：课程名称，2：课程时间，3：课程教师，4：教室名称，5：选择铃声}
	 */
	public String[] infoValues = new String[] { "19:00", "自由之翼", "19:00-01:00",
			"吉姆`雷诺", "星际争霸", "ooo.mp3" };
	/**
	 * 铃声列表
	 */
	public String[] ringNames = new String[] { "ooo.mp3" };
	private ImageButton btnEnable;
	/**
	 * 按钮点击监听
	 */
	private OnClickListener btnListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.schedule_setting_isAlarmEnable:
				if (enable == 1) {
					enable = 0;
				} else {
					enable = 1;
				}
				isAlarmEnable();
				break;
			case R.id.schedule_setting_save:
				settingSave();
				break;
			case R.id.schedule_setting_resume:
				settingResume();
				break;
			case R.id.schedule_setting_delete:
				scheduleDelete();
				break;
			default:
				break;
			}
		}
	};
	/**
	 * 列表点击监听
	 */
	private OnItemClickListener listListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			switch (arg2) {
			case 0:
				setRingTime();
				break;
			case 1:
				setLessonName();
				break;
			case 2:
				setLessonTime();
				break;
			case 3:
				setTeacherName();
				break;
			case 4:
				setClassRoom();
				break;
			case 5:
				setRingName();
				break;
			default:
				break;
			}
		}
	};
	private Calendar calendar;
	protected AlarmManager am;
	private infoAdapter mInfoAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("设置");
		setContentView(R.layout.schedule_setting);
		calendar = Calendar.getInstance();
		am = (AlarmManager) getSystemService(ALARM_SERVICE);
		initScheduleInfo();
		initSettingBtn();
		initSettingInfo();
	}

	/**
	 * 删除课程表
	 */
	protected void scheduleDelete() {
		boolean isEnable = (enable == 1) ? true : false;
		new DScheduleManager(DScheduleSetting.this).scheduleCancel(am,
				isEnable, _id); // 删除课程表
		finish();
	}

	/**
	 * 设置铃声
	 */
	protected void setRingName() {
		final Dialog dialog = new Dialog(DScheduleSetting.this);
		ListView view = new ListView(DScheduleSetting.this);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				DScheduleSetting.this, android.R.layout.simple_list_item_1,
				ringNames);
		view.setAdapter(adapter);
		view.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				infoValues[5] = ringNames[arg2];
				mInfoAdapter.notifyDataSetChanged();
				dialog.cancel();
			}
		});
		dialog.setTitle("选择铃声");
		dialog.setCanceledOnTouchOutside(true);
		dialog.setContentView(view);
		dialog.show();
	}

	/**
	 * 设置教室名称
	 */
	protected void setClassRoom() {
		final EditText et = new EditText(getApplicationContext());
		et.setHint("输入教室名称");
		new AlertDialog.Builder(DScheduleSetting.this).setView(et)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						infoValues[4] = et.getText().toString().trim();
						mInfoAdapter.notifyDataSetChanged();
					}
				}).setNegativeButton("取消", null).show();
	}

	/**
	 * 设置课程教师
	 */
	protected void setTeacherName() {
		final EditText et = new EditText(getApplicationContext());
		et.setHint("输入课程教师名称");
		new AlertDialog.Builder(DScheduleSetting.this).setView(et)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						infoValues[3] = et.getText().toString().trim();
						mInfoAdapter.notifyDataSetChanged();
					}
				}).setNegativeButton("取消", null).show();
	}

	/**
	 * 设置课程时间
	 */
	protected void setLessonTime() {
		View view = getLayoutInflater().inflate(
				R.layout.schedule_setting_lesson_time, null);
		final EditText etLessonTimeStart = (EditText) view
				.findViewById(R.id.schedule_setting_lesson_time_etStart);
		final EditText etLessonTimeStop = (EditText) view
				.findViewById(R.id.schedule_setting_lesson_time_etStop);
		Button btnLessonTimeStart = (Button) view
				.findViewById(R.id.schedule_setting_lesson_time_btnStart);
		Button btnLessonTimeStop = (Button) view
				.findViewById(R.id.schedule_setting_lesson_time_btnStop);
		// 设置课程开始时间
		btnLessonTimeStart.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				calendar.setTimeInMillis(System.currentTimeMillis());
				int mHour = calendar.get(Calendar.HOUR_OF_DAY);
				int mMinute = calendar.get(Calendar.MINUTE);
				new TimePickerDialog(DScheduleSetting.this,
						new TimePickerDialog.OnTimeSetListener() {

							@Override
							public void onTimeSet(TimePicker view,
									int hourOfDay, int minute) {
								etLessonTimeStart.setText(format(hourOfDay)
										+ ":" + format(minute));
							}
						}, mHour, mMinute, true).show();
			}
		});
		// 设置课程结束时间
		btnLessonTimeStop.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				calendar.setTimeInMillis(System.currentTimeMillis());
				int mHour = calendar.get(Calendar.HOUR_OF_DAY);
				int mMinute = calendar.get(Calendar.MINUTE);
				new TimePickerDialog(DScheduleSetting.this,
						new TimePickerDialog.OnTimeSetListener() {

							@Override
							public void onTimeSet(TimePicker view,
									int hourOfDay, int minute) {
								etLessonTimeStop.setText(format(hourOfDay)
										+ ":" + format(minute));
							}
						}, mHour, mMinute, true).show();
			}
		});
		new AlertDialog.Builder(DScheduleSetting.this).setView(view)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						infoValues[2] = etLessonTimeStart.getText().toString()
								.trim()
								+ "-"
								+ etLessonTimeStop.getText().toString().trim();
						mInfoAdapter.notifyDataSetChanged();
					}
				}).setNegativeButton("取消", null).show();
	}

	/**
	 * 设置课程名称
	 */
	protected void setLessonName() {
		final EditText et = new EditText(getApplicationContext());
		et.setHint("输入课程名称");
		new AlertDialog.Builder(DScheduleSetting.this).setView(et)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						infoValues[1] = et.getText().toString().trim();
						Log.d("课程名称", infoValues[1]);
						mInfoAdapter.notifyDataSetChanged();
					}
				}).setNegativeButton("取消", null).show();
	}

	/**
	 * 设置响铃时间
	 */
	protected void setRingTime() {
		calendar.setTimeInMillis(System.currentTimeMillis());
		int mHour = calendar.get(Calendar.HOUR_OF_DAY);
		int mMinute = calendar.get(Calendar.MINUTE);
		new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {

			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				infoValues[0] = format(hourOfDay) + ":" + format(minute);
				mInfoAdapter.notifyDataSetChanged();
			}
		}, mHour, mMinute, true).show();
	}

	/**
	 * 重置设置
	 */
	protected void settingResume() {
		initSettingInfo();
	}

	/**
	 * 保存设置
	 */
	protected void settingSave() {
		String[] temp = infoValues[0].split(":");
		int tempHour = Integer.valueOf(temp[0]);
		Log.d("小时", String.valueOf(tempHour));
		int tempMinute = Integer.valueOf(temp[1]);
		calendar.set(Calendar.DAY_OF_WEEK, weekDay);
		calendar.set(Calendar.HOUR_OF_DAY, tempHour);
		calendar.set(Calendar.MINUTE, tempMinute);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Intent intent = new Intent(DScheduleSetting.this, DAlarmReceiver.class);
		boolean isEnable = (enable == 1) ? true : false;
		intent.putExtra("isEnable", isEnable);
		intent.putExtra("ringName", ringName);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(
				DScheduleSetting.this, _id, intent, 0);
		am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
				pendingIntent);
		saveDataToDb();
	}

	/**
	 * 把数据保存到数据库
	 */
	private void saveDataToDb() {
		SQLiteDatabase db = new OldfeelDBManager(this).openOldfeelDb();
		ContentValues values = new ContentValues();
		values.put("enable", enable);
		values.put("weekDay", weekDay);
		values.put("ringTime", infoValues[0]);
		values.put("lessonName", infoValues[1]);
		values.put("lessonTime", infoValues[2]);
		values.put("teacherName", infoValues[3]);
		values.put("classRoom", infoValues[4]);
		values.put("ringName", infoValues[5]);
		db.update("schedule", values, "_id = " + _id, null);
		Log.d("设置", "db is update:响铃时间为" + infoValues[0]);
		db.close();
		finish(); // 退出设置
	}

	/**
	 * 判断提醒状态是否打开
	 */
	protected void isAlarmEnable() {
		if (enable == 1) {
			btnEnable.setImageResource(R.drawable.d_alarm_enable);
		} else {
			btnEnable.setImageResource(R.drawable.d_alarm_disable);
		}
	}

	/**
	 * 初始化设置里的按钮：开关、保存、清空
	 */
	private void initSettingBtn() {
		btnEnable = (ImageButton) findViewById(R.id.schedule_setting_isAlarmEnable);
		isAlarmEnable();
		Button btnSave = (Button) findViewById(R.id.schedule_setting_save);
		Button btnClear = (Button) findViewById(R.id.schedule_setting_resume);
		Button btnDelete = (Button) findViewById(R.id.schedule_setting_delete);
		btnEnable.setOnClickListener(btnListener);
		btnSave.setOnClickListener(btnListener);
		btnClear.setOnClickListener(btnListener);
		btnDelete.setOnClickListener(btnListener);
	}

	/**
	 * 初始化课程表信息
	 */
	private void initScheduleInfo() {
		_id = getIntent().getIntExtra("_id", 0);
		enable = getIntent().getIntExtra("enable", 0);
		weekDay = getIntent().getIntExtra("weekDay", 0);
		ringTime = getIntent().getStringExtra("ringTime");
		lessonName = getIntent().getStringExtra("lessonName");
		lessonTime = getIntent().getStringExtra("lessonTime");
		classRoom = getIntent().getStringExtra("classRoom");
		teacherName = getIntent().getStringExtra("teacherName");
		ringName = getIntent().getStringExtra("ringName");
	}

	/**
	 * 初始化设置信息
	 */
	private void initSettingInfo() {
		infoValues[0] = setSettingInfo(infoValues[0], ringTime);
		infoValues[1] = setSettingInfo(infoValues[1], lessonName);
		infoValues[2] = setSettingInfo(infoValues[2], lessonTime);
		infoValues[3] = setSettingInfo(infoValues[3], classRoom);
		infoValues[4] = setSettingInfo(infoValues[4], teacherName);
		infoValues[5] = setSettingInfo(infoValues[5], ringName);
		ListView infoList = (ListView) findViewById(R.id.schedule_setting_infoList);
		mInfoAdapter = new infoAdapter();
		infoList.setAdapter(mInfoAdapter);
		infoList.setOnItemClickListener(listListener);
	}

	/**
	 * 判断传入的课程信息是否为空，并赋值
	 */
	private String setSettingInfo(String infoValue, String arg) {
		String temp = (arg != null) ? arg : infoValue;
		return temp;
	}

	/**
	 * 设置信息列表adapter
	 */
	class infoAdapter extends BaseAdapter {

		@Override
		public void notifyDataSetChanged() {
			enable = 1;
			btnEnable.setImageResource(R.drawable.d_alarm_enable);
			super.notifyDataSetChanged();

		}

		@Override
		public int getCount() {
			return infoNames.length;
		}

		@Override
		public Object getItem(int position) {
			return infoNames[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = getLayoutInflater().inflate(
					R.layout.schedule_setting_list_item, null);
			TextView tvName = (TextView) convertView
					.findViewById(R.id.schedule_setting_list_item_name);
			TextView tvValue = (TextView) convertView
					.findViewById(R.id.schedule_setting_list_item_value);
			tvName.setText(infoNames[position]);
			tvValue.setText(infoValues[position]);
			return convertView;
		}
	}

	public String format(int hourOfDay) {
		String temp = "" + hourOfDay;
		if (hourOfDay < 10) {
			temp = "0" + temp;
		}
		return temp;
	}
}