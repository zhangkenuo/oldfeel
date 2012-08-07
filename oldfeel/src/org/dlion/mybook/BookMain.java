package org.dlion.mybook;

import java.io.File;
import java.util.ArrayList;

import org.dlion.oldfeel.DBHelper;
import org.dlion.oldfeel.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class BookMain extends Activity {
	private NotificationManager notificationManager;
	private Notification notification;

	String sdCardPath = Environment.getExternalStorageDirectory()
			.getAbsolutePath().toString();
	String TXT_FILE = "txt";

	GridView bookGridView;
	TextView textView;
	BookAdapter bookAdapter;

	private ArrayList<File> bookList = new ArrayList<File>();

	private ImageView bookLocation;

	private SQLiteDatabase db;
	private ImageView bookNetWork;
	private OnClickListener btnListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			switch (arg0.getId()) {
			case R.id.book_location:
				addLocationBook();
				break;
			case R.id.book_network:
				addNetWorkBook();
				break;
			default:
				break;
			}
		}
	};

	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		openDb();
		initNotification();
		initBookList();
		initView();
	}

	/**
	 * 发送推送通知
	 */
	private void sendNotification() {
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notification = new Notification(R.drawable.ic_launcher, "通知栏标题",
				System.currentTimeMillis());
		Intent notificationIntent = new Intent(this, BookMain.class);
		notificationIntent.putExtra("isNotification", true);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);
		notification.setLatestEventInfo(this, "标题", "通知内容", pendingIntent);
		notificationManager.notify(1, notification);
	};

	/**
	 * 初始化推送通知
	 */
	private void initNotification() {
		if (notificationIsOpen()) {
			sendNotification();
		}
		if (getIntent().getBooleanExtra("isNotification", false)) {
			notificationManager.cancel(1);
		}
	}

	/**
	 * 判断推送通知是否打开
	 */
	private boolean notificationIsOpen() {
		SharedPreferences notificationSp = getSharedPreferences("notification",
				Context.MODE_PRIVATE);
		boolean isChecked = notificationSp.getBoolean("notificationIsChecked",
				true);
		return isChecked;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_option_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
			startActivity(new Intent(BookMain.this, BookReadingConfig.class));
			return true;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * 初始化书列表
	 */
	private void initBookList() {
		bookList.clear();
		Cursor cursor = DBHelper.getAllCursor(db, "book_list");
		while (cursor.moveToNext()) {
			String bookname = cursor.getString(cursor
					.getColumnIndex("bookname"));
			File file = new File(bookname);
			bookList.add(file);
		}
	}

	/**
	 * 初始化视图
	 */
	private void initView() {
		bookGridView = (GridView) findViewById(R.id.listfile);
		bookAdapter = new BookAdapter();
		bookGridView.setAdapter(bookAdapter);
		bookGridView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View view, int pos,
					long id) {
				String pathes = bookList.get(pos).getAbsolutePath();
				Intent intent = new Intent();
				intent.setClass(BookMain.this, BookReading.class);
				intent.putExtra("pathes", pathes);
				startActivity(intent);
				bookAdapter.notifyDataSetChanged();
			}
		});
		bookGridView.setOnCreateContextMenuListener(this);
		bookLocation = (ImageView) findViewById(R.id.book_location);
		bookNetWork = (ImageView) findViewById(R.id.book_network);
		bookLocation.setOnClickListener(btnListener);
		bookNetWork.setOnClickListener(btnListener);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		getMenuInflater().inflate(R.menu.main_context_menu, menu);
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		File file = bookList.get(info.position);
		switch (item.getItemId()) {
		case R.id.book_rename:
			bookRename(file);
			break;
		case R.id.book_remove:
			bookRemove(file);
			break;
		case R.id.book_delete:
			bookDelete(file);
			break;
		default:
			break;
		}
		return super.onContextItemSelected(item);
	}

	private void bookDelete(final File file) {
		new AlertDialog.Builder(this).setTitle("删除文件")
				.setMessage("从sdcard删除文件？")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (file.delete()) {
							showToast("删除" + BookUtil.getFileName(file) + "成功");
							openDb();
							db.delete("book_list", "bookname = ?",
									new String[] { file.getAbsolutePath()
											.toString() });
							initBookList();
							bookAdapter.notifyDataSetChanged();
						}
					}
				}).setNegativeButton("取消", null).show();
	}

	private void bookRemove(File file) {
		openDb();
		db.delete("book_list", "bookname = ?", new String[] { file
				.getAbsolutePath().toString() });
		initBookList();
		bookAdapter.notifyDataSetChanged();
	}

	private void bookRename(final File file) {
		final EditText etNewName = new EditText(this);
		etNewName.setText(BookUtil.getFileName(file));
		new AlertDialog.Builder(this).setTitle("重命名").setView(etNewName)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						String oldName = file.getAbsolutePath().toString();
						String newName = oldName.substring(0,
								oldName.lastIndexOf("/") + 1)
								+ etNewName.getText().toString()
								+ "."
								+ BookUtil.getFileType(file);
						file.renameTo(new File(newName));
						openDb();
						db.execSQL("insert into book_list(bookname)values('"
								+ newName + "');");
						db.delete("book_list", "bookname = ?",
								new String[] { oldName });
						initBookList();
						bookAdapter.notifyDataSetChanged();
					}
				}).setNegativeButton("取消", null).show();
	}

	/**
	 * 添加本地书
	 */
	protected void addLocationBook() {
		Intent intent = new Intent();
		intent.setClass(this, BookLocation.class);
		startActivity(intent);
	}

	protected void addNetWorkBook() {
		showToast("暂不支持");
	}

	class BookAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return bookList.size();
		}

		@Override
		public Object getItem(int position) {
			return bookList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Context context = getApplicationContext();
			LinearLayout view = new LinearLayout(context);
			ImageView img = new ImageView(context);
			img.setImageResource(R.drawable.book);
			TextView textView = new TextView(context);
			textView.setText(BookUtil.getFileName(bookList.get(position)));
			textView.setGravity(Gravity.CENTER);
			textView.setTextColor(getResources().getColor(R.color.black));
			view.setOrientation(LinearLayout.VERTICAL);
			view.addView(img, LayoutParams.FILL_PARENT,
					LayoutParams.WRAP_CONTENT);
			view.addView(textView, LayoutParams.FILL_PARENT,
					LayoutParams.WRAP_CONTENT);
			return view;
		}

	}

	public void openDb() {
		if (db == null || !db.isOpen()) {
			db = DBHelper.openDb(this);
		}
	}

	private Toast toast;

	private void showToast(String string) {
		if (toast == null) {
			toast = Toast.makeText(this, string, Toast.LENGTH_SHORT);
		} else {
			toast.cancel();
			toast = Toast.makeText(this, string, Toast.LENGTH_SHORT);
		}
		toast.show();
	}

	public void showLog(String str) {
		Log.d("MainActivity", str);
	}

	@Override
	protected void onResume() {
		initBookList();
		bookAdapter.notifyDataSetChanged();
		super.onResume();
	}

	@Override
	protected void onPause() {
		openDb();
		super.onPause();
	}

}