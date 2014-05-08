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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import cn.nd.social.R;
import cn.nd.social.TabLauncherUI;
import cn.nd.social.account.BootActivity;
import cn.nd.social.account.CloundServer;
import cn.nd.social.account.usermanager.UserManager;

import com.nd.voice.meetingroom.manager.User;
import com.nd.voice.meetingroom.manager.UserManagerApi;
import com.nd.voice.meetingroom.manager.UserManagerCallBack;

public class LoginActivity extends Activity implements UserManagerCallBack{
	
	ImageView iv_face;
	TextView  tv_username;
	TextView tv_password;
	Button btn_regiter;
	Button btn_login;
	Button btn_forget;
	Button btn_try;
	Button btn_back;
	
	UserManagerApi mUserManagerApi;
	
	ProgressDialog mProgress;
	
	private boolean isFrameState;//是否为被Frame调用
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		otherInit();
		setupView();
		setupListener();
	}


	
	private void setupView() {
		iv_face = (ImageView) findViewById(R.id.iv_face);
		tv_username = (TextView) findViewById(R.id.tv_username);
		tv_password = (TextView) findViewById(R.id.tv_password);
		btn_regiter = (Button) findViewById(R.id.btn_regiter);
		btn_login = (Button) findViewById(R.id.btn_login);
		btn_try = (Button) findViewById(R.id.btn_try);
		btn_back = (Button)findViewById(R.id.btn_back);
		if(mUserManagerApi.isFirstUse()){
			btn_back.setVisibility(View.VISIBLE);
			btn_regiter.setVisibility(View.GONE);
		}else{
			btn_back.setVisibility(View.GONE);
			btn_regiter.setVisibility(View.VISIBLE);
		}
		String user = CloundServer.getInstance().getLogedUser();
		String pwd = CloundServer.getInstance().getLogPasswd();
		tv_username.setText(user);
		tv_password.setText(pwd);
		if(isFrameState){
			btn_try.setVisibility(View.INVISIBLE);
		}
	}

	private void setupListener() {
		btn_back.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				LoginActivity.this.finish();
			}
		});
		
		btn_regiter.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				LoginActivity.this.openRegiterActivity();
			}
		});
		
		btn_login.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				LoginActivity.this.loginAction();
			}
		});
		
//		
//		btn_forget.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				
//			}
//		});
//		
		btn_try.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				LoginActivity.this.launchMainScreen();
			}
		});
	}
	
	private void otherInit(){
		mUserManagerApi = new UserManager(this);
		isFrameState = this.getIntent().getBooleanExtra("isFrameState", false);
//		if(user != null && !user.equals("") 
//				&& pwd != null && !pwd.equals("")) {
//			mUserManagerApi.login(tv_username.getText().toString(), tv_password.getText().toString());
//		}	
	}
	
	private void openRegiterActivity(){
		Intent intent = new Intent(this, RegiterActivity.class);
		startActivity(intent);
	}
	
	private void loginAction(){
		if(tv_username.getText().length() == 0){
			new AlertDialog.Builder(this) 
			.setTitle("提交")
			.setMessage("请输入手机号")
			.setPositiveButton("确定",null)
			.show();
			return;
		}
		if(tv_password.getText().length() == 0){
			new AlertDialog.Builder(this) 
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
			Toast.makeText(this, "欢迎回来，" + mUserManagerApi.getMyInfo().getNickName(), 500).show();
			if(isFrameState){
				this.finish();
			}else{
				launchMainScreen();
			}
			
		}else{
			Toast.makeText(this, msg, 1500).show();
		}
	}


	@Override
	public void onRegisterCallBack(User user, boolean success, String msg) {
		// TODO Auto-generated method stub
		
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
			List<String> noFriendMobiles, List<Long> noFriendUids,
			boolean success, String msg) {
		// TODO Auto-generated method stub
		
	}

}
