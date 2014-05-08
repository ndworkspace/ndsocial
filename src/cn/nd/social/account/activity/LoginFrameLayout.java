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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import cn.nd.social.R;
import cn.nd.social.account.usermanager.UserManager;

import com.nd.voice.meetingroom.manager.User;
import com.nd.voice.meetingroom.manager.UserManagerApi;
import com.nd.voice.meetingroom.manager.UserManagerCallBack;


public class LoginFrameLayout extends FrameLayout implements UserManagerCallBack{
	
	private LoginFrameLayoutListener listener;
	
	public LoginFrameLayout(Context context) {
		super(context);
		mContext = context;
		rootView = LayoutInflater.from(mContext).inflate(
				R.layout.login, this);
		otherInit();
		setupView();
		setupListener();
	}

	public LoginFrameLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		rootView = LayoutInflater.from(mContext).inflate(
				R.layout.login, this);
		otherInit();
		setupView();
		setupListener();
	}

	public LoginFrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		rootView = LayoutInflater.from(mContext).inflate(
				R.layout.login, this);
		otherInit();
		setupView();
		setupListener();
	}



	ImageView iv_face;
	TextView  tv_username;
	TextView tv_password;
	Button btn_back;
	Button btn_regiter;
	Button btn_login;
	Button btn_forget;
	Button btn_try;
	View rootView;
	
	UserManagerApi mUserManagerApi;
	
	ProgressDialog mProgress;
	
	Context mContext;
	
	private void setupView() {
		btn_back = (Button) rootView.findViewById(R.id.btn_back);
		iv_face = (ImageView) rootView.findViewById(R.id.iv_face);
		tv_username = (TextView) rootView.findViewById(R.id.tv_username);
		tv_password = (TextView) rootView.findViewById(R.id.tv_password);
		btn_regiter = (Button) rootView.findViewById(R.id.btn_regiter);
		btn_login = (Button) rootView.findViewById(R.id.btn_login);
		btn_try = (Button) rootView.findViewById(R.id.btn_try);
		btn_try.setVisibility(View.INVISIBLE);
		if(!mUserManagerApi.isFirstUse()){
			btn_back.setVisibility(View.GONE);
		}
	}

	private void setupListener() {
		btn_regiter.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				LoginFrameLayout.this.openRegiterActivity();
			}
		});
		
		btn_login.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				LoginFrameLayout.this.loginAction();
			}
		});
		
//		btn_forget.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				
//			}
//		});
		
	}
	
	private void otherInit(){
		mUserManagerApi = new UserManager(this);
	}
	
	private void openRegiterActivity(){
		Intent intent = new Intent(mContext, RegiterActivity.class);
		intent.putExtra("isFrameState", true);
		mContext.startActivity(intent);
	}
	
	private void loginAction(){
		if(tv_username.getText().length() == 0){
			new AlertDialog.Builder(mContext) 
			.setTitle("提交")
			.setMessage("请输入手机号")
			.setPositiveButton("确定",null)
			.show();
			return;
		}
		if(tv_password.getText().length() == 0){
			new AlertDialog.Builder(mContext) 
			.setTitle("提示")
			.setMessage("请输入密码？")
			.setPositiveButton("确定",null)
			.show();
			return;
		}
		this.showProgressDialog();
		mUserManagerApi.login(tv_username.getText().toString(), tv_password.getText().toString());
	}
	

	@Override
	public void onGetUpdateUserInfoCallBack(long memberId, User userinfo,
			boolean success, String msg) {
		
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
		this.dismissProgressDialog();
		if(success){
			Toast.makeText(mContext, "欢迎回来，" + mUserManagerApi.getMyInfo().getNickName(), 500).show();
			if(this.listener != null){
				this.listener.onLoginSuccess();
			}
		}else{
			Toast.makeText(mContext, msg, 1500).show();
		}
	}


	@Override
	public void onRegisterCallBack(User user, boolean success, String msg) {
		// TODO Auto-generated method stub
		
	}
	
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

	public LoginFrameLayoutListener getListener() {
		return listener;
	}

	public void setListener(LoginFrameLayoutListener listener) {
		this.listener = listener;
	}

	@Override
	public void onAddFriendCallBack(long memberId, boolean success, String msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onQueryContactFriendCallBack(List<String> friendMobiles,
			List<String> noFriendMobiles, List<Long> noFriendUids,
			boolean success, String msg) {
		// TODO Auto-generated method stub
		
	}
	
}
