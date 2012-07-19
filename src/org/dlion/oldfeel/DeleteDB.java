package org.dlion.oldfeel;

import java.io.File;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

public class DeleteDB extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String dbPath = Environment.getExternalStorageDirectory()
				.getAbsolutePath().toString()
				+ "/oldfeel/database/oldfeel.db";
		File dbFile = new File(dbPath);
		if (dbFile.delete()) {
			Log.d("DeleteDB", "delete db file success");
		}
	}

}
