package cn.nd.social.card;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import cn.nd.social.R;
import cn.nd.social.card.CardUtil.CardDataPacker;
import cn.nd.social.util.AudioDataPacker;
import cn.nd.social.util.FilePathHelper;

import com.example.ofdmtransport.Modulation;


public class SendCardRadarView extends RelativeLayout implements OnClickListener,
		MediaPlayer.OnCompletionListener, OnTouchListener {
	public SendCardRadarView(Context context) {
		super(context);
	}
	public SendCardRadarView(Context context,AttributeSet attr) {
		super(context, attr);
	}
	
	public SendCardRadarView(Context context, AttributeSet attrs, int defStyle) {
		super(context,attrs,defStyle);
	}


	private static String sAudioFileName;
	private static String sAckFileName;



	private static final int EVENT_REPEAT_PLAY = 1001;
	private static final int EVENT_RECORD_RESUME_DELAY = 1002;
	
	private Handler mHandler;

	private boolean mIsSendingCard = false;
	private View mSendBtn;
	private boolean mPlayFlag = false;
	
	private MediaPlayer mMediaPlayer;
	
	static {
		sAudioFileName = FilePathHelper.getWaveTransFile();
		sAckFileName = FilePathHelper.getWaveAckFile();
	}

	@Override
	protected void onFinishInflate() {
		setupViews();
		mHandler = new EventHandler();
		super.onFinishInflate();
	}

	public void stop() {
		if(mMediaPlayer != null) {
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
		Modulation.releaseEncoder();
	}



	private void setupViews() {
		Modulation.initEncoder();//only init modulator
		mSendBtn = findViewById(R.id.share_btn);
		mSendBtn.setOnTouchListener(this);
		initAnim();
	}




	ImageView mRadarView;
	ImageView mRadarViewInner;
	AnimationSet mAnimSet;
	AnimationSet mAnimSetInner;

	private void initAnim() {
		mRadarView = (ImageView) findViewById(R.id.radar_view);
		mRadarViewInner = (ImageView) findViewById(R.id.radar_view_inner);
		mAnimSet = getAnimSet();
		mAnimSetInner = getAnimSet();
	}

	private void startAnim() {
		mRadarView.startAnimation(mAnimSet);
		mAnimSetInner.setStartOffset(ANIMATION_PLAY_INTERVAL / 2);
		mRadarViewInner.startAnimation(mAnimSetInner);
		mAnimSet.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				if (mPlayFlag) {
					mRadarView.startAnimation(mAnimSet);
				}

			}
		});
		mAnimSetInner.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				if (mPlayFlag) {
					mAnimSetInner.setStartOffset(0);
					mRadarViewInner.startAnimation(mAnimSetInner);
				}

			}
		});
	}

	private void stopAnim() {
		mRadarView.setAnimation(null);
		mRadarViewInner.setAnimation(null);
	}

	private static final int ANIMATION_PLAY_INTERVAL = 1800;

	private AnimationSet getAnimSet() {
		AnimationSet animSet = new AnimationSet(true);
		ScaleAnimation scale = new ScaleAnimation(1f, 5f, 1, 5f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		scale.setDuration(ANIMATION_PLAY_INTERVAL);
		AlphaAnimation alpha = new AlphaAnimation(1, 0);
		alpha.setDuration(ANIMATION_PLAY_INTERVAL);
		alpha.setStartOffset(0);
		animSet.addAnimation(scale);
		animSet.addAnimation(alpha);
		/* animSet.setRepeatCount(Animation.INFINITE); */
		return animSet;
	}

	private class EventHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case EVENT_REPEAT_PLAY:
				if(mMediaPlayer != null) {
					mMediaPlayer.start();
				}
				break;
			case EVENT_RECORD_RESUME_DELAY:				
				Log.e("PLAY audio","onCompletion post stop");
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	}

	

	// get card data from preference
	private void sendCard() {
		CardDataPacker cardPacker = new CardUtil.CardDataPacker(AudioDataPacker.TYPE_CARD_STRING);
		String cardStr = cardPacker.packAudioData(null);
		playAudio(cardStr);
	}

	// encoding string data into an audio-file then play the file
	private void playAudio(String content) {
		Modulation.setListenMode('p');
		boolean retval = Modulation.genWavFile(content,sAudioFileName);
		
		if (retval == false) {
			return;
		}
		try {
			if(mMediaPlayer == null) {
				mMediaPlayer = new MediaPlayer();
				mMediaPlayer.setAudioStreamType(AudioManager.STREAM_SYSTEM);//change to from STREAM_RING to STREAM_SYSTEM
				mMediaPlayer.setOnCompletionListener(this);
				mMediaPlayer.setDataSource(sAudioFileName);
				mMediaPlayer.prepare();
			} else {
				if(mMediaPlayer.isPlaying()) {
					mMediaPlayer.pause();
				}
			}
			mMediaPlayer.start();
			mPlayFlag = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void toggleAnim(boolean isSend) {
		if (isSend) {
			startAnim();
		} else {
			stopAnim();
		}
	}



	@Override
	public void onCompletion(MediaPlayer mplayer) {
		if (mPlayFlag) {
			mHandler.sendEmptyMessageDelayed(EVENT_REPEAT_PLAY, 500);
			Log.e("PLAY audio","onCompletion play");
		} else {
			mIsSendingCard = false;
			Modulation.setListenMode('r');
			mHandler.sendEmptyMessageDelayed(EVENT_RECORD_RESUME_DELAY, 1000);
			Log.e("PLAY audio","onCompletion stop");
			toggleAnim(false);
		}
	}

	

	@Override
	public boolean onTouch(View v, MotionEvent me) {
		int action = me.getAction();
		if (action == MotionEvent.ACTION_DOWN) {
			mIsSendingCard = true;
			toggleAnim(mIsSendingCard);
			sendCard();
		} else if (action == MotionEvent.ACTION_UP) {
			mPlayFlag = false;
		}
		return true;
	}

	@Override
	public void onClick(View v) {
			
	}

}