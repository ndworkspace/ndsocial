package com.nd.voice.meetingroom.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import cn.nd.social.account.business.BusinessMeetingManager;
import cn.nd.social.account.usermanager.UserManager;

import com.nd.voice.meetingroom.manager.MeetingDetailEntity;
import com.nd.voice.meetingroom.manager.MeetingEntity;
import com.nd.voice.meetingroom.manager.MeetingManagerApi;
import com.nd.voice.meetingroom.manager.MeetingManagerCallBack;
import com.nd.voice.meetingroom.manager.MeetingUser;
import com.nd.voice.meetingroom.manager.User;
import com.nd.voice.meetingroom.manager.UserManagerApi;

public class AddMeetingMemberActivity extends Activity implements MeetingManagerCallBack {
	
	private MeetingDetailEntity mMeetingDetail;
	private ArrayList<User> mAllUser;
	private UserManagerApi mUserManager;
	private MeetingManagerApi mMeetingManager;
	private ArrayList<User> mSelectedFriends;
	
	ListView listView;
	ImageButton btn_back;
	Button btn_sure;
	TextView tv_search;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friend_choose_frament);
		mMeetingDetail = (MeetingDetailEntity) this.getIntent().getSerializableExtra("meetingDetail");
		otherInit();
		setupView();
		setupListener();
	}

	private void otherInit() {
		// TODO Auto-generated method stub
		mUserManager = new UserManager();
		mMeetingManager = new BusinessMeetingManager(this);
		mAllUser = new ArrayList<User>();
		for(User user : mUserManager.getMyFriendList()){
			boolean flag = true;
			for(MeetingUser mUser : mMeetingDetail.getUsers()){
				if(user.getUserid() == mUser.getUserid()){
					flag = false;
					break;
				}
			}
			if(flag){
				mAllUser.add(user);
			}
		}
		mSelectedFriends = new ArrayList<User>();
	}

	private void setupView() {
		// TODO Auto-generated method stub
		btn_back = (ImageButton) findViewById(R.id.btn_back);
		btn_sure = (Button) findViewById(R.id.btn_sure);
		listView = (ListView) findViewById(R.id.listView_list);
		tv_search = (TextView)findViewById(R.id.et_search);
	}

	private void setupListener() {
		btn_back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				AddMeetingMemberActivity.this.finish();
			}
		});

		btn_sure.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(mSelectedFriends.size() == 0){
					new AlertDialog.Builder(AddMeetingMemberActivity.this) 
					.setMessage("请选择成员")
					.setPositiveButton("确定", null)
					.show();
					return;
				}
				addMeetingMemberAction();
			}
		});

		listView.setAdapter(mAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adpterView, View view, int position,
					long arg3) {
				User user = mAllUser.get(position);
				if(mSelectedFriends.contains(user)){
					mSelectedFriends.remove(user);
				}else{
					mSelectedFriends.add(user);
				}
				mAdapter.notifyDataSetChanged();
			}
		});
		
		tv_search.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				if (s.length() == 0) {  
					if(s.equals("\n")){
						
					}
                } else {  
                	
                }  
			}
		});
	}
	
	private void addMeetingMemberAction(){
		List<Long> userids = getSelectedUserIds();
		showProgressDialog();
		mMeetingManager.addMeetingMember(mMeetingDetail.getMeetingid(), userids);
	}

	private List<Long> getSelectedUserIds() {
		ArrayList<Long> list = new ArrayList<Long>();
		for(User user : mSelectedFriends){
			list.add(user.getUserid());
		}
		return list;
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

	FriendListAdapter mAdapter = new FriendListAdapter();
	private class FriendListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mAllUser.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return mAllUser.get(position);
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
			if (view == null) {
				view = LayoutInflater.from(AddMeetingMemberActivity.this)
						.inflate(R.layout.friend_list_item, null);
				
			}
			User user =  mAllUser.get(position);
			TextView tv_username = (TextView) view.findViewById(R.id.tv_username);
			tv_username.setText(user.getNickName());
			ImageView iv_selected = (ImageView) view.findViewById(R.id.iv_selected);
			ImageView iv_face = (ImageView) view.findViewById(R.id.iv_face);
			iv_face.setImageResource(user.getDefaultFaceResource());
			if(mSelectedFriends.contains(mAllUser.get(position))){
				iv_selected.setVisibility(View.VISIBLE);
			}else{
				iv_selected.setVisibility(View.INVISIBLE);
			}
			return view;
		}
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
		
	}

	@Override
	public void onRefuseMeetingCallBack(String meetingId, boolean success,
			String msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAddMeetingMemberCallBack(String meetingId, boolean success,
			String msg) {
		// TODO Auto-generated method stub
		dismissProgressDialog();
		if(success){
			Toast.makeText(this, "邀请好友成功", 1000).show();
			this.finish();
		} else {
			Toast.makeText(this, msg, 1000).show();
		}
	}
	
	

}
