package com.nd.voice.meetingroom.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import cn.nd.social.R;
import cn.nd.social.account.business.BusinessMeetingManager;
import cn.nd.social.account.usermanager.UserManager;

import com.nd.voice.meetingroom.manager.MeetingDetailEntity;
import com.nd.voice.meetingroom.manager.MeetingEntity;
import com.nd.voice.meetingroom.manager.MeetingManagerApi;
import com.nd.voice.meetingroom.manager.MeetingManagerCallBack;
import com.nd.voice.meetingroom.manager.MeetingUser;
import com.nd.voice.meetingroom.manager.User;
import com.nd.voice.meetingroom.manager.UserManagerApi;

public class MeetingInviteActivity extends Activity implements MeetingManagerCallBack{
	
	Button btn_accept;
	Button btn_reject;
	ImageButton btn_back;
	
	String meetingId;
	
	TextView tv_title;
	ListView listView;
	
	MeetingDetailEntity mMeetingDetailEntity;
	
	List<Object> mList = new ArrayList<Object>();
	
	List<String> groupkey = new ArrayList<String>();
	
	public MeetingManagerApi mMeetingManager;
	private UserManagerApi mUserManager;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		meetingId = this.getIntent().getStringExtra("meetingId");
		setContentView(R.layout.meeting_invite);
		setupView();
		setupListener();
		otherInit();
		loadData();
		super.onCreate(savedInstanceState);
	}

	private void loadData() {
		showProgressDialog();
		mMeetingManager.getDetailByMeetingId(meetingId);
	}
	
	private void setupView() {
		// TODO Auto-generated method stub
		btn_accept = (Button) findViewById(R.id.btn_accept);
		btn_reject = (Button) findViewById(R.id.btn_reject);
		btn_back = (ImageButton) findViewById(R.id.btn_back);
		listView = (ListView) findViewById(R.id.listView_list);
		tv_title = (TextView) findViewById(R.id.tv_title);
		
		listView.setAdapter(new MeetingDetailListAdapter());
	}
	
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
				User tuser = (User) mList.get(position);
				User user = mUserManager.getUserInfoLocal(tuser.getUserid());
				if(user == null){
					user = tuser;
				}
				//ToDo
				if(user !=null){
					User tUser = mUserManager.getUserInfoLocal(user.getUserid());
					if(tUser == null){
						iv_face.setImageResource(user.getDefaultFaceResource());
					}else{
						iv_face.setImageResource(tUser.getDefaultFaceResource());
					}
					
				}
				tv_username.setText(user.getNickName());
				TextView tv_state = (TextView)view.findViewById(R.id.tv_state);
				if(user.getUserid() == mMeetingDetailEntity.getHostUserId()){
					tv_state.setText(R.string.host);
				}else{
					if(tuser instanceof MeetingUser){
						MeetingUser mUser = (MeetingUser)tuser;
						tv_state.setText(mUser.getReplyStateString());
					}
				}
			}
			return view;
		}
		
	}

	private void setupListener() {
		// TODO Auto-generated method stub
		btn_accept.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				MeetingInviteActivity.this.acceptMeetingAction();
			}
		});
		
		btn_reject.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				MeetingInviteActivity.this.rejectMeetingAction();
			}
		});

		btn_back.setOnClickListener(new OnClickListener() {
	
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				MeetingInviteActivity.this.finish();
			}
		});
	}


	private void otherInit() {
		// TODO Auto-generated method stub
		mMeetingManager = new BusinessMeetingManager();
		mMeetingManager.setCallBack(this);
		mUserManager = new UserManager();
	}


	@Override
	public void onGetMeetingListCallBack(String memberId,
			List<MeetingEntity> list, boolean success, String msg) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onGetDetailByMeetingIdCallBack(String meetingId,
			MeetingDetailEntity entity, boolean success, String msg) {
		// TODO Auto-generated method stub
		dismissProgressDialog();
		mMeetingDetailEntity = entity;
		groupkey.clear();
		groupkey.add("主持人");
		groupkey.add("成员");
		mList.add(groupkey.get(0));
		mList.add(mMeetingDetailEntity.getHostUserInfo());
		mList.add(groupkey.get(1));
		mList.addAll(mMeetingDetailEntity.getUsers());
		tv_title.setText("受邀会议主题:" + mMeetingDetailEntity.getTitle());
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
		
	}


	@Override
	public void onAccpetMeetingCallBack(String meetingId, boolean success,
			String msg) {
		// TODO Auto-generated method stub
		dismissProgressDialog();
		if(success){
			Toast.makeText(this, "您已接受该会议邀请", 1000);
			this.finish();
		}else{
			Toast.makeText(this, msg, 2000);
		}
		
	}


	@Override
	public void onRefuseMeetingCallBack(String meetingId, boolean success,
			String msg) {
		// TODO Auto-generated method stub
		dismissProgressDialog();
		if(success){
			Toast.makeText(this, "您已拒绝该会议邀请", 1000);
			this.finish();
		}else{
			Toast.makeText(this, msg, 2000);
		}
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
	
	
	protected void rejectMeetingAction() {
		// TODO Auto-generated method stub
//		showProgressDialog();
		mMeetingManager.refuseMeeting(meetingId, null);
		this.finish();
	}


	protected void acceptMeetingAction() {
		// TODO Auto-generated method stub
//		showProgressDialog();
		mMeetingManager.accpetMeeting(meetingId, null);
		BusinessMeetingManager.addToLocalMeeting(mMeetingDetailEntity, false);
		this.sendBroadcast(new Intent(RoomListFrame.ACTION_MEETING_LIST_CHANGE));
		this.finish();
	}

	@Override
	public void onAddMeetingMemberCallBack(String meetingId, boolean success,
			String msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAddMeetingDoc(String meetingId, String filename,
			boolean success, String msg) {
		// TODO Auto-generated method stub
		
	}
	
}
