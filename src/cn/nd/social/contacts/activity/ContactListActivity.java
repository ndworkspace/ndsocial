package cn.nd.social.contacts.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import cn.nd.social.ui.controls.ClearableEditTextWithIcon;
import cn.nd.social.util.PinYin;
import cn.nd.social.util.UnicodeGBK2Alpha;

import com.nd.voice.meetingroom.manager.User;
import com.nd.voice.meetingroom.manager.UserManagerApi;
import com.nd.voice.meetingroom.manager.UserManagerCallBack;

public class ContactListActivity extends Activity implements UserManagerCallBack, ContactManagerCallBack{
	
	private ListView mListView;
	private ClearableEditTextWithIcon et_search;
	private Button btn_back;
	private List<MemberContact> mAllList = new ArrayList<MemberContact>();
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
		et_search = (ClearableEditTextWithIcon) findViewById(R.id.et_search);
	}

	private void registerEvent() {
		btn_back.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		
		et_search.addTextChangedListener(new TextWatcher() {
			
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
			public void afterTextChanged(Editable editable) {
				// TODO Auto-generated method stub
				mList.clear();
				List list = ContactListActivity.search(editable.toString(), mAllList);
				mList.addAll(list);
				adapter.notifyDataSetChanged();
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
				btn_action.setTextColor(0xff9EE4FF);
				btn_action.setEnabled(true);
			}else{
				btn_action.setEnabled(true);
				if(contact.isFriend()){
					btn_action.setText("已添加");
					btn_action.setTextColor(0xffCACCCC);
					btn_action.setEnabled(false);
				}else{
					btn_action.setText("添加");
					btn_action.setTextColor(0xff00FF9C);
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
				mContactManager.queryContactMembers(list);
			}
		}).run();
	}
	
	private void loadFromDB() {
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				mAllList.clear();
				mAllList.addAll(ContactDBHelper.getInstance().getContacts());
				mList.clear();
				
				mList = ContactListActivity.search("", mAllList);
				adapter.notifyDataSetChanged();
			}
		});
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
			mAllList.remove(mWaitContact);
			mAllList.add(mWaitContact);
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
	
	/**
	 * 按号码-拼音搜索联系人
	 * 
	 * @param str
	 */
	public static List<MemberContact> search(final String str,
			final List<MemberContact> allContacts) {
		ArrayList<MemberContact> contactList = new ArrayList<MemberContact>();
		if(str.isEmpty()){
			contactList.addAll(allContacts);
			return contactList;
		}
		// 如果搜索条件以0 1 +开头则按号码搜索
		if (str.startsWith("0") || str.startsWith("1") || str.startsWith("+")) {
			for (MemberContact contact : allContacts) {
				if (contact.getPhoneNumber() != null && contact.getContactName() != null) {
					if (contact.getPhoneNumber().contains(str)
							|| contact.getContactName().contains(str)) {
						contactList.add(contact);
					}
				}
			}
			return contactList;
		}

		// final ChineseSpelling finder = ChineseSpelling.getInstance();
		// finder.setResource(str);
		// final String result = finder.getSpelling();
		// 先将输入的字符串转换为拼音
		// final String result = PinYinUtil.getFullSpell(str);
		final String result = PinYin.getPinYin(str);
		for (MemberContact contact : allContacts) {
			if (contains(contact, result)) {
				contactList.add(contact);
			}
		}

		return contactList;
	}
	
	/**
	 * 根据拼音搜索
	 * 
	 * @param str
	 *            正则表达式
	 * @param pyName
	 *            拼音
	 * @param isIncludsive
	 *            搜索条件是否大于6个字符
	 * @return
	 */
	public static boolean contains(MemberContact contact, String search) {
		if (TextUtils.isEmpty(contact.getContactName()) || TextUtils.isEmpty(search)) {
			return false;
		}

		boolean flag = false;

		// 简拼匹配,如果输入在字符串长度大于6就不按首字母匹配了
		if (search.length() < 6) {
			// String firstLetters = FirstLetterUtil.getFirstLetter(contact
			// .getName());
			// 获得首字母字符串
			String firstLetters = UnicodeGBK2Alpha
					.getSimpleCharsOfString(contact.getContactName());
			// String firstLetters =
			// PinYinUtil.getFirstSpell(contact.getName());
			// 不区分大小写
			Pattern firstLetterMatcher = Pattern.compile("^" + search,
					Pattern.CASE_INSENSITIVE);
			flag = firstLetterMatcher.matcher(firstLetters).find();
		}

		if (!flag) { // 如果简拼已经找到了，就不使用全拼了
			// 全拼匹配
			// ChineseSpelling finder = ChineseSpelling.getInstance();
			// finder.setResource(contact.getName());
			// 不区分大小写
			Pattern pattern2 = Pattern
					.compile(search, Pattern.CASE_INSENSITIVE);
			Matcher matcher2 = pattern2.matcher(PinYin.getPinYin(contact
					.getContactName()));
			flag = matcher2.find();
		}
		return flag;
	}


}
