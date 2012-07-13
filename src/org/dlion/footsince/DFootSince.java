package org.dlion.footsince;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.dlion.oldfeel.OldfeelDBManager;
import org.dlion.oldfeel.R;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.LocationListener;
import com.baidu.mapapi.MapActivity;
import com.baidu.mapapi.MapView;
import com.baidu.mapapi.MyLocationOverlay;

public class DFootSince extends MapActivity {
	protected static final String TABLE_NAME = "foot_since";
	protected static final String path = Environment
			.getExternalStorageDirectory().getAbsolutePath()
			+ "/oldfeel/footsince";
	private static final String TAG = "DFootSince";
	OldfeelDBManager dDBManager;
	SQLiteDatabase db;

	MapView dMap;
	private BMapManager mBMapMan;
	private MyLocationOverlay mLocationOverlay;

	LocationClient mLocationClient;
	BDLocationListener myListener = new BDLocationListener() {
		@Override
		public void onReceivePoi(BDLocation location) {
			if (location == null)
				return;
			nowTime = location.getTime().toString();
			showLog(String.valueOf(nowTime));
			if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
				footName = location.getAddrStr().toString();
				etFootName.setText(footName);
			}
		}

		@Override
		public void onReceiveLocation(BDLocation poiLocation) {
			if (poiLocation == null)
				return;
			nowTime = poiLocation.getTime().toString();
			if (poiLocation.getLocType() == BDLocation.TypeNetWorkLocation) {
				footName = poiLocation.getAddrStr().toString();
				etFootName.setText(footName);
			}
		}
	};

	ImageButton btnCamera, btnBrowseImg, btnBrowseVideo;
	EditText etFootName;
	/**
	 * 是否选择足迹，true为已经选择，false为没有选择
	 */
	protected boolean isSelectedFootSince = false;
	private String footName = "";
	protected String nowTime;
	protected int lat;
	protected int lon;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.d_footsince);
		dDBManager = new OldfeelDBManager(this);
		db = dDBManager.openOldfeelDb();

		mLocationClient = new LocationClient(this); // 百度定位
		mLocationClient.registerLocationListener(myListener);

		mBMapMan = new BMapManager(this);
		// 自己申请的百度地图移动版API Key
		String myKey = "9592A8FAF4727B1B21708648D55AD11A6B2339C6";
		// 初始化地图
		mBMapMan.init(myKey, null);
		// 初始化Activity
		super.initMapActivity(mBMapMan);
		dMap = (MapView) findViewById(R.id.bmapView);
		// 设置启用内置的缩放软件
		dMap.setBuiltInZoomControls(true);
		// 设置在缩放动画过程中也显示overlay，默认为不绘制。
		dMap.setDrawOverlayWhenZooming(true);

		// 添加定位图层
		mLocationOverlay = new MyLocationOverlay(this, dMap);
		dMap.getOverlays().add(mLocationOverlay);

		initEtView();
	}

	/**
	 * 显示Log
	 */
	protected void showLog(String log) {
		Log.d("DFootSince", log);
	}

	/**
	 * 初始化编辑框
	 */
	private void initEtView() {
		btnCamera = (ImageButton) findViewById(R.id.d_footsince_btn_camera);
		btnBrowseImg = (ImageButton) findViewById(R.id.d_footsince_btn_browseImg);
		btnBrowseVideo = (ImageButton) findViewById(R.id.d_footsince_btn_browseVideo);
		etFootName = (EditText) findViewById(R.id.d_footsince_et_footName);
		btnCamera.setOnClickListener(btnListener);
		btnBrowseImg.setOnClickListener(btnListener);
		btnBrowseVideo.setOnClickListener(btnListener);
		etFootName.setOnClickListener(btnListener);
		etFootName.setFocusable(false);
	}

	/**
	 * 注册定位监听
	 */
	private LocationListener mLocationListener = new LocationListener() {

		@Override
		public void onLocationChanged(Location location) {
			if (location != null) {
				lat = (int) (location.getLatitude() * 1e6); // 获取当前纬度
				lon = (int) (location.getLongitude() * 1e6); // 获取当前经度
				moveToGeoPoint(lat, lon);
			}
		}
	};
	/**
	 * 注册按键监听
	 */
	private OnClickListener btnListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.d_footsince_et_footName:
				holdFootName();
				break;
			case R.id.d_footsince_btn_camera:
				letCamera();
				break;
			case R.id.d_footsince_btn_browseImg:
				browseFootFile("img");
				break;
			case R.id.d_footsince_btn_browseVideo:
				browseFootFile("video");
				break;
			default:
				break;
			}
		}
	};

	/**
	 * 获取当前日期
	 */
	protected String getNowTime() {
		String nowTime = new SimpleDateFormat("yyyy年MM月dd日").format(new Date());
		return nowTime;
	}

	/**
	 * hold住足迹命名
	 */
	protected void holdFootName() {
		setLocationOption();
		mLocationClient.start();
		if (mLocationClient != null && mLocationClient.isStarted())
			mLocationClient.requestLocation();
		mLocationClient.requestPoi();
		Log.d(TAG, "hold the etFootName");
		Intent intent = new Intent();
		intent.putExtra("footName", footName);
		intent.setClass(DFootSince.this, DFootName.class);
		startActivityForResult(intent, 1);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case 1:
			if (resultCode == 1) {
				isSelectedFootSince = true;
				footName = data.getStringExtra("footName");
				etFootName.setText(footName);
				lat = data.getIntExtra("lat", lat);
				lon = data.getIntExtra("lon", lon);
				syncDataToDb(footName);
				moveToGeoPoint(lat, lon);
			}
			break;
		default:
			break;
		}
	}

	/**
	 * 移动到指定坐标点
	 */
	void moveToGeoPoint(int lat, int lon) {
		GeoPoint pt = new GeoPoint(lat, lon);
		dMap.getController().animateTo(pt);
	}

	/**
	 * 浏览
	 */
	private void browseFootFile(String type) {
		String footFilePath = "";
		if (footName.length() < 1) {
			footFilePath = path;
		} else {
			footFilePath = path + "/" + footName + "/" + type;
		}
		Intent intent = new Intent();
		intent.setClass(DFootSince.this, DFootFile.class);
		intent.putExtra("path", footFilePath);
		startActivity(intent);
	}

	/**
	 * 开始拍照/录像
	 */
	protected void letCamera() {
		if (isSelectedFootSince) {
			Intent intent = new Intent();
			intent.setClass(getApplicationContext(), DFootCamera.class);
			intent.putExtra("footName", footName);
			startActivity(intent);
		} else {
			showMsg("你还没有命名足迹，请命名后继续!");
		}
	}

	/**
	 * 同步数据到数据库
	 */
	protected void syncDataToDb(String footName) {
		db = dDBManager.openOldfeelDb();
		Cursor c = db.query("foot_since", null,
				"footName = '" + footName + "'", null, null, null, null);
		if (c.moveToFirst()) {
			Log.d("syncDataToDb", "footName 已经存在");
		} else {
			Log.d("syncDataToDb", "插入新的足迹");
			ContentValues values = new ContentValues();
			values.put("latitude", lat);
			values.put("longitude", lon);
			values.put("footName", footName);
			values.put("date", getNowTime());
			db.insert("foot_since", "_id", values);
		}
	}

	@Override
	protected void onResume() {
		if (!db.isOpen()) {
			db = dDBManager.openOldfeelDb();
		}
		mBMapMan.getLocationManager().requestLocationUpdates(mLocationListener);
		mLocationOverlay.enableMyLocation();
		mBMapMan.start();
		super.onResume();
	}

	@Override
	protected void onPause() {
		if (db.isOpen()) {
			db.close();
		}
		mBMapMan.getLocationManager().removeUpdates(mLocationListener);
		mLocationOverlay.disableMyLocation();
		mBMapMan.stop();
		super.onPause();
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	/**
	 * 设置百度定位的相关参数
	 */
	private void setLocationOption() {
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true); // 打开gps
		option.setCoorType("gcj02"); // 设置坐标类型
		option.setScanSpan(5000); // 设置定位模式，小于1秒则一次定位;大于等于1秒则定时定位
		mLocationClient.setLocOption(option);
	}

	public Toast toast;

	/**
	 * 消息提示
	 */
	public void showMsg(String arg) {
		if (toast == null) {
			toast = Toast.makeText(this, arg, Toast.LENGTH_SHORT);
		} else {
			toast.cancel();
			toast.setText(arg);
		}
		toast.show();
	}
}