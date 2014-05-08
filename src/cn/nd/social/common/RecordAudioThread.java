package cn.nd.social.common;

import java.io.UnsupportedEncodingException;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;

import com.example.ofdmtransport.Modulation;

public class RecordAudioThread extends Thread {
	private int nread = 0;
	private int retval = 0;
	private short[] recdata;
	private AudioRecord recorder = null;
	private int recBufSize;
	public boolean mStopRecording = false;
	private final static int SAMPLE_RATE = 48000;

	private Handler mHandler;
	private int mEventId;

	public RecordAudioThread(Handler handler, int eventId) {
		mHandler = handler;
		mEventId = eventId;
	}

	public boolean initRecord() {
		
		if(startRecording()) {
			this.start();
			return true;
		}
		return false;
	}
	
	private boolean startRecording() {
		boolean success = false;
		recBufSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
				AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
		if (recBufSize < SAMPLE_RATE) {
			recBufSize = SAMPLE_RATE;
		}
		if (recBufSize != AudioRecord.ERROR_BAD_VALUE) {
			recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
					SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
					AudioFormat.ENCODING_PCM_16BIT, recBufSize);
			if (recorder.getState() == AudioRecord.STATE_INITIALIZED) {
				recorder.startRecording();
				success = true;
			} else {
				try {
					recorder.release();
					recorder = null;
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		return success;
	}
	

	public void finiRecord() { // when main thread exit,this method will get
								// called, and send a signal to stop the Record
								// thread
		mStopRecording = true;
		this.interrupt();// recorder set to null, so we should interrupt

	}

	@Override
	public void run() {
		android.os.Process
				.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
		Modulation.initDecoder();// init demodulator
		Modulation.initProcess();
		recdata = new short[recBufSize / 2];

		while (!Thread.interrupted() && !mStopRecording) {
			if (recorder == null) {
				break;
			}
			nread = recorder.read(recdata, 0, recBufSize / 2);


			
			if (nread > 0) {
				retval = Modulation.process(recdata, nread);
				if (retval == 2) {
					String str = "";
					byte[] result = Modulation.getResult();
					try {
						str = new String(result, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					
					if(mStopRecording) {
						continue;
					}
					
					Message msg = mHandler.obtainMessage();
					msg.what = mEventId;
					msg.obj = str;

					mHandler.sendMessage(msg);
					try {
						// when receive a message, sleep a little while;
						// so the main thread has a chance to stop recording immediately
						Thread.sleep(200); 
					} catch (InterruptedException e) {
						continue;
					}
				}
			}
		}
		try {
			recorder.stop();
			recorder.release();
		} catch(Exception e) {
			e.printStackTrace();
		}
		recorder = null;
		Modulation.releaseDecoder();
	}
}
