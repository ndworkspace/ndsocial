package cn.nd.social.card;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;
import cn.nd.social.R;
import cn.nd.social.util.Utils;

public class MyCardEditor extends Activity {
	private EditText mNameEdit;
	private EditText mCompanyEdit;
	private EditText mTitleEdit;
	private EditText mMobileEdit;
	private EditText mPhoneEdit;
	private EditText mEmailEdit;
	private EditText mAddrEdit;
	private SharedPreferences mPrefs;
	private View mSave;
	private View mCancel;
	
	public static final String KEY_MODEL_ID = "model_id";

	private String mNameStr;
	private String mCompanyStr;
	private String mTitleStr;
	private String mMobileStr;
	private String mPhoneStr;
	private String mEmailStr;
	private String mAddrStr;
	
	private int mModelID = -1;
	private View mSelectModel;

	private int mEnterType = 0;
	
	private final static int REQUEST_SELECT_CARD_TEMPLATE = 1000;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		mEnterType = intent.getIntExtra(Utils.MY_CARD_EDIT_TYPE, 0);
		setContentView(R.layout.my_card_editor_fancy);
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		initResRef();
		setupViews();
	}

	private void initResRef() {
		mNameEdit = (EditText) findViewById(R.id.name_id);
		mCompanyEdit = (EditText) findViewById(R.id.company_id);		
		mTitleEdit = (EditText) findViewById(R.id.occupation_id);
		mMobileEdit = (EditText) findViewById(R.id.mobile_id);
		mPhoneEdit = (EditText) findViewById(R.id.phone_id);
		mEmailEdit = (EditText) findViewById(R.id.email_id);
		mAddrEdit = (EditText) findViewById(R.id.address_id);
		mSave = findViewById(R.id.right_btn);
		mCancel = findViewById(R.id.left_btn);
		mSelectModel = findViewById(R.id.select_model);		

	}

	private void setupViews() {
		mNameStr = mPrefs.getString(CardUtil.NAME_STR, "");
		mCompanyStr = mPrefs.getString(CardUtil.COMPANY_STR, "");
		mTitleStr = mPrefs.getString(CardUtil.TITLE_STR, "");
		mMobileStr = mPrefs.getString(CardUtil.MOBILE_STR, "");
		mPhoneStr = mPrefs.getString(CardUtil.PHONE_STR, "");
		mEmailStr = mPrefs.getString(CardUtil.EMAIL_STR, "");
		mAddrStr = mPrefs.getString(CardUtil.ADDR_STR, "");

		mNameEdit.setText(mNameStr);
		mCompanyEdit.setText(mCompanyStr);
		mTitleEdit.setText(mTitleStr);
		mMobileEdit.setText(mMobileStr);
		mPhoneEdit.setText(mPhoneStr);
		mEmailEdit.setText(mEmailStr);
		mAddrEdit.setText(mAddrStr);

		if(mEnterType == Utils.MY_CARD_EDIT_TYPE_BOOT) {
			mCancel.setVisibility(View.GONE);
		}
		
		mSave.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SharedPreferences.Editor editor = mPrefs.edit();
				if (mNameEdit.getText().toString().equals("")) {
					Toast.makeText(MyCardEditor.this, R.string.name_empty_hint,
							Toast.LENGTH_SHORT).show();
					mNameEdit.requestFocus();
					return;
				}
				if (mMobileEdit.getText().toString().equals("")) {
					Toast.makeText(MyCardEditor.this, R.string.mobile_empty_hint,
							Toast.LENGTH_SHORT).show();
					mMobileEdit.requestFocus();
					return;
				}

				editor.putString(CardUtil.NAME_STR, mNameEdit.getText().toString());
				editor.putString(CardUtil.COMPANY_STR, mCompanyEdit.getText().toString());
				editor.putString(CardUtil.TITLE_STR, mTitleEdit.getText().toString());
				editor.putString(CardUtil.MOBILE_STR, mMobileEdit.getText().toString());
				editor.putString(CardUtil.PHONE_STR, mPhoneEdit.getText().toString());
				editor.putString(CardUtil.EMAIL_STR, mEmailEdit.getText().toString());
				editor.putString(CardUtil.ADDR_STR, mAddrEdit.getText().toString());

				editor.putBoolean(Utils.MY_CARD_BUILT_FLAG, true);// save built
				if(mModelID != -1) {													// flag
					editor.putInt(CardUtil.MODEL_ID, mModelID);
				}
				editor.commit();
				
				
				Intent intent = new Intent(CardUtil.ACTION_SELF_CARD_REFRESH);
				sendBroadcast(intent);
				
				if (isCardDirty()) {
					updateCardInfo();
				}
				mNameStr = mPrefs.getString(CardUtil.NAME_STR, "");
				mCompanyStr = mPrefs.getString(CardUtil.COMPANY_STR, "");
				mTitleStr = mPrefs.getString(CardUtil.TITLE_STR, "");
				mMobileStr = mPrefs.getString(CardUtil.MOBILE_STR, "");
				mPhoneStr = mPrefs.getString(CardUtil.PHONE_STR, "");
				mEmailStr = mPrefs.getString(CardUtil.EMAIL_STR, "");
				mAddrStr = mPrefs.getString(CardUtil.ADDR_STR, "");

				Toast.makeText(getApplicationContext(), R.string.save_success,
						Toast.LENGTH_SHORT).show();
				
				returnTarget();
			}
		});

		mCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				MyCardEditor.this.finish();
			}
		});
		
		mSelectModel.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MyCardEditor.this, CardTemplateSelector.class);
				startActivityForResult(intent, REQUEST_SELECT_CARD_TEMPLATE);
			}
		});
	}
	
	private void returnTarget() {
		if(mEnterType == Utils.MY_CARD_EDIT_TYPE_BOOT) {
			Intent returnIntent = new Intent();
			setResult(RESULT_OK, returnIntent);
		}
		finish();
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == REQUEST_SELECT_CARD_TEMPLATE) {
			if(resultCode == RESULT_OK) {
				mModelID = data.getIntExtra(KEY_MODEL_ID, 0);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	private void updateCardInfo() { // async update card info(will upload in
									// TabLauncherUI)
		SharedPreferences.Editor editor = Utils.getAppSharedPrefs().edit();
		editor.putBoolean(Utils.MY_CARD_UPLOAD_FLAG, false);
		editor.commit();
	}

	private boolean isCardDirty() {
		return !(mNameStr.equals(mNameEdit.getText().toString())
				&& mCompanyStr.equals(mCompanyEdit.getText().toString())
				&& mTitleStr.equals(mTitleEdit.getText().toString())
				&& mMobileStr.equals(mMobileEdit.getText().toString())
				&& mPhoneStr.equals(mPhoneEdit.getText().toString())
				&& mEmailStr.equals(mEmailEdit.getText().toString()) 
				&& mAddrStr.equals(mAddrEdit.getText().toString())
				&& mModelID == -1);
	}

	@Override
	public void onBackPressed() {
		if(mEnterType == Utils.MY_CARD_EDIT_TYPE_BOOT) {
			Toast.makeText(this, R.string.build_self_card_hint, Toast.LENGTH_SHORT).show();
			return;
		}
		
		if (isCardDirty()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(getString(R.string.hint))
					.setMessage(getString(R.string.exit_card_editor_hint))
					.setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
								}
							})
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									MyCardEditor.super.onBackPressed();// finish();
								}
							});
			builder.show();
		} else {
			super.onBackPressed();
		}
	}
}
