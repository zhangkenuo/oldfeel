package org.dlion.oldfeel;

import java.io.File;
import java.util.ArrayList;

import org.dlion.oldfeel.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore.Video;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FileBrowser extends Activity {

	String basePath = Environment.getExternalStorageDirectory()
			.getAbsoluteFile().toString()
			+ "/oldfeel";
	String currentPath;
	ListView fileListView;
	ArrayList<FileInfo> files;
	FileAdapter fileAdapter;
	private OnItemClickListener listener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			FileInfo info = files.get(arg2);
			if (info.isDirectory) {
				initFileListView(info.path);
			} else {
				openFile(info.path, info.type);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.file);
		currentPath = getIntent().getStringExtra("path");
		initFileListView(currentPath);
	}

	/**
	 * 打开文件
	 */
	protected void openFile(String filePath, String type) {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);
		File f = new File(filePath);
		intent.setDataAndType(Uri.fromFile(f), type);
		startActivity(intent);
	}

	/**
	 * 初始化文件列表
	 */
	private void initFileListView(String path) {
		currentPath = path;
		setTitle(currentPath);
		// 初始化文件信息
		files = FileUtil.getFiles(FileBrowser.this, path);
		if (files == null) {
			return;
		}
		fileListView = (ListView) findViewById(R.id.d_file_listView);
		fileAdapter = new FileAdapter();
		fileListView.setAdapter(fileAdapter);
		fileListView.setOnItemClickListener(listener);
		fileListView.setOnCreateContextMenuListener(this);
		new AsyncLoadedImage().execute();
	}

	/**
	 * 创建上下文菜单
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		getMenuInflater().inflate(R.menu.file_context_menu, menu);
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	/**
	 * 上下文菜单监听
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		File file = new File(files.get(info.position).path);
		switch (item.getItemId()) {
		case R.id.file_context_menu_rename:
			renameFile(file);
			return true;
			// case R.id.file_context_menu_copy:
			// pasteFile(file.getPath(), "COPY");
			// return true;
			// case R.id.file_context_menu_move:
			// pasteFile(file.getPath(), "MOVE");
			// return true;
		case R.id.file_context_menu_delete:
			FileUtil.deleteFile(file);
			initFileListView(currentPath);
			return true;
		case R.id.file_context_menu_detail:
			FileUtil.fileDetail(FileBrowser.this, file);
			return true;
		default:
			break;
		}
		return super.onContextItemSelected(item);
	}

	/**
	 * 重命名文件
	 */
	private void renameFile(final File file) {
		if (file.isDirectory()) {
			showMsg("该目录为自动生成,不可更改!");
			return;
		}
		final EditText view = new EditText(FileBrowser.this);
		view.setText(file.getName());
		new AlertDialog.Builder(FileBrowser.this).setView(view)
				.setTitle("重命名")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String filePath = file.getParentFile().getPath();
						String newName = view.getText().toString().trim();
						if (newName.equalsIgnoreCase(file.getName())) {
							return;
						}
						if (newName.length() == 0) {
							showMsg("名字不能为空");
							return;
						}
						String fullFileName = FileUtil.combinPath(filePath,
								newName);

						File newFile = new File(fullFileName);
						if (newFile.exists()) {
							showMsg(newName + "已经存在,请重新命名");
						} else {
							try {
								file.renameTo(newFile);
							} catch (Exception e) {
								showLog("重命名失败");
								e.printStackTrace();
							}
						}
						initFileListView(currentPath);
					}
				}).setNegativeButton("取消", null).show();
	}

	/**
	 * 复制粘贴
	 */
	// private void pasteFile(String path2, String action) {
	// Intent intent = new Intent();
	// Bundle bundle = new Bundle();
	// bundle.putString("CURRENTPASTEFILEPATH", currentPath);
	// bundle.putString("ACTION", action);
	// intent.putExtras(bundle);
	// intent.setClass(DFileBrowser.this, DPasteFile.class);
	// // 打开一个Activity并等待结果
	// startActivityForResult(intent, 0);
	// }

	/**
	 * 异步加载缩略图
	 */
	class AsyncLoadedImage extends AsyncTask<String, LoadedImage, String> {
		@Override
		protected String doInBackground(String... params) {
			for (int i = 0; i < files.size(); i++) {
				FileInfo info = files.get(i);
				if (info.type.equalsIgnoreCase("video/*")) {
					info.bitmap = ThumbnailUtils.createVideoThumbnail(
							info.path, Video.Thumbnails.MINI_KIND);
				} else if (info.type.equalsIgnoreCase("image/*")) {
					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inSampleSize = 10;
					Bitmap bitmap = BitmapFactory
							.decodeFile(info.path, options);
					info.bitmap = ThumbnailUtils.extractThumbnail(bitmap, 48,
							48);
				}
				publishProgress(new LoadedImage(i, info));
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(LoadedImage... values) {
			updateListView(values);
		}

	}

	private void updateListView(LoadedImage... value) {
		for (LoadedImage img : value) {
			files.set(img.i, img.info);
			fileAdapter.notifyDataSetChanged();
		}
	}

	class LoadedImage {
		public int i;
		public FileInfo info;

		public LoadedImage(int i, FileInfo info) {
			this.i = i;
			this.info = info;
		}
	}

	/**
	 * 文件adapter
	 */
	public class FileAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return files.size();
		}

		@Override
		public Object getItem(int arg0) {
			return files.get(arg0);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = getLayoutInflater().inflate(R.layout.file_item,
					null);
			ImageView ivIcon = (ImageView) convertView
					.findViewById(R.id.d_file_item_icon);
			TextView tvName = (TextView) convertView
					.findViewById(R.id.d_file_item_name);
			TextView tvTime = (TextView) convertView
					.findViewById(R.id.d_file_item_time);
			FileInfo info = files.get(position);
			if (info.bitmap == null) {
				showLog("no bitmap");
				ivIcon.setImageResource(info.getIconResourceId());
			} else {
				showLog("have bitmap");
				ivIcon.setImageBitmap(info.bitmap);
			}
			tvName.setText(info.name);
			tvTime.setText(info.lastModified);
			showLog("getView");
			return convertView;
		}
	}

	/**
	 * 覆写返回键监听
	 */
	@Override
	public void onBackPressed() {
		File file = new File(currentPath);
		String parentPath = file.getParent();
		if (!parentPath.equalsIgnoreCase(basePath)) {
			initFileListView(parentPath);
		} else {
			finish();
		}
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
	protected void showLog(String string) {
		Log.d("DFileBrowser", string);
	}
}
