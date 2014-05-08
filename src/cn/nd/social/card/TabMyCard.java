package cn.nd.social.card;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import cn.nd.social.R;
import cn.nd.social.account.CAConstant;
import cn.nd.social.account.activity.RegiterActivity;
import cn.nd.social.card.CardUtil.CardData;
import cn.nd.social.card.CardViewCfgReader.CardLayoutCfg;
import cn.nd.social.common.PopMenu;
import cn.nd.social.common.PopMenuItem;
import cn.nd.social.common.RecordAudioThread;
import cn.nd.social.ui.controls.CustomizeableLayout;
import cn.nd.social.ui.controls.FrameLayoutEx;
import cn.nd.social.util.AudioDataPacker;

import com.nineoldandroids.view.ViewHelper;

public class TabMyCard extends Activity{
	LayoutInflater mInflater;

	CustomizeableLayout mCustView;
	private CardLayoutCfg mXmlConfig;// get configuration from xml file

	private final static int EVENT_SINGLE_TAP = 101;
	private final static int EVENT_DOUBLE_TAP = 102;
	private final static int EVENT_UPDATE_VIEW = 103;
	private final static int EVENT_SOUND_WAVE_MSG = 104;
	
	private final static int TAP_POST_DELAY = ViewConfiguration
			.getDoubleTapTimeout(); // milliseconds
	TextView mMobileLabel;
	TextView mPhoneLabel;
	TextView mEmailLabel;
	TextView mAddrLabel;

	TextView mNameText;
	TextView mCompanyText;
	TextView mTitleText;
	TextView mMobileText;
	TextView mPhoneText;
	TextView mEmailText;
	TextView mAddrText;

	LinearLayout mMobileLayout;
	LinearLayout mPhoneLayout;
	LinearLayout mEmailLayout;
	LinearLayout mAddrLayout;

	private SharedPreferences mPrefs;

	private boolean mTookFirstEvent;

	private int mModelId;
	private FrameLayoutEx mCustContainer;
	private View mRootView;

	private double mScale = 1;

	
	private View mShareLayout;
	private SendCardRadarView mShareRadar;
	private TextView mTitle;
	private View mEditCard;
	private View mCardListBtn;
	private View mTitleBar;
	private View mMoreBtn;
	
	private View mRecvBtn;
	
	private int mLaunchFrom = 0;
	GestureDetector mDetector;
	
	FrameLayout mLayout_teach;
	
	public final static String LAUNCH_FROM = "launch_from";
	
	private final static int MENU_ID_CARD_LIST = 0;
	private final static int MENU_ID_EDIT_CARD = 1;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		mLaunchFrom = intent.getIntExtra(LAUNCH_FROM, 0);
		setContentView(R.layout.main_tab_card);
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		mModelId = mPrefs.getInt(CardUtil.MODEL_ID,1);
		mXmlConfig = CardViewCfgReader.getXmlConfig(this,mModelId);
		setupViews();
		
		ViewHelper.setRotation(mCustView, 90);
		
		registerReceiver(mCardRefreshReceiver, 
				new IntentFilter(CardUtil.ACTION_SELF_CARD_REFRESH));
		
		if(mLaunchFrom != 0) {
			enterShareMode();
		} else {
			//tangtaotao@NetDragon_20140211
			mRootView.setOnClickListener(null);		
			mShareLayout.setVisibility(View.VISIBLE);
		}
		
	}
	
	private void enterShareMode() {
		//mEditCard.setVisibility(View.GONE);
		mRootView.setOnClickListener(null);		
		mShareLayout.setVisibility(View.VISIBLE);
		mTitle.setText(R.string.share_card);
	}
	
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	/*
	 * BroadcastReceiver for self card refresh
	 */
	private BroadcastReceiver mCardRefreshReceiver = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(action.equals(CardUtil.ACTION_SELF_CARD_REFRESH)) {
				mModelId = mPrefs.getInt(CardUtil.MODEL_ID,1);
				mXmlConfig = CardViewCfgReader.getXmlConfig(TabMyCard.this,mModelId);
				updateView();
			}
		}
	};
	
	private void onRefreshCard() {
		mNameText.setText(getPrefString(CardUtil.NAME_STR));
		mCompanyText.setText(getPrefString(CardUtil.COMPANY_STR));
		mTitleText.setText(getPrefString(CardUtil.TITLE_STR));
		mMobileText.setText(getPrefString(CardUtil.MOBILE_STR));
		mPhoneText.setText(getPrefString(CardUtil.PHONE_STR));
		mEmailText.setText(getPrefString(CardUtil.EMAIL_STR));
		mAddrText.setText(getPrefString(CardUtil.ADDR_STR));
		
		mModelId = mPrefs.getInt(CardUtil.MODEL_ID,1);
		mXmlConfig = CardViewCfgReader.getXmlConfig(this,mModelId);
		
		int resId = CardUtil.getCardBgId(mModelId);

		if (resId != 0) {
			mCustView.setBackgroundResource(resId);
		}
		
/*		if(mLaunchFrom == 0) {
			toggleShareBtn(false);
		}*/
	}

	private void setupViews() {
		
		initViewRefs();
		
		View back = findViewById(R.id.back_btn);
		back.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
/*				if(mLaunchFrom == 0 && mShareLayout.getVisibility() == View.VISIBLE) {
					toggleShareBtn(false);
				} else {*/
					finish();
/*				}*/
								
			}
		});
		
		mRootView.setOnClickListener(new ClickListener());
		

		
		mCustContainer.setOnMeasureListener(new FrameLayoutEx.OnMeasureListener() {
			@Override
			public void onDimenMesured() {
				mCustView.setVisibility(View.VISIBLE); // show the cust view
				mHandler.sendEmptyMessage(EVENT_UPDATE_VIEW);
			}
		});

		
		mCustView.setVisibility(View.GONE); // hide cust view util we get xml
											// config
		mDetector = new GestureDetector(this,new GestureDetector.SimpleOnGestureListener(){

			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float arg2,
					float arg3) {
				boolean flag = false;
				if (e1.getX() - e2.getX() > 120) {
					flag = true;
					mModelId --;
					if(mModelId < 1){
						mModelId = 4; 
					}
					
		        } else if (e1.getX() - e2.getX() < -120) {
		        	flag = true;
		        	mModelId ++;
					if(mModelId > 4){
						mModelId = 1; 
					}
		        }
				if(flag){
					mPrefs.edit().putInt(CardUtil.MODEL_ID, mModelId).commit();
					mXmlConfig = CardViewCfgReader.getXmlConfig(TabMyCard.this,mModelId);
					updateView();
					Intent intent = new Intent(CardUtil.ACTION_SELF_CARD_REFRESH);
					sendBroadcast(intent);
		            return true;
				}
				return false;
			}

		});
		mCustContainer.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return mDetector.onTouchEvent(event);
			}
		});
		
		
		mLayout_teach.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				mPrefs.edit().putBoolean("ISFIREST", false).commit();
				mLayout_teach.setVisibility(View.GONE);
				
			}
		});
		
/*		mEditCard.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(TabMyCard.this, RegiterActivity.class);
				intent.putExtra("isEditCardState", true);
				startActivity(intent);	
			}
		});
		
		mCardListBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(TabMyCard.this,CardListActivity.class);
				startActivity(intent);				
			}
		});*/
		
		mMoreBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				final PopMenu menu = new PopMenu(TabMyCard.this);
				menu.addItem(new PopMenuItem(MENU_ID_CARD_LIST, getString( R.string.card_title), 0));
				menu.addItem(new PopMenuItem(MENU_ID_EDIT_CARD, getString( R.string.edit_card), 0));
				
				menu.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> adapterView, View arg1,
							int position, long arg3) { 
						PopMenuItem item = (PopMenuItem) adapterView.getItemAtPosition(position);
						switch(item.getItemId()) {
						case MENU_ID_CARD_LIST: {
							Intent intent = new Intent(TabMyCard.this,CardListActivity.class);
							startActivity(intent);							
						}	
							break;
							
						case MENU_ID_EDIT_CARD:{	
							Intent intent = new Intent(TabMyCard.this, RegiterActivity.class);
							intent.putExtra("isEditCardState", true);
							startActivity(intent);
						}
							break;
						}

						menu.dismiss();
					}
				});
				menu.showAsDropDown(mMoreBtn);				
			}
		});
		
		mRecvBtn.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN){
					startRecorder();
				}else if(event.getAction() == MotionEvent.ACTION_UP 
						|| event.getAction() == MotionEvent.ACTION_CANCEL){
					stopRecorder();
				}
				return false;
			}
		});
		
	}
	
	
	
	private void initViewRefs() {
		mRootView = findViewById(R.id.root_view);
		
		mCustContainer = (FrameLayoutEx) findViewById(R.id.cust_container);
		mCustView = (CustomizeableLayout) findViewById(R.id.customize_view);

		mNameText = (TextView) findViewById(R.id.cust_name_text);
		mCompanyText = (TextView) findViewById(R.id.cust_company_text);
		mTitleText = (TextView) findViewById(R.id.cust_title_text);
		mMobileText = (TextView) findViewById(R.id.cust_mobile_text);
		mPhoneText = (TextView) findViewById(R.id.cust_phone_text);
		mEmailText = (TextView) findViewById(R.id.cust_email_text);
		mAddrText = (TextView) findViewById(R.id.cust_addr_text);

		mMobileLabel = (TextView) findViewById(R.id.cust_mobile_label);
		mPhoneLabel = (TextView) findViewById(R.id.cust_phone_label);
		mEmailLabel = (TextView) findViewById(R.id.cust_email_label);
		mAddrLabel = (TextView) findViewById(R.id.cust_addr_label);

		mMobileLayout = (LinearLayout) findViewById(R.id.mobile_layout);
		mPhoneLayout = (LinearLayout) findViewById(R.id.phone_layout);
		mEmailLayout = (LinearLayout) findViewById(R.id.email_layout);
		mAddrLayout = (LinearLayout) findViewById(R.id.addr_layout);
		
		mShareLayout = findViewById(R.id.fl_share);
		mShareRadar = (SendCardRadarView)findViewById(R.id.share_card_radar);
		//mShareRadar.setVisibility(View.GONE);
		mTitleBar = findViewById(R.id.title);
		mTitle = (TextView)findViewById(R.id.title_text);
		
		mShareLayout.setAlpha(0.6f);
		
		mMoreBtn = findViewById(R.id.right_btn);
/*		mEditCard = findViewById(R.id.edit_card);
		mCardListBtn = findViewById(R.id.card_list);*/
		
		mLayout_teach = (FrameLayout) findViewById(R.id.layout_teach);
		boolean isFirst = mPrefs.getBoolean("ISFIREST", true);
		if(isFirst){
			mLayout_teach.setVisibility(View.VISIBLE);
		}else{
			mLayout_teach.setVisibility(View.GONE);
		}
			
		mRecvBtn = findViewById(R.id.btn_receive);	
		
	}
	
	private void updateView() {

		int width = mCustContainer.getWidthEx();
		int height = mCustContainer.getHeightEx();
		if (width == 0 || height == 0) {
			DisplayMetrics metrics = getResources().getDisplayMetrics();
			width = metrics.widthPixels;
			height = metrics.heightPixels;
		}
		
		 
		int []viewSize = new int[2];
		int []targetSize = new int[2];
		viewSize[0] = width;
		viewSize[1] = height;
		
		CardUtil.adjustTargetSize(viewSize, targetSize);
		mScale = (double)targetSize[1] / mXmlConfig.defaultWidth;
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				targetSize[1], targetSize[0], Gravity.CENTER);
		mCustContainer.updateViewLayout(mCustView, params);
		updateCustViewFromConfig();
		
		onRefreshCard();
	}

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {
			case EVENT_SINGLE_TAP:
				mTookFirstEvent = false;
				//toggleShareBtn(false);
				break;
			case EVENT_DOUBLE_TAP:
				/*Intent intent = new Intent(TabMyCard.this, MyCardEditor.class);
				startActivity(intent);*/
				//toggleShareBtn(true);
				break;
			case EVENT_UPDATE_VIEW:
				updateView();
				break;
				
			case EVENT_SOUND_WAVE_MSG:
				handleSoundWaveMsg((String)msg.obj);
				Log.e("PLAY audio","onCompletion post stop");
				break;
			}
		}

	};

	private class ClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			if (!mTookFirstEvent) {
				mTookFirstEvent = true;
				Message message = mHandler.obtainMessage();
				mHandler.removeMessages(EVENT_SINGLE_TAP);
				message.what = EVENT_SINGLE_TAP;
				mHandler.sendMessageDelayed(message, TAP_POST_DELAY);
			} else {
				mHandler.removeMessages(EVENT_SINGLE_TAP);
				Message message = mHandler.obtainMessage();
				message.what = EVENT_DOUBLE_TAP;
				mHandler.sendMessageAtFrontOfQueue(message);
				mTookFirstEvent = false;
			}
		}

	}

	private String getPrefString(String key) {
		return mPrefs.getString(key, "");
	}

	private void scaleXYProp(CardLayoutCfg config) {
		scaleXYPropInt(config.nameProp);
		scaleXYPropInt(config.compProp);
		scaleXYPropInt(config.titleProp);
		scaleXYPropInt(config.mobileProp);
		scaleXYPropInt(config.phoneProp);
		scaleXYPropInt(config.emailProp);
		scaleXYPropInt(config.addrProp);
	}

	private void scaleXYPropInt(int[] prop) {
		prop[0] = (int) (prop[0] * mScale);
		prop[1] = (int) (prop[1] * mScale);
	}

	void updateCustViewFromConfig() {
		if (mXmlConfig != null) {

			scaleXYProp(mXmlConfig);

			mModelId = mPrefs.getInt(CardUtil.MODEL_ID,1);
			int resId = CardUtil.getCardBgId(mModelId);			
			mCustView.setBackgroundResource(resId);


			setLabelProp(mMobileLabel, mXmlConfig.mobileProp);
			setLabelProp(mPhoneLabel, mXmlConfig.phoneProp);
			setLabelProp(mEmailLabel, mXmlConfig.emailProp);
			setLabelProp(mAddrLabel, mXmlConfig.addrProp);

			setTextProp(mNameText, mXmlConfig.nameProp);
			setTextProp(mCompanyText, mXmlConfig.compProp);
			setTextProp(mTitleText, mXmlConfig.titleProp);
			setTextProp(mMobileText, mXmlConfig.mobileProp, true);
			setTextProp(mPhoneText, mXmlConfig.phoneProp, true);
			setTextProp(mEmailText, mXmlConfig.emailProp, true);
			setTextProp(mAddrText, mXmlConfig.addrProp, true);

			updateCustViewLayout(mNameText, mXmlConfig.nameProp);
			updateCustViewLayout(mCompanyText, mXmlConfig.compProp);			
			updateCustViewLayout(mTitleText, mXmlConfig.titleProp);

			updateCustViewLayout(mMobileLayout, mXmlConfig.mobileProp);
			updateCustViewLayout(mPhoneLayout, mXmlConfig.phoneProp);
			updateCustViewLayout(mEmailLayout, mXmlConfig.emailProp);
			updateCustViewLayout(mAddrLayout, mXmlConfig.addrProp);
		}
	}





	void setLabelProp(TextView tv, int[] prop) {
		if (CardViewCfgReader.isColorSet(mXmlConfig.gStyle.defFontColor)) {
			tv.setTextColor(prop[3]);
		} else if (CardViewCfgReader.isColorSet(mXmlConfig.gStyle.defFontColor)) {
			tv.setTextColor(mXmlConfig.gStyle.defFontColor);
		}
		if (CardViewCfgReader.isFontSizeSet(prop[2])) {
			tv.setTextSize(CardUtil.SIZE_UNIT, CardViewCfgReader.getFontSize(prop[2],mScale));
		} else if (CardViewCfgReader.isFontSizeSet(mXmlConfig.gStyle.defFontSize)) {
			tv.setTextSize(CardUtil.SIZE_UNIT,
					CardViewCfgReader.getFontSize(mXmlConfig.gStyle.defFontSize,mScale));
		}
	}

	void setTextProp(TextView tv, int[] prop) {
		setTextProp(tv, prop, false);
	}

	void setTextProp(TextView tv, int[] prop, boolean needMargin) {
		if (CardViewCfgReader.isColorSet(mXmlConfig.gStyle.defFontColor)) {
			tv.setTextColor(prop[3]);
		} else if (CardViewCfgReader.isColorSet(mXmlConfig.gStyle.defFontColor)) {
			tv.setTextColor(mXmlConfig.gStyle.defFontColor);
		}
		if (CardViewCfgReader.isFontSizeSet(prop[2])) {
			tv.setTextSize(CardUtil.SIZE_UNIT, CardViewCfgReader.getFontSize(prop[2],mScale));
		} else if (CardViewCfgReader.isFontSizeSet(mXmlConfig.gStyle.defFontSize)) {
			tv.setTextSize(CardUtil.SIZE_UNIT,
					CardViewCfgReader.getFontSize(mXmlConfig.gStyle.defFontSize,mScale));
		}
		if (needMargin) {
			ViewGroup.LayoutParams params = tv.getLayoutParams();
			if (params instanceof ViewGroup.MarginLayoutParams) {
				((ViewGroup.MarginLayoutParams) params).rightMargin = prop[0];
			} else {
				ViewGroup.MarginLayoutParams marginParam = new ViewGroup.MarginLayoutParams(
						params);
				marginParam.setMargins(0, prop[0], 0, 0);
				tv.setLayoutParams(marginParam);
			}
		}
	}

	void updateCustViewLayout(View v, int[] prop) {
		int x = prop[0];
		int y = prop[1];
		int len = LayoutParams.WRAP_CONTENT;
		mCustView.updateViewLayout(v, new CustomizeableLayout.LayoutParams(len,
				len, x, y));
	}


	private void toggleShareBtn(boolean forceVisible) {
		
		if(mShareLayout.getVisibility() == View.VISIBLE) {
			mShareLayout.setVisibility(View.GONE);
			mCustContainer.setVisibility(View.VISIBLE);
			mTitle.setText(R.string.my_card_label);
		} else if(forceVisible){
/*			AssetFileDescriptor afd;
			
			try {
				afd = getAssets().openFd("radar_pop.mp3");
				MediaPlayer player = new MediaPlayer();				
				player.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(), afd.getLength());
				player.prepare();
				player.start();
				player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
					@Override
					public void onCompletion(MediaPlayer mp) {
						mp.stop();
						mp.release();
					}
				});
			} catch (IOException e) {
				e.printStackTrace();
			}	*/		
			mShareLayout.setVisibility(View.VISIBLE);
			AnimationSet anim = (AnimationSet)AnimationUtils.loadAnimation(this, R.anim.radar_turn_out);
			mShareRadar.startAnimation(anim);
//			mCustContainer.setVisibility(View.GONE);			
			mTitle.setText(R.string.share_card);
		}
	}

	@Override
	public void onBackPressed() {
/*		if(mLaunchFrom == 0 && (mShareLayout.getVisibility() == View.VISIBLE)) {
			toggleShareBtn(false);
		} else {*/
			super.onBackPressed();
		/*}*/
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		return false;
	}

	@Override
	protected void onStop() {
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		if(mShareRadar != null) {
			mShareRadar.stop();
		}		
		unregisterReceiver(mCardRefreshReceiver);
		super.onDestroy();
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {

		if(mDetector.onTouchEvent(event))
	     {
	            event.setAction(MotionEvent.ACTION_CANCEL);
	     }
	     return super.dispatchTouchEvent(event);

	}
	
	
    RecordAudioThread mSoundWaveRecorder = null;
    private boolean startRecorder() {
		if (mSoundWaveRecorder == null || !mSoundWaveRecorder.isAlive()) {
			mSoundWaveRecorder = new RecordAudioThread(mHandler,
					EVENT_SOUND_WAVE_MSG);
			return mSoundWaveRecorder.initRecord();
		}
		return true;
	}
    
    /** sound wave handler */
	void handleSoundWaveMsg(String rawWaveData) {
		stopRecorder(); // TODO:change the strategy
		int type = AudioDataPacker.getType(rawWaveData);
		if (type == AudioDataPacker.TYPE_CARD_STRING) { // receive card
			onCardDataArrival(rawWaveData);
		} 
	}
	
	
	private void onCardDataArrival(String rawData) {
		ReceiveCardHandler cardHandler = ReceiveCardHandler.getInstance();
		// cardHandler.onCardDataArrival(rawData, mActivity);
		CardData cardData = cardHandler.getCardData(rawData);
		if (cardData == null) {
			return;
		}

		CardUtil.storeCardFromNFC(this, cardData);
	}
	
	private void stopRecorder() {
		if (mSoundWaveRecorder != null) {
			mSoundWaveRecorder.finiRecord();
			/*
			 * try { // tangtaotao@ND_20140227: TODO:add may cause unstable
			 * mSoundWaveRecorder.join(); } catch (InterruptedException e) {
			 * e.printStackTrace(); }
			 */
			mSoundWaveRecorder = null;
		}
	}

}