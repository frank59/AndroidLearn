package com.geewaza.android.test001;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnInfoListener;
import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceHolder;
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
	Camera c;
	ProgressBar progressBar;
	ProgressTimer progressTimer;
	
	String recordDirPath;
	String outputDirPath;
	
	private boolean isRecording = false;
	private File videoFile;
	
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
			recordDirPath = Environment.getExternalStorageDirectory().getCanonicalFile() + CONSTAND.RECORD_FILE_PATH;
			File recordDir = new File(recordDirPath);
			if (!recordDir.exists() || !recordDir.isDirectory()) {
				recordDir.mkdirs();
			}
			outputDirPath = Environment.getExternalStorageDirectory().getCanonicalFile() + CONSTAND.OUT_PUT_FILE_PATH;
			File outputDir = new File(outputDirPath);
			if (!outputDir.exists() || !outputDir.isDirectory()) {
				outputDir.mkdirs();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
		startButton.setOnClickListener(this);
		compressButton.setOnClickListener(this);
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
	
	public boolean prepareRecorder() throws IOException {
		String fileName = dateFormat.format(Calendar.getInstance().getTime());
		videoFile = new File(recordDirPath + "/" + fileName + ".mp4");
		System.out.println(videoFile.getAbsolutePath());
		Toast.makeText(RecordVideoActivity.this, videoFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
		c = Camera.open();
		c.getParameters().setPreviewSize(1024, 768);
		c.setDisplayOrientation(90);
//		c.setPreviewDisplay(preView.getHolder());
		c.unlock();
		mRecorder.reset();
		mRecorder.setCamera(c);
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);//设置声音源
		mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);//设置视频源
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
		mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
		mRecorder.setVideoSize(1024, 768);
		mRecorder.setOrientationHint(90);
//		CamcorderProfile cProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH); 
//		mRecorder.setProfile(cProfile);
//		mRecorder.setVideoSize(320, 280);
		mRecorder.setVideoEncodingBitRate(5 * 1024 * 768);//每秒4帧
		mRecorder.setOutputFile(videoFile.getAbsolutePath());
		mRecorder.setMaxDuration(MAX_DURATING);
		mRecorder.setOnInfoListener(new OnInfoListener() {
			
			@Override
			public void onInfo(MediaRecorder arg0, int what, int extra) {
				switch (what) {
				case MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED:
					stopRecording();
				}
			}
		});
		mRecorder.setPreviewDisplay(preView.getHolder().getSurface());
		mRecorder.prepare();
		return true;
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
				prepareRecorder();
				mRecorder.start();
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
		if (isRecording) {
			try {
				mRecorder.stop();
				c.stopPreview();
				c.lock();
				c.release();
				startButton.setEnabled(true);
				isRecording = false;
				resetProgressbar();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
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
			int stap = maxTime / 10;
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
