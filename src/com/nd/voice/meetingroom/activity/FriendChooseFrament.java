package com.nd.voice.meetingroom.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import cn.nd.social.util.DateStringUtils;

import com.nd.voice.meetingroom.manager.MeetingDetailEntity;
import com.nd.voice.meetingroom.manager.MeetingEntity;
import com.nd.voice.meetingroom.manager.MeetingManagerApi;
import com.nd.voice.meetingroom.manager.MeetingManagerCallBack;
import com.nd.voice.meetingroom.manager.User;
import com.nd.voice.meetingroom.manager.UserManagerApi;
import com.nd.voice.meetingroom.manager.UserManagerCallBack;

interface FriendChooseListener{
	void friendChooseOnBackBtnClick();
}

public class FriendChooseFrament extends Fragment implements UserManagerCallBack,MeetingManagerCallBack{
	
	ResereActivity mActivity;
	View mRootView;
	ListView listView;
	
	ImageButton btn_back;
	Button btn_sure;
	
	TextView tv_search;
	
	FriendChooseListener listener;

	List<User> mFriendList = new ArrayList<User>();
	
	List<User> mSelectedFriends = new ArrayList<User>();
	
	private String meetingTitle;
	
	private Calendar meetingDate;
	
	UserManagerApi mUserManager;
	
	private MeetingManagerApi mMeetingManager;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mActivity = (ResereActivity)getActivity();
        mRootView = inflater.inflate(R.layout.friend_choose_frament,null); 
		initData();
		setupView();
		setupListener();
		return mRootView;
	}
	
	private void initData(){
		mUserManager = new UserManager(this);
		mMeetingManager = new BusinessMeetingManager(this);
		mFriendList.addAll(mUserManager.getMyFriendList());
		mFriendList.remove(mUserManager.getMyInfo());
		mUserManager.getFriendList(mUserManager.getMyInfo().getUserid());
	}
	
	private void setupView() {
		btn_back = (ImageButton) mRootView.findViewById(R.id.btn_back);
		btn_sure = (Button) mRootView.findViewById(R.id.btn_sure);
		listView = (ListView) mRootView.findViewById(R.id.listView_list);
		tv_search = (TextView)mRootView.findViewById(R.id.et_search);
	}

	private void setupListener() {
		btn_back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(FriendChooseFrament.this.getListener() != null){
					FriendChooseFrament.this.getListener().friendChooseOnBackBtnClick();
				}
			}
		});

		btn_sure.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(mSelectedFriends.size() == 0){
					new AlertDialog.Builder(mActivity) 
					.setMessage("请选择成员")
					.setPositiveButton("确定", null)
					.show();
					return;
				}
				FriendChooseFrament.this.addMeetingAction();
			}
		});

		listView.setAdapter(listAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adpterView, View view, int position,
					long arg3) {
				User user = mFriendList.get(position);
				if(mSelectedFriends.contains(user)){
					FriendChooseFrament.this.mSelectedFriends.remove(user);
				}else{
					FriendChooseFrament.this.mSelectedFriends.add(user);
				}
				listAdapter.notifyDataSetChanged();
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

	FriendListAdapter listAdapter = new FriendListAdapter();
	private class FriendListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mFriendList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return mFriendList.get(position);
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
				view = LayoutInflater.from(mActivity)
						.inflate(R.layout.friend_list_item, null);
				
			}
			User user =  mFriendList.get(position);
			TextView tv_username = (TextView) view.findViewById(R.id.tv_username);
			tv_username.setText(user.getNickName());
			ImageView iv_selected = (ImageView) view.findViewById(R.id.iv_selected);
			ImageView iv_face = (ImageView) view.findViewById(R.id.iv_face);
			iv_face.setImageResource(user.getDefaultFaceResource());
			if(mSelectedFriends.contains(mFriendList.get(position))){
				iv_selected.setVisibility(View.VISIBLE);
			}else{
				iv_selected.setVisibility(View.INVISIBLE);
			}
			return view;
		}

	}
	
	private void addMeetingAction(){
		MeetingEntity entity = new MeetingEntity();
		entity.setTitle(meetingTitle);
		entity.setMeetingTime(DateStringUtils.dateToString(meetingDate.getTime(), "yyyy-MM-dd HH:mm:ss"));
		showProgressDialog();
		mMeetingManager.addMeetingEntity(entity, mSelectedFriends);
	}

	public FriendChooseListener getListener() {
		return listener;
	}

	public void setListener(FriendChooseListener listener) {
		this.listener = listener;
	}
	

	public String getMeetingTitle() {
		return meetingTitle;
	}

	public void setMeetingTitle(String meetingTitle) {
		this.meetingTitle = meetingTitle;
	}

	public Calendar getMeetingDate() {
		return meetingDate;
	}

	public void setMeetingDate(Calendar meetingDate) {
		this.meetingDate = meetingDate;
	}


	@Override
	public void onGetUpdateUserInfoCallBack(long memberId, User userinfo,
			boolean success, String msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onGetFriendListCallBack(long memberId, List<User> friends,
			boolean success, String msg) {
		if(success) {
			mFriendList.clear();
			mFriendList.addAll(mUserManager.getMyFriendList());
			mFriendList.remove(mUserManager.getMyInfo());
			listAdapter.notifyDataSetChanged();
		}
		
	}

	@Override
	public void onLoginCallBack(String username, User user, boolean success,
			String msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRegisterCallBack(User user, boolean success, String msg) {
		// TODO Auto-generated method stub
		
	}
	
	ProgressDialog mProgress;
	
	private void showProgressDialog() {
		mProgress = new ProgressDialog(mActivity);
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
		mActivity.sendBroadcast(new Intent(RoomListFrame.ACTION_MEETING_LIST_CHANGE));
		
		dismissProgressDialog();
		if(success){
			Toast.makeText(mActivity, "预约成功", 1000).show();
			mActivity.finish();
		}else{
			Toast.makeText(mActivity,msg, 1000).show();
		}
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
		
	}
	
}
