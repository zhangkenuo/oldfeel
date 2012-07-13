package org.dlion.oldfeel;

import java.io.File;
import java.util.ArrayList;

public class OldfeelFileHelper {
	/**
	 * 获取一个文件夹下的所有文件
	 */
	public static ArrayList<OldfeelFileInfo> getFiles(OldfeelFile oldfeelFile, String path) {
		File file = new File(path);
		File[] files = file.listFiles();
		if (files == null) {
		}
		return null;
	}
}
