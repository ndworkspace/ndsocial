package com.nd.voice;

import java.util.HashMap;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import android.widget.Toast;
import cn.nd.social.R;
import cn.nd.social.account.usermanager.UserManager;
import cn.nd.social.common.PopMenu;
import cn.nd.social.common.PopMenuItem;
//import cn.nd.social.syncbrowsing.manager.MeetingSyncEnterReceiver;
import cn.nd.social.syncbrowsing.manager.SyncMsgFactory;
import cn.nd.social.syncbrowsing.meeting.activity.ClientLayoutDelegate;
import cn.nd.social.syncbrowsing.meeting.activity.ClientPageActivity;
import cn.nd.social.syncbrowsing.meeting.activity.ClientPageFrameLayout;
import cn.nd.social.syncbrowsing.meeting.activity.HostLayoutDelegate;
import cn.nd.social.syncbrowsing.meeting.activity.HostPageFrameLayout;
import cn.nd.social.syncbrowsing.ui.HostSyncActivity;
import cn.nd.social.syncbrowsing.ui.SyncBrowserDialogActivity;

import com.nd.voice.chatroom.EnterRoomActivity;
import com.nd.voice.chatroom.MultiTalkSettingView;
import com.nd.voice.chatroom.MultiTalkSettingView.InterfaceForSetting;
import com.nd.voice.meetingroom.activity.MeetingDetailActivity;
import com.nd.voice.meetingroom.manager.MeetingDetailEntity;
import com.nd.voice.meetingroom.manager.MeetingEntity;
import com.nd.voice.meetingroom.manager.User;
import com.nd.voice.meetingroom.manager.UserManagerApi;

public class MultiTalk extends Activity implements
		VoiceEndpoint.ConferenceCallback, InterfaceForSetting,
		SensorEventListener, HostLayoutDelegate {

	private Button mBackBtn;
	private Button mMoreBtn;
	private FrameLayout mDrawBg;
	private FrameLayout mBigLayout;
	private FrameLayout mBottomLayout;
	private ImageView mUserImg;
	private ImageView mSpeakBtn;
	private AnimationDrawable animationDrawable;
	private MultiTalkSettingView mSettingView;
	private ImageView mBlurView;
	private FrameLayout mScreenShot;
	private TextView mTitleTV;
	private HostPageFrameLayout mHostPageFrament;
	private ClientPageFrameLayout mClientPageFrament;
	private ViewGroup rootView;
	
	private View toolBar;

	private SoundPool spOn;
	private SoundPool spOff;
	private int musicOn;
	private int musicOff;

	private int mScreenWidth;
	private int mScreenHeight;
	private int center_x;
	private int center_y;

	private String mRoom = null;
	private String mode = null;
	private boolean isSettingShow = false;

	private static HashMap<Long, UsrConnectInRoom> mUsrInRoom = new HashMap<Long, UsrConnectInRoom>();
	private long mLocalUsr;

	private int userIndex = 0;
	private int userCount = 0;
	private int[] indexArr;
	private static int[] srcArr = { R.drawable.face1, R.drawable.face2,
			R.drawable.face3, R.drawable.face4 };
	public static MultiTalk mMultiAct;

	private int mFaceId = 0;
	private boolean mMeetingMode = false;
	private boolean mIsMeetingHost = false;
	private long[] userIdArray = null;
	private UserManagerApi mUserManager;

	private WakeLock mWakeLock;
	
	private boolean isTalking;
	
	private boolean mIsSyncShowing = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		setContentView(R.layout.multi_talk);

		rootView = (ViewGroup)findViewById(R.id.mrootlayout);
		
		indexArr = new int[MAX_MEMBER_SIZE];

		Intent intent = getIntent();
		mRoom = intent.getStringExtra("room_name");
		mode = intent.getStringExtra("mode");
		if (mode != null && mode.equals("meeting")) {
			mMeetingMode = true;
		}
		if (mMeetingMode) {
			initMeeting(intent);
		}

		// mLocalUsr = TabLauncherUI.VOICE_UID;
		mLocalUsr = mUserManager.getMyInfo().getUserid();

		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		mScreenWidth = metrics.widthPixels;
		mScreenHeight = metrics.heightPixels;
		center_x = mScreenWidth / 2;
		center_y = (int) (mScreenHeight * 4 / 7);

		setupView();
		addLocalInfoView();
		setupEvent();

		VoiceEndpoint.api().setLoudspeaker(mRoom, true);
		VoiceEndpoint.api().setPlayoutMute(mRoom, true);
		VoiceEndpoint.api().setRecordingMute(mRoom, true);

		if ("meeting".equals(mode)) {
			if (MeetingDetailActivity.index != 0) {
				for (int i = 0; i < MeetingDetailActivity.index; i++) {
					onConferenceMemberEnter(MeetingDetailActivity.usrHold[i]);
				}
			}
			mTitleTV.setText(R.string.meetingroom);
		} else {
			if (EnterRoomActivity.index != 0) {
				for (int i = 0; i < EnterRoomActivity.index; i++) {
					onConferenceMemberEnter(EnterRoomActivity.usrHold[i]);
				}
			}
			mTitleTV.setText(R.string.talkbar);
		}
		initAudioManager();

		super.onCreate(savedInstanceState);

		/**
		 * put mMultiAct on this place is to let MultiTalk class ready for load
		 * users
		 */
		mMultiAct = this;
		VoiceEndpoint.api().setPlayoutMute(mRoom, false);
	}

	private SyncEnterReceiver mSyncEnterReceiver;

	private void initMeeting(Intent intent) {
		mFaceId = intent.getIntExtra("faceres", 0);
		mIsMeetingHost = intent.getBooleanExtra("ishost", false);

		// userIdArray = intent.getLongArrayExtra("idarray");
		mUserManager = new UserManager();
		mSyncEnterReceiver = new SyncEnterReceiver(this,
				MeetingDetailEntity.getMeetingIdByUid(mRoom));
		registerReceiver(mSyncEnterReceiver,
				mSyncEnterReceiver.getIntentFilter());
	}

	public void addLocalInfoView() {

		for (int i = 0; i < 4; i++) {
			ConnectUserView temp = new ConnectUserView(this, i);
			mBottomLayout.addView(temp);
		}

		mUserImg = new ImageView(this);
		int usrCircle = (int) getResources().getDimension(R.dimen.localusr_r);
		int x = ((int) (mLocalUsr & 0x3)) % 4;
		int faceRes = srcArr[x];

		// TODO: change implementation
		if (mMeetingMode && mFaceId != 0) {
			faceRes = mFaceId;
		}

		/*
		 * Bitmap userBp = initImageToCircle(faceRes, true);
		 * mUserImg.setImageBitmap(userBp); mBottomLayout.addView(mUserImg);
		 * MarginLayoutParams mgParams = (MarginLayoutParams) mUserImg
		 * .getLayoutParams(); mgParams.height = usrCircle * 2; mgParams.width =
		 * usrCircle * 2; mgParams.setMargins(center_x - usrCircle, center_y -
		 * usrCircle, 0, 0); mUserImg.setLayoutParams(mgParams);
		 */

		mSpeakBtn = new ImageView(this);
		mSpeakBtn.setBackgroundColor(Color.TRANSPARENT);
		mSpeakBtn.setImageResource(R.drawable.mic_normal);

		mBottomLayout.addView(mSpeakBtn);
		MarginLayoutParams speakParam = (MarginLayoutParams) mSpeakBtn
				.getLayoutParams();
		speakParam.height = usrCircle * 3;
		speakParam.width = usrCircle * 3;
		speakParam.setMargins(center_x - speakParam.height / 2, center_y
				- speakParam.width / 2, 0, 0);
		// speakParam.setMargins(center_x + usrCircle / 2, center_y + usrCircle
		// /2, 0, 0);
		mSpeakBtn.setLayoutParams(speakParam);
		mSpeakBtn.setTag(false);

	}

	public void setupView() {
		mTitleTV = (TextView) findViewById(R.id.multi_talk_title);
		mBackBtn = (Button) findViewById(R.id.multi_talk_reback_btn);
		mMoreBtn = (Button) findViewById(R.id.multi_talk_more);
		mDrawBg = (FrameLayout) findViewById(R.id.multi_talk_frame_draw);
		mBigLayout = (FrameLayout) findViewById(R.id.multi_talk_frame);
		mBottomLayout = (FrameLayout) findViewById(R.id.multi_talk_frame_bottom);

		mSettingView = (MultiTalkSettingView) findViewById(R.id.multi_talk_set);
		mScreenShot = (FrameLayout) findViewById(R.id.multi_talk_screenshot);
		mBlurView = (ImageView) findViewById(R.id.multi_talk_blur_view);
		mBlurView.setVisibility(View.INVISIBLE);

		spOn = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);
		musicOn = spOn.load(this, R.raw.on, 1);
		spOff = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);
		musicOff = spOff.load(this, R.raw.off, 1);

		if (mMeetingMode && !mIsMeetingHost) {
			mMoreBtn.setVisibility(View.GONE);
		}
		
		toolBar = findViewById(R.id.rl_top_tool);
	}

	public Bitmap initImageToCircle(int imgId, boolean isInRoom) {
		Resources res = getResources();
		Bitmap bmp = BitmapFactory.decodeResource(res, imgId);
		Bitmap bp = UtilsForMultiTalk.toRoundBitmap(bmp, isInRoom);
		return bp;
	}

	private void finiMeeting() {
		unregisterReceiver(mSyncEnterReceiver);
	}

	@Override
	protected void onDestroy() {
		if (mMeetingMode) {
			finiMeeting();
		}
		mMultiAct = null;
		VoiceEndpoint.leave(mRoom);
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		if (isSettingShow) {
			Animation transDown = AnimationUtils.loadAnimation(this,
					R.anim.push_bottom_out);
			mSettingView.startAnimation(transDown);
			mSettingView.setVisibility(View.INVISIBLE);
			mBlurView.setVisibility(View.INVISIBLE);
			isSettingShow = false;
		} else if(mIsSyncShowing) {
			if(mHostPageFrament != null) {
				mHostPageFrament.exitHint();
			} else if(mClientPageFrament != null) {
				hideClientPageFrameLayout();
			}
		} else {
			if(mClientPageFrament != null) {
				mClientPageFrament.release();
			}
			if(mHostPageFrament != null) {
				mHostPageFrament.release();
			}
			mMultiAct = null;
			VoiceEndpoint.leave(mRoom);
			finish();
		}
	}

	private void showSyncBrowSerAcicty() {
		Intent intent = new Intent(MultiTalk.this,
				SyncBrowserDialogActivity.class);
		startActivityForResult(intent, RESULT_FIRST_USER);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == RESULT_FIRST_USER) {
			if (resultCode == RESULT_OK) {
				String filePath = data
						.getStringExtra(HostSyncActivity.FILE_ID_KEY);
				showHostPageFrameLayout(filePath);
			}
		}
	}

	private void showHostPageFrameLayout(String filePath) {
		mIsSyncShowing = true;
		if(mHostPageFrament == null){
			mHostPageFrament = new HostPageFrameLayout(this);
			mHostPageFrament.setDelegate(this);
			rootView.addView(mHostPageFrament);
			mHostPageFrament.setmMeetingId(MeetingEntity.getMeetingIdByUid(mRoom));
			if(filePath != null){
				mHostPageFrament.setmPath(filePath);
			}
			mHostPageFrament.showDoc();
			//这样点击事件就不会透到下一层view
			mHostPageFrament.setOnClickListener(new View.OnClickListener() {				
				@Override
				public void onClick(View v) {
					
				}
			});
		}else{
			Animation animation = AnimationUtils.loadAnimation(this, R.anim.push_bottom_in);
			animation.setAnimationListener(new AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation arg0) {
					// TODO Auto-generated method stub
					mHostPageFrament.setVisibility(View.VISIBLE);
				}
				
				@Override
				public void onAnimationRepeat(Animation arg0) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onAnimationEnd(Animation arg0) {
					// TODO Auto-generated method stub
					toolBar.setVisibility(View.GONE);
				}
			});
			mHostPageFrament.startAnimation(animation);
		}
		
	}
	
	private void hideHostPageFrameLayout(){
		mIsSyncShowing = false;
		if(mHostPageFrament != null){
			Animation animation = AnimationUtils.loadAnimation(this, R.anim.push_bottom_out);
			animation.setAnimationListener(new AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation arg0) {
					// TODO Auto-generated method stub
					toolBar.setVisibility(View.VISIBLE);
				}
				
				@Override
				public void onAnimationRepeat(Animation arg0) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onAnimationEnd(Animation arg0) {
					// TODO Auto-generated method stub
					mHostPageFrament.setVisibility(View.GONE);
				}
			});
			mHostPageFrament.startAnimation(animation);
		}
	}
	
	private void closeHostPageFrameLayout(){
		
		mIsSyncShowing = false;
		Animation animation = AnimationUtils.loadAnimation(this, R.anim.push_bottom_out);
		animation.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				toolBar.setVisibility(View.GONE);
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				rootView.removeView(mHostPageFrament);
				mHostPageFrament = null;
			}
		});
		
		mHostPageFrament.startAnimation(animation);
	}

	private static Bitmap takeScreenShot(Activity activity) {
		// View是你需要截图的View
		View view = activity.getWindow().getDecorView();
		view.setDrawingCacheEnabled(true);
		view.buildDrawingCache();
		Bitmap b1 = view.getDrawingCache();

		// 获取状态栏高度
		Rect frame = new Rect();
		activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
		int statusBarHeight = frame.top;

		// 获取屏幕长和�?
		int width = activity.getWindowManager().getDefaultDisplay().getWidth();
		int height = activity.getWindowManager().getDefaultDisplay()
				.getHeight();
		// 去掉标题�?
		// Bitmap b = Bitmap.createBitmap(b1, 0, 25, 320, 455);
		Bitmap b = Bitmap.createBitmap(b1, 0, statusBarHeight, width, height
				- statusBarHeight);
		view.destroyDrawingCache();
		return b;
	}

	private void cutWindow() {
		getWindow().getDecorView().setDrawingCacheEnabled(true);

		Bitmap bmp = getWindow().getDecorView().getDrawingCache();

		Rect rect = new Rect();

		View view = getWindow().getDecorView();
		view.getWindowVisibleDisplayFrame(rect);

		int statusBarHeight = rect.top;

		int wintop = getWindow().findViewById(android.R.id.content).getTop();

		int titleBarHeight = wintop - statusBarHeight;

	}

	private static int[] CIRCLE_RADIUS = { R.dimen.circle_radius_1,
			R.dimen.circle_radius_2, R.dimen.circle_radius_3,
			R.dimen.circle_radius_4 };

	public class ConnectUserView extends View {

		private int which;

		public ConnectUserView(Context context, int w) {
			super(context);
			this.which = w;
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);

			Paint p = new Paint();
			p.setStyle(Paint.Style.STROKE);
			p.setStrokeWidth(2);
			p.setColor(Color.WHITE);
			p.setAlpha(50);

			int r = (int) getResources().getDimension(CIRCLE_RADIUS[which]);
			canvas.drawCircle(center_x, center_y, r, p);
		}

	}

	private void cleanConnector() {
		for (int i = 0; i < mDrawBg.getChildCount(); i++) {
			mDrawBg.removeViewAt(i);
		}
	}

	private void removeSingleConnector(View view) {
		mDrawBg.removeView(view);
	}

	public int rCircle;

	public class UsrConnectInRoom {
		int indexOfWhich;
		int x;
		int y;
		int x_talk;
		View usrView;
		View talkView;
		View nameView;
	}

	private static int MAX_MEMBER_SIZE = 8;
	static double[] portion = { -0.866, -0.5, 0.866, -0.5, -0.5, -0.866, 0.5,
			-0.866, -0.423, -0.906, 0.423, -0.906, -0.342, -0.940, 0.342,
			-0.940 };

	private Point getOffsetPoint(int which) {
		int index = which / 2;
		int circleR = (int) getResources().getDimension(CIRCLE_RADIUS[index]);
		Point point = new Point();
		point.x = (int) (circleR * portion[which * 2]);
		point.y = (int) (circleR * portion[which * 2 + 1]);
		return point;
	}

	// draw usr
	public UsrConnectInRoom drawConnector(ImageView mImg, int which, int srcId,
			long uid) {

		int x, y, z;

		rCircle = (int) getResources().getDimension(R.dimen.little_circle_r);

		/*
		 * int circleR = (int)
		 * getResources().getDimension(CIRCLE_RADIUS[index]); // int degree =
		 * UtilsForMultiTalk.getAngleByRandom(R, mScreenWidth); // degree =
		 * (int) (degree * Math.PI / 180);
		 * 
		 * int degree = 45;
		 * 
		 * double cosV = circleR * Math.cos(degree); double sinV = circleR *
		 * Math.sin(degree);
		 */

		int viewTalkHeight = (int) getResources().getDimension(
				R.dimen.connector_energy_bar_height);
		int viewTalkWidth = (int) getResources().getDimension(
				R.dimen.connector_energy_bar_width);

		ImageView viewTalk = new ImageView(this);
		if (which % 2 == 0) {
			x = (int) (center_x + getOffsetPoint(which).x) - rCircle;
			y = (int) (center_y + getOffsetPoint(which).y) - rCircle;
			z = x + 2 * rCircle;
			viewTalk.setImageDrawable(getResources().getDrawable(
					R.drawable.talk_energy_bar));
		} else {
			x = (int) (center_x + getOffsetPoint(which).x) - rCircle;
			y = (int) (center_y + getOffsetPoint(which).y) - rCircle;
			z = x - viewTalkWidth;
			viewTalk.setImageDrawable(getResources().getDrawable(
					R.drawable.talk_energy_bar));
		}

		UsrConnectInRoom usr = new UsrConnectInRoom();
		usr.indexOfWhich = which;
		usr.x = x;
		usr.y = y;
		usr.x_talk = z;

		if (mImg == null) {
			mImg = new ImageView(this);
		}
		mImg.setScaleType(ScaleType.CENTER_INSIDE);
		mImg.setImageBitmap(initImageToCircle(srcId, true));
		mImg.setTag(which);
		mDrawBg.addView(mImg);
		MarginLayoutParams mgParams = (MarginLayoutParams) mImg
				.getLayoutParams();
		mgParams.height = rCircle * 2;
		mgParams.width = rCircle * 2;
		mgParams.setMargins(x, y, 0, 0);
		mImg.setLayoutParams(mgParams);

		mDrawBg.addView(viewTalk);
		MarginLayoutParams viewTalkParams = (MarginLayoutParams) viewTalk
				.getLayoutParams();

		viewTalkParams.height = viewTalkHeight;
		viewTalkParams.width = viewTalkWidth;
		viewTalkParams.setMargins(z, y + mgParams.height - viewTalkHeight - 10,
				0, 0);
		viewTalk.setLayoutParams(viewTalkParams);

		if (mMeetingMode) {
			User user = mUserManager.getUserInfoLocal(uid);
			if (user != null && user.getUserName() != null) {
				TextView tvName = new TextView(this);
				tvName.setText(user.getNickName());
				tvName.setGravity(Gravity.CENTER);
				mDrawBg.addView(tvName);
				MarginLayoutParams nameParam = (MarginLayoutParams) tvName
						.getLayoutParams();
				nameParam.height = (int) getResources().getDimension(
						R.dimen.connector_name_textview_height);
				nameParam.width = (int) getResources().getDimension(
						R.dimen.connector_name_textview_width);

				nameParam.setMargins(
						x + (mgParams.width - nameParam.width) / 2, y
								+ mgParams.height, 0, 0);
				tvName.setLayoutParams(nameParam);
				usr.nameView = tvName;
			}
		}
		usr.usrView = mImg;
		usr.talkView = viewTalk;
		mUsrInRoom.put(uid, usr);

		return usr;
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		mSensorManger.registerListener(this, mSensor,
				SensorManager.SENSOR_DELAY_NORMAL);
		PowerManager pManager = ((PowerManager) getSystemService(POWER_SERVICE));
		mWakeLock = pManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
				| PowerManager.ON_AFTER_RELEASE, "lock");
		mWakeLock.acquire();
		super.onResume();
	}

	public void setupEvent() {

		mSettingView.setmListener(this);
		mBackBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				mMultiAct = null;
				VoiceEndpoint.leave(mRoom);
				finish();
			}
		});

		mMoreBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if ("meeting".equals(mode)) {
					// TODO Auto-generated method stub
					final PopMenu menu = new PopMenu(MultiTalk.this);
					menu.addItem(new PopMenuItem(0,
							getString(R.string.sync_read), R.drawable.icon_file));
					menu.setOnItemClickListener(new OnItemClickListener() {
						@Override
						public void onItemClick(AdapterView<?> adapterView,
								View arg1, int position, long arg3) {
							PopMenuItem item = (PopMenuItem) adapterView
									.getItemAtPosition(position);
							menu.dismiss();
							showSyncBrowSerAcicty();
						}
					});
					menu.showAsDropDown(mMoreBtn);
				} else {
					if (!isSettingShow) {
						// final View content =
						// getWindow().findViewById(android.R.id.content).getRootView();
						//
						// if (content.getWidth() > 0) {
						// Bitmap image = BlurBuilder.blur(content);
						// getWindow().setBackgroundDrawable(new
						// BitmapDrawable(getResources(), image));
						// } else {
						// content.getViewTreeObserver().addOnGlobalLayoutListener(new
						// OnGlobalLayoutListener() {
						// @Override
						// public void onGlobalLayout() {
						// Bitmap image = BlurBuilder.blur(content);
						// getWindow().setBackgroundDrawable(new
						// BitmapDrawable(getResources(), image));
						// }
						// });
						// }

						// Bitmap bmp = BlurBuilder.blur(mScreenShot);
						// mBlurView.setImageBitmap(bmp);
						// mBlurView.setVisibility(View.VISIBLE);
						mSettingView.setVisibility(View.VISIBLE);
						Animation transUp = AnimationUtils.loadAnimation(
								MultiTalk.this, R.anim.push_bottom_in);
						mSettingView.startAnimation(transUp);
						isSettingShow = true;
					} else {
						Animation transDown = AnimationUtils.loadAnimation(
								MultiTalk.this, R.anim.push_bottom_out);
						mSettingView.startAnimation(transDown);
						mSettingView.setVisibility(View.INVISIBLE);
						mBlurView.setVisibility(View.INVISIBLE);
						isSettingShow = false;
					}
				}

			}
		});

		mSpeakBtn.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				// TODO Auto-generated method stub
				if(event.getAction() == MotionEvent.ACTION_DOWN){
					startSpeak();
				}else if(event.getAction() == MotionEvent.ACTION_UP 
						|| event.getAction() == MotionEvent.ACTION_CANCEL){
					stopSpeak();
				}
				return true;
			}
		});
		
//		mSpeakBtn.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View arg0) {
//
//				boolean m = (Boolean) mSpeakBtn.getTag();
//				if (!m) {
//					startSpeak();
//				} else {
//					stopSpeak();
//				}
//			}
//		});
		
		toolBar.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if(mClientPageFrament != null) {
					showClientPageFrameLayout(false, null);
				} else {
					showHostPageFrameLayout(null);
				}
			}
		});
	}

	public int getConnectCount() {
		return 0;
	}

	@Override
	public void onJoinConference(boolean success) {
		if (success) {
			Bitmap userBp = initImageToCircle(R.drawable.face1, true);
			mUserImg.setImageBitmap(userBp);
		}

	}

	@Override
	public void onLeaveConference() {

		Bitmap userBp = initImageToCircle(R.drawable.face1, true);
		mUserImg.setImageBitmap(userBp);

		mUsrInRoom.clear();
		cleanConnector();
	}

	@Override
	public void onConferenceMemberEnter(long uid) {
		if (mLocalUsr != uid && !mUsrInRoom.containsKey(uid)) {
			
			ImageView v = new ImageView(this);
			int i = ((int) (uid & 0x3)); // user head will be uniform
			int faceRes = srcArr[i];
			userCount = mUsrInRoom.size();
			if (userCount >= MAX_MEMBER_SIZE) {
				Toast.makeText(MultiTalk.this, "Room is full of people",
						Toast.LENGTH_SHORT).show();
				return;
			}
			userIndex = getIndexOfWhich(userCount);
			if (mMeetingMode) {
				User user = mUserManager.getUserInfoLocal(uid);
				if (user != null) {
					faceRes = user.getDefaultFaceResource();
				}
			}
			UsrConnectInRoom u = drawConnector(v, userIndex, faceRes, uid);
			mUsrInRoom.put(uid, u);
			indexArr[userIndex] = 1;
			userCount = mUsrInRoom.size();

		} else {
			//self comming
		}

	}

	private int getIndexOfWhich(int userCount) {

		if (indexArr[userCount] == 0) {
			return userCount;
		} else {

			for (int i = 0; i < indexArr.length; i++) {
				if (indexArr[i] == 0) {
					return i;
				}
			}
		}
		return 0;
	}

	@Override
	public void onConferenceMemberLeave(long uid) {

		if (mLocalUsr != uid) {
			if (mUsrInRoom.containsKey(uid)) {
				UsrConnectInRoom u = mUsrInRoom.get(uid);
				removeSingleConnector(u.usrView);
				removeSingleConnector(u.talkView);
				if (u.nameView != null) {
					removeSingleConnector(u.nameView);
				}
				indexArr[u.indexOfWhich] = 0;
				mUsrInRoom.remove(uid);
			}

		} else {
		}

	}

	@Override
	public void onConferenceMediaOption(int cmd, int value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConferenceAudioEnergy(long uid, int energy) {

		if (mLocalUsr != uid) {
			if (energy > 2) {
				if (mUsrInRoom.containsKey(uid)) {
					usrTalkStart(uid);
				}
			} else {
				if (mUsrInRoom.containsKey(uid)) {
					usrTalkStop(uid);
				}
			}
		}

	}

	public void startSpeak() {
		if(!isTalking){
			mSpeakBtn.setTag(true);
			VoiceEndpoint.api().setRecordingMute(mRoom, false);
			spOn.play(musicOn, 1, 1, 0, 0, 1);
			mSpeakBtn.setImageResource(R.drawable.mic_speaking);
			isTalking = true;
		}
		
	}

	public void stopSpeak() {
		mSpeakBtn.setTag(false);
		VoiceEndpoint.api().setRecordingMute(mRoom, true);
		spOff.play(musicOff, 1, 1, 0, 0, 1);
		mSpeakBtn.setImageResource(R.drawable.mic_normal);
		isTalking = false;
	}

	public void usrTalkStart(long uid) {
		ImageView v = (ImageView) mUsrInRoom.get(uid).talkView;
		AnimationDrawable anim = (AnimationDrawable) v.getDrawable();
		anim.start();
	}

	public void usrTalkStop(long uid) {
		ImageView v = (ImageView) mUsrInRoom.get(uid).talkView;
		AnimationDrawable anim = (AnimationDrawable) v.getDrawable();
		anim.stop();
		anim.selectDrawable(0);
	}

	@Override
	public void onHonkListener(boolean isSelected) {
		// TODO Auto-generated method stub
		VoiceEndpoint.api().setLoudspeaker(mRoom, isSelected);
		VoiceEndpoint.api().setPlayoutMute(mRoom, isSelected);
	}

	@Override
	public void onManVoiceListener() {
		VoiceEndpoint.api().voiceChange(mRoom, 0);
	}

	@Override
	public void onWomanVoiceListener() {
		VoiceEndpoint.api().voiceChange(mRoom, 1);
	}

	@Override
	public void onMosterVoiceListener() {
		VoiceEndpoint.api().voiceChange(mRoom, 2);
	}

	@Override
	public void onRobertVoiceListener() {
		VoiceEndpoint.api().voiceChange(mRoom, 3);
	}

	@Override
	public void onVoiceSeekBarChange(int progress) {
		// VoiceEndpoint.api().setSpeakerVolume(mRoom, progress);
	}

	@Override
	public void onGainSeekBarChange(int progress) {
		// VoiceEndpoint.api().setAudioGain(mRoom, progress);
	}

	@Override
	public void onSetUid(long uid, boolean success) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDisconnect() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDebug(String d) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	private AudioManager mAudioManager;
	private SensorManager mSensorManger;
	private Sensor mSensor;
	private int mOldVolume;

	private void initAudioManager() {
		mAudioManager = (AudioManager) this
				.getSystemService(Context.AUDIO_SERVICE);
		mSensorManger = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mSensor = mSensorManger.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		// VoiceEndpoint.api().setSpeakerVolume(mRoom, 200);
		// VoiceEndpoint.api().setAudioGain(mRoom, 350);
		mOldVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
	}

	@Override
	protected void onPause() {
		mSensorManger.unregisterListener(this);
		super.onPause();
		if (null != mWakeLock) {
			mWakeLock.release();
		}
	}

	private void setHonkEnable(boolean isEnable) {
		VoiceEndpoint.api().setLoudspeaker(mRoom, isEnable);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		float range = event.values[0];

		if (range == mSensor.getMaximumRange()) {
			setHonkEnable(true);
			Toast.makeText(this, "正常模式", Toast.LENGTH_LONG).show();
		} else {
			setHonkEnable(false);
			Toast.makeText(this, "听筒模式", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		boolean handled = true;

		AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		int currentVolume = mAudioManager
				.getStreamVolume(AudioManager.STREAM_MUSIC);

		switch (keyCode) {
		case KeyEvent.KEYCODE_VOLUME_UP:// 音量增大
			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
					currentVolume + 1, 1);
			break;
		case KeyEvent.KEYCODE_VOLUME_DOWN:// 音量减小
			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
					currentVolume - 1, 1);
			break;

		case KeyEvent.KEYCODE_BACK:// 返回键
			onBackPressed();// jniOnCallCppEvent();
			return true;
			// return super.onKeyDown(keyCode, event);

		default:
			handled = false;
			break;
		}

		return handled;
		// return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onUpAction() {
		// TODO Auto-generated method stub
		showHostPageFrameLayout(null);
	}
	@Override
	
	public void onDownAction() {
		// TODO Auto-generated method stub
		hideHostPageFrameLayout();
	}

	@Override
	public void onCloseAction() {
		// TODO Auto-generated method stub
		closeHostPageFrameLayout();
	}
	
	
	public class SyncEnterReceiver extends BroadcastReceiver {		
		
		Context mContext;
		String mMeetingId;
		boolean mHasEnterSync = false;	
		

		public SyncEnterReceiver(Context context,String meetingId) {
			mContext = context;
			mMeetingId = meetingId;
		}

		public IntentFilter getIntentFilter() {
			IntentFilter filter = new IntentFilter(SyncMsgFactory.ACTION_RECV_SYNC_ENTER);
			return filter;
		}
		
		public void onExitSync() {
			mHasEnterSync = false;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(SyncMsgFactory.ACTION_RECV_SYNC_ENTER)) {
				String meetingId = intent.getStringExtra("meetingid");
				if(mMeetingId != null && mMeetingId.equals(meetingId)) {
	/*				if(!mHasEnterSync) {
						mHasEnterSync = true;*/
						boolean hasExtra = intent.getBooleanExtra("hasextra",false);						
						Bundle extras = null;
						if(hasExtra) {
							extras = intent.getExtras();							
						}
						showClientPageFrameLayout(hasExtra, extras)	;	
	/*				}*/
				}
			}
		}

	}
	
	private ClientLayoutDelegate mClientSyncDelegate = new ClientLayoutDelegate() {
		
		@Override
		public void onUpAction() {
			showClientPageFrameLayout(false,null);			
		}
		
		@Override
		public void onDownAction() {
			hideClientPageFrameLayout();			
		}
		
		@Override
		public void onCloseAction() {
			closeClientPageFrameLayout();			
		}
	};
	
	
	private void showClientPageFrameLayout(boolean hasExtra,Bundle extras) {
		
		mIsSyncShowing = true;
		
		if(mClientPageFrament == null){
			mClientPageFrament = new ClientPageFrameLayout(this);
			rootView.addView(mClientPageFrament);
			mClientPageFrament.setDelegate(mClientSyncDelegate);
			mClientPageFrament.laterInit(hasExtra, extras);
			//这样点击事件就不会透到下一层view
			mClientPageFrament.setOnClickListener(new View.OnClickListener() {				
				@Override
				public void onClick(View v) {
					
				}
			});
		}else{
			Animation animation = AnimationUtils.loadAnimation(this, R.anim.push_bottom_in);
			animation.setAnimationListener(new AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation arg0) {
					// TODO Auto-generated method stub
					mClientPageFrament.setVisibility(View.VISIBLE);
				}
				
				@Override
				public void onAnimationRepeat(Animation arg0) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onAnimationEnd(Animation arg0) {
					// TODO Auto-generated method stub
					toolBar.setVisibility(View.GONE);
				}
			});
			mClientPageFrament.startAnimation(animation);
		}
		
	}
	
	private void hideClientPageFrameLayout(){
		
		mIsSyncShowing = false;
		
		if(mClientPageFrament != null){
			Animation animation = AnimationUtils.loadAnimation(this, R.anim.push_bottom_out);
			animation.setAnimationListener(new AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation arg0) {
					// TODO Auto-generated method stub
					toolBar.setVisibility(View.VISIBLE);
				}
				
				@Override
				public void onAnimationRepeat(Animation arg0) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onAnimationEnd(Animation arg0) {
					// TODO Auto-generated method stub
					mClientPageFrament.setVisibility(View.GONE);
				}
			});
			mClientPageFrament.startAnimation(animation);
		}
	}
	
	private void closeClientPageFrameLayout(){
		
		mIsSyncShowing = false;
		if(mClientPageFrament == null) {
			return;
		}
		mClientPageFrament.release();
		
		Animation animation = AnimationUtils.loadAnimation(this, R.anim.push_bottom_out);
		animation.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				toolBar.setVisibility(View.GONE);
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				rootView.removeView(mClientPageFrament);
				mClientPageFrament = null;
			}
		});
		
		mClientPageFrament.startAnimation(animation);
	}

}
