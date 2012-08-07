package org.dlion.schedule;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		showLog("AlarmReceiver onReceive");
		Intent serviceIntent = new Intent("org.dlion.dalarm.AlarmService");
		context.startService(serviceIntent);
	}

	private void showLog(String log) {
		Log.d("AlarmReceiver", log);
	}
}