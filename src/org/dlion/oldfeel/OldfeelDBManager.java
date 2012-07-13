package org.dlion.oldfeel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

public class OldfeelDBManager {
	Context context;

	public OldfeelDBManager(Context context) {
		this.context = context;
	}

	/**
	 * 打开数据库
	 */
	public SQLiteDatabase openOldfeelDb() {
		String path = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/oldfeel/database";
		File dir = new File(path);
		if (!dir.exists()) {
			dir.mkdir();
		}
		String dbFilePath = path + "/oldfeel.db";
		File dbFile = new File(dbFilePath);
		if (!dbFile.exists()) {
			try {
				InputStream is = context.getResources().openRawResource(
						R.raw.oldfeel);
				FileOutputStream fos = new FileOutputStream(dbFile);
				byte[] buffer = new byte[1024];
				int count = 0;
				while ((count = is.read(buffer)) > 0) {
					fos.write(buffer, 0, count);
				}
				fos.close();
				is.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbFilePath,
				null);
		return db;
	}
}
