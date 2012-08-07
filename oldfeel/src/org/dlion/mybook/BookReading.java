package org.dlion.mybook;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.dlion.oldfeel.R;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

public class BookReading extends Activity {

	public final static int OPENMARK = 0;
	public final static int SAVEMARK = 1;
	public final static int TEXTSET = 2;
	private static final int STYLE_BG = 0;
	private static final int STYLE_COLOR = 1;

	private CurlFactory mPageFactory;
	Bitmap mCurPageBitmap, mNextPageBitmap;
	Canvas mCurPageCanvas, mNextPageCanvas;
	int screenWidth;
	int screenHeight;
	private String[] catalogPosition;
	private String bookPath;
	private File bookFile;
	private String bookName; // 书名
	private String pagePosition; // 打开位置(单位:byte[])
	private BookReadingConfigUtil settingUtil; // 储存设置类
	int[] bgIds = new int[] { R.drawable.bg_1, R.drawable.bg_2,
			R.drawable.bg_3, R.drawable.bg_4, R.drawable.bg_5 };
	int[] colorIds = new int[] { Color.BLACK, Color.YELLOW, Color.RED,
			Color.WHITE };
	int[] colorImgIds = new int[] { R.drawable.color_black,
			R.drawable.color_yellow, R.drawable.color_red,
			R.drawable.color_white };
	private int bookLength;
	private int currentProgress;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		bookPath = getIntent().getStringExtra("pathes");
		bookFile = new File(bookPath);
		bookName = bookPath.substring(bookPath.lastIndexOf('/') + 1,
				bookPath.length());
		settingUtil = new BookReadingConfigUtil(this, bookName);
		initBookPageFactory();
	}

	private void initBookPageFactory() {
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		screenHeight = dm.heightPixels;
		screenWidth = dm.widthPixels;
		mCurPageBitmap = Bitmap.createBitmap(screenWidth, screenHeight,
				Bitmap.Config.ARGB_8888);
		mNextPageBitmap = Bitmap.createBitmap(screenWidth, screenHeight,
				Bitmap.Config.ARGB_8888);
		mCurPageCanvas = new Canvas(mCurPageBitmap);
		mNextPageCanvas = new Canvas(mNextPageBitmap);
		mPageFactory = new CurlFactory(screenWidth, screenHeight,
				settingUtil.getFontSize());
		mPageFactory.setTextColor(settingUtil.getTextColor());
		mPageFactory.setBgBitmap(BitmapFactory.decodeResource(getResources(),
				settingUtil.getBackground()));
		if ((pagePosition = getIntent().getStringExtra("pos")) == null) {
			pagePosition = String.valueOf(settingUtil.getCurrentPosition());
		}
		try {
			mPageFactory.openBook(bookPath);
			mPageFactory.setBeginPos(Integer.valueOf(pagePosition));
			mPageFactory.onDraw(mCurPageCanvas);
		} catch (Exception e) {
			showLog(bookPath);
			showToast(bookName + "不存在 ");
		}
		initBookPageWidget();
	}

	private void initBookPageWidget() {
		final CurlWidget mPageWidget = new CurlWidget(this);
		mPageWidget.setWidth(screenWidth);
		mPageWidget.setHeight(screenHeight);
		mPageWidget.setBitmaps(mCurPageBitmap, mNextPageBitmap);
		mPageWidget.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent e) {
				boolean ret = false;
				if (v == mPageWidget) {
					if (e.getAction() == MotionEvent.ACTION_DOWN) {
						mPageWidget.abortAnimation();
						mPageWidget.calcCornerXY(e.getX(), e.getY());
						mPageFactory.onDraw(mCurPageCanvas);
						if (mPageWidget.DragToRight()) {
							try {
								mPageFactory.prePage();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
							if (mPageFactory.isFirstPage()) {
								showToast("这已经是第一页了");
								return false;
							}
							mPageFactory.onDraw(mNextPageCanvas);
						} else {
							try {
								mPageFactory.nextPage();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
							if (mPageFactory.isLastPage()) {
								showToast("这是最后一页");
								return false;
							}
							mPageFactory.onDraw(mNextPageCanvas);
						}
						mPageWidget.setBitmaps(mCurPageBitmap, mNextPageBitmap);
					}
					ret = mPageWidget.doTouchEvent(e);

					return ret;
				}
				return false;
			}
		});
		setContentView(mPageWidget);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.reading_option_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.reading_option_menu_catalog:
			settingCatalog();
			break;
		case R.id.reading_option_menu_more:
			settingMore();
			break;
		case R.id.reading_option_menu_progress:
			settingProgress();
			break;
		case R.id.reading_option_menu_style:
			settingBackground();
			break;
		case R.id.reading_option_menu_fontsize:
			settingFontSize();
			break;
		case R.id.reading_option_menu_textcolor:
			settingTextColor();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * 设置字体大小
	 */
	private void settingFontSize() {
		View view = getLayoutInflater().inflate(
				R.layout.reading_setting_progress, null);
		final SeekBar fontSizeSB = (SeekBar) view
				.findViewById(R.id.book_reading_setting_fontsize_seekbar);
		final Button fontSizeAdd = (Button) view
				.findViewById(R.id.book_reading_setting_fontsize_add);
		final Button fontSizeDecrease = (Button) view
				.findViewById(R.id.book_reading_setting_fontsize_decrease);
		fontSizeSB.setProgress(mPageFactory.getFontSize() - 10);
		fontSizeAdd.setText((fontSizeSB.getProgress() + 10) + " +");
		fontSizeDecrease.setText((fontSizeSB.getProgress() + 10) + " -");
		fontSizeSB.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				settingUtil.setPageFontSize(fontSizeSB.getProgress() + 10);
				initBookPageFactory();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				fontSizeAdd.setText((progress + 10) + " +");
				fontSizeDecrease.setText((progress + 10) + " -");
			}
		});
		OnClickListener btnListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				int i = fontSizeSB.getProgress();
				switch (v.getId()) {
				case R.id.book_reading_setting_fontsize_add:
					i++;
					break;
				case R.id.book_reading_setting_fontsize_decrease:
					i--;
					break;
				default:
					break;
				}
				fontSizeSB.setProgress(i);
				settingUtil.setPageFontSize(fontSizeSB.getProgress() + 10);
				initBookPageFactory();
			}
		};
		fontSizeAdd.setOnClickListener(btnListener);
		fontSizeDecrease.setOnClickListener(btnListener);
		Dialog dialog = new Dialog(this, R.style.dialog_noTitile);
		Window window = dialog.getWindow();
		LayoutParams params = new LayoutParams();
		params.gravity = Gravity.BOTTOM;
		window.setAttributes(params);
		dialog.setCanceledOnTouchOutside(true);
		dialog.setContentView(view);
		dialog.show();
	}

	/**
	 * 进度跳转
	 */
	private void settingProgress() {
		currentProgress = getCurrentProgress();
		View view = getLayoutInflater().inflate(
				R.layout.reading_setting_progress, null);
		final SeekBar currentProgressSB = (SeekBar) view
				.findViewById(R.id.book_reading_setting_fontsize_seekbar);
		final Button progressAdd = (Button) view
				.findViewById(R.id.book_reading_setting_fontsize_add);
		final Button progressDecrease = (Button) view
				.findViewById(R.id.book_reading_setting_fontsize_decrease);
		currentProgressSB.setProgress(currentProgress);
		progressAdd.setText(currentProgress * 2 + "% +");
		progressDecrease.setText(currentProgress * 2 + "% -");
		currentProgressSB
				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						settingUtil.setCurrentPosition(bookLength
								* seekBar.getProgress() / 50);
						initBookPageFactory();
					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
					}

					@Override
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						progressAdd.setText(progress * 2 + "% +");
						progressDecrease.setText(progress * 2 + "% -");
					}
				});
		OnClickListener btnListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				switch (v.getId()) {
				case R.id.book_reading_setting_fontsize_add:
					currentProgress++;
					break;
				case R.id.book_reading_setting_fontsize_decrease:
					currentProgress--;
					break;
				default:
					break;
				}
				currentProgressSB.setProgress(currentProgress);
				settingUtil.setCurrentPosition(currentProgress * bookLength
						/ 50);
				initBookPageFactory();
			}
		};
		progressAdd.setOnClickListener(btnListener);
		progressDecrease.setOnClickListener(btnListener);
		Dialog dialog = new Dialog(this, R.style.dialog_noTitile);
		Window window = dialog.getWindow();
		LayoutParams params = new LayoutParams();
		params.gravity = Gravity.BOTTOM;
		window.setAttributes(params);
		dialog.setCanceledOnTouchOutside(true);
		dialog.setContentView(view);
		dialog.show();
	}

	/**
	 * 设置阅读风格
	 */
	private void settingBackground() {
		View view = getLayoutInflater().inflate(
				R.layout.reading_setting_bgstyle, null);
		Gallery gallery = (Gallery) view
				.findViewById(R.id.reading_setting_style_background_list);
		gallery.setAdapter(new styleAdapter(bgIds, STYLE_BG));
		gallery.setSelection(getCurrentBackground());
		gallery.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				settingUtil.setBgStyle(bgIds[arg2]);
				initBookPageFactory();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		Dialog dialog = new Dialog(this, R.style.dialog_noTitile);
		Window window = dialog.getWindow();
		LayoutParams params = new LayoutParams();
		params.gravity = Gravity.BOTTOM;
		window.setAttributes(params);
		dialog.setCanceledOnTouchOutside(true);
		dialog.setContentView(view);
		dialog.show();
	}

	/**
	 * 设置文本字体颜色
	 */
	private void settingTextColor() {
		View view = getLayoutInflater().inflate(
				R.layout.reading_setting_colorstyle, null);
		Gallery gallery = (Gallery) view
				.findViewById(R.id.reading_setting_style_textcolor_list);
		gallery.setAdapter(new styleAdapter(colorImgIds, STYLE_COLOR));
		gallery.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				settingUtil.setColorStyle(colorIds[arg2]);
				initBookPageFactory();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		gallery.setSelection(getCurrentTextColor());
		Dialog dialog = new Dialog(this, R.style.dialog_noTitile);
		Window window = dialog.getWindow();
		LayoutParams params = new LayoutParams();
		params.gravity = Gravity.BOTTOM;
		window.setAttributes(params);
		dialog.setCanceledOnTouchOutside(true);
		dialog.setContentView(view);
		dialog.show();
	}

	/**
	 * 获取当前字体颜色
	 */
	private int getCurrentTextColor() {
		int id = settingUtil.getTextColor();
		for (int j = 0; j < colorIds.length; j++) {
			if (id == colorIds[j]) {
				return j;
			}
		}
		return 0;
	}

	/**
	 * 获取当前背景
	 */
	private int getCurrentBackground() {
		int id = settingUtil.getBackground();
		for (int j = 0; j < bgIds.length; j++) {
			if (id == bgIds[j]) {
				return j;
			}
		}
		return 0;
	}

	/**
	 * 更多
	 */
	private void settingMore() {
		showToast("暂无更多");
	}

	// 显示目录
	private void settingCatalog() {
		ArrayList<Map<String, String>> catalogList = BookUtil
				.getCatalogList(bookPath);
		String[] catalogNames = new String[catalogList.size()];
		catalogPosition = new String[catalogList.size()];
		// 如果book只有一条目录,说明暂无章节
		if (catalogList.size() == 1) {
			catalogNames[0] = "本书暂无章节";
			catalogPosition[0] = "1";
		} else {
			for (int i = 0; i < catalogList.size(); i++) {
				catalogNames[i] = catalogList.get(i).get("name");
				catalogPosition[i] = catalogList.get(i).get("pos");
			}
		}
		final Dialog dialog = new Dialog(this, R.style.dialog_noTitile);
		dialog.setCanceledOnTouchOutside(true);
		ListView listView = new ListView(this);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, android.R.id.text1,
				catalogNames);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Intent intent = new Intent(BookReading.this, BookReading.class);
				intent.putExtra("pos", catalogPosition[arg2]);
				intent.putExtra("pathes", bookPath);
				startActivity(intent);
				dialog.cancel();
				finish();
			}
		});
		dialog.setContentView(listView);
		dialog.show();
	}

	class styleAdapter extends BaseAdapter {
		int[] ids;
		int what;

		public styleAdapter(int[] ids, int what) {
			this.ids = ids;
			this.what = what;
		}

		@Override
		public int getCount() {
			return ids.length;
		}

		@Override
		public Object getItem(int position) {
			return ids[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView imageView = new ImageView(BookReading.this);
			imageView.setScaleType(ImageView.ScaleType.FIT_XY);
			imageView.setLayoutParams(new Gallery.LayoutParams(80, 80));
			switch (what) {
			case STYLE_BG:
				final Bitmap bgBitmap = BitmapFactory.decodeResource(
						getResources(), ids[position]);
				Bitmap newBgBitmap = ThumbnailUtils.extractThumbnail(bgBitmap,
						80, 80);
				imageView.setImageBitmap(newBgBitmap);
				return imageView;
			case STYLE_COLOR:
				final Bitmap colorBitmap = BitmapFactory.decodeResource(
						getResources(), ids[position]);
				Bitmap newColorBitmap = ThumbnailUtils.extractThumbnail(
						colorBitmap, 80, 80);
				imageView.setImageBitmap(newColorBitmap);
				return imageView;
			default:
				return null;
			}
		}
	}

	/**
	 * 获取当前进度
	 */
	private int getCurrentProgress() {
		bookLength = (int) bookFile.length();
		int currentPosition = mPageFactory.getCurrentPostion();
		if (currentPosition <= 0 || currentPosition >= bookLength) {
			currentPosition = Integer.valueOf(settingUtil.getCurrentPosition());
		}
		float currentProgress = (float) (currentPosition * 1.0 / bookLength);
		return (int) (currentProgress * 50);
	}

	private Toast toast;

	private void showToast(String string) {
		if (toast == null) {
			toast = Toast.makeText(this, string, Toast.LENGTH_LONG);
		} else {
			toast.cancel();
			toast = Toast.makeText(this, string, Toast.LENGTH_LONG);
		}
		toast.show();
	}

	protected void showLog(String arg) {
		Log.d("YiWebBook", arg);
	}

	protected void showLog(int arg) {
		Log.d("YiWebBook", String.valueOf(arg));
	}

	@Override
	protected void onPause() {
		settingUtil.setCurrentPosition(mPageFactory.getCurrentPostion());
		super.onPause();
	}
}