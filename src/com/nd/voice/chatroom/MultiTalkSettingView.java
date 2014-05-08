package com.nd.voice.chatroom;

import cn.nd.social.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MultiTalkSettingView extends LinearLayout{

	private Context mContext;
	private View mSetting;
	
//	private ImageView mHonk;
	private ImageView[] mVoiceChange = new ImageView[4];
	private ImageView mManVoice;
	private ImageView mWomanVoice;
	private ImageView mMonsterVoice;
	private ImageView mRobertVoice;
	
//	private SeekBar mVoiceSeek;
//	private SeekBar mGainSeek;
	
	public static Boolean isHonkOpen = true;
	private int mChosen = 0;
	
	private InterfaceForSetting mListener;
	
	public MultiTalkSettingView(Context context) {
		super(context);
		mContext = context;
	}
	
	public MultiTalkSettingView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		initView();
		initListener();
		
//		if (isHonkOpen == false) {
//			mHonk.setImageDrawable(getResources().getDrawable(R.drawable.multi_talk_handfree_normal));
//		}else{
//			mHonk.setImageDrawable(getResources().getDrawable(R.drawable.multi_talk_handfree_press));
//		}
//		
	}


	private void initView(){
		
		mSetting = LayoutInflater.from(mContext).inflate(R.layout.multi_talk_setting, this);
		
//		mHonk = (ImageView)mSetting.findViewById(R.id.multi_talk_audio);
		
		mManVoice = (ImageView)mSetting.findViewById(R.id.multi_talk_man);
		mWomanVoice = (ImageView)mSetting.findViewById(R.id.multi_talk_woman);
		mMonsterVoice = (ImageView)mSetting.findViewById(R.id.multi_talk_monster);
		mRobertVoice = (ImageView)mSetting.findViewById(R.id.multi_talk_robert);
		mManVoice.setSelected(true);
		
//		mVoiceSeek = (SeekBar)mSetting.findViewById(R.id.multi_talk_voice_seekbar);
//		mGainSeek = (SeekBar)mSetting.findViewById(R.id.multi_talk_voice_seek);
		
		mVoiceChange[0] = mManVoice;
		mVoiceChange[1] = mWomanVoice;
		mVoiceChange[2] = mMonsterVoice;
		mVoiceChange[3] = mRobertVoice;
	}
	
	private void initListener(){
		
//		mHonk.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View arg0) {
//				// TODO Auto-generated method stub
//				
//				if (!isHonkOpen) {
//					mHonk.setImageDrawable(getResources().getDrawable(R.drawable.multi_talk_handfree_press));				
//					isHonkOpen = true;
//				}else{
//					mHonk.setImageDrawable(getResources().getDrawable(R.drawable.multi_talk_handfree_normal));
//					isHonkOpen = false;
//				}
//					
//				mListener.onHonkListener(isHonkOpen);
//			}
//		});
		
		mManVoice.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mVoiceChange[mChosen].setSelected(false);
				mChosen = 0;
				mVoiceChange[0].setSelected(true);
				mListener.onManVoiceListener();
			}
		});
		
		mWomanVoice.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mVoiceChange[mChosen].setSelected(false);
				mChosen = 1;
				mVoiceChange[1].setSelected(true);
				mListener.onWomanVoiceListener();
			}
		});
		
		mMonsterVoice.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mVoiceChange[mChosen].setSelected(false);
				mChosen = 2;
				mVoiceChange[2].setSelected(true);
				mListener.onMosterVoiceListener();
			}
		});
		
		mRobertVoice.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mVoiceChange[mChosen].setSelected(false);
				mChosen = 3;
				mVoiceChange[3].setSelected(true);
				mListener.onRobertVoiceListener();
			}
		});
//		mVoiceSeek.setMax(255);
//		mVoiceSeek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
//			
//			@Override
//			public void onStopTrackingTouch(SeekBar seekBar) {
//				// TODO Auto-generated method stub
//				
//			}
//			
//			@Override
//			public void onStartTrackingTouch(SeekBar seekBar) {
//				// TODO Auto-generated method stub
//				
//			}
//			
//			@Override
//			public void onProgressChanged(SeekBar seekBar, int progress,
//					boolean fromUser) {
//				// TODO Auto-generated method stub
//				mListener.onVoiceSeekBarChange(progress);
//			}
//		});
//		mGainSeek.setMax(500);
//		mGainSeek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
//			
//			@Override
//			public void onStopTrackingTouch(SeekBar seekBar) {
//				// TODO Auto-generated method stub
//				
//			}
//			
//			@Override
//			public void onStartTrackingTouch(SeekBar seekBar) {
//				// TODO Auto-generated method stub
//				
//			}
//			
//			@Override
//			public void onProgressChanged(SeekBar seekBar, int progress,
//					boolean fromUser) {
//				// TODO Auto-generated method stub
//				mListener.onGainSeekBarChange(progress);
//			}
//		});
	}

	public void setmListener(InterfaceForSetting mListener) {
		this.mListener = mListener;
	}


	public interface InterfaceForSetting {
		void onHonkListener(boolean isSelected);
		void onManVoiceListener();
		void onWomanVoiceListener();
		void onMosterVoiceListener();
		void onRobertVoiceListener();
		void onVoiceSeekBarChange(int progress);
		void onGainSeekBarChange(int progress);		
	}
	
	

}
