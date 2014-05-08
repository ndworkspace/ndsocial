package com.nd.voice.meetingroom.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import cn.nd.social.R;
import cn.nd.social.TabLauncherUI;
import cn.nd.social.account.business.BusinessMeetingManager;
import cn.nd.social.account.usermanager.UserManager;
import cn.nd.social.common.PopMenu;
import cn.nd.social.common.PopMenuItem;
import cn.nd.social.syncbrowsing.ui.SyncBrowserDialogActivity;
import cn.nd.social.util.LogToFile;

import com.nd.voice.MultiTalk;
import com.nd.voice.VoiceEndpoint;
import com.nd.voice.VoiceEndpoint.ConferenceCallback;
import com.nd.voice.meetingroom.manager.MeetingDetailEntity;
import com.nd.voice.meetingroom.manager.MeetingEntity;
import com.nd.voice.meetingroom.manager.MeetingManagerApi;
import com.nd.voice.meetingroom.manager.MeetingManagerCallBack;
import com.nd.voice.meetingroom.manager.MeetingUser;
import com.nd.voice.meetingroom.manager.User;
import com.nd.voice.meetingroom.manager.UserManagerApi;

public class MeetingDetailActivity extends Activity implements ConferenceCallback,MeetingManagerCallBack{
	
	private static final int INDEX_FILE = 0;
	private static final int INDEX_NEWMEMBER = 1;
	private static final int INDEX_DESTORY = 2;
	
	TextView tv_title;
	ImageButton btn_back;
	ImageButton btn_right;
	Button btn_enter;
	ListView listView;
	
	MeetingDetailEntity mMeetingDetailEntity;
	
	String meetingId;
	long hostId;
	
	List<Object> mList = new ArrayList<Object>();
	
	List<String> groupkey = new ArrayList<String>();
	
	public static long[] usrHold = new long[8];
	public static int index = 0;
	
	public MeetingManagerApi mMeetingManager;
	private UserManagerApi mUserManager;
	
	public static final int RESULT_FILENAME = 1;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		String meetingId = getIntent().getStringExtra("meetingId");
		long hostId = getIntent().getLongExtra("hostId",0l);
		this.meetingId = meetingId;
		this.hostId = hostId;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.meeting_detail);
		otherInit();
		setupView();
		setupListener();
	}

	private void otherInit() {
		mMeetingManager = new BusinessMeetingManager(this);
		mUserManager = new UserManager();
	}

	private void loadData() {
		showProgressDialog();
		mMeetingManager.getDetailByMeetingId(meetingId);
	}

	private void setupView() {
		btn_back = (ImageButton) findViewById(R.id.btn_back);
		btn_right = (ImageButton) findViewById(R.id.btn_right);
		listView = (ListView) findViewById(R.id.listView_list);
		tv_title = (TextView) findViewById(R.id.tv_title);
		btn_enter = (Button) findViewById(R.id.btn_enter);
		if(mUserManager.getMyInfo().getUserid() != hostId){
			btn_right.setVisibility(View.INVISIBLE);
		}
	}
	
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		loadData();
	}
	
	private void enterMeetingRoom() {
		showProgressDialog();
		VoiceEndpoint.setUID(TabLauncherUI.VOICE_UID);//UserTestManager.getInstance().getMyInfo().getUserid());
		VoiceEndpoint.join(getApplicationContext(), mMeetingDetailEntity.getMeetingUid(), MeetingDetailActivity.this); 
		handler.sendEmptyMessageDelayed(0, 10000);
	}


	private void setupListener() {
		btn_back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				handler.removeMessages(0);
				finish();
			}
		});

		btn_right.setOnClickListener(new OnClickListener() {
			PopMenu menu = null;
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				menu = new PopMenu(MeetingDetailActivity.this);
				menu.addItem(new PopMenuItem(INDEX_FILE, getString( R.string.meetingfile), R.drawable.icon_file));
				menu.addItem(new PopMenuItem(INDEX_NEWMEMBER, getString(R.string.addmember), R.drawable.icon_member));
				menu.addItem(new PopMenuItem(INDEX_DESTORY, getString(R.string.destorymeeting), R.drawable.icon_del));
				menu.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> adapterView, View arg1,
							int position, long arg3) {
						PopMenuItem item = (PopMenuItem) adapterView.getItemAtPosition(position);
						if(item.getItemId() == INDEX_FILE){
							menu.dismiss();
						}else if(item.getItemId() == INDEX_NEWMEMBER){
							menu.dismiss();
							if(mUserManager.getMyFriendList().size() == mMeetingDetailEntity.getUsers().size()){
								new AlertDialog.Builder(MeetingDetailActivity.this).setMessage("已邀请所有好友参加").setTitle("确定").setNegativeButton("确定", null).show();
								return;
							}
							Intent intent = new Intent(MeetingDetailActivity.this,AddMeetingMemberActivity.class);
							intent.putExtra("meetingDetail", mMeetingDetailEntity);
							startActivity(intent);
						}else if(item.getItemId() == INDEX_DESTORY){
							menu.dismiss();
							MeetingDetailActivity.this.destoryMeeting();
						}
					}
				});
				menu.showAsDropDown(btn_right);
			}
		});
		
		btn_enter.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				showProgressDialog();
				VoiceEndpoint.setUID(mUserManager.getMyInfo().getUserid());//UserTestManager.getInstance().getMyInfo().getUserid());
				VoiceEndpoint.join(getApplicationContext(), mMeetingDetailEntity.getMeetingUid(), MeetingDetailActivity.this); 
				handler.sendEmptyMessageDelayed(0, 10000);
			}
		});

		listView.setAdapter(mAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				Object object =  mList.get(position);
				if(object instanceof User){
					User user = (User) object;
					if(position > 1){
						if(user.getState() == 0){
							
						}
					}
				}
			}
		});
		

	}
	
	protected void destoryMeeting() {
		// TODO Auto-generated method stub
		new AlertDialog.Builder(this) 
		.setTitle("提示")
		.setMessage("确定要取消该会议吗？")
		.setPositiveButton("是", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				showProgressDialog();
				mMeetingManager.delMeetingEntity(meetingId);
			}
		})
		.setNegativeButton("否", null)
		.show();
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			dismissProgressDialog();
			showError();
		}
	};
	
	MeetingDetailListAdapter mAdapter = new MeetingDetailListAdapter();
	
	private class MeetingDetailListAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return mList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return mList.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			View view = convertView;
			if (groupkey.contains(getItem(position))) {
				view = LayoutInflater.from(getApplicationContext())
						.inflate(R.layout.room_list_tagitem, null);
				TextView tv_tag = (TextView) view.findViewById(R.id.tv_tag);
				tv_tag.setText((CharSequence) mList.get(position));
			} else {
				view = LayoutInflater.from(getApplicationContext())
						.inflate(R.layout.meeting_detail_item, null);
				ImageView iv_face = (ImageView) view.findViewById(R.id.iv_face);
				TextView tv_username = (TextView) view.findViewById(R.id.tv_username);
				User user = (User) mList.get(position);
				User tUser = null;
				if(user.getUserid() == mUserManager.getMyInfo().getUserid()){
					tUser = mUserManager.getMyInfo();
				}else{
					tUser = mUserManager.getUserInfoLocal(user.getUserid());
				}
				if(tUser == null){
					tUser = user;
					iv_face.setImageResource(new User().getDefaultFaceResource());
				}else{
					iv_face.setImageResource(tUser.getDefaultFaceResource());
				}
				tv_username.setText(tUser.getNickName());
				TextView tv_state = (TextView)view.findViewById(R.id.tv_state);
				if(user.getUserid() == mMeetingDetailEntity.getHostUserId()){
					tv_state.setText(R.string.host);
				}else{
					if(user instanceof MeetingUser){
						MeetingUser mUser = (MeetingUser)user;
						tv_state.setText(mUser.getReplyStateString());
					}
				}
			}
			return view;
		}
		
	}
	
	private void showError(){
		handler.removeMessages(0);
		dismissProgressDialog();
		Builder builder = new  AlertDialog.Builder(this);
		builder.setMessage("网络不给力");
		builder.setTitle("提示");
		builder.setIcon(android.R.drawable.ic_dialog_alert);
		builder.setCancelable(true);
		builder.create().show();
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
	public void onJoinConference(boolean success) {
		// TODO Auto-generated method stub
		handler.removeMessages(0);
		dismissProgressDialog();
//		MultiTalk.mMultiAct.onJoinConference(success);
		Intent intent = new Intent(MeetingDetailActivity.this,MultiTalk.class);
		intent.putExtra("room_name", mMeetingDetailEntity.getMeetingUid());
		intent.putExtra("mode", "meeting");
		intent.putExtra("endtime", mMeetingDetailEntity.getEndTime());
		intent.putExtra("idarray", mMeetingDetailEntity.getUserIdArray());
		long userId = mUserManager.getMyInfo().getUserid();
		
		int resId; 
		User buildIn = mUserManager.getUserInfoLocal(userId);
		if(buildIn != null) {
			resId = buildIn.getDefaultFaceResource();
		} else {
			resId = mUserManager.getMyInfo().getDefaultFaceResource();
		}
		
		if(resId > 1 ) {
			intent.putExtra("faceres",resId);
		}
		
		intent.putExtra("ishost",userId == this.hostId);
		startActivity(intent);
	}

	@Override
	public void onLeaveConference() {
		Toast.makeText(MeetingDetailActivity.this, "You left ",
				Toast.LENGTH_SHORT).show();
		usrHold = new long[4];
		index = 0;
	}

	@Override
	public void onConferenceMemberEnter(long uid) {
		// TODO Auto-generated method stub
		LogToFile.d("Member_Enter", "User"+ uid + " succeeded in entering Room:"+ mMeetingDetailEntity.getTitle());
		if (TabLauncherUI.VOICE_UID == uid) {			
			Toast.makeText(MeetingDetailActivity.this, "coming me",
					Toast.LENGTH_SHORT).show();
		}else{
			Toast.makeText(MeetingDetailActivity.this, "coming you",
					Toast.LENGTH_SHORT).show();
			if (MultiTalk.mMultiAct != null) {
				MultiTalk.mMultiAct.onConferenceMemberEnter(uid);
			}else{
				usrHold[index] = uid;
				index++;
			}			
		}

	}

	@Override
	public void onConferenceMemberLeave(long uid) {
		LogToFile.d("Member_Leave", "User"+ uid + " succeeded in leaving Room:"+ mMeetingDetailEntity.getTitle());
		if (TabLauncherUI.VOICE_UID == uid) {			
			Toast.makeText(MeetingDetailActivity.this, "leaving me",
					Toast.LENGTH_SHORT).show();			
		}else{
			Toast.makeText(MeetingDetailActivity.this, "leaving you",
					Toast.LENGTH_SHORT).show();
			if (MultiTalk.mMultiAct != null) {
				MultiTalk.mMultiAct.onConferenceMemberLeave(uid);
			}
		}
		
	}

	@Override
	public void onConferenceMediaOption(int cmd, int value) {
		LogToFile.i("Media_Option", "Media option is "+ cmd + ", and value is "+ value);
	}

	@Override
	public void onConferenceAudioEnergy(long uid, int energy) {
		// TODO Auto-generated method stub
		//LogToFile.i("Audio_Energy", "The sound of User"+ uid + " is "+ energy);	
			if (MultiTalk.mMultiAct != null) {
				MultiTalk.mMultiAct.onConferenceAudioEnergy(uid, energy);
			}
		
	}

	@Override
	public void onSetUid(long uid, boolean success) {
		if (success) {
			LogToFile.d("Set_UID", "User"+ uid + "succeeded in setting ID");
		}else{
			LogToFile.e("Set_UID", "User"+ uid + "failed in setting ID");
		}
						
	}

	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		handler.removeMessages(0);
		super.onBackPressed();
	}


	@Override
	public void onGetMeetingListCallBack(String memberId,
			List<MeetingEntity> list, boolean success, String msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onGetDetailByMeetingIdCallBack(String meetingId,
			MeetingDetailEntity entity, boolean success, String msg) {		
		dismissProgressDialog();
		if(!success) {
			if(msg == null || msg.equals("")) {
				msg = "未知错误";
			}
			Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
			return;
		}
		mMeetingDetailEntity = entity;
		groupkey.clear();
		mList.clear();
		groupkey.add("主持人");
		groupkey.add("成员");
		mList.add(groupkey.get(0));
		mList.add(mMeetingDetailEntity.getHostUserInfo());
		mList.add(groupkey.get(1));
		mList.addAll(mMeetingDetailEntity.getUsers());
		tv_title.setText("会议主题:" + mMeetingDetailEntity.getTitle());
		mAdapter.notifyDataSetInvalidated();
	}

	@Override
	public void onAddMeetingEntityCallBack(MeetingDetailEntity entity,
			boolean success, String msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDelMeetingentityCallBack(String meetingId, boolean success,
			String msg) {
		// TODO Auto-generated method stub
		dismissProgressDialog();
		if(success){
			this.finish();
			Toast.makeText(this, R.string.meetingdestoryed, 1000).show();
		}else{
			Toast.makeText(this, msg, 2000).show();
		}
	}

	@Override
	public void onAccpetMeetingCallBack(String meetingId, boolean success,
			String msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRefuseMeetingCallBack(String meetingId, boolean success,
			String msg) {
		// TODO Auto-generated method stub
		
	}
	
	
	ProgressDialog mProgress;
	
	private void showProgressDialog() {
		mProgress = new ProgressDialog(this);
		mProgress.setMessage(getText(R.string.wait_hint));
		mProgress.setIndeterminate(true);
		mProgress.setCancelable(true);
		mProgress.show();
	}

	private void dismissProgressDialog() {
		if (mProgress != null) {
			mProgress.dismiss();
			mProgress = null;
		}
	}

	@Override
	public void onAddMeetingMemberCallBack(String meetingId, boolean success,
			String msg) {
		// TODO Auto-generated method stub
		
	}

}
