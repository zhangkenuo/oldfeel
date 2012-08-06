package org.dlion.mybook;

import java.io.File;
import java.util.ArrayList;

import org.dlion.oldfeel.DBHelper;
import org.dlion.oldfeel.R;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;

public class BookLocation extends Activity {
	public static final String sdCardPath = Environment
			.getExternalStorageDirectory().getAbsolutePath().toString();
	public static final String FILE_TYPE = "txt";
	public static final int CURRENT_PATH_FILE = 1;
	public static final int SCAN_FILE = 2;
	private ListView locBookList;
	private Button btnSubmit;
	private Button btnScan;
	private Button btnParentFile;
	private TextView tvCurPath;
	private ArrayList<File> fileList = new ArrayList<File>();
	private ArrayList<String> addBookList = new ArrayList<String>();
	private ArrayList<String> delBookList = new ArrayList<String>();
	private String currentPath;
	BookAdapter bookAdapter = new BookAdapter();
	private OnClickListener btnListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_submit:
				syncBookToList();
				break;
			case R.id.btn_scan:
				fileList.clear();
				new ShowFiles().execute(SCAN_FILE);
				break;
			case R.id.btn_parentFile:
				pathToParentFile();
				break;
			default:
				break;
			}
		}
	};
	private SQLiteDatabase db;
	private ArrayList<String> bookList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.location_list);
		initBookList();
		locBookList = (ListView) findViewById(R.id.loc_book_list);
		tvCurPath = (TextView) findViewById(R.id.tv_curPath);
		btnSubmit = (Button) findViewById(R.id.btn_submit);
		btnScan = (Button) findViewById(R.id.btn_scan);
		btnParentFile = (Button) findViewById(R.id.btn_parentFile);
		if ((currentPath = getIntent().getStringExtra("currentPath")) == null)
			currentPath = sdCardPath;
		initView();
	}

	protected void pathToParentFile() {
		File file = new File(currentPath);
		if (currentPath == sdCardPath || file.getParent().equals(sdCardPath)) {
			finish();
		} else {
			currentPath = file.getParent().toString();
			refreshLocationBook();
		}
	}

	protected void refreshLocationBook() {
		Intent intent = new Intent();
		intent.setClass(this, BookLocation.class);
		intent.putExtra("currentPath", currentPath);
		startActivity(intent);
		finish();
	}

	private void initBookList() {
		if (db == null || !db.isOpen()) {
			db = DBHelper.openDb(this);
		}
		bookList = new ArrayList<String>();
		Cursor c = DBHelper.getAllCursor(db, "book_list");
		while (c.moveToNext())
			bookList.add(c.getString(c.getColumnIndex("bookname")));
	}

	private void initView() {
		new ShowFiles().execute(CURRENT_PATH_FILE);
		locBookList.setAdapter(bookAdapter);
		tvCurPath.setText(currentPath);
		btnSubmit.setOnClickListener(btnListener);
		btnScan.setOnClickListener(btnListener);
		btnParentFile.setOnClickListener(btnListener);
	}

	protected void syncBookToList() {
		for (int i = 0; i < addBookList.size(); i++) {
			ContentValues values = new ContentValues();
			values.put("bookname", addBookList.get(i));
			db.insert("book_list", "_id", values);
		}
		for (int i = 0; i < delBookList.size(); i++) {
			db.delete("book_list", "bookname = ?",
					new String[] { delBookList.get(i) });
		}
	}

	class BookAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return fileList.size();
		}

		@Override
		public Object getItem(int position) {
			return fileList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View view, ViewGroup parent) {
			view = getLayoutInflater().inflate(R.layout.location_list_item,
					null);
			TextView tvBookName = (TextView) view
					.findViewById(R.id.tv_bookName);
			TextView tvBookType = (TextView) view
					.findViewById(R.id.tv_bookType);
			TextView tvBookSize = (TextView) view
					.findViewById(R.id.tv_bookSize);
			CheckBox cbIsAdd = (CheckBox) view.findViewById(R.id.cb_isAdd);
			tvBookName.setText(BookUtil.getFileName(fileList.get(position)));
			tvBookType.setText(BookUtil.getFileType(fileList.get(position)));
			tvBookSize.setText(BookUtil.getFileSize(fileList.get(position)));
			cbIsAdd.setVisibility(BookUtil.isVisibility(BookUtil
					.getFileType(fileList.get(position))));
			cbIsAdd.setChecked(BookUtil.isChecked(bookList,
					fileList.get(position)));
			cbIsAdd.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					if (isChecked) {
						if (!BookUtil.isChecked(bookList,
								fileList.get(position))) {
							addBookList.add(fileList.get(position)
									.getAbsolutePath());
						}
					} else {
						if (BookUtil.isChecked(bookList, fileList.get(position))) {
							delBookList.add(fileList.get(position)
									.getAbsolutePath());
						} else {
							addBookList.remove(addBookList.indexOf(fileList
									.get(position).getAbsolutePath()));
						}
					}
				}
			});
			view.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (fileList.get(position).isDirectory()) {
						currentPath = fileList.get(position).getAbsolutePath()
								.toString();
						refreshLocationBook();
					}
				}
			});
			return view;
		}
	}

	class ShowFiles extends AsyncTask<Integer, File, File> {

		@Override
		protected File doInBackground(Integer... params) {
			switch (params[0]) {
			case CURRENT_PATH_FILE:
				File rootFile = new File(currentPath);
				File files[] = rootFile.listFiles();
				if (files != null) {
					for (File f : files) {
						publishProgress(f);
					}
				}
				break;
			case SCAN_FILE:
				getScanFile(currentPath);
				break;
			default:
				break;
			}
			return null;
		}

		protected void getScanFile(String path) {
			File rootFile = new File(path);
			File files[] = rootFile.listFiles();
			if (files != null) {
				for (File f : files) {
					if (f.isDirectory()) {
						getScanFile(f.getAbsolutePath().toString());
					} else {
						if (BookUtil.getFileType(f).equals("txt")) {
							publishProgress(f);
						}
					}
				}
			}
		}

		@Override
		protected void onPostExecute(File result) {
			super.onPostExecute(result);
		}

		@Override
		protected void onProgressUpdate(File... values) {
			updateBookFiles(values);
		}
	}

	public void updateBookFiles(File[] values) {
		for (File file : values) {
			fileList.add(file);
			bookAdapter.notifyDataSetChanged();
		}
	}

	@Override
	protected void onPause() {
		if (db != null && db.isOpen()) {
			db.close();
		}
		super.onPause();
	}

	public void showLog(String log) {
		Log.d("LocationBook", log);
	}
}
