package org.dlion.schedule;

import java.util.Calendar;

import org.dlion.oldfeel.DBHelper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ScheduleUtil {
	Context context;
	SQLiteDatabase db;

	public ScheduleUtil(Context context, SQLiteDatabase db) {
		this.context = context;
		this.db = db;
	}

	/**
	 * 添加数据库中所有被启用的闹钟到AlarmManager中
	 */
	public void scheduleBootAdd() {
		Calendar calendar = Calendar.getInstance();
		Cursor c = DBHelper.getAllCursor(db, "schedule");
		while (c.moveToNext()) {
			String ringTime = c.getString(c.getColumnIndex("ringTime"));
			String[] ringTimes = ringTime.split(":");
			int _id = c.getInt(c.getColumnIndex("_id"));
			int enable = c.getInt(c.getColumnIndex("enable"));
			int weekDay = c.getInt(c.getColumnIndex("weekDay"));
			int tempHour = Integer.valueOf(ringTimes[0]);
			int tempMinute = Integer.valueOf(ringTimes[1]);
			String ringName = c.getString(c.getColumnIndex("ringName"));
			calendar.set(Calendar.DAY_OF_WEEK, weekDay);
			calendar.set(Calendar.HOUR_OF_DAY, tempHour);
			calendar.set(Calendar.MINUTE, tempMinute);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			Intent intent = new Intent(context, AlarmReceiver.class);
			boolean isEnable = (enable == 1) ? true : false;
			if (isEnable) { // 判断是否启用，把启用的添加到闹钟管理里边。
				intent.putExtra("isEnable", isEnable);
				intent.putExtra("ringName", ringName);
				PendingIntent pendingIntent = PendingIntent.getBroadcast(
						context, _id, intent, 0);
				AlarmManager am = (AlarmManager) context
						.getSystemService(Context.ALARM_SERVICE);
				am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
						pendingIntent);
			}
		}
		db.close();
	}

	/**
	 * 删除课程表
	 */
	public void scheduleDel(ScheduleInfo tempSchedule) {
		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, AlarmReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
				tempSchedule._id, intent, 0);
		am.cancel(pendingIntent);
		db.delete("schedule", "_id = " + tempSchedule._id, null);
	}

	/**
	 * 铃声开关
	 */
	public void setEnable(ScheduleInfo tempSchedule) {
		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Calendar calendar = Calendar.getInstance();
		String[] temps = tempSchedule.ringTime.split(":");
		int _id = tempSchedule._id;
		int enable = tempSchedule.enable;
		int hour = Integer.valueOf(temps[0]);
		int minute = Integer.valueOf(temps[1]);
		int weekDay = tempSchedule.weekDay;

		ContentValues values = new ContentValues();
		values.put("enable", enable);
		db.update("schedule", values, "_id = " + _id, null);

		calendar.set(Calendar.DAY_OF_WEEK, weekDay);
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minute);
		showLog("switch " + enable);
		showLog("week " + weekDay);
		showLog(weekDay + ":" + hour + ":" + minute);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		Intent intent = new Intent(context, AlarmReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, _id,
				intent, 0);
		am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
				pendingIntent);
	}

	/**
	 * 显示Log
	 */
	void showLog(String log) {
		Log.d("ScheduleUtil", log);
	}
}
