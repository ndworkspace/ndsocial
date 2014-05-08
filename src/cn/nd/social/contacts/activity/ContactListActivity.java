package cn.nd.social.contacts.activity;

import java.util.ArrayList;
import java.util.List;

import com.nd.voice.meetingroom.manager.User;
import com.nd.voice.meetingroom.manager.UserManagerApi;
import com.nd.voice.meetingroom.manager.UserManagerCallBack;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import cn.nd.social.R;
import cn.nd.social.account.usermanager.UserManager;
import cn.nd.social.common.ShareThisApp;
import cn.nd.social.contacts.manager.ContactDBHelper;
import cn.nd.social.contacts.manager.ContactManager;
import cn.nd.social.contacts.manager.ContactManagerCallBack;
import cn.nd.social.contacts.manager.GetContacts;
import cn.nd.social.contacts.manager.MemberContact;

public class ContactListActivity extends Activity implements UserManagerCallBack, ContactManagerCallBack{
	
	private ListView mListView;
	private Button btn_back;
	private List<MemberContact> mList = new ArrayList<MemberContact>();
	private UserManagerApi mUserManagerApi;
	private MemberContact mWaitContact;
	private ContactManager mContactManager;
	
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
					btn_action.setText("已添加为好友");
					btn_action.setEnabled(false);
				}else{
					btn_action.setText("添加");
				}
			}
			btn_action.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(final View view) {
					if(contact.getUid() == null){
						sendSms(contact);
					}else{
						mWaitContact = contact;
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
		showProgressDialog();
		loadFromDB();
		List<MemberContact> list =  GetContacts.getPhoneContacts(this);
		mContactManager.queryContactMembers(list);
		dismissProgressDialog();
	}
	
	private void loadFromDB() {
		mList.clear();
		mList.addAll(ContactDBHelper.getInstance().getContacts());
		adapter.notifyDataSetChanged();
	}

	
	ProgressDialog mProgress;
	
	private void showProgressDialog() {
		mProgress = new ProgressDialog(this);
		mProgress.setMessage("正在获取朋友信息");
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
		mWaitContact.setFriend(true);
		ContactDBHelper.getInstance().save(mWaitContact);
		loadFromDB();
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
