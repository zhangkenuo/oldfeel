package org.dlion.oldfeel;

import org.dlion.oldfeel.R;

/**
 * 文件信息类
 */
public class OldfeelFileInfo {
	String fileName;
	String filePath;
	String fileType;
	String fileTime;
	boolean isDirectory = false;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public String getFileTime() {
		return fileTime;
	}

	public void setFileTime(String fileTime) {
		this.fileTime = fileTime;
	}

	public boolean isDirectory() {
		return isDirectory;
	}

	public void setDirectory(boolean isDirectory) {
		this.isDirectory = isDirectory;
	}

	public int getIconResourceId() {
		if (isDirectory) {
			return R.drawable.d_file_icon_dir;
		} else if (fileType == "video") {
			return R.drawable.d_file_icon_video;
		}
		return R.drawable.d_file_icon_img;
	}
}
