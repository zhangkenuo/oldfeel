package org.dlion.footsince;

import java.io.File;
import java.util.ArrayList;

import org.dlion.oldfeel.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore.Video;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author dlion
 * 
 */
public class DFootFile extends Activity {

	private static final int MENU_DELETE = Menu.FIRST;
	private static final int MENU_RENAME = Menu.FIRST + 1;
	private GridView fileBrowse;
	private FileAdapter fileAdapter;
	private ArrayList<LoadedImage> photos = new ArrayList<LoadedImage>();
	private EditText etRename;
	private LoadedImage loadedImage;
	public String path;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.d_footsince_file_list);

		fileBrowse = (GridView) findViewById(R.id.arc_hf_file_show);
		setupViews();
		setProgressBarIndeterminateVisibility(true);

		// 注册上下文菜单
		registerForContextMenu(fileBrowse);
		new AsyncLoadedImage().execute();

	}

	/*
	 * 覆写上下文菜单
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		AdapterView.AdapterContextMenuInfo info = null;

		try {
			info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		} catch (ClassCastException e) {
			return;
		}
		menu.setHeaderTitle(photos.get(info.position).getName());
		menu.add(0, MENU_DELETE, 1, "删除");
		menu.add(0, MENU_RENAME, 2, "重命名");
	}

	/*
	 * 上下文菜单监听
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		loadedImage = photos.get(info.position);
		switch (item.getItemId()) {
		case MENU_DELETE:
			fileDel(loadedImage);
			return true;
		case MENU_RENAME:
			fileRename(loadedImage);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	/*
	 * 初始化文件浏览View
	 */
	private void setupViews() {
		fileAdapter = new FileAdapter();
		fileBrowse.setAdapter(fileAdapter);
		fileBrowse.setOnItemClickListener(new fileListener());
	}

	/*
	 * 添加Adapter元素
	 */
	private void addImage(LoadedImage... value) {
		for (LoadedImage image : value) {
			fileAdapter.addPhoto(image);
			fileAdapter.notifyDataSetChanged();
		}
	}

	/*
	 * 删除Adapter元素
	 */
	private void fileDel(LoadedImage photo) {
		File file = new File(photo.getPath());
		try {
			file.delete();
			Log.i("delete", photo.getPath());
		} catch (Exception e) {
			e.printStackTrace();
		}
		fileAdapter.delPhoto(photo);
		fileAdapter.notifyDataSetChanged();
	}

	/*
	 * 重命名Adapter元素
	 */
	private void fileRename(LoadedImage photo) {
		this.loadedImage = photo;
		etRename = new EditText(this);
		etRename.setText(photo.getName());
		etRename.selectAll();
		new AlertDialog.Builder(this).setView(etRename)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						String newName = etRename.getText().toString().trim();
						if ((loadedImage.getType()).equals("jpg")) {
							newName += ".jpg";
						} else if ((loadedImage.getType()).equals("3gp")) {
							newName += ".3gp";
						}

						fileAdapter.renamePhoto(loadedImage, newName);
					}
				}).setNegativeButton("取消", null).show();
	}

	/*
	 * 点击监听
	 */
	class fileListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> paramAdapterView,
				View paramView, int paramInt, long paramLong) {
			File file = new File(photos.get(paramInt).getPath());
			String type = getFileType(photos.get(paramInt).getType());
			Intent intent = new Intent();
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setAction(android.content.Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(file), type);
			startActivity(intent);
		}
	}

	private String getFileType(String type) {
		if (type.equals("3gp")) {
			type = "video";
		} else if (type.equals("jpg")) {
			type = "image";
		} else {
			type = "*";
		}
		type += "/*";
		return type;
	}

	/*
	 * 异步加载缩略图到LoadedImage然后调用addImage方法更新Adapter
	 */
	class AsyncLoadedImage extends AsyncTask<Object, LoadedImage, Object> {

		private String name;
		private String type;
		private File[] files;
		private String[] paths;

		@Override
		protected Object doInBackground(Object... params) {
			path = getIntent().getStringExtra("path");
			File file = new File(path);
			if (!file.exists()) {
				file.mkdirs();
			} else {
				files = file.listFiles();
				paths = new String[files.length];
				Bitmap bitmap;
				Bitmap newBitmap = null;
				for (int i = 0; i < files.length; i++) {
					paths[i] = files[i].getPath();
					String filePath = paths[i];
					try {
						if ((filePath.lastIndexOf("/") != -1)
								&& (filePath.lastIndexOf(".") != -1)) {
							name = filePath.substring(
									filePath.lastIndexOf("/") + 1,
									filePath.lastIndexOf("."));
							type = filePath.substring(
									filePath.lastIndexOf(".") + 1,
									filePath.length());
							if (type.equals("3gp")) {
								newBitmap = ThumbnailUtils
										.createVideoThumbnail(filePath,
												Video.Thumbnails.MINI_KIND);
							} else if (type.equals("jpg")) {
								BitmapFactory.Options options = new BitmapFactory.Options();
								options.inSampleSize = 10;
								bitmap = BitmapFactory.decodeFile(filePath,
										options);
								newBitmap = ThumbnailUtils.extractThumbnail(
										bitmap, 300, 240);
								bitmap.recycle();
							} else {
								name = filePath.substring(
										filePath.lastIndexOf("/") + 1,
										filePath.length());
								newBitmap = ((BitmapDrawable) getResources()
										.getDrawable(R.drawable.d_file_icon_img))
										.getBitmap();
							}
							if (newBitmap != null) {
								publishProgress(new LoadedImage(newBitmap,
										filePath, name, type));
							}
						} else {
							name = filePath.substring(
									filePath.lastIndexOf("/") + 1,
									filePath.length());
							newBitmap = ((BitmapDrawable) getResources()
									.getDrawable(R.drawable.d_file_icon_img)).getBitmap();
							publishProgress(new LoadedImage(newBitmap,
									filePath, name, type));
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			return null;
		}

		@Override
		public void onProgressUpdate(LoadedImage... value) {
			addImage(value);
		}

		@Override
		protected void onPostExecute(Object result) {
			setProgressBarIndeterminateVisibility(false);
		}
	}

	/*
	 * Adapter
	 */
	class FileAdapter extends BaseAdapter {

		public void addPhoto(LoadedImage photo) {
			photos.add(photo);
		}

		/**
		 * @param value
		 * @param newName
		 */
		public void renamePhoto(LoadedImage photo, String newName) {
			String tempName = newName.substring(0, newName.lastIndexOf("."));
			File file = new File(photo.getPath());
			File newFile = new File(path, newName);
			if (newFile.exists()) {
				showMsg(newName + "已经存在");
			} else
				file.renameTo(newFile);
			photo.setName(tempName);
			photo.setPath(path + newName);
		}

		public void delPhoto(LoadedImage photo) {
			photos.remove(photo);
		}

		public int getCount() {
			return photos.size();
		}

		public Object getItem(int position) {
			return photos.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = getLayoutInflater().inflate(R.layout.d_footsince_file_item,
					null);
			ImageView icon = (ImageView) convertView
					.findViewById(R.id.arc_hf_file_icon);
			ImageView style = (ImageView) convertView
					.findViewById(R.id.arc_hf_file_icon_style);
			TextView name = (TextView) convertView
					.findViewById(R.id.arc_hf_file_name);
			icon.setImageBitmap(photos.get(position).getBitmap());
			if (photos.get(position).getType().equals("jpg")) {
				style.setImageResource(R.drawable.d_file_icon_img);
			} else if (photos.get(position).getType().equals("3gp")) {
				style.setImageResource(R.drawable.d_file_icon_video);
			}
			name.setText(photos.get(position).getName());
			return convertView;
		}
	}

	/*
	 * 这是个保存bitmap的类，加入Adapter的ArrayList中，随着addImage更新Adapter
	 */
	private static class LoadedImage {
		Bitmap mBitmap;
		String mName;
		String mPath;
		String mType;

		LoadedImage(Bitmap bitmap, String path, String name, String type) {
			mBitmap = bitmap;
			mName = name;
			mPath = path;
			mType = type;
		}

		public void setPath(String path) {
			mPath = path;
		}

		public void setName(String newName) {
			mName = newName;
		}

		public Bitmap getBitmap() {
			return mBitmap;
		}

		public String getPath() {
			return mPath;
		}

		public String getName() {
			return mName;
		}

		public String getType() {
			return mType;
		}
	}

	/*
	 * 消息提示
	 */
	private Toast toast;

	public void showMsg(String arg) {
		if (toast == null) {
			toast = Toast.makeText(this, arg, Toast.LENGTH_SHORT);
		} else {
			toast.cancel();
			toast.setText(arg);
		}
		toast.show();
	}
}