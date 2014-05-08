package cn.nd.social.card;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import cn.nd.social.R;
import cn.nd.social.util.NDConfig;
import cn.nd.social.util.Utils;

public class BootCardEditor extends Activity {
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
	public static final String NAME_STR = "name";
	public static final String TITLE_STR = "title";
	public static final String MOBILE_STR = "mobile";
	public static final String PHONE_STR = "phone";
	public static final String EMAIL_STR = "email";
	public static final String ADDR_STR = "address";
	public static final String COMPANY_STR = "company";
	public static final String MODEL_ID = "model";
	
	public static final String KEY_MODEL_ID = "model_id";

	
	private int mModelID = -1;
	private View mSelectModel;
	private View mExtraItems;
	private View mMore;
	private ImageView mMoreIcon;
	private TextView mMoreText;
	
	private boolean mIsExtraShow = false;
	public final static String ACTION_SELF_CARD_REFRESH = "cn.nd.social.refresh_card";
	
	private final static int REQUEST_SELECT_CARD_TEMPLATE = 1000;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.boot_card_editor);
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		initResRef();
		setupViews();
		if(NDConfig.DEBUG) {
			mNameEdit.setText("Joe");
			mMobileEdit.setText("13800138000");
		}
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
		
		mMore = findViewById(R.id.ll_more);
		mMoreIcon = (ImageView)findViewById(R.id.iv_more);
		mMoreText = (TextView)findViewById(R.id.tv_more);
		
		mExtraItems = findViewById(R.id.extra_items);
		

	}

	private String getEditStr(EditText edit) {
		return edit.getText().toString();
	}
	
	private void setupViews() {
		mExtraItems.setVisibility(View.GONE);
		mCancel.setVisibility(View.GONE);
		
		mSave.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SharedPreferences.Editor editor = mPrefs.edit();
				if (getEditStr(mNameEdit).equals("")) {
					Toast.makeText(BootCardEditor.this, R.string.name_empty_hint,
							Toast.LENGTH_SHORT).show();
					mNameEdit.requestFocus();
					return;
				}
				if (getEditStr(mMobileEdit).equals("")) {
					Toast.makeText(BootCardEditor.this, R.string.mobile_empty_hint,
							Toast.LENGTH_SHORT).show();
					mMobileEdit.requestFocus();
					return;
				}

				editor.putString(NAME_STR, getEditStr(mNameEdit));
				editor.putString(COMPANY_STR,getEditStr(mCompanyEdit));
				editor.putString(TITLE_STR, getEditStr(mTitleEdit));
				editor.putString(MOBILE_STR, getEditStr(mMobileEdit));
				editor.putString(PHONE_STR, getEditStr(mPhoneEdit));
				editor.putString(EMAIL_STR, getEditStr(mEmailEdit));
				editor.putString(ADDR_STR, getEditStr(mAddrEdit));

				editor.putBoolean(Utils.MY_CARD_BUILT_FLAG, true);// save built flag
				if(mModelID != -1) {
					editor.putInt(MODEL_ID, mModelID);
				}
				editor.commit();			
				
				Intent intent = new Intent(ACTION_SELF_CARD_REFRESH);
				sendBroadcast(intent);				
				returnTarget();
			}
		});

		mCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				BootCardEditor.this.finish();
			}
		});
		
		mSelectModel.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(BootCardEditor.this, CardTemplateSelector.class);
				startActivityForResult(intent, REQUEST_SELECT_CARD_TEMPLATE);
			}
		});
		
		mMore.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				toggleExtraItems();				
			}
		});
	}
	
	private void toggleExtraItems() {
		mIsExtraShow = !mIsExtraShow;
		if(mIsExtraShow) {
			mExtraItems.setVisibility(View.VISIBLE);
			mMoreIcon.setImageResource(R.drawable.shrink_icon);
			mMoreText.setText(R.string.shrink_str);
		} else {
			mExtraItems.setVisibility(View.GONE);
			mMoreIcon.setImageResource(R.drawable.expand_icon);
			mMoreText.setText(R.string.more_str);
		}
	}
	
	private void returnTarget() {
		Intent returnIntent = new Intent();
		setResult(RESULT_OK, returnIntent);
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
	


	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}
}
