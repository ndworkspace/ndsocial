package cn.nd.social.tobedelete;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;
import cn.nd.social.R;
import cn.nd.social.account.CAConstant;
import cn.nd.social.account.CloundServer;
import cn.nd.social.ui.controls.ClearableEditTextWithIcon;
import cn.nd.social.ui.controls.MobileEditText;
import cn.nd.social.util.Utils;

public class NewAccountActivity extends Activity {

	View mMobilePage;
	View mVerifyCodePage;
	View mPasswdPage;
	View mBack;
	View mNext;
	View mRegNew;
	View mConfirm;

	MobileEditText mUser;
	ClearableEditTextWithIcon mVerifyNumberEdit;
	ClearableEditTextWithIcon mPasswd;
	ClearableEditTextWithIcon mConfirmPasswd;
	private int mRegStatus = STATE_INIT;
	private static final int STATE_INIT = 0;
	private static final int STATE_VERIFY_PHONE_NUMBER = 1;
	private static final int STATE_VERIFY_PASSWD = 2;

	private static final int EVENT_GET_PHONE_VERIFY_CODE = 101;
	private static final int EVENT_VERIFY_CODE_SUCCESS = 102;
	private static final int EVENT_VERIFY_PASSWD_SUCCESS = 103;
	private static final int EVENT_SHOW_LOGIN = 104;

	private static final int DELAY_DEBUG = 1000; // just for debug
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case EVENT_GET_PHONE_VERIFY_CODE:
				dismissProgressDialog(); // just for debug
				mRegStatus = STATE_VERIFY_PHONE_NUMBER;
				togglePage();
				break;
			case EVENT_VERIFY_CODE_SUCCESS:
				dismissProgressDialog();
				if (mVerifyNumberEdit.getText().toString().equals("888888")) { // just
																				// for
																				// debug
					mRegStatus = STATE_VERIFY_PASSWD;
					togglePage();
				} else {
					showToast(getString(R.string.verify_code_error));
				}
				break;
			case EVENT_VERIFY_PASSWD_SUCCESS:
				break;
			case CAConstant.APP_EVENT_REGISTER_RSP:
				dismissProgressDialog();
				if (msg.arg1 == 0) {// success
					showToast(getString(R.string.reg_success_hint));
					Message loginMsg = obtainMessage();
					loginMsg.what = EVENT_SHOW_LOGIN;
					sendMessageDelayed(loginMsg, 1000);
				} else if (msg.obj instanceof String) {
					showToast((String) msg.obj);
				} else {
					showToast(getString(R.string.reg_fail_hint));
				}
				break;
			case EVENT_SHOW_LOGIN:
				showLogin();
				break;
			default:
				break;
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register_layout);
		mRegStatus = STATE_INIT;
		setupView();
		togglePage();
	}

	private void setupView() {
		mBack = findViewById(R.id.button_tiltebar_back);
		mNext = findViewById(R.id.verify_button);
		mConfirm = findViewById(R.id.confirm_btn);
		mRegNew = findViewById(R.id.register_new_btn);

		mMobilePage = findViewById(R.id.mobile_page);
		mVerifyCodePage = findViewById(R.id.verify_code_page);
		mPasswdPage = findViewById(R.id.simple_reg_page);

		mUser = (MobileEditText) findViewById(R.id.mobile_edittext);

		mVerifyNumberEdit = (ClearableEditTextWithIcon) findViewById(R.id.verify_code_edittext);
		mVerifyNumberEdit.setIconResource(R.drawable.g_ic_passwd);

		mPasswd = (ClearableEditTextWithIcon) findViewById(R.id.passwd_text);
		mConfirmPasswd = (ClearableEditTextWithIcon) findViewById(R.id.passwd_confirm_text);
		mPasswd.setIconResource(R.drawable.g_ic_passwd);
		mConfirmPasswd.setIconResource(R.drawable.g_ic_passwd);

		mBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				goBack();
			}
		});

		mRegNew.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// sendPasswd(mPasswd.getText().toString());
				if (verifyRegInfo()) {
					sendRegInfo(mUser.getTextString(), mPasswd.getTextString());
				}
			}
		});

		mNext.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				moveForward();
			}
		});
	}

	private void togglePage() {
		mMobilePage.setVisibility(View.GONE);
		mVerifyCodePage.setVisibility(View.GONE);
		mPasswdPage.setVisibility(View.GONE);
		mConfirm.setVisibility(View.GONE);

		if (mRegStatus == STATE_INIT) {
			mMobilePage.setVisibility(View.VISIBLE);
			mNext.setVisibility(View.VISIBLE);
		} else if (mRegStatus == STATE_VERIFY_PHONE_NUMBER) {
			mVerifyCodePage.setVisibility(View.VISIBLE);
			mNext.setVisibility(View.VISIBLE);
			mVerifyNumberEdit.requestFocus();
		} else if (mRegStatus == STATE_VERIFY_PASSWD) {
			mPasswdPage.setVisibility(View.VISIBLE);
			mPasswd.requestFocus();
			mNext.setVisibility(View.GONE);
		}
	}

	private void goBack() {
		if (mRegStatus == STATE_INIT) {
			finish();
		} else if (mRegStatus == STATE_VERIFY_PHONE_NUMBER) {
			mRegStatus = STATE_INIT;
			togglePage();
		} else if (mRegStatus == STATE_VERIFY_PASSWD) {
			mRegStatus = STATE_VERIFY_PHONE_NUMBER;
			togglePage();
		}
	}

	private void moveForward() {
		if (mRegStatus == STATE_INIT) {
			if (isEditTextEmpty(mUser)) {
				showToast(getString(R.string.user_name_empty_hint));
				return;
			} else {
				String user = mUser.getText().toString();
				if (user.length() != 11 || !user.startsWith("1")) {
					showToast(getString(R.string.phone_num_error));
					return;
				}
			}
			sendUserId(mUser.getText().toString());
			showProgressDialog();
			if (true) { // just for debug
				Message msg = mHandler.obtainMessage();
				msg.what = EVENT_GET_PHONE_VERIFY_CODE;
				mHandler.sendMessageDelayed(msg, DELAY_DEBUG);
			}
		} else if (mRegStatus == STATE_VERIFY_PHONE_NUMBER) {
			sendVerifyCode(mVerifyNumberEdit.getText().toString());
			showProgressDialog();
			if (true) {// just for debug
				Message msg = mHandler.obtainMessage();
				msg.what = EVENT_VERIFY_CODE_SUCCESS;
				mHandler.sendMessageDelayed(msg, DELAY_DEBUG);
			}

		} else if (mRegStatus == STATE_VERIFY_PASSWD) {
			sendPasswd(mPasswd.getText().toString());
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

	private boolean verifyRegInfo() {
		boolean passed = false;
		if (isEditTextEmpty(mUser)) {
			showToast(getString(R.string.user_name_fail));
		} else if (isEditTextEmpty(mPasswd)) {
			showToast(getString(R.string.passwd_fail));
		} else if (mPasswd.getTextString().length() < 6) {
			showToast(getString(R.string.passwd_too_short));
		} else if (isEditTextEmpty(mConfirmPasswd)) {
			showToast(getString(R.string.confirm_pass_fail));
		} else if (!mConfirmPasswd.getTextString().equals(
				mPasswd.getTextString())) {
			showToast(getString(R.string.passwd_verify_fail));
		} else {
			passed = true;
		}
		return passed;
	}

	private Boolean isEditTextEmpty(EditText edit) {
		return edit.getText().toString().equals("");
	}

	private void showToast(String text) {
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}

	private void sendRegInfo(String user, String passwd) {
		if (!CloundServer.getInstance().isNetworkReady()) {
			Toast.makeText(this, "network not ready,", Toast.LENGTH_SHORT)
					.show();
			return;
		}
		if (CloundServer.getInstance().getCARequest().registerUser(user, passwd,
				mHandler)) {
			showProgressDialog();
		} else {
			Toast.makeText(this, "registering, please wait", Toast.LENGTH_SHORT)
					.show();
		}
	}

	private void getRegReply() {

	}

	private void sendUserId(String userId) {
		
	}

	private void sendUserIdReply(String userId, int result) {
		dismissProgressDialog();
	}

	private void sendVerifyCode(String code) {

	}

	private void sendVerifyCodeReply(String userId, int result) {

	}

	private void sendPasswd(String passwd) {
		if (true) {
			showToast(getString(R.string.reg_success_hint));
			Intent intent = new Intent(this, LoginActivityBack.class);
			startActivity(intent);
			finish();
		}
	}

	private void showLogin() {
		Intent intent = new Intent(this, LoginActivityBack.class);
		intent.putExtra(Utils.INTENT_STR_USRE_NAME, mUser.getText().toString());
		startActivity(intent);
		finish();
	}

	private void sendPasswdReply(String userId, int result) {

	}

}
