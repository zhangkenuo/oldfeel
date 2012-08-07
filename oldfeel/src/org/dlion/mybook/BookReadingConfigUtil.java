package org.dlion.mybook;

import org.dlion.oldfeel.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;

public class BookReadingConfigUtil {
	Context context;
	SharedPreferences sp;
	Editor ed;

	public BookReadingConfigUtil(Context context, String bookName) {
		this.context = context;
		sp = context.getSharedPreferences(bookName, Context.MODE_PRIVATE);
		ed = sp.edit();
	}

	public void setCurrentPosition(int currentPosition) {
		ed.putInt("pageCurrentPosition", currentPosition);
		ed.commit();
	}

	public int getCurrentPosition() {
		int currentPosition = sp.getInt("pageCurrentPosition", 0);
		return currentPosition;
	}

	public void setPageFontSize(int fontSize) {
		ed.putInt("pageFontSize", fontSize);
		ed.commit();
	}

	public int getFontSize() {
		int fontSize = sp.getInt("pageFontSize", 16);
		return fontSize;
	}

	public void setBgStyle(int bgStyleId) {
		ed.putInt("pageBgStyle", bgStyleId);
		ed.commit();
	}

	public int getBackground() {
		int bgStyle = sp.getInt("pageBgStyle", R.drawable.bg_1);
		return bgStyle;
	}

	public void setColorStyle(int colorStyleId) {
		ed.putInt("pageColorStyle", colorStyleId);
		ed.commit();
	}

	public int getTextColor() {
		int colorStyle = sp.getInt("pageColorStyle", Color.BLACK);
		return colorStyle;
	}
}
