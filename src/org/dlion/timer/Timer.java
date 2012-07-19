package org.dlion.timer;

import java.util.ArrayList;

import org.dlion.oldfeel.DBHelper;
import org.dlion.oldfeel.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class Timer extends Activity {
	SQLiteDatabase db;
	GridView timerGridView;
	TextView timerTextView;
	Button timerBtnAdd, timerBtnStop;
	private ArrayList<String> names;
	private ArrayList<TimerInfo> timers;
	private ArrayAdapter<String> adapter;
	private final static int STOP_TIMER_BECAUSE_ADD = 1;
	private final static int STOP_TIMER_BECAUSE_CONTEXTMENU = 2;
	private final static int STOP_TIMER_BECAUSE_START = 3;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.timer);
		setTitle("计时器");
		db = DBHelper.openOldfeelDb(this);
		timerGridView = (GridView) findViewById(R.id.d_timer_gridView);
		timerTextView = (TextView) findViewById(R.id.d_timer_textView);
		timerBtnAdd = (Button) findViewById(R.id.d_timer_btnAdd);
		timerBtnStop = (Button) findViewById(R.id.timer_btnStop);
		initTimerView();
	}

	/**
	 * 初始化计时器视图
	 */
	private void initTimerView() {
		initTimerData();
		adapter = new ArrayAdapter<String>(Timer.this,
				android.R.layout.simple_list_item_1, android.R.id.text1, names);
		timerGridView.setAdapter(adapter);
		timerGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if (!bool)
					startTimer(timers.get(arg2).time);
				else
					stopTimer(STOP_TIMER_BECAUSE_START, timers.get(arg2).time);
			}
		});
		timerGridView.setOnCreateContextMenuListener(this);
		timerBtnAdd.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!bool)
					addTimer();
				else
					stopTimer(STOP_TIMER_BECAUSE_ADD, 0);
			}
		});
		timerBtnStop.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				stopTimer(0, 0);
			}
		});
	}

	/**
	 * 初始化计时器数据
	 */
	private void initTimerData() {
		names = new ArrayList<String>();
		timers = new ArrayList<TimerInfo>();
		Cursor c = DBHelper.getAllCursor(db, "timer");
		while (c.moveToNext()) {
			int _id = c.getInt(c.getColumnIndex("_id"));
			int time = c.getInt(c.getColumnIndex("time"));
			String name = c.getString(c.getColumnIndex("name"));
			String ring = c.getString(c.getColumnIndex("ring"));
			TimerInfo timerInfo = new TimerInfo(_id, time, name, ring);
			names.add(name);
			timers.add(timerInfo);
		}
	}

	/**
	 * 添加计时器
	 */
	protected void addTimer() {
		new TimePickerDialog(Timer.this,
				new TimePickerDialog.OnTimeSetListener() {
					@Override
					public void onTimeSet(TimePicker view, int hourOfDay,
							int minute) {
						ContentValues values = new ContentValues();
						int time = hourOfDay * 60 + minute;
						values.put("time", time);
						values.put("name", String.valueOf(time));
						values.put("ring", "ring");
						db.insert("timer", "_id", values);
						initTimerView();
					}
				}, 0, 0, true).show();
	}

	/**
	 * 创建上下文菜单
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		if (!bool) {
			getMenuInflater().inflate(R.menu.timer_context_menu, menu);
			super.onCreateContextMenu(menu, v, menuInfo);
		} else {
			stopTimer(STOP_TIMER_BECAUSE_CONTEXTMENU, 0);
		}
	}

	/**
	 * 上下文菜单监听
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		TimerInfo timerInfo = timers.get(info.position);
		switch (item.getItemId()) {
		case R.id.timer_context_menu_delete:
			timers.remove(info.position);
			timerDelete(timerInfo);
			break;
		case R.id.timer_context_menu_edit:
			timerEdit(timerInfo);
			break;
		case R.id.timer_context_menu_add:
			addTimer();
			break;
		default:
			break;
		}
		return super.onContextItemSelected(item);
	}

	/**
	 * 删除计时器
	 */
	private void timerDelete(TimerInfo timerInfo) {
		db.delete("timer", "_id = " + timerInfo._id, null);
		initTimerView();
	}

	/**
	 * 编辑计时器
	 */
	private void timerEdit(final TimerInfo timerInfo) {
		View view = getLayoutInflater().inflate(R.layout.timer_edit, null);
		final EditText etTime = (EditText) view
				.findViewById(R.id.timer_edit_time);
		final EditText etName = (EditText) view
				.findViewById(R.id.timer_edit_name);
		Spinner spRing = (Spinner) view.findViewById(R.id.timer_edit_ring);
		etTime.setText("" + timerInfo.time);
		etName.setText(timerInfo.name);
		new AlertDialog.Builder(this).setTitle("编辑").setView(view)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						ContentValues values = new ContentValues();
						values.put(
								"time",
								Integer.valueOf(etTime.getText().toString()
										.trim()));
						values.put("name", etName.getText().toString().trim());
						db.update("timer", values, "_id = " + timerInfo._id,
								null);
						initTimerView();
					}
				}).setNegativeButton("取消", null).show();
	}

	/**
	 * 开始计时
	 */
	protected void startTimer(int time) {
		bool = true;
		second = time * 60;
		showLog("startTimer");
		handler.removeCallbacks(task);
		handler.postDelayed(task, 1000);
	}

	/**
	 * 停止计时
	 */
	protected void stopTimer(final int index, final int time) {
		new AlertDialog.Builder(this).setTitle("正在计时,确定要关闭吗?")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						bool = !bool;
						handler.removeCallbacks(task);
						timerTextView.setText("00:00:00");
						switch (index) {
						case STOP_TIMER_BECAUSE_ADD:
							addTimer();
							break;
						case STOP_TIMER_BECAUSE_CONTEXTMENU:
							break;
						case STOP_TIMER_BECAUSE_START:
							startTimer(time);
							break;
						default:
							break;
						}
					}
				}).setNegativeButton("取消", null).show();
	}

	private int hour = 0;
	private int minute = 0;
	private int second = 0;
	private boolean bool;
	/**
	 * 定时器设置，实现计时
	 */
	private Handler handler = new Handler();
	private Runnable task = new Runnable() {
		public void run() {
			if (bool) {
				handler.postDelayed(this, 1000);
				showLog(String.valueOf(second));
				int tempSecond = second % 60;
				minute = second / 60 % 60;
				hour = second / 3600;
				timerTextView.setText(format(hour) + ":" + format(minute) + ":"
						+ format(tempSecond));
				second--;
				if (second < 0) {
					bool = !bool;
					handler.removeCallbacks(task);
					playMusic();
				}
			}
		}
	};

	/**
	 * 播放音乐
	 */
	protected void playMusic() {
		showLog("play music");
	}

	/**
	 * 格式化时间
	 */
	public String format(int i) {
		String s = i + "";
		if (s.length() == 1) {
			s = "0" + s;
		}
		return s;
	}

	/**
	 * 格式化计时器
	 */
	public String formatTimer(int time) {
		int tempHour = time / 60;
		int tempMinute = time % 60;
		String tempTimer = format(tempHour) + ":" + format(tempMinute);
		return tempTimer;
	}

	@Override
	protected void onResume() {
		if (!db.isOpen() || db == null) {
			db = DBHelper.openOldfeelDb(this);
		}
		super.onResume();
	}

	@Override
	protected void onPause() {
		if (db.isOpen()) {
			db.close();
		}
		super.onPause();
	}

	public Toast toast;

	/**
	 * 消息提示
	 */
	public void showMsg(String arg) {
		if (toast == null) {
			toast = Toast.makeText(this, arg, Toast.LENGTH_SHORT);
		} else {
			toast.cancel();
			toast.setText(arg);
		}
		toast.show();
	}

	/**
	 * 显示Log
	 */
	protected void showLog(String log) {
		Log.d("DFootSince", log);
	}
}
