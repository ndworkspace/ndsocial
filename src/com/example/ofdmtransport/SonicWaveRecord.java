package com.example.ofdmtransport;

import java.io.UnsupportedEncodingException;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

/**
 * record ultrasonic wave, use startRecord to begin the recording
 * user stopRecord to end the recording, this two methods should be called in pairs;
 * should implement RecordingListener
 * */
public class SonicWaveRecord {
	private final static int SAMPLE_RATE = 48000;

	private int mRecBufSize;
	private AudioRecord mRecorder = null;
	private boolean mKeepRecording = true;
	private RecorderState mRecordState = RecorderState.CLOSED;
	private RecordingListener mListener;
	private Thread mThread;
	
	public final int RET_SUCCESS = 0;
	public final int RET_OCCUPIED = 1;
	public final int RET_UNKNOW_ERROR = 2;
	
	private static SonicWaveRecord sInstance;
	
	private enum RecorderState {
		CLOSED,RECORDING,STOP
	}

	private SonicWaveRecord() {
	}
	
	synchronized public static SonicWaveRecord getInstance() {
		if(sInstance == null) {
			sInstance = new SonicWaveRecord();			
		}
		return sInstance;
	}
	
	/**API start*/
	/*********************************************************/
	public void setRecordListner(RecordingListener listener) {
		mListener = listener;
	}
	
	public int startRecord() {
		if(mRecordState == RecorderState.RECORDING) {
			return RET_OCCUPIED;
		}
		//we need to return to user immediately, so recorder is created here instead of RecordThread;
		if (!initRecord()) { 
			return RET_UNKNOW_ERROR;
		}
		mRecordState = RecorderState.RECORDING;
		mKeepRecording = true;
		
		mThread = new RecordThread();
		mThread.start();		
		return RET_SUCCESS;
	}

	public boolean stopRecord() {
		if(mRecordState != RecorderState.RECORDING) {
			return false;
		}
		mRecordState = RecorderState.CLOSED;
		finiRecord();
		return true;
	}
	
	/*********************************************************/
	/**API end*/
	
	private boolean initRecord() {
		mRecBufSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
				AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
		if (mRecBufSize < SAMPLE_RATE) {
			mRecBufSize = SAMPLE_RATE;
		}
		if (mRecBufSize != AudioRecord.ERROR_BAD_VALUE) {
			mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
					SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
					AudioFormat.ENCODING_PCM_16BIT, mRecBufSize);
			if (mRecorder.getState() == AudioRecord.STATE_INITIALIZED) {
				mRecorder.startRecording();
				return true;
			}
		}
		return false;
	}

	private void finiRecord() {
		if (mRecorder != null) {
			mKeepRecording = false;

			mThread.interrupt();// we should interrupt thread			
			mThread = null;
			
			Log.e("DEBUG","mRecorder stop");
		}
	}
	

	
	public class RecordThread extends Thread {
		@Override
		public void run() {
			android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
			Modulation.initDecoder();//TODO: use singleton to manager this			
			Modulation.initProcess();
			short[] recdata = new short[mRecBufSize / 2];
			while (!Thread.interrupted() && mKeepRecording) {
				if (mListener != null && mListener.interceptRecording()) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException ie) {
						continue;
					} catch (Exception e) {
						e.printStackTrace();
					}
					//flush the recording data by reading
					mRecorder.read(recdata, 0, mRecBufSize / 2);
					continue;
				}
				
				int nread = mRecorder.read(recdata, 0, mRecBufSize / 2); //may block here
				if(!mKeepRecording ) {//if record.read may get blocked, check again
					break;
				}
				if (nread > 0) {
					int retval = Modulation.process(recdata, nread);
					if (retval == 2) {
						byte[] result = Modulation.getResult();
						try {
							String str = new String(result, "UTF-8");
							mListener.onValidWaveArrival(str);
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}
					}
				}
			}
			try {
				mRecorder.stop();
				mRecorder.release();
			} catch (Exception e) {
				e.printStackTrace();
			}			
			mRecorder = null;
			Modulation.releaseDecoder();//TODO: use singleton to manager this
		}
	}
}
