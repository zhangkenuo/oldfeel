package org.dlion.mybook;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.Vector;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.util.Log;

public class CurlFactory {

	private File bookFile = null;
	private MappedByteBuffer mappedByteBuffer = null;
	private int bookLength = 0;
	private int beginPosition = 0;
	private int endPosition = 0;
	private String charsetName = "UTF-8";
	private Bitmap background = null;
	private int screenWidth;
	private int screenHeight;

	private Vector<String> pageLines = new Vector<String>();

	private int fontSize = 16;
	private int textColor = Color.BLACK;
	private int backColor = 0xffff9e85; // 背景颜色
	private int marginWidth = 15; // 左右与边缘的距离
	private int marginHeight = 20; // 上下与边缘的距离

	private int lineEachPage; // 每页可以显示的行数
	private float canvasHeight; // 画布的高
	private float canvasWidth; // 画布的宽
	private boolean isFirstPage, isLastPage;

	private Paint paint;

	// 进度格式
	DecimalFormat decimalFormat = new DecimalFormat("##.##");

	public CurlFactory(int screenWidth, int screenHeight, int fontSize) {
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setTextAlign(Align.LEFT);
		paint.setTextSize(fontSize);
		paint.setColor(textColor);
		canvasWidth = screenWidth - marginWidth * 2;
		canvasHeight = screenHeight - marginHeight * 2;
		lineEachPage = (int) (canvasHeight / fontSize); // 每一页的行数
		this.fontSize = fontSize;
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
	}

	@SuppressWarnings("resource")
	public void openBook(String filePath) throws IOException {
		bookFile = new File(filePath);
		bookLength = (int) bookFile.length();
		mappedByteBuffer = new RandomAccessFile(bookFile, "r").getChannel()
				.map(FileChannel.MapMode.READ_ONLY, 0, bookFile.length());
	}

	// 读取上一段落
	protected byte[] readParagraphBack(int currentPosition) {
		int fontPosition = currentPosition - 1;
		byte b0;
		while (fontPosition > 0) {
			b0 = mappedByteBuffer.get(fontPosition);
			if (b0 == 0x0a && fontPosition != currentPosition - 1) {
				fontPosition++;
				break;
			}
			fontPosition--;
		}
		if (fontPosition < 0)
			fontPosition = 0;
		int nParaSize = currentPosition - fontPosition;
		int j;
		byte[] buf = new byte[nParaSize];
		for (j = 0; j < nParaSize; j++) {
			buf[j] = mappedByteBuffer.get(fontPosition + j);
		}
		return buf;
	}

	// 读取下一段落
	protected byte[] readParagraphForward(int currentPosition) {
		int nStart = currentPosition;
		int i = nStart;
		byte b0;
		while (i < bookLength) {
			b0 = mappedByteBuffer.get(i++);
			if (b0 == 0x0a) {
				break;
			}
		}
		int nParaSize = i - nStart;
		byte[] buf = new byte[nParaSize];
		for (i = 0; i < nParaSize; i++) {
			buf[i] = mappedByteBuffer.get(currentPosition + i);
		}
		return buf;
	}

	protected Vector<String> pageDown() {
		String strParagraph = "";
		Vector<String> lines = new Vector<String>();
		while (lines.size() < lineEachPage && endPosition < bookLength) {
			byte[] paraBuf = readParagraphForward(endPosition); // 读取一个段落
			endPosition += paraBuf.length;
			try {
				strParagraph = new String(paraBuf, charsetName);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			String strReturn = "";
			if (strParagraph.indexOf("\r\n") != -1) {
				strReturn = "\r\n";
				strParagraph = strParagraph.replaceAll("\r\n", "");
			} else if (strParagraph.indexOf("\n") != -1) {
				strReturn = "\n";
				strParagraph = strParagraph.replaceAll("\n", "");
			}

			if (strParagraph.length() == 0) {
				lines.add(strParagraph);
			}
			while (strParagraph.length() > 0) {
				int nSize = paint.breakText(strParagraph, true, canvasWidth,
						null);
				lines.add(strParagraph.substring(0, nSize));
				strParagraph = strParagraph.substring(nSize);
				if (lines.size() >= lineEachPage) {
					break;
				}
			}
			if (strParagraph.length() != 0) {
				try {
					endPosition -= (strParagraph + strReturn)
							.getBytes(charsetName).length;
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		}
		return lines;
	}

	public Vector<String> pageUp() {
		if (beginPosition < 0) {
			beginPosition = 0;
		}
		Vector<String> lines = new Vector<String>();
		String strParagraph = "";
		while (lines.size() < lineEachPage && beginPosition > 0) {
			Vector<String> paraLines = new Vector<String>();
			byte[] paraBuf = readParagraphBack(beginPosition);
			beginPosition -= paraBuf.length;
			try {
				strParagraph = new String(paraBuf, charsetName);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			strParagraph = strParagraph.replaceAll("\r\n", "");
			strParagraph = strParagraph.replaceAll("\n", "");

			if (strParagraph.length() == 0) {
				paraLines.add(strParagraph);
			}
			while (strParagraph.length() > 0) {
				int nSize = paint.breakText(strParagraph, true, canvasWidth,
						null);
				paraLines.add(strParagraph.substring(0, nSize));
				strParagraph = strParagraph.substring(nSize);
			}
			lines.addAll(0, paraLines);
		}
		while (lines.size() > lineEachPage) {
			try {
				beginPosition += lines.get(0).getBytes(charsetName).length;
				lines.remove(0);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		endPosition = beginPosition;

		return lines;
	}

	public void prePage() throws IOException {
		if (beginPosition <= 0) {
			beginPosition = 0;
			isFirstPage = true;
			return;
		} else {
			isFirstPage = false;
		}
		pageLines.clear();
		pageUp();
		pageLines = pageDown();
	}

	public void nextPage() throws IOException {
		if (endPosition >= bookLength) {
			isLastPage = true;
			return;
		} else {
			isLastPage = false;
		}
		pageLines.clear();
		beginPosition = endPosition;
		pageLines = pageDown();
	}

	public void onDraw(Canvas c) {
		if (pageLines.size() == 0) {
			pageLines = pageDown();
		}
		if (pageLines.size() > 0) {
			if (background == null)
				c.drawColor(backColor);
			else
				c.drawBitmap(background, 0, 0, null);
			int y = marginHeight;
			for (String strLine : pageLines) {
				y += fontSize;
				c.drawText(strLine, marginWidth, y, paint);
			}
		}
		float fPercent = (float) (beginPosition * 1.0 / bookLength);
		String strPercent = decimalFormat.format(fPercent * 100) + "%";
		int nPercentWidth = (int) paint.measureText("999.9%") + 1;
		c.drawText(strPercent, screenWidth - nPercentWidth, screenHeight - 5,
				paint);
	}

	public void setBgBitmap(Bitmap BG) {
		background = BG;
	}

	public boolean isFirstPage() {
		return isFirstPage;
	}

	public boolean isLastPage() {
		return isLastPage;
	}

	public int getCurrentPostion() {
		return beginPosition;
	}

	public void setBeginPos(int pos) {
		endPosition = pos;
		beginPosition = pos;
	}

	public int getBufLen() {
		return bookLength;
	}

	public String getOneLine() {
		return pageLines.toString().substring(0, 10);
	}

	public void changBackGround(int color) {
		paint.setColor(color);
	}

	public void setFontSize(int size) {
		fontSize = size;
		paint.setTextSize(size);
	}

	public int getFontSize() {
		return fontSize;
	}

	public int getTextColor() {
		return textColor;
	}

	public void setTextColor(int m_textColor) {
		this.textColor = m_textColor;
		paint.setColor(m_textColor);
	}

	public void showLog(String string) {
		Log.d("MyPageFactory", string);
	}

	public void showLog(int i) {
		Log.d("MyPageFactory", String.valueOf(i));
	}

}
