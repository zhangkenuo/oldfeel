﻿package org.dlion.alarm;

import org.dlion.schedule.DScheduleManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 开机启动加载这个
 */
public class DAlarmBootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
			new DScheduleManager(context).scheduleBootAdd();
		}
	}

}
