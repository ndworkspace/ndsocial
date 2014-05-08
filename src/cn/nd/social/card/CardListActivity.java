package cn.nd.social.card;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import cn.nd.social.R;
import cn.nd.social.account.CAConstant;
import cn.nd.social.account.CloundServer;
import cn.nd.social.card.CardUtil.CardData;
import cn.nd.social.card.CardUtil.CardDataPacker;
import cn.nd.social.card.flipview.CardFlipActivity;
import cn.nd.social.common.FlyAnimation;
import cn.nd.social.common.PopMenu;
import cn.nd.social.common.PopMenuItem;
import cn.nd.social.common.RecordAudioThread;
import cn.nd.social.contacts.manager.ContactDBHelper;
import cn.nd.social.contacts.manager.ContactManager;
import cn.nd.social.contacts.manager.ContactManagerCallBack;
import cn.nd.social.contacts.manager.ImportContact;
import cn.nd.social.contacts.manager.ImportContact.ImportContactCallBack;
import cn.nd.social.contacts.manager.MemberContact;
import cn.nd.social.data.CardProvider;
import cn.nd.social.sendfile.SendFilesActivity;
import cn.nd.social.ui.controls.HFGridView;
import cn.nd.social.ui.controls.RadarAnimalLayout;
import cn.nd.social.util.AudioDataPacker;
import cn.nd.social.util.DataFactory;
import cn.nd.social.util.FilePathHelper;
import cn.nd.social.util.Utils;

import com.example.ofdmtransport.Modulation;

public class CardListActivity extends Activity implements OnCompletionListener, ContactManagerCallBack {
	CardListQueryHandler mQueryHandler;
	private HFGridView mGrid;
	private View mEmptyView;
	private CardGridAdapter mAdapter;
	private CardViewLayout mMyCard;
	private View mHeadView;
	private Context mContext;
	private boolean mMassiveImport = false;
	////////////////////////////////////
	public int mMultiSelectedMode = 0;
	private CheckBox mCkbx;
	private View mRightBtn;	
	private ImageView mFlyCardView;
	private ViewGroup mOutCardLayout;
	private View mBackBtn;
	private FrameLayout layout_teach;
	
	private SharedPreferences mPrefs;
	
	private final static String TAG = "CardListActivity";
	private final static String FIRSTSHOWFLAG = "FIRSTSHOWFLAG";
	
	public final static int CARD_QUERY_TOKEN_ALL = 1000;
	public final static int CARD_QUERY_TOKEN_SINGLE = 1001;
	public final static int EVENT_SOUND_WAVE_MSG = 1003;
	
	private final static int WIDTH_BASE = 3;
	private final static int HEIGHT_BASE = 2;
	
	private final static int MENU_ITEM_IMPORT_CONTACTS = 100;
	private final static int MENU_ITEM_MULTI_SELECT = 101;
	private final static int MENU_ITEM_MULTI_SEND = 102;
	private final static int MENU_ITEM_MULTI_DEL = 103;
	
	private int mMyCardWidth;
	private int mMyCardHeight;
	
	private static SparseArray<Boolean> sMultiMap;
	
	private AnimationSet mAnimInteract;
	
	private Button btn_reserve;
	
	private RadarAnimalLayout laout_radar;
	
	private ContactManager mContactManager;


	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case CAConstant.APP_EVENT_DEL_FRIEND_RSP:
				dismissProgressDialog();
				if (msg.arg1 != 0) {
					Toast.makeText(mContext, "network delete error",
							Toast.LENGTH_SHORT).show();
				}
				break;
			case EVENT_REPEAT_PLAY:
				if(mMediaPlayer != null) {
					mMediaPlayer.start();
				}
				break;
			case EVENT_RECORD_RESUME_DELAY:				
				Log.e("PLAY audio","onCompletion post stop");
				break;
			case EVENT_SOUND_WAVE_MSG:
				handleSoundWaveMsg((String)msg.obj);
				Log.e("PLAY audio","onCompletion post stop");
				break;
			default:
				break;
			}
		}
	};


    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.card_list_activity);
        
        sMultiMap = new SparseArray<Boolean>();
        mAnimInteract = (AnimationSet)AnimationUtils.loadAnimation(this, R.anim.tool_open);
        

        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);     
        ////////////////////////////////////////////////
        sMultiMode = false;
      
        ////////////////////////////////////////////////
        setupViews();
        registerEvent();
        mQueryHandler = new CardListQueryHandler(mContext.getContentResolver()); 
        startAsyncQuery();	/**get data*/
    }
    
    
    
	@Override
	public void onResume() {
		super.onResume();
	}
    
	private void setupViews() {
		mBackBtn = findViewById(R.id.back_btn);
		mRightBtn = findViewById(R.id.tl_right_btn);
		mOutCardLayout = (FrameLayout) findViewById(R.id.lauche_layout);
		mGrid = (HFGridView) findViewById(R.id.gridid);
		mEmptyView = findViewById(R.id.empty);
		mFlyCardView = (ImageView) findViewById(R.id.fly_card_img);
		btn_reserve = (Button) findViewById(R.id.btn_reserve);
		layout_teach = (FrameLayout) findViewById(R.id.layout_teach);

		mHeadView = getLayoutInflater().inflate(R.layout.main_tab_mycard, null);
		
		laout_radar = (RadarAnimalLayout)mHeadView.findViewById(R.id.layout_radar);
		mMyCard = (CardViewLayout) mHeadView.findViewById(R.id.customize_view);
		mCkbx = (CheckBox) mHeadView.findViewById(R.id.mycard_ckbx);
		mCkbx.setChecked(false);
		
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		int screenWidth = metrics.widthPixels;
		int margin = (int)(getResources().getDimension(R.dimen.mycard_thumb_side_margin) 
				+ getResources().getDimension(R.dimen.card_list_grid_space));
		mMyCardWidth = screenWidth - 2 * margin;
		mMyCardHeight = mMyCardWidth *  HEIGHT_BASE / WIDTH_BASE;

		ViewGroup.LayoutParams params = mMyCard.getLayoutParams();
		params.height = mMyCardHeight;
		int gridSpace = (int) getResources().getDimension(
				R.dimen.card_list_grid_space);
		mGrid.setColumnWidth((mMyCardWidth - gridSpace) / 2);
		mGrid.setNumColumns(2);
		mGrid.setVerticalSpacing(gridSpace);
		mGrid.setHorizontalSpacing(gridSpace);
		// mGrid.setEmptyView(mEmptyView);/**set empty view will cause always
		// show empty view*/
		mGrid.addHeaderView(mHeadView);
		boolean flag = mPrefs.getBoolean(FIRSTSHOWFLAG, true);
		if(flag){
			layout_teach.setVisibility(View.VISIBLE);
		}
	}
    
    
    private void registerEvent() {
    	
    	initGridAdapter();
    	mBackBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
    	mMyCard.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				if (isMultiSelectMode()) {
					if (getItemIsSelected(-1)) {
						mCkbx.setChecked(false);
						sMultiMap.remove(-1);
					}else{
						mCkbx.setChecked(true);
						sMultiMap.put(-1, true);
					}					
				}else{
					Intent intent = new Intent(mContext,TabMyCard.class);
					startActivity(intent);
				}
			}
		});
    	
    	mMyCard.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				sendCard();
				laout_radar.beginAnim();
				return false;
			}
		});
    	
    	
    	mMyCard.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
					if(mPlayFlag){
						mPlayFlag = false;
						laout_radar.endAnim();
						return true;
					}
				}
				return false;
			}
		});
    	
		
		
		mContext.getContentResolver().registerContentObserver(CardProvider.CONTENT_URI,
				false, mUriChangeObserver);
		
		mContext.registerReceiver(mCardRefreshReceiver, 
				new IntentFilter(CardUtil.ACTION_SELF_CARD_REFRESH));
		
		mRightBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				View view = (View)v.getParent();
				if(view == null) {
					view = v;
				}
				showPopupMenu(view,mMultiSelectedMode);				
			}
		});
		
		btn_reserve.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN){
					startRecorder();
				}else if(event.getAction() == MotionEvent.ACTION_UP){
					stopRecorder();
				}
				return false;
			}
		});
		
		layout_teach.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				layout_teach.setVisibility(View.GONE);
				mPrefs.edit().putBoolean(FIRSTSHOWFLAG, false).commit();
			}
		});
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

//		Intent intent = CardUtil.getAddContactIntent(cardData);
//		startActivity(intent);
		// startActivityForResult(intent, REQ_CODE_ADD_CONTACT);
		// temprary solution add contact activity will not return result
		// so we can't know the contact is select or not, just store the card
		CardUtil.storeCardFromNFC(mContext, cardData);
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
    
 // get card data from preference
 	private void sendCard() {
 		CardDataPacker cardPacker = new CardUtil.CardDataPacker(AudioDataPacker.TYPE_CARD_STRING);
 		String cardStr = cardPacker.packAudioData(null);
 		playAudio(cardStr);
 	}
 	
 	private MediaPlayer mMediaPlayer;
 	boolean mPlayFlag;

 	// encoding string data into an audio-file then play the file
 	private void playAudio(String content) {
 		Modulation.initEncoder();
 		Modulation.setListenMode('p');
 		String sAudioFileName = FilePathHelper.getWaveTransFile();
 		boolean retval = Modulation.genWavFile(content,sAudioFileName);
 		Modulation.releaseEncoder();
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
 	
 	@Override
	public void onCompletion(MediaPlayer arg0) {
		// TODO Auto-generated method stub
		if (mPlayFlag) {
			mHandler.sendEmptyMessageDelayed(EVENT_REPEAT_PLAY, 500);
			Log.e("PLAY audio","onCompletion play");
		} else {
			Modulation.setListenMode('r');
			mHandler.sendEmptyMessageDelayed(EVENT_RECORD_RESUME_DELAY, 1000);
			Log.e("PLAY audio","onCompletion stop");
		}
	}
    
 	
	/*
	 * BroadcastReceiver for self card refresh
	 */
	private BroadcastReceiver mCardRefreshReceiver = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(action.equals(CardUtil.ACTION_SELF_CARD_REFRESH)) {
				if(mMyCard != null) {
					mMyCard.refreshView();
				}
			}
		}
	};


	@Override
	public void onDestroy() {
		mContext.getContentResolver().unregisterContentObserver(mUriChangeObserver);
		mContext.unregisterReceiver(mCardRefreshReceiver);
		super.onDestroy();
	}
	

	@Override
	public void onBackPressed() {
		mMultiSelectedMode = 0;
		if (isMultiSelectMode()) {
			if (getItemIsSelected(-1)) {
				mCkbx.setChecked(false);
			}
			setMultiSelectMode(false);
			mAdapter.notifyDataSetChanged();
			return;
		} 
		super.onBackPressed();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		View v = (View)mRightBtn.getParent();
		if(v == null) {
			v= mRightBtn;
		}
		showPopupMenu(v, mMultiSelectedMode);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return true;
	}
	
	private void initGridAdapter() {
		
		final int height = mMyCardHeight / 2;
		mAdapter = new CardGridAdapter(mContext, null,height);
		
		
		
		mAdapter.setOnItemClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				
				int pos = (Integer)v.getTag();
				
				Cursor cursor = (Cursor) mAdapter.getItem(pos);
				
				if (cursor == null) {
					return;
				}
				int id = cursor.getInt(0);
				if (isMultiSelectMode()) {
					CardListItem cItem = (CardListItem)v;
					if (cItem.mCkbx.isChecked()) {
						cItem.setSelected(false);
						cItem.mCkbx.setChecked(false);
						sMultiMap.remove(id);
					}else{
						cItem.mCkbx.setChecked(true);
						cItem.setSelected(true);
						sMultiMap.put(id, true);
						startAnimation(v,cursor.getPosition(),cItem.getBgId(),height);
					}
					setMultiCount();
				}else{										
					startCardViewer(cursor.getInt(0),pos);
				}
				
			}
		});
		
		
		mAdapter.setOnItemLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {	
				int pos = (Integer)v.getTag();
				displayItemContextMenu(v, pos);
				return true;
			}
		});
		
		mAdapter.setOnItemTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN) {
					v.startAnimation(mAnimInteract);
				}
				return false;
			}

		});
		
		mGrid.setListener(new HFGridView.HFGridViewListener() {
			@Override
			public void readyToDisposeItems() {
				mGrid.setAdapter(mAdapter);				
			}
		});		
		
	}

	////////////////////////////////////////////////////////
	


	public static boolean getItemIsSelected(int id){
		if (sMultiMap != null) {
			return sMultiMap.get(id,false);
		}else{
			return false;
		}
		
	}
	
	private static Boolean sMultiMode = false;
	
	public static boolean isMultiSelectMode() {
		return sMultiMode;
	}
	
	public void adapterChanged(){
		if (mAdapter != null) {
			mAdapter.notifyDataSetChanged();
		}
	}

	public void enterMutliSelectMode() {
		mMultiSelectedMode = 1;
		setMultiSelectMode(true);
	}
	
	public void setMultiSelectMode(Boolean multiMode) {
		if (sMultiMode == multiMode) {
			return;
		}
		if (!multiMode) {
			sMultiMap.clear();
		}
		sMultiMode = multiMode;		
		
	}
	
	private void setMultiCount() {
		int size = sMultiMap.size();
//		mShopNum.setText(String.format(getString(R.string.tabbar_multi_send),
//				Integer.valueOf(size)));
	}
	////////////////////////////////////////////////////////
	

	private final ContentObserver mUriChangeObserver = new ContentObserver(
			new Handler()) {
		@Override
		public void onChange(boolean selfUpdate) {
			if(mMassiveImport) {
				return;
			}
			startAsyncQuery();
		}
	};

	public class ItemLongClickListener implements OnItemLongClickListener {

		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View v, int pos,
				long arg3) {
			displayItemContextMenu(v, pos);
			return true;
		}

	}


	private void displayItemContextMenu(final View v, final int pos) {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle("perform action");
		builder.setItems(R.array.card_item_option,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						CardListItem item = (CardListItem) v;
						int cardId = item.getId();

						switch (which) {
						case 0:
							startCardViewer(cardId,pos);
							break;
						case 1: // delete card item
							mContext.getContentResolver().delete(
									Uri.parse(CardProvider.CONTENT_URI + "/"
											+ cardId), null, null);
							if (CloundServer.getInstance().isNetworkReady()) {
								CloundServer.getInstance().getCARequest().delFriend(
										item.getUserId(), 0, mHandler);
								//showProgressDialog();
							}
							break;
						}
					}
				});
		builder.setInverseBackgroundForced(true);
		builder.create();
		builder.show();
	}

	private void startCardViewer(int id,int pos) {
		// Intent intent = new Intent(TabCardList.this,TabMyCard.class);
		//use CardEntity as transfer media, like the Mms package conversation
		Intent intent = new Intent(mContext, CardFlipActivity.class);
		//Intent intent = new Intent(TabCardList.this, CardViewer.class);
		intent.putExtra("card_id", id);
		intent.putExtra("position", pos);
		intent.putIntegerArrayListExtra("id_list", mCardIdArray);
		startActivity(intent);
	}

	public class GridItemClickListenr implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> arg0, View v, int pos, long id) {
			Cursor cursor = (Cursor) mGrid.getItemAtPosition(pos);
			if (cursor == null) {
				return;
			}
			startCardViewer(cursor.getInt(0),pos);
		}

	}
	
	ArrayList<Integer> mCardIdArray = new ArrayList<Integer>();

	private void startAsyncQuery() {
		mQueryHandler.startQuery(CARD_QUERY_TOKEN_ALL, null,
				CardProvider.CONTENT_URI, CardUtil.CARD_LIST_PROJECTION, null,
				null, null);
	}

	private void startAsyncQuerySingle(String selection) {
		mQueryHandler.startQuery(CARD_QUERY_TOKEN_SINGLE, null,
				CardProvider.CONTENT_URI, CardUtil.CARD_LIST_PROJECTION,
				selection, null, null);
	}

	public final class CardListQueryHandler extends AsyncQueryHandler {
		public CardListQueryHandler(ContentResolver contentResolver) {
			super(contentResolver);
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			if (cursor == null) {
				return;
			}
			switch (token) {
			case CARD_QUERY_TOKEN_ALL:
				mAdapter.changeCursor(cursor);
				mCardIdArray.clear();
				for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()){
					mCardIdArray.add(cursor.getInt(0));
				}				
				break;
			default:
				break;
			}
		}
	}
	

	ProgressDialog mProgress;

	private void showProgressDialog() {
		mProgress = new ProgressDialog(mContext);
		mProgress.setMessage(getText(R.string.wait_hint));
		mProgress.setIndeterminate(true);
		mProgress.setCancelable(false);
		mProgress.show();
	}

	private void dismissProgressDialog() {
		if (mProgress != null) {
			mProgress.dismiss();
			mProgress = null;
		}
	}



	private void showPopupMenu(View v,int flag) {
		final PopMenu menu = new PopMenu(this);
		if(flag == 0) {
			menu.addItem(new PopMenuItem(MENU_ITEM_IMPORT_CONTACTS, getString(R.string.add_contacts), 0));
			menu.addItem(new PopMenuItem(MENU_ITEM_MULTI_SELECT, getString(R.string.multi_select), 0));
		} else {
			menu.addItem(new PopMenuItem(MENU_ITEM_MULTI_SEND, getString(R.string.send), 0));
			menu.addItem(new PopMenuItem(MENU_ITEM_MULTI_DEL, getString(R.string.delete), 0));
		}
		menu.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position,
					long id) {				
				PopMenuItem item = (PopMenuItem)parent.getItemAtPosition(position);
				switch(item.getItemId()) {
				case MENU_ITEM_IMPORT_CONTACTS:
					addContactFriend();
//					importContact();
					break;
					
				case MENU_ITEM_MULTI_SELECT:
					enterMutliSelectMode();					
					break;
					
				case MENU_ITEM_MULTI_SEND:
					sendMultiple();
					break;
					
				case MENU_ITEM_MULTI_DEL:
					delMultiple();
					break;
				
				default:
					break;
				}
				menu.dismiss();
				
			}
		});
		menu.showAsDropDown(v);
	}


	private void sendMultiple() {
		sendMultipleItem();
		if (getItemIsSelected(-1)) {
			mCkbx.setChecked(false);
		}
		sMultiMap.clear();
		mAdapter.notifyDataSetChanged();
		mMultiSelectedMode = 0;
	}
	
	private void delMultiple() {
		for(int i=0;i < sMultiMap.size();i++){
			int cardid = sMultiMap.keyAt(i);
			if (cardid == -1) {
				mCkbx.setChecked(false);
				continue;
			}
			mContext.getContentResolver().delete(
					Uri.parse(CardProvider.CONTENT_URI + "/"
							+ cardid), null, null);
		}
		sMultiMap.clear();						
		mAdapter.notifyDataSetChanged();
		mMultiSelectedMode = 0;
	}

	
	//TODO:
	private void sendMultipleItem() {
		if(sMultiMap.size() < 1 ) {
			Toast.makeText(mContext, "no card for transmitting", Toast.LENGTH_SHORT).show();
			return;
		}
		startSendCards();
		
	}
	
	private void startSendCards(){	
		ArrayList<Integer> cardIdList = new ArrayList<Integer>();
		for(int i=0; i<sMultiMap.size(); i++) {
			int id = sMultiMap.keyAt(i);
			if(getItemIsSelected(id)) {
				cardIdList.add(id);
			}
		}
		
		StringBuilder builder = new StringBuilder();
		
		for(int id:cardIdList) {
			CardData data;
			if(id == -1){
				data = CardUtil.getSelfCardData();
			} else {
				CardEntity entity = CardEntity.from(mContext, id);
				data = CardEntity.getCardData(entity);								
			}
			CardDataPacker cardPacker = new CardUtil.CardDataPacker();
			String cardStr = cardPacker.packAudioData(data);
			builder.append(cardStr);
			builder.append(CardUtil.CARD_RECORD_SEPERATOR);
		}
		String fileName = FilePathHelper.getTmpFilePath()+File.separator +System.currentTimeMillis();
		DataFactory.getFileFromBytes(builder.toString().getBytes(),fileName);
		
		sendItemSkipActivity(fileName);

	}
	
	

	private void sendItemSkipActivity(String fileName) {
		Intent intent = new Intent(mContext, SendFilesActivity.class);
		intent.putExtra(SendFilesActivity.KEY_SEND_FILENAME, fileName);
		intent.putExtra(SendFilesActivity.KEY_DATA_PACKET_TYPE,
				AudioDataPacker.TYPE_WIFI_CARDS_SHARE);
		intent.putExtra(SendFilesActivity.SEND_SOURCE, 0);
		startActivity(intent);
	}
	
	
	private void addContactFriend(){
		if(ContactDBHelper.getInstance().getContacts().size() == 0){
			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			builder.setTitle(R.string.hint);
			builder.setMessage(R.string.uploadContact);
			builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {			
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();				
				}
			});
			builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {			
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					if(mContactManager == null){
						mContactManager = new ContactManager(CardListActivity.this);
					}
					List<MemberContact> contacts = ContactDBHelper.getInstance().getContacts();
					showProgressDialog();
					mContactManager.queryContactMembers(contacts);
				}
			});
			builder.create().show();
		}
		
	}
	
	private void importContact() {
		SharedPreferences prefs = Utils.getAppSharedPrefs();
		int msgStrId = R.string.pre_import_hint;
		if(prefs.getInt("isLoadContacts", 0) == 1) {
			msgStrId = R.string.import_again_hint;
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle(R.string.hint);
		builder.setMessage(msgStrId);
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();				
			}
		});
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				ImportContactTask task = new ImportContactTask(mContext);
		        task.execute();
			}
		});
		builder.create().show();
	}
	
    class ImportContactTask extends AsyncTask<Void,Integer,Void> {
        // variable length argument, first params match doInBackground 
    	// second params match onProgressUpdate, third params match onPostExecute
        ProgressDialog pdialog;
        public ImportContactTask(Context context){
            pdialog = new ProgressDialog(context, 0);   
            pdialog.setCancelable(false);
            pdialog.setMax(0);
            pdialog.setMessage(getString(R.string.import_wait));
            pdialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            //pdialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pdialog.show();
        }
        
       
	
        @Override
		protected Void doInBackground(Void... params) {
        	try{
        		mMassiveImport = true;
        		
/*        		GetContacts importContact = new GetContacts();
        		importContact.getPhoneContacts(mActivity.getApplicationContext());*/
        		ImportContact importContact = new ImportContact();
        		importContact.importAllContacts(new ImportContactCallBack() {					
					@Override
					public boolean onImportOneContact(int totalCount, int index) {
						publishProgress(totalCount,index);
						return false;
					}
				});
    	    	SharedPreferences.Editor editor = Utils.getAppSharedPrefs()
    					.edit();
    	    	editor.putInt("isLoadContacts", 1);
    	    	editor.commit();
    	    	
    	    	startAsyncQuery();
    	    	
               return null;
            } catch(Exception e) {
               e.printStackTrace();
            } finally {
            	mMassiveImport = false;
            }
			return null;
		}


        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
        

        @Override
		protected void onPostExecute(Void result) { 
        	if(pdialog != null) {
        		pdialog.dismiss();
        		pdialog = null;
        	}
        	
			super.onPostExecute(result);
		}


		@Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
        	if(pdialog != null) {
        		int totalCount = values[0];
        		int index = values[1];
        		pdialog.setMax(totalCount);
        		pdialog.setProgress(index);
        		
        	}
        }
     }
    
    
    private void startAnimation(View v,int pos,int BgId,int mHeight){
    	
		int resId = CardUtil.getCardBgIdMini(BgId);
		if (resId != 0) {
			mFlyCardView.setBackgroundResource(resId);
		}		
		ViewGroup.LayoutParams param = mFlyCardView.getLayoutParams();
		param.height = mHeight;
		param.width = mHeight*3/2;
		
		FlyAnimation flyAnim = new FlyAnimation(mFlyCardView);
		flyAnim.startFly(v, mRightBtn, mOutCardLayout);

	}

    private static final int EVENT_REPEAT_PLAY = 1001;
	private static final int EVENT_RECORD_RESUME_DELAY = 1002;


	@Override
	public void onQueryContactMembersCallBack(boolean success, String msg) {
		dismissProgressDialog();
		Intent intent = new Intent(this,CardListActivity.class);
		startActivity(intent);
	}
	

	
}
