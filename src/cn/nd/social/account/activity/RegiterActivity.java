package cn.nd.social.account.activity;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import cn.nd.social.R;
import cn.nd.social.TabLauncherUI;
import cn.nd.social.account.BootActivity;
import cn.nd.social.account.CloundServer;
import cn.nd.social.account.usermanager.UserManager;
import cn.nd.social.card.CardUtil;
import cn.nd.social.card.CardUtil.CardData;

import com.nd.voice.meetingroom.manager.User;
import com.nd.voice.meetingroom.manager.UserManagerApi;
import com.nd.voice.meetingroom.manager.UserManagerCallBack;

public class RegiterActivity extends Activity implements UserManagerCallBack{
	
	Button btn_back;
	
	ImageView iv_face;
	EditText et_username;
	EditText et_password;
	EditText et_confirm;
	
	Button btn_more;

	View layout_more;
	EditText et_realname;
	EditText et_phone;
	EditText et_email;
	EditText et_company;
	EditText et_address;

	Button btn_submit;
	
	Button btn_hascard;
	Button btn_try;
	TextView tv_title;
	
	View ly_other;
	
	User mUserInfo;
	
	UserManagerApi mUserManager;
	
	private boolean isFrameState;//是否为被Frame调用
	
	private boolean isEditCardState;//是否为修改名片
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);
		otherInit();
		setupView();
		setupListener();
	}


	private void setupView() {
		btn_back = (Button) findViewById(R.id.btn_back);
		tv_title = (TextView)findViewById(R.id.tv_title);
		btn_hascard = (Button) findViewById(R.id.btn_hascard);
		ly_other = (View)findViewById(R.id.ly_other);
		if(mUserManager.isFirstUse()){
			btn_back.setVisibility(View.GONE);
			tv_title.setVisibility(View.VISIBLE);
			ly_other.setVisibility(View.VISIBLE);
		}else{
			btn_back.setVisibility(View.VISIBLE);
			tv_title.setVisibility(View.GONE);
			ly_other.setVisibility(View.GONE);
		}
		iv_face = (ImageView) findViewById(R.id.iv_face);
		iv_face.setImageResource(mUserInfo.getDefaultFaceResource());
		et_username = (EditText) findViewById(R.id.et_username);
		et_password = (EditText) findViewById(R.id.et_password);
		et_confirm = (EditText) findViewById(R.id.et_confirm);
		btn_more = (Button) findViewById(R.id.btn_more);
		layout_more = findViewById(R.id.layout_more);
		et_realname = (EditText) findViewById(R.id.et_realname);
		et_phone = (EditText)findViewById(R.id.et_phone);
		et_email = (EditText) findViewById(R.id.et_email);
		et_company = (EditText) findViewById(R.id.et_company);
		et_address = (EditText) findViewById(R.id.et_address);
		btn_submit = (Button) findViewById(R.id.btn_submit);
		btn_try = (Button)findViewById(R.id.btn_try);
		if(isFrameState){
			btn_try.setVisibility(View.INVISIBLE);
		}
		if(isEditCardState) {
			setEditView();			
		}
	}
	
	private void setEditView() {
		et_password.setVisibility(View.GONE);
		et_confirm.setVisibility(View.GONE);
		ly_other.setVisibility(View.GONE);
		btn_submit.setText(R.string.savecard);
		fillEditInfo();
	}
	
	private void fillEditInfo() {
		CardData cardData = CardUtil.getSelfCardData();
		et_realname.setText(cardData.name);
		et_username.setText(cardData.mobile);
		et_phone.setText(cardData.phone);
		et_email.setText(cardData.email);
		et_company.setText(cardData.company);
		et_address.setText(cardData.addr);
		//TODO: temporary solution
		mUserInfo.setDefaultFace(cardData.avatarId);
		iv_face.setImageResource(mUserInfo.getDefaultFaceResource());
	}
	
	private void saveCardData() {
		//if(isCardDirty()) {
			fillUserInfo();
			CardUtil.saveCardFromUser(mUserInfo);
			Intent intent = new Intent(CardUtil.ACTION_SELF_CARD_REFRESH);
			sendBroadcast(intent);
			Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
			
			CloundServer.getInstance().updateSelfInfo();
			this.finish();
		//}
		/* else {
			Toast.makeText(this, "名片未修改", Toast.LENGTH_SHORT).show();	
		}*/
	}
	
	private boolean isCardDirty() {
		CardData cardData = CardUtil.getSelfCardData();
		return !(cardData.name.equals(et_realname.getText().toString())
				&& cardData.mobile.equals(et_username.getText().toString())
				&& cardData.phone.equals(et_phone.getText().toString())
				&& cardData.email.equals(et_email.getText().toString())
				&& cardData.company.equals(et_company.getText().toString())
				&& cardData.addr.equals(et_address.getText().toString())
				&& cardData.avatarId == mUserInfo.getDefaultFace() );
	}

	private void setupListener() {
		btn_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				RegiterActivity.this.finish();
			}
		});
		
		btn_hascard.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				RegiterActivity.this.loginAction();
			}
		});
		
		iv_face.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				mUserInfo.getRandomFace();
				iv_face.setImageResource(mUserInfo.getDefaultFaceResource());
			}
		});
		btn_more.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(layout_more.getVisibility() == View.GONE){
					layout_more.setVisibility(View.VISIBLE);
				}else{
					layout_more.setVisibility(View.GONE);
				}
			}
		});
		
		if(isEditCardState) {
			btn_submit.setOnClickListener(new OnClickListener() {				
				@Override
				public void onClick(View v) {
					saveCardData();
				}
			});
		} else {
			btn_submit.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					submitAction();
				}
			});
		}
		
		btn_try.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				RegiterActivity.this.launchMainScreen();
			}
		});

	}
	
	protected void loginAction() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this, LoginActivity.class);
		startActivity(intent);
	}


	private void otherInit(){
		mUserInfo = new User();
		mUserInfo.getRandomFace();
		mUserManager = new UserManager(this);
		
		isFrameState = this.getIntent().getBooleanExtra("isFrameState", false);
		isEditCardState = this.getIntent().getBooleanExtra("isEditCardState", false);
	}
	
	//bussiness
	
	private void submitAction(){
		String error = this.validate();
		if(error != null){
			new AlertDialog.Builder(this) 
			.setTitle("提示")
			.setMessage(error)
			.setPositiveButton("确定",null)
			.show();
			return;
		}
		fillUserInfo();
		showProgressDialog();
		mUserManager.register(mUserInfo);
	}
	
	/**
	 * 验证数码
	 * @return	为null为通过，否则为错误信息
	 */
	private String validate(){
		if(et_realname.getText().length() == 0){
			return getString(R.string.real_name_fail);
		}else if(et_username.getText().length() == 0){
			return getString(R.string.login_input_phone_tips);
		}else if(et_password.getText().length() == 0){
			return getString(R.string.passwd_fail);
		}else if(et_confirm.getText().length() < 6){
			return getString(R.string.passwd_too_short);
		}else if(et_confirm.getText().length() == 0){
			return getString(R.string.confirm_pass_fail);
		}else if(!et_confirm.getText().toString().equals(et_password.getText().toString())){
			return getString(R.string.passwd_verify_fail);
		}
		return null;
	}
	
	private void fillUserInfo(){
		mUserInfo.setUserName(et_username.getText().toString());
		mUserInfo.setMobile(et_username.getText().toString());
		mUserInfo.setNickName(et_realname.getText().toString());
		mUserInfo.setPassword(et_password.getText().toString());
		mUserInfo.setEmail(et_email.getText().toString());
		mUserInfo.setPhone(et_phone.getText().toString());
		mUserInfo.setCompany(et_company.getText().toString());
		mUserInfo.setAddress(et_address.getText().toString());
	}


	@Override
	public void onGetUpdateUserInfoCallBack(long memberId, User userinfo,
			boolean success, String msg) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onGetFriendListCallBack(long memberId, List<User> friends,
			boolean success, String msg) {
		// TODO Auto-generated )method stub
		
	}


	@Override
	public void onLoginCallBack(String username, User user, boolean success,
			String msg) {
		// TODO Auto-generated method stub
		dismissProgressDialog();
		if(success){
			if(isFrameState){
				this.finish();
			}else{
				launchMainScreen();
			}
			Toast.makeText(this, R.string.reg_success_hint, 1000).show();
		}else{
			Toast.makeText(this, msg, 2000).show();
		}
	}


	@Override
	public void onRegisterCallBack(User user, boolean success, String msg) {
		if(success) {
			mUserManager.login(et_username.getText().toString(), et_password.getText().toString());
		} else {
			if(msg != null) {
				Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
			}
		}
		
	}
	
	private void launchMainScreen() {
		Intent intent = new Intent(this, TabLauncherUI.class);
		startActivity(intent);
		finish();
		if (BootActivity.sSocialBoot != null) {
			BootActivity.sSocialBoot.finish();
		}
		return;
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
	public void onAddFriendCallBack(long memberId, boolean success, String msg) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onQueryContactFriendCallBack(List<String> friendMobiles,
			List<String> noFriendMobiles, boolean success, String msg) {
		// TODO Auto-generated method stub
		
	}
	
}
