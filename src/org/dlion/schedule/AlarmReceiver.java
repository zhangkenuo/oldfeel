package org.dlion.schedule;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("receiver", "receiver is start");
		Intent serviceIntent = new Intent("org.dlion.dalarm.AlarmService");
		boolean isEnable = intent.getBooleanExtra("isEnable", false);
		String ringName = intent.getStringExtra("ringName");
		serviceIntent.putExtra("isEnable", isEnable);
		serviceIntent.putExtra("ringName", ringName);
		if (isEnable) {
			Log.i("Receiver", "true");
			context.startService(serviceIntent);
		} else {
			Log.i("Receiver", "false");
			context.stopService(serviceIntent);
		}
	}
}
