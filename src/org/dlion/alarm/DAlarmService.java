package org.dlion.alarm;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

public class DAlarmService extends Service {
	private final String RING_DIR_PATH = android.os.Environment
			.getExternalStorageDirectory().getAbsolutePath() + "/oldfeel/ring/";
	private MediaPlayer player;
	private String ringName = "";
	private boolean isEnable = false;

	@Override
	public IBinder onBind(Intent intent) {
		if (intent != null) {
			ringName = intent.getStringExtra("ringName");
			isEnable = intent.getBooleanExtra("isEnable", false);
			Log.d("oldfeel", ringName);
			if (isEnable) {
				playMusic(ringName);
			} else {
				stopMusic();
			}
		}
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (player != null) {
			player.stop();
			player.release();
		}
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		if (intent != null) {
			ringName = intent.getStringExtra("ringName");
			isEnable = intent.getBooleanExtra("isEnable", false);
			if (isEnable) {
				playMusic(ringName);
			} else {
				stopMusic();
			}
		}
	}

	private void stopMusic() {
		if (player != null) {
			player.stop();
			try {
				player.release();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void playMusic(String ringName) {
		Log.d("oldfeel", "play ring" + ringName);
		String ringFilePath = RING_DIR_PATH + ringName;
		if (player == null) {
			try {
				player = new MediaPlayer();
				player.setDataSource(ringFilePath);
				AudioManager audioManager = (AudioManager) this
						.getSystemService(Context.AUDIO_SERVICE);
				if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
					player.setAudioStreamType(AudioManager.STREAM_ALARM);
					player.setLooping(true);
					player.prepare();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}