package org.dlion.schedule;

import java.util.Calendar;

import org.dlion.oldfeel.DBHelper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ScheduleManager {
	Context context;
	SQLiteDatabase db;
	private String TABLE_NAME = "schedule";

	public ScheduleManager(Context context) {
		this.context = context;
		db = DBHelper.openOldfeelDb(context);
	}

	/**
	 * 添加数据库中所有被启用的闹钟到AlarmManager中
	 */
	public void scheduleBootAdd() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		Cursor c = db.query(TABLE_NAME, null, null, null, null, null, null);
		while (c.moveToNext()) {
			String ringTime = c.getString(c.getColumnIndex("ringTime"));
			String[] ringTimes = ringTime.split(":");
			int _id = c.getInt(c.getColumnIndex("_id"));
			int enable = c.getInt(c.getColumnIndex("ebabke"));
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
	public void scheduleCancel(AlarmManager am, boolean isEnable, int _id) {
		Intent intent = new Intent(context, AlarmReceiver.class);
		intent.putExtra("isEnable", isEnable);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, _id,
				intent, 0);
		am.cancel(pendingIntent);
		db.delete("schedule", "_id = " + _id, null);
		db.close();
	}
}
