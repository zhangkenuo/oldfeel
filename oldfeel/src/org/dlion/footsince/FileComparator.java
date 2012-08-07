package org.dlion.footsince;

import java.util.Comparator;

/**
 * 排序
 */
public class FileComparator implements Comparator<FileInfo> {

	@Override
	public int compare(FileInfo lhs, FileInfo rhs) {
		// 文件夹排在前面
		if (lhs.isDirectory && !rhs.isDirectory) {
			return -1000;
		} else if (!lhs.isDirectory && rhs.isDirectory) {
			return 1000;
		}
		// 相同类型按名称排序
		return lhs.name.compareTo(rhs.name);
	}
}
