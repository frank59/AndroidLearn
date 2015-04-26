package com.geewaza.android.test001;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnInfoListener;
import android.os.Bundle;
import android.os.Environment;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

public class RecordVideoActivity extends Activity implements OnClickListener {
	
	
	private static int MAX_DURATING = 2900;
	
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);
	
	
	Button startButton;
	Button compressButton;
	SurfaceView preView;
	MediaRecorder mRecorder;
	Camera mCamera;
	ProgressBar progressBar;
	ProgressTimer progressTimer;
	
	String recordDirPath;
	String outputDirPath;
	
	private boolean isRecording = false;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_recoder);
		
		startButton = (Button) findViewById(R.id.recorder_button_start);
		compressButton = (Button) findViewById(R.id.recorder_button_compress);
		preView = (SurfaceView) findViewById(R.id.preview);
		progressBar = (ProgressBar) findViewById(R.id.recorder_progressbar);
		progressBar.setMax(MAX_DURATING);
		
		try {
			recordDirPath = Environment.getExternalStorageDirectory().getCanonicalFile() + CONSTANTS.RECORD_FILE_PATH;
			File recordDir = new File(recordDirPath);
			if (!recordDir.exists() || !recordDir.isDirectory()) {
				recordDir.mkdirs();
			}
			outputDirPath = Environment.getExternalStorageDirectory().getCanonicalFile() + CONSTANTS.OUT_PUT_FILE_PATH;
			File outputDir = new File(outputDirPath);
			if (!outputDir.exists() || !outputDir.isDirectory()) {
				outputDir.mkdirs();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		startButton.setOnClickListener(this);
		compressButton.setOnClickListener(this);
		preView.getHolder().addCallback(new Callback() {
			
			@Override
			public void surfaceDestroyed(SurfaceHolder arg0) {
				releaseCamera();
			}
			
			@Override
			public void surfaceCreated(SurfaceHolder arg0) {
				initpreview();
			}
			
			@Override
			public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
				
			}
		});
		startButton.setEnabled(true);
		preView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		preView.getHolder().setFixedSize(800, 480);
		preView.getHolder().setKeepScreenOn(true);
		mRecorder = new MediaRecorder();
		progressTimer = new ProgressTimer(MAX_DURATING, progressBar);
		
		
	}
	
	private void resetProgressbar() {
		progressBar.setProgress(0);
	}

	@Override
	public void onClick(View source) {
		switch (source.getId()) {
		case R.id.recorder_button_start:
			try {
				if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
					Toast.makeText(RecordVideoActivity.this, "SD卡不存在，请插入SD卡！", Toast.LENGTH_LONG).show();
					return;
				}
//				prepareRecorder();
//				mRecorder.start();
				
				startmediaRecorder();
				new Thread(progressTimer).start();
				System.out.println("----Start Recording----");
				startButton.setEnabled(false);
				isRecording = true;
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case R.id.recorder_button_compress:
			try {
				compressButton.setEnabled(false);
				compressAllVideos();
				compressButton.setEnabled(true);
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		default:
			break;
		}
	}
	
	private void compressAllVideos() throws IOException {
		String fileName = dateFormat.format(Calendar.getInstance().getTime()) + ".mp4";
		String outputFile = outputDirPath + "/" + fileName;
		File recordDir = new File(recordDirPath);
		String[] videoFileNames = recordDir.list();
		for (int i = 0; i < videoFileNames.length; i++) {
			videoFileNames[i] = recordDirPath + "/" + videoFileNames[i];
			System.out.println(videoFileNames[i]);
		}
		ShortenExample.appendVideo(outputFile, videoFileNames);
	}

	private void stopRecording() {
		System.out.println("isRecording = " + isRecording);
		if (isRecording) {
			try {
				stopmediaRecorder();
//				mRecorder.stop();
//				mCamera.stopPreview();
//				mCamera.lock();
//				mCamera.release();
				startButton.setEnabled(true);
				isRecording = false;
				resetProgressbar();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	protected void releaseCamera() {
		if(mCamera!=null){
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
	}
	
	protected void initpreview() {
		mCamera = Camera.open(CameraInfo.CAMERA_FACING_BACK);
		try {
			mCamera.setPreviewDisplay(preView.getHolder());
		} catch (IOException e) {
			e.printStackTrace();
		}
		setCameraDisplayOrientation(this,CameraInfo.CAMERA_FACING_BACK,mCamera);
		mCamera.startPreview();
	}
	
	public static void setCameraDisplayOrientation(Activity activity,
	         int cameraId, android.hardware.Camera camera) {
	     android.hardware.Camera.CameraInfo info =
	             new android.hardware.Camera.CameraInfo();
	     android.hardware.Camera.getCameraInfo(cameraId, info);
	     int rotation = activity.getWindowManager().getDefaultDisplay()
	             .getRotation();
	     int degrees = 0;
	     switch (rotation) {
	         case Surface.ROTATION_0: degrees = 0; break;
	         case Surface.ROTATION_90: degrees = 90; break;
	         case Surface.ROTATION_180: degrees = 180; break;
	         case Surface.ROTATION_270: degrees = 270; break;
	     }

	     int result;
	     if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
	         result = (info.orientation + degrees) % 360;
	         result = (360 - result) % 360;  // compensate the mirror
	     } else {  // back-facing
	         result = (info.orientation - degrees + 360) % 360;
	     }
	     camera.setDisplayOrientation(result);
	}
	
	private void stopmediaRecorder() {
		if(mRecorder!=null){
			if(isRecording){
				mRecorder.stop();
				mRecorder.reset();
				mRecorder.release();
				mRecorder=null;
				isRecording = false;
				try {
					mCamera.reconnect();
				} catch (IOException e) {
					Toast.makeText(this, "reconect fail", Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				}
			}
		}
	}
	
	private void startmediaRecorder() {
		mCamera.unlock();
		isRecording = true;
		mRecorder = new MediaRecorder();
		mRecorder.reset();
		mRecorder.setCamera(mCamera);
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);//设置声音源
		mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);//设置视频源
//		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
//		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
//		mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
//		mRecorder.setVideoSize(1024, 768);
//		mRecorder.setOrientationHint(90);
//		mRecorder.setVideoEncodingBitRate(5 * 1024 * 768);//每秒4帧
		CamcorderProfile mCamcorderProfile = CamcorderProfile.get(CameraInfo.CAMERA_FACING_BACK, CamcorderProfile.QUALITY_HIGH);
		mRecorder.setProfile(mCamcorderProfile);
		String fileName = dateFormat.format(Calendar.getInstance().getTime()) + ".mp4";
		String recordFile = recordDirPath + "/" + fileName;
		mRecorder.setOutputFile(recordFile);
		mRecorder.setPreviewDisplay(preView.getHolder().getSurface());
		mRecorder.setMaxDuration(MAX_DURATING);
		mRecorder.setOnInfoListener(new OnInfoListener() {
			@Override
			public void onInfo(MediaRecorder arg0, int what, int extra) {
				switch (what) {
				case MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED:
					System.out.println("what = " +what);
					stopRecording();
				}
			}
		});
		try {
			mRecorder.prepare();
		} catch (Exception e) {
			isRecording = false;
			Toast.makeText(this, "fail", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
			mCamera.lock();
		}
		mRecorder.start();
		
	}
	
	static class ProgressTimer implements Runnable {
		
		private int maxTime;
		private ProgressBar progressBar;
		public ProgressTimer(int maxTime, ProgressBar progressBar) {
			super();
			this.maxTime = maxTime;
			this.progressBar = progressBar;
		}

		@Override
		public void run() {
			int stap = maxTime / 100;
			try {
				for (int i = 0; i < maxTime; i += stap) {
					progressBar.setProgress(i + stap);
					TimeUnit.MILLISECONDS.sleep(stap);
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		
	}

}
