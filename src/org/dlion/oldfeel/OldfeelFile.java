package org.dlion.oldfeel;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class OldfeelFile extends Activity {

	String path = Environment.getExternalStorageDirectory().getAbsoluteFile()
			.toString()
			+ "/oldfeel";;
	ArrayList<OldfeelFileInfo> files = new ArrayList<OldfeelFileInfo>();
	ListView fileListView;
	fileAdapter adapter = new fileAdapter();
	private OnItemClickListener listener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.oldfeel_file);
		setTitle(path);
		initFileInfo();
		initFileListView();
	}

	/**
	 * 初始化文件信息
	 */
	private void initFileInfo() {
		ArrayList<OldfeelFileInfo> temp = OldfeelFileHelper.getFiles(
				OldfeelFile.this, path);
		files = temp;
	}

	/**
	 * 初始化文件列表
	 */
	private void initFileListView() {
		fileListView = (ListView) findViewById(R.id.oldfeel_file_listView);
		fileListView.setAdapter(adapter);
		fileListView.setOnItemClickListener(listener);
		fileListView.setOnCreateContextMenuListener(this);
	}

	/**
	 * 创建上下文菜单
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		getMenuInflater().inflate(R.menu.footname_context_menu, menu);
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	/**
	 * 上下文菜单监听
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		return super.onContextItemSelected(item);
	}

	/**
	 * 文件adapter
	 */
	public class fileAdapter extends BaseAdapter {

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
			convertView = getLayoutInflater().inflate(
					R.layout.oldfeel_file_item, null);
			ImageView ivIcon = (ImageView) convertView
					.findViewById(R.id.oldfeel_file_item_icon);
			TextView tvName = (TextView) convertView
					.findViewById(R.id.oldfeel_file_item_name);
			TextView tvTime = (TextView) convertView
					.findViewById(R.id.oldfeel_file_item_time);
			OldfeelFileInfo file = files.get(position);
			ivIcon.setImageResource(file.getIconResourceId());
			tvName.setText(file.getFileName());
			tvTime.setText(file.getFileTime());
			return convertView;
		}
	}
}
