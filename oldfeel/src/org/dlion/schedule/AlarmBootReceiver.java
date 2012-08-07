package org.dlion.schedule;

import org.dlion.oldfeel.DBHelper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;

/**
 * 开机启动加载这个
 */
public class AlarmBootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
			SQLiteDatabase db = DBHelper.openDb(context);
			new ScheduleUtil(context, db).scheduleBootAdd();
		}
	}
}
