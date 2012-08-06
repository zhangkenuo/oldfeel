package org.dlion.mybook;

import org.dlion.oldfeel.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;

public class BookReadingSettingUtil {
	Context context;
	SharedPreferences sp;
	String bookName;
	Editor ed;

	public BookReadingSettingUtil(Context context) {
		this.context = context;
		sp = context.getSharedPreferences(bookName, Context.MODE_PRIVATE);
		ed = sp.edit();
	}

	public void setPageFontSize(int fontSize) {
		ed.putInt("pageFontSize", fontSize);
		ed.commit();
	}

	public int getPageFontSize() {
		int fontSize = sp.getInt("pageFontSize", 16);
		return fontSize;
	}

	public void setBgStyle(int bgStyleId) {
		ed.putInt("pageBgStyle", bgStyleId);
		ed.commit();
	}

	public int getBgStyle() {
		int bgStyle = sp.getInt("pageBgStyle", R.drawable.bg_1);
		return bgStyle;
	}

	public void setColorStyle(int colorStyleId) {
		ed.putInt("pageColorStyle", colorStyleId);
		ed.commit();
	}

	public int getColorStyle() {
		int colorStyle = sp.getInt("pageColorStyle", Color.BLACK);
		return colorStyle;
	}
}
