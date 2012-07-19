package org.dlion.footsince;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.dlion.oldfeel.R;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class FootCamera extends Activity {
	private static final String TAG = "OldfeelCamera";
	private ImageButton mVideoStartBtn;
	private SurfaceView mSurfaceview;
	private MediaRecorder mMediaRecorder;
	private SurfaceHolder mSurfaceHolder;
	private String path;
	private TextView timer;
	private int hour = 0;
	private int minute = 0;
	private int second = 0;
	private boolean bool;
	protected Camera camera;
	protected boolean isPreview;
	private boolean isRecording = true; // true表示没有录像，点击开始；false表示正在录像，点击暂停

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 全屏显示
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		setContentView(R.layout.footsince_camera);
		timer = (TextView) findViewById(R.id.oldfeel_camera_timer);
		mVideoStartBtn = (ImageButton) findViewById(R.id.oldfeel_camera_video);
		mSurfaceview = (SurfaceView) this
				.findViewById(R.id.oldfeel_camera_surfaceView);

		// 设置计时器不可见
		timer.setVisibility(View.GONE);

		// 设置路径
		String footName = getIntent().getStringExtra("footName");
		path = Environment.getExternalStorageDirectory().getAbsolutePath()
				+ "/oldfeel/footsince/" + footName;
		File dir = new File(path);
		if (!dir.exists()) {
			dir.mkdirs();
		}

		// 绑定预览视图
		SurfaceHolder holder = mSurfaceview.getHolder();
		holder.addCallback(new Callback() {

			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				if (camera != null) {
					if (isPreview) {
						camera.stopPreview();
						isPreview = false;
					}
					camera.release();
					camera = null; // 记得释放
				}
				mSurfaceview = null;
				mSurfaceHolder = null;
				mMediaRecorder = null;
			}

			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				mSurfaceHolder = holder;
				openPreview();
			}

			@Override
			public void surfaceChanged(SurfaceHolder holder, int format,
					int width, int height) {
				mSurfaceHolder = holder;
			}
		});
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		mVideoStartBtn.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isRecording) {
					// 开始录像
					letRecorder();
				} else {
					// 停止
					releaseRecorder();
					openPreview();
				}
			}
		});
		ImageButton btnImgStart = (ImageButton) findViewById(R.id.oldfeel_camera_img);
		btnImgStart.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mMediaRecorder != null) {
					releaseRecorder();
					openPreview();
				}
				if (camera != null) {
					letPhotograph();
				}
			}
		});
	}

	/**
	 * 录像
	 */
	private void letRecorder() {
		if (isPreview) {
			camera.stopPreview();
			camera.release();
			camera = null;
		}
		second = 0;
		minute = 0;
		hour = 0;
		bool = true;
		if (mMediaRecorder == null)
			mMediaRecorder = new MediaRecorder();
		else
			mMediaRecorder.reset();
		mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
		mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
		mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
		mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		mMediaRecorder.setVideoSize(320, 240);
		mMediaRecorder.setVideoFrameRate(15);
		mMediaRecorder.setOutputFile(getVideoFile().getAbsolutePath());
		try {
			mMediaRecorder.prepare();
			timer.setVisibility(View.VISIBLE);
			handler.postDelayed(task, 1000);
			mMediaRecorder.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		showMsg("开始录制");
		mVideoStartBtn
				.setImageResource(R.drawable.d_footsince_camera_video_stop);
		isRecording = !isRecording;
	}

	/**
	 * 释放录像机
	 */
	private void releaseRecorder() {
		if (mMediaRecorder != null) {
			mMediaRecorder.stop();
			mMediaRecorder.release();
			mMediaRecorder = null;
			showMsg("录制完成，已保存");
		}
		bool = false;
		timer.setText(format(hour) + ":" + format(minute) + ":"
				+ format(second));
		isRecording = !isRecording;
		mVideoStartBtn
				.setImageResource(R.drawable.d_footsince_camera_video_start);
	}

	/**
	 * 开打照相机预览
	 */
	private void openPreview() {
		Log.d(TAG, "start preview");
		try {
			camera = Camera.open();
			Camera.Parameters parameters = camera.getParameters();
			parameters.setPreviewFrameRate(5); // 每秒5帧
			parameters.setPictureFormat(PixelFormat.JPEG);// 设置照片的输出格式
			parameters.set("jpeg-quality", 85);// 照片质量
			camera.setParameters(parameters);
			camera.setPreviewDisplay(mSurfaceHolder);
			camera.startPreview();
			isPreview = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 照相
	 */
	private void letPhotograph() {
		camera.autoFocus(null);
		camera.takePicture(null, null, new PictureCallback() {
			@Override
			public void onPictureTaken(byte[] data, Camera camera) {
				Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0,
						data.length);
				Matrix matrix = new Matrix();
				// 设置缩放
				matrix.postScale(5f, 4f);
				bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
						bitmap.getHeight(), matrix, true);

				String imgPath = path + "/img/";
				String fileName = new SimpleDateFormat("MM月dd日HH时mm分ss秒")
						.format(new Date()) + ".jpg";
				File out = new File(imgPath);
				if (!out.exists()) {
					out.mkdirs();
				}
				out = new File(imgPath, fileName);
				try {
					FileOutputStream outStream = new FileOutputStream(out);
					bitmap.compress(CompressFormat.JPEG, 100, outStream);
					outStream.close();
					camera.startPreview();
				} catch (Exception e) {
					e.printStackTrace();
				}
				showMsg("拍照成功");
			}
		});
	}

	/**
	 * 生成video文件名字
	 */
	protected File getVideoFile() {
		String videoPath = path + "/video/";
		String fileName = new SimpleDateFormat("MM月dd日HH时mm分ss秒")
				.format(new Date()) + ".3gp";
		File out = new File(videoPath);
		if (!out.exists()) {
			out.mkdirs();
		}
		out = new File(videoPath, fileName);
		return out;
	}

	/**
	 * 定时器设置，实现计时
	 */
	private Handler handler = new Handler();
	private Runnable task = new Runnable() {
		public void run() {
			if (bool) {
				handler.postDelayed(this, 1000);
				second++;
				if (second >= 60) {
					minute++;
					second = second % 60;
				}
				if (minute >= 60) {
					hour++;
					minute = minute % 60;
				}
				timer.setText(format(hour) + ":" + format(minute) + ":"
						+ format(second));
			}
		}
	};

	/**
	 * 格式化时间
	 */
	public String format(int i) {
		String s = i + "";
		if (s.length() == 1) {
			s = "0" + s;
		}
		return s;
	}

	/**
	 * 消息提示
	 */
	private Toast toast;

	public void showMsg(String arg) {
		if (toast == null) {
			toast = Toast.makeText(this, arg, Toast.LENGTH_SHORT);
		} else {
			toast.cancel();
			toast.setText(arg);
		}
		toast.show();
	}

	@Override
	protected void onPause() {
		super.onPause();
		releaseRecorder();
	}
}