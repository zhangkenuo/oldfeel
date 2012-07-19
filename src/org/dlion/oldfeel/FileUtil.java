package org.dlion.oldfeel;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import org.dlion.oldfeel.R;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

/**
 * 文件处理工具
 */
public class FileUtil {
	/**
	 * 获取一个文件夹下的所有文件
	 */
	public static ArrayList<FileInfo> getFiles(FileBrowser fileBrowser,
			String path) {
		File file = new File(path);
		File[] files = file.listFiles();
		if (files == null) {
			return null;
		}
		ArrayList<FileInfo> fileList = new ArrayList<FileInfo>();
		// 获取文件列表
		for (int i = 0; i < files.length; i++) {
			File tempFile = files[i];
			FileInfo fileInfo = getFileInfo(tempFile);
			fileList.add(fileInfo);
		}
		// 排序
		Collections.sort(fileList, new FileComparator());
		return fileList;
	}

	/**
	 * 获取文件信息
	 */
	private static FileInfo getFileInfo(File tempFile) {
		FileInfo fileInfo = new FileInfo();
		fileInfo.isDirectory = tempFile.isDirectory();
		fileInfo.name = tempFile.getName();
		fileInfo.path = tempFile.getPath();
		fileInfo.lastModified = getLastModified(tempFile);
		fileInfo.type = getType(tempFile);
		calcFileContent(fileInfo, tempFile);
		return fileInfo;
	}

	/**
	 * 获得文件类型
	 */
	private static String getType(File tempFile) {
		String name = tempFile.getName();
		String type = "";
		String end = name.substring(name.lastIndexOf(".") + 1, name.length())
				.toLowerCase();
		if (end.equals("apk")) {
			return "application/vnd.android.package-archive";
		} else if (end.equals("mp4") || end.equals("avi") || end.equals("3gp")
				|| end.equals("rmvb")) {
			type = "video";
		} else if (end.equals("m4a") || end.equals("mp3") || end.equals("mid")
				|| end.equals("xmf") || end.equals("ogg") || end.equals("wav")) {
			type = "audio";
		} else if (end.equals("jpg") || end.equals("gif") || end.equals("png")
				|| end.equals("jpeg") || end.equals("bmp")) {
			type = "image";
		} else if (end.equals("txt") || end.equals("log")) {
			type = "text";
		} else {
			type = "*";
		}
		type += "/*";
		return type;
	}

	/**
	 * 计算文件内容
	 */
	private static void calcFileContent(FileInfo info, File f) {
		if (f.isFile()) {
			info.size += f.length();
		}
		if (f.isDirectory()) {
			File[] files = f.listFiles();
			if (files != null && files.length > 0) {
				for (int i = 0; i < files.length; ++i) {
					File tmp = files[i];
					if (tmp.isDirectory()) {
						info.folderCount++;
					} else if (tmp.isFile()) {
						info.fileCount++;
					}
					if (info.fileCount + info.folderCount >= 10000) { // 超过一万不计算
						break;
					}
					calcFileContent(info, tmp);
				}
			}
		}
	}

	/**
	 * 转换文件大小
	 */
	public static String formatFileSize(long size) {
		DecimalFormat df = new DecimalFormat("#.00");
		String fileSizeString = "";
		if (size < 1024) {
			fileSizeString = size + " B";
		} else if (size < 1048576) {
			fileSizeString = df.format((double) size / 1024) + " K";
		} else if (size < 1073741824) {
			fileSizeString = df.format((double) size / 1048576) + " M";
		} else {
			fileSizeString = df.format((double) size / 1073741824) + " G";
		}
		return fileSizeString;
	}

	/**
	 * 获取文件最后修改时间
	 */
	private static String getLastModified(File tempFile) {
		Calendar cal = Calendar.getInstance();
		long time = tempFile.lastModified();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日");
		cal.setTimeInMillis(time);
		return formatter.format(cal.getTime());
	}

	/**
	 * 删除文件
	 */
	public static void deleteFile(File file) {
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			if (files != null && files.length > 0) {
				for (int i = 0; i < files.length; ++i) {
					deleteFile(files[i]);
				}
			}
		}
		file.delete();
	}

	/**
	 * 合并路径
	 */
	public static String combinPath(String path, String newName) {
		return path + (path.endsWith(File.separator) ? "" : File.separator)
				+ newName;
	}

	/**
	 * 文件详细信息
	 */
	public static void fileDetail(Context context, File file) {
		View view = LayoutInflater.from(context).inflate(
				R.layout.file_detail, null);
		FileInfo info = FileUtil.getFileInfo(file);
		((TextView) view.findViewById(R.id.file_name)).setText(file.getName());
		((TextView) view.findViewById(R.id.file_type)).setText(info.type);
		((TextView) view.findViewById(R.id.file_lastmodified))
				.setText(new Date(file.lastModified()).toLocaleString());
		((TextView) view.findViewById(R.id.file_size)).setText(FileUtil
				.formatFileSize(info.size));
		if (file.isDirectory()) {
			((TextView) view.findViewById(R.id.file_contents)).setText("文件夹: "
					+ info.folderCount + ", 文件 " + info.fileCount);
		}
		new AlertDialog.Builder(context).setView(view).setTitle("详细信息")
				.setPositiveButton("确定", null).show();
	}
}