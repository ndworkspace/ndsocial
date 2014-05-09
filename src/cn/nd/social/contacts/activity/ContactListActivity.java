package cn.nd.social.contacts.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import cn.nd.social.R;
import cn.nd.social.account.usermanager.UserManager;
import cn.nd.social.common.ShareThisApp;
import cn.nd.social.contacts.manager.ContactDBHelper;
import cn.nd.social.contacts.manager.ContactManager;
import cn.nd.social.contacts.manager.ContactManagerCallBack;
import cn.nd.social.contacts.manager.GetContacts;
import cn.nd.social.contacts.manager.MemberContact;

import com.nd.voice.meetingroom.manager.User;
import com.nd.voice.meetingroom.manager.UserManagerApi;
import com.nd.voice.meetingroom.manager.UserManagerCallBack;

public class ContactListActivity extends Activity implements UserManagerCallBack, ContactManagerCallBack{
	
	private ListView mListView;
	private Button btn_back;
	private List<MemberContact> mList = new ArrayList<MemberContact>();
	private UserManagerApi mUserManagerApi;
	private MemberContact mWaitContact;
	private ContactManager mContactManager;
	private Handler mHandler;
	
	 @Override
	    public void onCreate(Bundle savedInstanceState) {
	    	super.onCreate(savedInstanceState);
	    	setContentView(R.layout.contactlist);
	    	otherInit();
	    	setupViews();
	        registerEvent();
	        loadContactList();
	    }

	private void otherInit() {
		// TODO Auto-generated method stub
		mUserManagerApi = new UserManager();
		mUserManagerApi.setCallBack(this);
		mContactManager = new ContactManager(this);
		mHandler = new Handler();
	}

	private void setupViews() {
		mListView = (ListView) findViewById(R.id.listView);
		btn_back = (Button) findViewById(R.id.btn_back);
	}

	private void registerEvent() {
		btn_back.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		
		mListView.setAdapter(adapter );
	}
	
	ContactListAdapter adapter = new ContactListAdapter();

	private class ContactListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			MemberContact item = null;
			item = mList.get(position);
			return item;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			View view = convertView;
			if (convertView == null) {
				view = LayoutInflater.from(getApplicationContext())
						.inflate(R.layout.contact_list_item, null);
			} 
			TextView tv_name = (TextView) view.findViewById(R.id.tv_name);
			final MemberContact contact = mList.get(position);
			tv_name.setText(contact.getContactName());
			Button btn_action = (Button) view.findViewById(R.id.btn_action);
			if(!contact.isMember()){
				btn_action.setText("邀请");
				btn_action.setEnabled(true);
			}else{
				btn_action.setEnabled(true);
				if(contact.isFriend()){
					btn_action.setText("已添加");
					btn_action.setEnabled(false);
				}else{
					btn_action.setText("添加");
				}
			}
			btn_action.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(final View view) {
					if(!contact.isMember()){
						sendSms(contact);
					}else{
						mWaitContact = contact;
						showProgressDialog("正在提交申请");
						sendAddFriendRequest(contact);
					}
				}
			});
			return view;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

	}
	
	
	private void sendSms(MemberContact contact){
		new ShareThisApp(this).share();
	}
	
	private void sendAddFriendRequest(MemberContact contact){
		mUserManagerApi.addFriend(contact.getUid());
	}
	
	
	private void loadContactList(){
		showProgressDialog("正在获取朋友信息");
		loadFromDB();
		new Thread(new Runnable() {
			@Override
			public void run() {
				final List<MemberContact> list =  GetContacts.getPhoneContacts(ContactListActivity.this);
				
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						mContactManager.queryContactMembers(list);
					}
				});
			}
		}).run();
	}
	
	private void loadFromDB() {
		mList.clear();
		mList.addAll(ContactDBHelper.getInstance().getContacts());
		adapter.notifyDataSetChanged();
	}

	
	ProgressDialog mProgress;
	
	private void showProgressDialog(String title) {
		mProgress = new ProgressDialog(this);
		mProgress.setMessage(title);
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
	public void onGetUpdateUserInfoCallBack(long memberId, User userinfo,
			boolean success, String msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onGetFriendListCallBack(long memberId, List<User> friends,
			boolean success, String msg) {
		// TODO Auto-generated method stub
		
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

	@Override
	public void onAddFriendCallBack(long memberId, boolean success, String msg) {
		// TODO Auto-generated method stub
		dismissProgressDialog();
		if(success){
			Toast.makeText(this, "添加成功", 1000);
			mWaitContact.setFriend(true);
			mList.remove(mWaitContact);
			mList.add(mWaitContact);
			adapter.notifyDataSetChanged();
			mUserManagerApi.getFriendList(mUserManagerApi.getMyInfo().getUserid());
		}else{
			Toast.makeText(this, msg, 1000);
		}
		
	}


	@Override
	public void onQueryContactMembersCallBack(
			boolean success, String msg) {
		dismissProgressDialog();
		if(success){
			loadFromDB();
		}
	}

	@Override
	public void onQueryContactFriendCallBack(List<String> friendMobiles,
			List<String> noFriendMobiles, List<Long> noFriendUids,
			boolean success, String msg) {
		// TODO Auto-generated method stub
		
	}

}
