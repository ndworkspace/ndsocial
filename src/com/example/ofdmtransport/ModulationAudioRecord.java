package com.example.ofdmtransport;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import cn.nd.social.util.AudioDataPacker;
import cn.nd.social.util.FilePathHelper;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ModulationAudioRecord extends Thread {

	private static final String TAG = "ModulationRec";

	private static final int SAMPLE_RATE = 48000;

	private Handler mHandler = null;

	private int mRecordBufSize;
	private short[] mRecordData = null;

	private AudioRecord mRecorder = null;
	
	public boolean set(Handler handler) {
		mHandler = handler;

		return true;
	}

	public boolean startRecord() {
		this.start();

		return true;
	}

	public void stopRecord() {
		this.interrupt();// recorder set to null, so we should interrupt thread

		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		if (initRecord() == false) {
			// send message
			return;
		}
		
		Modulation.initDecoder();
		
		Modulation.initProcess();
		
		android.os.Process
				.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

		mRecordData = new short[mRecordBufSize / 2];
		
		while (!Thread.interrupted()) {

			int readBytes = mRecorder.read(mRecordData, 0, mRecordBufSize / 2);

			if (readBytes > 0) {
				int ret = Modulation.process(mRecordData, readBytes);
				if (ret == 2) {
					String str = "";
					byte[] result = Modulation.getResult();
					try {
						str = new String(result, "UTF-8");

						Log.d(TAG, "recorder ProcessData GetResult : " + str);
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}

					Message msg = mHandler.obtainMessage();
					msg.what = Modulation.MODULATION_HANDLER_RECV_NOTIFY;
					msg.obj = str;
					mHandler.sendMessage(msg);
					
					playAckIfNeed();
					
					break;
				}
			} else {
				Log.e(TAG, "recorder read ret : " + readBytes);
			}
		}

		if (mRecorder != null) {
			mRecorder.stop();
			mRecorder.release();
			mRecorder = null;
		}
		
		Modulation.releaseDecoder();
	}

	public boolean initRecord() {
		mRecordBufSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
				AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

		if (mRecordBufSize != AudioRecord.ERROR_BAD_VALUE) {
			if (mRecordBufSize < SAMPLE_RATE) {
				mRecordBufSize = SAMPLE_RATE;
			}

			mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
					SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
					AudioFormat.ENCODING_PCM_16BIT, mRecordBufSize);

			if (mRecorder.getState() == AudioRecord.STATE_INITIALIZED) {
				mRecorder.startRecording();
			} else {
				Log.w(TAG, "Could not start recording.");
			}

			return true;
		} else {
			mRecordBufSize = 0;
		}

		return false;
	}


	
	private void playAckIfNeed() {
		if(Modulation.MODULATION_OPTION_PLAY_ACK) {
			AudioDataPacker.playModulationAck();
		}
		
	}
}