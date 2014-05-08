package com.nd.voice.meetingroom.activity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
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
import android.widget.TextView;
import cn.nd.social.R;
import cn.nd.social.account.business.BusinessMeetingManager;
import cn.nd.social.account.usermanager.UserManager;
import cn.nd.social.common.PopMenu;
import cn.nd.social.common.PopMenuItem;

import com.nd.voice.chatroom.EnterRoomActivity;
import com.nd.voice.meetingroom.lib.SwipeListView;
import com.nd.voice.meetingroom.lib.SwipeListView.OnSwipeCallback;
import com.nd.voice.meetingroom.manager.MeetingDetailEntity;
import com.nd.voice.meetingroom.manager.MeetingEntity;
import com.nd.voice.meetingroom.manager.MeetingManagerApi;
import com.nd.voice.meetingroom.manager.MeetingManagerCallBack;
import com.nd.voice.meetingroom.manager.UserManagerApi;

public class RoomListActivity extends FragmentActivity implements MeetingManagerCallBack{
	
	private static final int CHOOSETYPE_MY = 0;
	private static final int CHOOSETYPE_OTHER = 1; 
	
	private static final int MENU_CREATEROOM = 0;
	private static final int MENU_TALKBAR = 1;

	ImageButton btn_back;
	ImageButton btn_more;
	SwipeListView listView;
	Button btn_my;
	Button btn_other;
	
	List<MeetingEntity> myMeetingList = new ArrayList<MeetingEntity>();
	List<MeetingEntity> otherMeetingList = new ArrayList<MeetingEntity>();
	
	Set<MeetingEntity> waitDelMeetingList = new HashSet<MeetingEntity>();
	
	MeetingManagerApi mMeetingManager;
	
	UserManagerApi mUserManager;
	
	int chooseType;//0 :我的会议  1:受邀会议

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		setContentView(R.layout.room_list);
		setupView();
		setupListener();
		otherInit();
		super.onCreate(savedInstanceState);
	}
	
	private void otherInit() {
		// TODO Auto-generated method stub
		mMeetingManager = new BusinessMeetingManager(this); 
		mUserManager = new UserManager();
	}

	@Override
	protected void onResume() {
		super.onResume();
		this.loadData();
		loadDataFromNet();
	}
	
	private void loadData() {
		myMeetingList.clear();
		myMeetingList.addAll(mMeetingManager.getMyMeetingListLocal());
		otherMeetingList.clear();
		otherMeetingList.addAll(mMeetingManager.getOtherMeetingListLocal());
		adapter.notifyDataSetChanged();
	}
	
	private void loadDataFromNet(){
		if(mUserManager.getMyInfo()!=null){
			showProgressDialog();
			mMeetingManager.getMeetingList("" + mUserManager.getMyInfo().getUserid());
		}
	}
	
	private void setupView() {
		btn_back = (ImageButton) findViewById(R.id.btn_back);
		btn_more = (ImageButton) findViewById(R.id.btn_more);
		listView = (SwipeListView) findViewById(R.id.listView_list);
		btn_my = (Button)findViewById(R.id.btn_my);
		btn_other = (Button)findViewById(R.id.btn_other);
		chooseTypUpdate(CHOOSETYPE_MY);
	}

	private void setupListener() {
		btn_back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});

		btn_more.setOnClickListener(new OnClickListener() {
			PopMenu menu = null;
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				menu = new PopMenu(RoomListActivity.this);
				menu.addItem(new PopMenuItem(MENU_CREATEROOM, getString( R.string.reservemeeting), 0));
				menu.addItem(new PopMenuItem(MENU_TALKBAR, getString(R.string.talkbar), 0));
				menu.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> adapterView, View arg1,
							int position, long arg3) {
						PopMenuItem item = (PopMenuItem) adapterView.getItemAtPosition(position);
						if(item.getItemId() == MENU_CREATEROOM){
							menu.dismiss();
							Intent intent = new Intent(RoomListActivity.this,ResereActivity.class);
							startActivity(intent);
						}else if(item.getItemId() == MENU_TALKBAR){
							menu.dismiss();
							Intent intent = new Intent(RoomListActivity.this,EnterRoomActivity.class);
							startActivity(intent);
						}
					}
				});
				menu.showAsDropDown(btn_more);
			}
		});

		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				MeetingEntity meetingEntity = null;
				if (CHOOSETYPE_MY == chooseType){
					meetingEntity = (MeetingEntity) myMeetingList.get(position);
				}else{
					meetingEntity = (MeetingEntity) otherMeetingList.get(position);
				}
				if(waitDelMeetingList.contains(meetingEntity)){
					waitDelMeetingList.remove(meetingEntity);
					adapter.notifyDataSetChanged();
					return;
				}
				Intent intent = new Intent(RoomListActivity.this,MeetingDetailActivity.class);
				intent.putExtra("meetingId", meetingEntity.getMeetingid());
				startActivity(intent);
			}
		});
		
		listView.setOnSwipeCallback(new OnSwipeCallback() {
			
			@Override
			public void onSwipe(int position, View holdView) {
				// TODO Auto-generated method stub
				MeetingEntity meetingEntity = null;
				if (CHOOSETYPE_MY == chooseType){
					meetingEntity = (MeetingEntity) myMeetingList.get(position);
					waitDelMeetingList.add(meetingEntity);
					adapter.notifyDataSetChanged();
				}
			}
			
			@Override
			public void onCancelSwipe(int position, View holdView) {
				// TODO Auto-generated method stub
				if(myMeetingList!=null && position<myMeetingList.size()){
					MeetingEntity meetingEntity = null;
					if (CHOOSETYPE_MY == chooseType){
						meetingEntity = (MeetingEntity) myMeetingList.get(position);
					}else{
						meetingEntity = (MeetingEntity) otherMeetingList.get(position);
					}
					waitDelMeetingList.remove(meetingEntity);
					adapter.notifyDataSetChanged();
				}
			}
		});
		
		btn_my.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				chooseTypUpdate(CHOOSETYPE_MY);
			}
		});
		
		btn_other.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				chooseTypUpdate(CHOOSETYPE_OTHER);
			}
		});
		
	}
	
	
	private void chooseTypUpdate(int type){
		chooseType = type;
		if(type == CHOOSETYPE_MY){
			btn_my.setBackgroundResource(R.drawable.button_tableft_selected);
			btn_my.setTextColor(0xffffffff);
			btn_other.setBackgroundResource(R.drawable.button_tabright_normal);
			btn_other.setTextColor(0x60ffffff);
		}else{
			btn_my.setBackgroundResource(R.drawable.button_tableft_normal);
			btn_my.setTextColor(0x60ffffff);
			btn_other.setBackgroundResource(R.drawable.button_tabright_selected);
			btn_other.setTextColor(0xffffffff);
		}
		adapter.notifyDataSetChanged();
	}
	
	RoomListAdapter adapter = new RoomListAdapter();

	private class RoomListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			if (CHOOSETYPE_MY == chooseType){
				return myMeetingList.size();
			}else{
				return otherMeetingList.size();
			}
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			MeetingEntity meetingEntity = null;
			if (CHOOSETYPE_MY == chooseType){
				meetingEntity = (MeetingEntity) myMeetingList.get(position);
			}else{
				meetingEntity = (MeetingEntity) otherMeetingList.get(position);
			}
			return meetingEntity;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}
		
		@Override
		public int getViewTypeCount() {
			return 2;
		}
		
		@Override
		public int getItemViewType(int position) {
			if (CHOOSETYPE_MY == chooseType){
				return 0;
			}else{
				return 1;
			}
		}
		

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			View view = convertView;
			if (convertView == null) {
				if (CHOOSETYPE_MY != chooseType){
					view = LayoutInflater.from(getApplicationContext())
							.inflate(R.layout.room_list_item, null);
				}else{
					view = LayoutInflater.from(getApplicationContext())
							.inflate(R.layout.room_mylist_item, null);
				}
			} 
			ImageView iv_face = (ImageView) view.findViewById(R.id.iv_face);
			TextView tv_date = (TextView) view.findViewById(R.id.tv_date);
			TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
			MeetingEntity entity = null;
			if (CHOOSETYPE_MY == chooseType){
				entity = (MeetingEntity) myMeetingList.get(position);
			}else{
				entity = (MeetingEntity) otherMeetingList.get(position);
				TextView tv_username = (TextView) view.findViewById(R.id.tv_username);
				tv_username.setText(entity.getHostName());
				
			}
			tv_date.setText(entity.getMeetingTime());
			tv_title.setText(entity.getTitle());
			Button btn_delete = (Button) view.findViewById(R.id.btn_delete);
			if(waitDelMeetingList.contains(entity)){
				btn_delete.setVisibility(View.VISIBLE);
			}else{
				btn_delete.setVisibility(View.INVISIBLE);
			}
			btn_delete.setTag(position);
			btn_delete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(final View view) {
					if (CHOOSETYPE_MY == chooseType){
						new AlertDialog.Builder(RoomListActivity.this) 
						.setTitle("确认")
						.setMessage("确定要删除吗？")
						.setPositiveButton("是", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								int position = (Integer) view.getTag();
								if(chooseType == CHOOSETYPE_MY){
									myMeetingList.remove(position);
								}else{
									otherMeetingList.remove(position);
								}
								MeetingEntity entity = null;
								if (CHOOSETYPE_MY == chooseType){
									entity = (MeetingEntity) myMeetingList.get(position);
								}else{
									entity = (MeetingEntity) otherMeetingList.get(position);
								}
								waitDelMeetingList.remove(entity);
								adapter.notifyDataSetChanged();
							}
						})
						.setNegativeButton("否", null)
						.show();
					}
				}
			});
			return view;
		}

	}

	@Override
	public void onGetMeetingListCallBack(String memberId,
			List<MeetingEntity> list, boolean success, String msg) {
		// TODO Auto-generated method stub
		dismissProgressDialog();
		if(success){
			loadData();
		}
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
