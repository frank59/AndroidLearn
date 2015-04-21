package com.geewaza.android.test001;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class RecordVideoActivity extends Activity implements OnClickListener {
	
	Button startButton;
	Button stopButton;
	SurfaceView preView;
	MediaRecorder mRecorder;
	
	private boolean isRecording = false;
	private File videoFile;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_recoder);
		
		startButton = (Button) findViewById(R.id.recordStartButton);
		stopButton = (Button) findViewById(R.id.recordStopButton);
		preView = (SurfaceView) findViewById(R.id.preView);
		
		startButton.setOnClickListener(this);
		startButton.setEnabled(true);
		stopButton.setEnabled(false);
		stopButton.setOnClickListener(this);
		preView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		preView.getHolder().setFixedSize(320, 280);
		preView.getHolder().setKeepScreenOn(true);
	}
	
	private boolean prepareRecoder() throws IllegalStateException, IOException {
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			Toast.makeText(RecordVideoActivity.this, "SD卡不存在，请插入SD卡！", Toast.LENGTH_LONG).show();
			return false;
		}
		String videoPath = Environment.getExternalStorageDirectory().getCanonicalFile() + "/myvideo";
		File videoDir = new File(videoPath);
		if (!videoDir.exists() || !videoDir.isDirectory()) {
			videoDir.mkdirs();
		}
		videoFile = new File(videoPath + "/myvideo.mp4");
		mRecorder = new MediaRecorder();
		mRecorder.reset();
		Camera c = Camera.open();
		c.setDisplayOrientation(90);
		c.unlock();
		mRecorder.setCamera(c);
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);//设置声音源
		mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);//设置视频源
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
		mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
		mRecorder.setVideoSize(320, 280);
		mRecorder.setVideoEncodingBitRate(4);//每秒4帧
		mRecorder.setOutputFile(videoFile.getAbsolutePath());
		mRecorder.setPreviewDisplay(preView.getHolder().getSurface());
		mRecorder.prepare();
		return true;
	}

	@Override
	public void onClick(View source) {
		switch (source.getId()) {
		case R.id.recordStartButton:
			try {
				if (!prepareRecoder()) {
					return;
				}
				mRecorder.start();
				Toast.makeText(RecordVideoActivity.this, videoFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
				System.out.println("----Start Recording----");
				startButton.setEnabled(false);
				stopButton.setEnabled(true);
				isRecording = true;
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case R.id.recordStopButton:
			if (isRecording) {
				try {
					mRecorder.stop();
					mRecorder.release();
					mRecorder = null;
					startButton.setEnabled(true);
					stopButton.setEnabled(false);
					prepareRecoder();
					isRecording = false;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		default:
			break;
		}
	}

}
