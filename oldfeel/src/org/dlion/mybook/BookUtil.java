package org.dlion.mybook;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.View;

public class BookUtil {

	/**
	 * 获取book的目录列表
	 */
	public static ArrayList<Map<String, String>> getCatalogList(String fileName) {
		ArrayList<Map<String, String>> partList = new ArrayList<Map<String, String>>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String tempStr = br.readLine();
			int pos = tempStr.getBytes().length;
			Map<String, String> firstMap = new HashMap<String, String>();
			firstMap.put("name", tempStr);
			firstMap.put("pos", "1");
			partList.add(firstMap);
			while ((tempStr = br.readLine()) != null) {
				pos += tempStr.getBytes().length + 2;
				if (tempStr.equals("(本章完)")
						&& ((tempStr = br.readLine()) != null)) {
					pos += tempStr.getBytes().length + 2;
					int curCatalogLength = tempStr.getBytes().length;
					Map<String, String> map = new HashMap<String, String>();
					map.put("name", tempStr);
					map.put("pos", String.valueOf(pos - curCatalogLength));
					partList.add(map);
				}
			}
			br.close();
			return partList;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 保存book的最后阅读位置,在onPause里使用
	 */
	public static void saveLastPagePos(SharedPreferences pre, String pos) {
		Editor ed = pre.edit();
		ed.putString("pos", pos);
		ed.commit();
	}

	/**
	 * 读取book的最后阅读位置
	 */
	public static String getLastPagePos(SharedPreferences pre) {
		String pos = pre.getString("pos", null);
		return pos;
	}

	/**
	 * 获取文件名
	 */
	public static CharSequence getFileName(File file) {
		String tmpFileName = file.getName();
		if (tmpFileName.indexOf(".") > 0) // 如果文件名包含'.',并且该'.'不在第一位,返回名字最后一个'.'以前的字符为文件名字
			tmpFileName = tmpFileName
					.substring(0, tmpFileName.lastIndexOf("."));
		return tmpFileName;
	}

	/**
	 * 获取文件类型
	 */
	public static CharSequence getFileType(File file) {
		String name = file.getName();
		String type = name.substring(name.lastIndexOf(".") + 1);
		if (file.isDirectory()) {
			return "文件夹";
		} else {
			return type;
		}
	}

	/**
	 * 判断是目录还是文件,分别返回目录里的文件数量和文件大小
	 */
	public static CharSequence getFileSize(File file) {
		if (file.isDirectory()) {
			if (file.list() != null)
				return file.list().length + "项";
			else
				return "0项";
		} else {
			return file.length() / 1024 + "K";
		}
	}

	/**
	 * 如果数据库存在该名字的书，返回true，如果不存在就返回false
	 */
	public static boolean isChecked(ArrayList<String> booklist, File file) {
		for (String string : booklist) {
			if (file.getAbsolutePath().toString().equals(string))
				return true;
		}
		return false;
	}

	/**
	 * 根据文件type判断是否可读，返回是否显示CheckBox
	 */
	public static int isVisibility(CharSequence charSequence) {
		if (charSequence.equals("txt")) {
			return View.VISIBLE;
		} else {
			return View.GONE;
		}
	}
}
