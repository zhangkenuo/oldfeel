package org.dlion.footsince;

import org.dlion.oldfeel.R;

import android.graphics.Bitmap;

/**
 * 文件信息类
 */
public class FileInfo {
	public boolean isDirectory = false;
	public String name;
	public String path;
	public String lastModified;
	public String type;
	public long size;
	public int fileCount = 0;
	public int folderCount = 0;
	public Bitmap bitmap;

	public int getIconResourceId() {
		if (isDirectory) {
			return R.drawable.d_file_icon_dir;
		} else if (type.equalsIgnoreCase("video/*")) {
			return R.drawable.d_file_icon_video;
		} else if (type.equalsIgnoreCase("image/*")) {
			return R.drawable.d_file_icon_img;
		}
		return R.drawable.ic_launcher;
	}
}
