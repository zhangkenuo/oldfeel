package org.dlion.schedule;

import org.dlion.oldfeel.R;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class AlarmRing extends Activity {

	private MediaPlayer player;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		showLog("onCreate");
		setContentView(R.layout.alarm_ring);
		playRing();
		TextView tv = (TextView) findViewById(R.id.alarm_dialog_content);
		tv.setText("AlarmAlert");
		Button btnDelay = (Button) findViewById(R.id.alarm_dialog_btnDelay);
		Button btnCancel = (Button) findViewById(R.id.alarm_dialog_btnCancel);
		btnDelay.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showLog("delay");
				delayRing();
			}
		});
		btnCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showLog("cancel");
				stopRing();
			}
		});
	}

	/**
	 * 推迟5分钟
	 */
	protected void delayRing() {
		showLog("delayRing 5 minute");
	}

	/**
	 * 播放铃声
	 */
	private void playRing() {
		showLog("playRing");
		player = new MediaPlayer();
		player = MediaPlayer.create(this, R.raw.ooo);
		player.start();
	}

	/**
	 * 停止播放
	 */
	private void stopRing() {
		showLog("stopRing");
		player.stop();
		player.release();
		finish();
	}

	/**
	 * 显示Log
	 */
	private void showLog(String log) {
		Log.d("AlertActivity", log);
	}

	@Override
	public void onBackPressed() {
		stopRing();
		super.onBackPressed();
	}

}
