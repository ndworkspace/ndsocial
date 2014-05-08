package cn.nd.social.tobedelete;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;
import cn.nd.social.R;
import cn.nd.social.SocialApplication;
import cn.nd.social.TabLauncherUI;
import cn.nd.social.R.drawable;
import cn.nd.social.R.id;
import cn.nd.social.R.layout;
import cn.nd.social.R.string;
import cn.nd.social.account.BootActivity;
import cn.nd.social.account.CACallBack;
import cn.nd.social.account.CAConstant;
import cn.nd.social.account.CAUtils;
import cn.nd.social.account.CloundServer;
import cn.nd.social.util.Utils;

public class LoginActivityBack extends Activity {
	private EditText mUser;
	private EditText mPassword;
	View mLoginBtn;
	private SharedPreferences mPrefs;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		String userName = intent.getStringExtra(Utils.INTENT_STR_USRE_NAME);

		setContentView(R.layout.login);
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
//		mUser = (EditText) findViewById(R.id.login_user_edit);
//		mPassword = (EditText) findViewById(R.id.login_passwd_edit);
//		mLoginBtn = findViewById(R.id.login_btn);
		if (userName != null) {
			mUser.setText(userName);
		}
		mLoginBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onLoginRequest();
			}
		});
	}

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case CAConstant.APP_EVENT_LOGIN_RSP:
				dismissProgressDialog();
				if (msg.arg1 == CACallBack.RET_SUCCESS) {
					String str = mUser.getText().toString();
					CAUtils.saveUserName(str);
					CloundServer.getInstance().setLogeduser(str);
					launchMainScreen();
				} else {
					if (msg.obj instanceof String) {
						Toast.makeText(LoginActivityBack.this, (String) msg.obj,
								Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(LoginActivityBack.this, (String) msg.obj,
								Toast.LENGTH_SHORT).show();
					}
				}
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}

	};

	private void sendMobileInfo() {
		TelephonyManager mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		String imsi = mTelephonyMgr.getSubscriberId();
		String imei = mTelephonyMgr.getDeviceId();
	}

	private void onLoginRequest() {
		onLoginVerify();
		sendMobileInfo();
	}

	public void onLoginResponse(boolean success) {
		if (success) {
			CAUtils.saveUserName(mUser.getText().toString());
			sendMobileInfo();
		} else {
			Toast.makeText(this, getString(R.string.user_passwd_error),
					Toast.LENGTH_SHORT).show();
		}
	}

	public void onLoginVerify() {
		if ("123".equals(mUser.getText().toString())) { // just for debug
			String str = mUser.getText().toString();
			CAUtils.saveUserName(str);
			launchMainScreen();
			return;
		}
		if ("".equals(mUser.getText().toString())
				|| "".equals(mPassword.getText().toString())) {
			new AlertDialog.Builder(LoginActivityBack.this)
					.setIcon(
							getResources().getDrawable(
									R.drawable.login_error_icon))
					.setTitle(getString(R.string.login_fail))
					.setMessage(getString(R.string.user_passwd_empty)).create()
					.show();
		} else {
			if (!CloundServer.getInstance().isNetworkReady()) {
				Toast.makeText(this, "network not ready", Toast.LENGTH_SHORT)
						.show();
				return;
			}
			CloundServer.getInstance().getCARequest().login(
					mUser.getText().toString(), mPassword.getText().toString(),
					mHandler);
			showProgressDialog();
		}

	}

	ProgressDialog mProgress;

	private void showProgressDialog() {
		mProgress = new ProgressDialog(this);
		mProgress.setMessage(getText(R.string.wait_hint));
		mProgress.setIndeterminate(true);
		mProgress.setCancelable(false);
		mProgress.show();

	}

	private void dismissProgressDialog() {
		if (mProgress != null) {
			mProgress.dismiss();
			mProgress = null;
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

	public void login_back(View v) {
		this.finish();
	}

	public void login_pw(View v) {
		Uri uri = Uri.parse("http://www.nd.com.cn");
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		startActivity(intent);
	}
}
