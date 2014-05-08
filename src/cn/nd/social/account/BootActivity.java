package cn.nd.social.account;

import com.nd.voice.meetingroom.manager.UserManagerApi;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import cn.nd.social.R;
import cn.nd.social.SocialApplication;
import cn.nd.social.TabLauncherUI;
import cn.nd.social.account.activity.LoginActivity;
import cn.nd.social.account.activity.RegiterActivity;
import cn.nd.social.account.usermanager.UserManager;
import cn.nd.social.net.ClientLogin;
import cn.nd.social.tobedelete.LoginActivityBack;
import cn.nd.social.tobedelete.NewAccountActivity;
import cn.nd.social.util.Utils;

public class BootActivity extends Activity {
	private View mSignIn; // login
	private View mSignUp; // register
	public static BootActivity sSocialBoot;

	private SharedPreferences mPrefs;

	public final static int REQ_CODE_BUILD_MY_CARD = 1000;
	
	public UserManagerApi userManager;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.appstart_welcome);
		sSocialBoot = this;
		userManager = new UserManager();
		
		CloundServer.getInstance().init();
		
		if(!androidVersionCheckPass()) {			
			return;
		}
		if(userManager.isFirstUse()){
			Intent intent = new Intent(this,RegiterActivity.class);
			startActivityForResult(intent,REQ_CODE_BUILD_MY_CARD);
		}else if(!userManager.hasValidUser()){
			Intent intent = new Intent(this,LoginActivity.class);
			startActivityForResult(intent, REQ_CODE_BUILD_MY_CARD);
		}else if(userManager.hasValidUser()) {
			initView();
		}

}
	
	private boolean androidVersionCheckPass() {
		if(Utils.isAndroidVersionGood()) {
			return true;
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.hint);

		builder.setMessage(R.string.android_lower_version_hint);

		builder.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						onBackPressed();
					}
				});
		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {			
			@Override
			public void onCancel(DialogInterface dialog) {
				onBackPressed();
			}
		});
		builder.create().show();
		
		return false;
	}

	@SuppressWarnings("unused")
	private void initView() {
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		if(true) {
			Intent intent = new Intent(this, TabLauncherUI.class);
			//comment out 2014_0127, not show login
/*			Intent intent = new Intent(this, LoginActivity.class);
			intent.putExtra(Utils.INTENT_STR_USRE_NAME,
					SocialApplication.getUserName(mPrefs));*/
			
			startActivity(intent);
			finish();
			return;
		}
		
		if (!CAUtils.getUserName().equals("")) {
			if (CAUtils.isIMEIBinded(mPrefs)) {
				ClientLogin.sendLogin(CAUtils.getBindedIMEI(mPrefs));
				ClientLogin.setCurrentUser(CAUtils
						.getUserName());
				if (CloundServer.getInstance().isNetworkReady()) {
					CloundServer.getInstance().getCARequest().login(
							CAUtils.getUserName(), null, null);
				}
				Intent intent = new Intent(this, TabLauncherUI.class);
				startActivity(intent);
				finish();
				return;
			} else {
				Intent intent = new Intent(this, LoginActivityBack.class);
				intent.putExtra(Utils.INTENT_STR_USRE_NAME,
						CAUtils.getUserName());
				startActivity(intent);
				// finish();
			}
		}

		setContentView(R.layout.appstart);
		setupView();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == REQ_CODE_BUILD_MY_CARD) {
			if(resultCode == RESULT_OK) {
				initView();
			} else {
				finish();
				SocialApplication.getAppInstance().exit();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	protected void onDestroy() {
		sSocialBoot = null;
		super.onDestroy();
	}

	private void setupView() {
		mSignIn = findViewById(R.id.sign_in);// login
		mSignUp = findViewById(R.id.sign_up);// register
		mSignIn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(BootActivity.this,
						LoginActivityBack.class);
				startActivity(intent);
			}
		});

		mSignUp.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Intent intent = new Intent(BootActivity.this,
				// RegNewAccount.class);
				Intent intent = new Intent(BootActivity.this,
						NewAccountActivity.class);
				startActivity(intent);
			}
		});
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		SocialApplication.getAppInstance().exit();
	}
}