package com.example.ofdmtransport;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

import cn.nd.social.util.FilePathHelper;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


/**
 * play modulation audio
 * 
 * 
 * */
public class ModulationPlayer implements
		MediaPlayer.OnCompletionListener {

	private static final String TAG = "ModulationPlayer";

	private static final int SAMPLE_RATE = 48000;
	
	private static final int MIN_PLAY_INTERVAL = 1800;

	private Handler mHandler = null;

	private String mPlayContent = null;
	private int mPlayCount;
	private int mPlayInterval;
	private int mCurCount = 0;
	
	private int mRecordBufSize;
	private short[] mRecordData = null;	
	private AudioRecord mRecorder = null;
	
	private Timer mTimer = null;
	private TimerTask mTimerTask = null;
	
	private PlayFinishListener mPlayFinishListener = null;	
	private WeakReference<MediaPlayer> mPlayerWeakRef = null;

	private boolean mIsCurrPlaying = false;
	private boolean mIsPlaying = false;
	

	private MediaPlayer mediaPlayer;
	
	/**
	 * use for touch-play mode
	 * */
	public void setPlayFinishListener(PlayFinishListener l) {
		mPlayFinishListener = l;
	}
	
	public boolean set(Handler handler, String content, int playCount,
			int playInterval) {
		mHandler = handler;

		mPlayContent = content;

		mPlayCount = playCount;

		if(playInterval < MIN_PLAY_INTERVAL) {
			mPlayInterval = MIN_PLAY_INTERVAL;
		} else {
			mPlayInterval = playInterval;
		}
		return true;
	}

	/**
	 * should only be called once for a single thread
	 * */
	public boolean startPlay() {
		if(mIsPlaying) {
			return false;
		}
		new Thread() {
			@Override
			public void run() {
				mIsPlaying = true; 
				boolean retVal = Modulation.genWavFile(mPlayContent, 2, FilePathHelper.getWaveTransFile());
				if(retVal) {
					playModulation();
				} else {
					Log.e(TAG,"genWavFile error");
				}
				mIsPlaying = false; 
			};
		}.start();
		return true;
	}

	public boolean isPlaying() {
		return mIsPlaying;
	}
	
	public void stopPlay() {
		mIsPlaying = false;
	}



	private void playModulation() {
		
		Modulation.initEncoder();  //for play
		
		mTimer = new Timer(true);
		mTimerTask = new TimerTask() {
			public void run() {
				if(mIsPlaying) {
					if(!mIsCurrPlaying) {
						play();
						mCurCount++;
					}
				}
			}
		};
		
		mTimer.schedule(mTimerTask, mPlayInterval, mPlayInterval);		
		
		if(Modulation.MODULATION_OPTION_LISTEN_REPLY 
				&& initRecord()) {
			listenAck();			
		} else {
			while (mIsPlaying) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;

			mTimerTask = null;
		}
		
		try {
			if(mPlayerWeakRef != null && mPlayerWeakRef.get() != null) {
				mPlayerWeakRef.get().release();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Modulation.releaseEncoder();
	}
	
	private boolean play() {
		try {
			if(mediaPlayer == null) {
				mediaPlayer = new MediaPlayer();
				mPlayerWeakRef = new WeakReference<MediaPlayer>(mediaPlayer);	
				
				mediaPlayer.setAudioStreamType(AudioManager.STREAM_SYSTEM);
				mediaPlayer.setOnCompletionListener(this);
				
				mediaPlayer.setDataSource(FilePathHelper.getWaveTransFile());
				mediaPlayer.prepare();
							
			} else {
				if(mediaPlayer.isPlaying()) {
					mediaPlayer.pause();
				}
			}
			
			mediaPlayer.start();
		} catch(Exception e) {
			e.printStackTrace();
			mediaPlayer = null;
		}
		
		if(mediaPlayer == null) {
			return false;
		}

		mIsCurrPlaying = true;
		return true;
	}

	
	
	
	@Override
	public void onCompletion(MediaPlayer mplayer) {
		mIsCurrPlaying = false;
		checkContinuePlay();
	}
	
	private void checkContinuePlay() {
		if (mCurCount >= mPlayCount) {
			//tangtaotao@ND_20140220 add
			if(mPlayFinishListener != null && !mPlayFinishListener.onPlayFinish()) {
				return; //continue to play
			} else {
				mIsPlaying = false;
				mHandler.sendEmptyMessageDelayed(Modulation.MODULATION_HANDLER_PLAY_FINISH,500);
			}						
		}
	}
	
	private void listenAck() {
		Modulation.initDecoder();//for record
		Modulation.setListenMode('p');			
		
		mRecordData = new short[mRecordBufSize / 2];
		
		while (!Thread.interrupted() && mIsPlaying) {
			
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
					msg.what = Modulation.MODULATION_HANDLER_RECV_NOTIFY_ACK;
					mHandler.sendMessage(msg);					
					//break;
				}
			} else {
				Log.d(TAG, "recorder read ret : " + readBytes);
			}
		}

		try {
			if (mRecorder != null) {
				mRecorder.stop();
				mRecorder.release();
				mRecorder = null;
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		Modulation.setListenMode('r');
		Modulation.releaseDecoder();
	}
	
	private boolean initRecord() {
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
	
	public interface PlayFinishListener {
		/**
		 * indicate if we should stop play
		 * true to stop
		 * */
		boolean onPlayFinish();
	}
}
