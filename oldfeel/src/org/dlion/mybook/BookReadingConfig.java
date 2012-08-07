package org.dlion.mybook;

import org.dlion.oldfeel.R;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class BookReadingConfig extends Activity {

	private TextView notificationStatus;
	private CheckBox notificationIsChecked;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reading_setting);
		notificationStatus = (TextView) findViewById(R.id.setting_notification_status);
		notificationIsChecked = (CheckBox) findViewById(R.id.setting_notification_isOpen);
		notificationIsChecked.setChecked(notificationIsOpen());
		notificationStatus
				.setText((notificationIsOpen()) ? "启用推送通知" : "关闭推送通知");
		notificationIsChecked
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						SharedPreferences notificationSp = getSharedPreferences(
								"notification", Context.MODE_PRIVATE);
						Editor editor = notificationSp.edit();
						editor.putBoolean("notificationIsChecked", isChecked);
						editor.commit();
						if (isChecked) {
							notificationStatus.setText("启用推送通知");
						} else {
							notificationStatus.setText("关闭推送通知");
						}
					}
				});
	}

	// 判断设置中的推送通知是否已经打开
	private boolean notificationIsOpen() {
		SharedPreferences notificationSp = getSharedPreferences("notification",
				Context.MODE_PRIVATE);
		boolean isChecked = notificationSp.getBoolean("notificationIsChecked",
				true);
		return isChecked;
	}
}
