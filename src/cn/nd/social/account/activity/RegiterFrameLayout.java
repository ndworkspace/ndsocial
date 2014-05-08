package cn.nd.social.account.activity;

import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import cn.nd.social.R;
import cn.nd.social.account.usermanager.UserManager;

import com.nd.voice.meetingroom.manager.User;
import com.nd.voice.meetingroom.manager.UserManagerApi;
import com.nd.voice.meetingroom.manager.UserManagerCallBack;

public class RegiterFrameLayout extends FrameLayout implements UserManagerCallBack{
	
	View rootView;
	Context mContext;
	
	Button btn_back;
	
	ImageView iv_face;
	EditText et_username;
	EditText et_password;
	EditText et_confirm;
	
	Button btn_more;
	Button btn_try;

	View layout_more;
	EditText et_realname;
	EditText et_phone;
	EditText et_email;
	EditText et_company;
	EditText et_address;

	Button btn_submit;
	
	Button btn_hascard;
	TextView tv_title;
	
	View ly_other;
	
	User mUserInfo;
	
	UserManagerApi mUserManager;
	
	private RegiterFrameLayoutListener listener;
	
	
	public RegiterFrameLayout(Context context) {
		super(context);
		mContext = context;
		otherInit();
		rootView = LayoutInflater.from(mContext).inflate(
				R.layout.register, this);
		setupView();
		setupListener();
		
	}

	public RegiterFrameLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		otherInit();
		rootView = LayoutInflater.from(mContext).inflate(
				R.layout.register, this);
		setupView();
		setupListener();
	}

	public RegiterFrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		otherInit();
		rootView = LayoutInflater.from(mContext).inflate(
				R.layout.register, this);
		setupView();
		setupListener();
	}
	


	private void setupView() {
		btn_back = (Button) rootView.findViewById(R.id.btn_back);
		tv_title = (TextView)rootView.findViewById(R.id.tv_title);
		btn_hascard = (Button) rootView.findViewById(R.id.btn_hascard);
		ly_other = (View)rootView.findViewById(R.id.ly_other);
		btn_back.setVisibility(View.GONE);
		tv_title.setVisibility(View.VISIBLE);
		ly_other.setVisibility(View.VISIBLE);
		iv_face = (ImageView) rootView.findViewById(R.id.iv_face);
		iv_face.setImageResource(mUserInfo.getDefaultFaceResource());
		et_username = (EditText) rootView.findViewById(R.id.et_username);
		et_password = (EditText) rootView.findViewById(R.id.et_password);
		et_confirm = (EditText) rootView.findViewById(R.id.et_confirm);
		btn_more = (Button) rootView.findViewById(R.id.btn_more);
		layout_more = rootView.findViewById(R.id.layout_more);
		et_realname = (EditText) rootView.findViewById(R.id.et_realname);
		et_phone = (EditText)rootView.findViewById(R.id.et_phone);
		et_email = (EditText) rootView.findViewById(R.id.et_email);
		et_company = (EditText) rootView.findViewById(R.id.et_company);
		et_address = (EditText) rootView.findViewById(R.id.et_address);
		btn_submit = (Button) rootView.findViewById(R.id.btn_submit);
		btn_try = (Button) rootView.findViewById(R.id.btn_try);
		btn_try.setVisibility(View.INVISIBLE);
	}

	private void setupListener() {
		btn_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
//				RegiterFrameLayout.this.finish();
			}
		});
		
		btn_hascard.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				RegiterFrameLayout.this.loginAction();
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
		
		btn_submit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				submitAction();
			}
		});
		
	}
	
	protected void loginAction() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(mContext, LoginActivity.class);
		intent.putExtra("isFrameState", true);
		mContext.startActivity(intent);
	}


	private void otherInit(){
		mUserInfo = new User();
		mUserInfo.getRandomFace();
		mUserManager = new UserManager(this);
	}
	
	//bussiness
	
	private void submitAction(){
		String error = this.validate();
		if(error != null){
			new AlertDialog.Builder(mContext) 
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
			return mContext.getString(R.string.real_name_fail);
		}else if(et_username.getText().length() == 0){
			return mContext.getString(R.string.login_input_phone_tips);
		}else if(et_password.getText().length() == 0){
			return mContext.getString(R.string.passwd_fail);
		}else if(et_confirm.getText().length() < 6){
			return mContext.getString(R.string.passwd_too_short);
		}else if(et_confirm.getText().length() == 0){
			return mContext.getString(R.string.confirm_pass_fail);
		}else if(!et_confirm.getText().toString().equals(et_password.getText().toString())){
			return mContext.getString(R.string.passwd_verify_fail);
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
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onLoginCallBack(String username, User user, boolean success,
			String msg) {
		// TODO Auto-generated method stub
		dismissProgressDialog();
		if(success){
			if(this.listener != null){
				this.listener.onRegiterSuccess();
			}
			Toast.makeText(mContext, R.string.reg_success_hint, 1000).show();
		}else{
			Toast.makeText(mContext, msg, 2000).show();
		}
	}


	@Override
	public void onRegisterCallBack(User user, boolean success, String msg) {
		// TODO Auto-generated method stub
		if(success) {
			mUserManager.login(et_username.getText().toString(), et_password.getText().toString());
		} else {
			if(msg != null) {
				Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
			}
		}
		
	}
	
	
	ProgressDialog mProgress;
	
	private void showProgressDialog() {
		mProgress = new ProgressDialog(mContext);
		mProgress.setMessage(mContext.getText(R.string.wait_hint));
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

	public void setListener(RegiterFrameLayoutListener listener) {
		this.listener = listener;
	}

	@Override
	public void onAddFriendCallBack(long memberId, boolean success, String msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onQueryContactFriendCallBack(List<String> friendMobiles,
			List<String> noFriendMobiles, List<String> noFriendUids,
			boolean success, String msg) {
		// TODO Auto-generated method stub
		
	}
	
	
}
