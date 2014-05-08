package cn.nd.social.card;

import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.nd.social.R;
import cn.nd.social.card.CardViewCfgReader.CardLayoutCfg;
import cn.nd.social.ui.controls.CustomizeableLayout;
import cn.nd.social.ui.controls.FrameLayoutEx;

import com.nineoldandroids.view.ViewHelper;

public class CardViewer extends Activity {
	LayoutInflater mInflater;
	LinearLayout mCardBody;
	ImageView mLogo;
	CustomizeableLayout mCustView;
	View mAddToContact;
	private CardLayoutCfg mXmlConfig;// get configuration from xml file
	private Bitmap mBgBitmap;

	private TextView mMobileLabel;
	private TextView mPhoneLabel;
	private TextView mEmailLabel;
	private TextView mAddrLabel;

	private TextView mNameText;
	private TextView mCompanyText;
	private TextView mTitleText;
	private TextView mMobileText;
	private TextView mPhoneText;
	private TextView mEmailText;
	private TextView mAddrText;
	private int mBackGroudID;

	LinearLayout mMobileLayout;
	LinearLayout mPhoneLayout;
	LinearLayout mEmailLayout;
	LinearLayout mAddrLayout;

	private FrameLayoutEx mCustRoot;
	private int mCustWidth;
	private int mCustHeight;

	
	private final static int CUST_VIEW_DEFAULT_WIDTH = 440;
	private final static int CUST_VIEW_DEFAULT_HEIGHT = 660;
	private final static int EVENT_UPDATE_VIEW = 10000;
	private final static double DEFAULT_SCALE = (double) CUST_VIEW_DEFAULT_HEIGHT
			/ CUST_VIEW_DEFAULT_WIDTH;
	private double mScale = 1;
	
	private CardEntity mCard;
	
	
	private boolean mHasCard = true;
	private TextView mEmptyInfo;

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {
			case EVENT_UPDATE_VIEW:
				updateView();
				break;
			}
		}

	};

	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.card_viewer);

		Intent intent = getIntent();
		int id = intent.getIntExtra("card_id", 0);
		mCard = new CardEntity(this, id, true);
		if(id == 0 ) {
			mHasCard = false;
			findViewById(R.id.card_content).setVisibility(View.GONE);
			findViewById(R.id.empty_card_view).setVisibility(View.VISIBLE);
			mEmptyInfo = (TextView)findViewById(R.id.empty_info);
		} else {
			mBackGroudID = mCard.getBgId();
			mXmlConfig = CardViewCfgReader.getXmlConfig(this,mBackGroudID);
	
			initViewRefs();
			mCustRoot.setOnMeasureListener(new FrameLayoutEx.OnMeasureListener() {
				@Override
				public void onDimenMesured() {
					mHandler.sendEmptyMessage(EVENT_UPDATE_VIEW);
				}
			});
	
			mAddToContact.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					insertContact();
				}
			});
			ViewHelper.setRotation(mCustView, 90);
			
		}

		//Extension for msg details
		View back = findViewById(R.id.back);
		if(back != null) {
			back.setOnClickListener(new OnClickListener() {			
				@Override
				public void onClick(View v) {
					onBackPressed();				
				}
			});
		}
		View menu = findViewById(R.id.right_btn);
		if(menu != null) {
			menu.setOnClickListener(new OnClickListener() {			
				@Override
				public void onClick(View v) {
					Intent menuIntent = new Intent (CardViewer.this,MsgMenuDialog.class);			
					startActivityForResult(menuIntent,1000);			
				}
			});
			
		}
		
	}

	private void initViewRefs() {
		mCustRoot = (FrameLayoutEx) findViewById(R.id.tab_card);
		mCustView = (CustomizeableLayout) findViewById(R.id.customize_view);
		mAddToContact = findViewById(R.id.add_to_contact);

		mNameText = (TextView) findViewById(R.id.cust_name_text);
		mCompanyText = (TextView) findViewById(R.id.cust_name_text);
		mTitleText = (TextView) findViewById(R.id.cust_title_text);
		mMobileText = (TextView) findViewById(R.id.cust_mobile_text);
		mPhoneText = (TextView) findViewById(R.id.cust_phone_text);
		mEmailText = (TextView) findViewById(R.id.cust_email_text);
		mAddrText = (TextView) findViewById(R.id.cust_addr_text);

		mMobileLabel = (TextView) findViewById(R.id.cust_mobile_label);
		mPhoneLabel = (TextView) findViewById(R.id.cust_phone_label);
		mEmailLabel = (TextView) findViewById(R.id.cust_email_label);
		mAddrLabel = (TextView) findViewById(R.id.cust_addr_label);

		mMobileLayout = (LinearLayout) findViewById(R.id.mobile_layout);
		mPhoneLayout = (LinearLayout) findViewById(R.id.phone_layout);
		mEmailLayout = (LinearLayout) findViewById(R.id.email_layout);
		mAddrLayout = (LinearLayout) findViewById(R.id.addr_layout);
	}

	private void initCardData(){
		Intent intent = getIntent();
		String name = intent.getStringExtra("name");
		String title = intent.getStringExtra("title");
		long utc = intent.getLongExtra("utc",0);

		int id = intent.getIntExtra("card_id", 0);
		if(id == 0 ) {
			String info = getString(R.string.from_card_prefix) 
					+  "\"" + name + "\"" + getString(R.string.from_card_suffix);
			Date date = new Date(utc);
			mEmptyInfo.setText(date.toLocaleString() +"\n" + info);
		} else {			
			mBackGroudID = mCard.getBgId();
		}
	}
	@Override
	protected void onResume() {
		Intent intent = getIntent();
		
		//TODO: show detail message
		String name = intent.getStringExtra("name");
		String title = intent.getStringExtra("title");
		long utc = intent.getLongExtra("utc",0);

		int id = intent.getIntExtra("card_id", 0);
		if(id == 0 ) {
			String info = intent.getStringExtra("content");
			if(info == null) {
				info = getString(R.string.from_card_prefix) 
					+  "\"" + name + "\"" + getString(R.string.from_card_suffix);
			}
			Date date = new Date(utc);
			mEmptyInfo.setText(date.toLocaleString() +"\n" + info);
		} else {
			mNameText.setText(mCard.getName());
			mCompanyText.setText(mCard.getCompany());
			mTitleText.setText(mCard.getTitle());
			mMobileText.setText(mCard.getMobile());
			mPhoneText.setText(mCard.getPhone());
			mEmailText.setText(mCard.getEmail());
			mAddrText.setText(mCard.getAddr());
			mBackGroudID = mCard.getBgId();
		}
		super.onResume();
	}

	private void updateView() {
		mCustWidth = mCustRoot.getWidthEx();
		mCustHeight = mCustRoot.getHeightEx();
		if (mCustWidth == 0 || mCustHeight == 0) {
			DisplayMetrics metrics = getResources().getDisplayMetrics();
			mCustWidth = metrics.widthPixels;
			mCustHeight = metrics.heightPixels;
		}

		double currScale = (double) mCustHeight / mCustWidth;
		if (currScale > DEFAULT_SCALE) {
			mCustHeight = (int) (mCustWidth * DEFAULT_SCALE);
			mScale = (double) mCustWidth / CUST_VIEW_DEFAULT_WIDTH;
		} else {
			mScale = (double) mCustHeight / CUST_VIEW_DEFAULT_HEIGHT;
		}

		int adjustWidth = (int) (CUST_VIEW_DEFAULT_WIDTH * mScale);
		if (adjustWidth % 2 == 1) {
			adjustWidth++;
		}
		int adjustHeight = (int) (CUST_VIEW_DEFAULT_HEIGHT * mScale);
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				adjustHeight,adjustWidth,Gravity.CENTER);
		mCustRoot.updateViewLayout(mCustView, params);

		updateCustViewFromConfig();
	}

	private void insertContact() {
		if(mNameText == null) {
			return;
		}
		String name = mNameText.getText().toString();
		String phone = mMobileText.getText().toString();
		String email = mEmailText.getText().toString();
		String title = mTitleText.getText().toString();
		String company = mCompanyText.getText().toString();
		// address is not available in ContactsContract.Intents.Insert
		// insert or update
		Intent inOrUp = new Intent(ContactsContract.Intents.Insert.ACTION,
				ContactsContract.Contacts.CONTENT_URI);

		inOrUp.setType(ContactsContract.Contacts.CONTENT_TYPE);
		if (!name.equals("")) {
			inOrUp.putExtra(ContactsContract.Intents.Insert.NAME, name);
		}
		if (!phone.equals("")) {
			inOrUp.putExtra(ContactsContract.Intents.Insert.PHONE, phone);
		}
		if (!email.equals("")) {
			inOrUp.putExtra(ContactsContract.Intents.Insert.EMAIL, email);
		}
		if (!title.equals("")) {
			inOrUp.putExtra(ContactsContract.Intents.Insert.JOB_TITLE, title);
		}
		if(!company.equals("")) {
			inOrUp.putExtra(ContactsContract.Intents.Insert.COMPANY, company);
		}

		inOrUp.putExtra(ContactsContract.Data.IS_SUPER_PRIMARY, 1);
		try {
			startActivity(inOrUp);
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this, "can't add contact", Toast.LENGTH_SHORT)
					.show();
		}
	}

	private void scaleXYProp(CardLayoutCfg config) {
		scaleXYPropInt(config.nameProp);
		scaleXYPropInt(config.compProp);
		scaleXYPropInt(config.titleProp);
		scaleXYPropInt(config.mobileProp);
		scaleXYPropInt(config.phoneProp);
		scaleXYPropInt(config.emailProp);
		scaleXYPropInt(config.addrProp);
	}

	private void scaleXYPropInt(int[] prop) {
		prop[0] = (int) (prop[0] * mScale);
		prop[1] = (int) (prop[1] * mScale);
	}

	private void updateCustViewFromConfig() {

		if (mXmlConfig != null) {

			scaleXYProp(mXmlConfig);
			
			mCustView.setBackgroundResource(CardUtil.getCardBgId(mBackGroudID));

			setLabelProp(mMobileLabel, mXmlConfig.mobileProp);
			setLabelProp(mPhoneLabel, mXmlConfig.phoneProp);
			setLabelProp(mEmailLabel, mXmlConfig.emailProp);
			setLabelProp(mAddrLabel, mXmlConfig.addrProp);

			setTextProp(mNameText, mXmlConfig.nameProp);
			setTextProp(mCompanyText, mXmlConfig.compProp);
			setTextProp(mTitleText, mXmlConfig.titleProp);
			setTextProp(mMobileText, mXmlConfig.mobileProp, true);
			setTextProp(mPhoneText, mXmlConfig.phoneProp, true);
			setTextProp(mEmailText, mXmlConfig.emailProp, true);
			setTextProp(mAddrText, mXmlConfig.addrProp, true);

			updateCustViewLayout(mNameText, mXmlConfig.nameProp);
			updateCustViewLayout(mCompanyText, mXmlConfig.compProp);
			updateCustViewLayout(mTitleText, mXmlConfig.titleProp);

			updateCustViewLayout(mMobileLayout, mXmlConfig.mobileProp);
			updateCustViewLayout(mPhoneLayout, mXmlConfig.phoneProp);
			updateCustViewLayout(mEmailLayout, mXmlConfig.emailProp);
			updateCustViewLayout(mAddrLayout, mXmlConfig.addrProp);

		}
	}

	private void setLabelProp(TextView tv, int[] prop) {
		if (CardViewCfgReader.isColorSet(mXmlConfig.gStyle.defFontColor)) {
			tv.setTextColor(prop[3]);
		} else if (CardViewCfgReader.isColorSet(mXmlConfig.gStyle.defFontColor)) {
			tv.setTextColor(mXmlConfig.gStyle.defFontColor);
		}
		if (CardViewCfgReader.isFontSizeSet(prop[2])) {
			tv.setTextSize(CardUtil.SIZE_UNIT, CardViewCfgReader.getFontSize(prop[2],mScale));
		} else if (CardViewCfgReader.isFontSizeSet(mXmlConfig.gStyle.defFontSize)) {
			tv.setTextSize(CardUtil.SIZE_UNIT,
					CardViewCfgReader.getFontSize(mXmlConfig.gStyle.defFontSize,mScale));
		}
	}

	private void setTextProp(TextView tv, int[] prop) {
		setTextProp(tv, prop, false);
	}

	private void setTextProp(TextView tv, int[] prop, boolean needMargin) {
		if (CardViewCfgReader.isColorSet(mXmlConfig.gStyle.defFontColor)) {
			tv.setTextColor(prop[3]);
		} else if (CardViewCfgReader.isColorSet(mXmlConfig.gStyle.defFontColor)) {
			tv.setTextColor(mXmlConfig.gStyle.defFontColor);
		}
		if (CardViewCfgReader.isFontSizeSet(prop[2])) {
			tv.setTextSize(CardUtil.SIZE_UNIT, CardViewCfgReader.getFontSize(prop[2],mScale));
		} else if (CardViewCfgReader.isFontSizeSet(mXmlConfig.gStyle.defFontSize)) {
			tv.setTextSize(CardUtil.SIZE_UNIT,
					CardViewCfgReader.getFontSize(mXmlConfig.gStyle.defFontSize,mScale));
		}
		if (needMargin) {
			ViewGroup.LayoutParams params = tv.getLayoutParams();
			if (params instanceof ViewGroup.MarginLayoutParams) {
				((ViewGroup.MarginLayoutParams) params).rightMargin = prop[0];
			} else {
				ViewGroup.MarginLayoutParams marginParam = new ViewGroup.MarginLayoutParams(
						params);
				marginParam.setMargins(0, prop[0], 0, 0);
				tv.setLayoutParams(marginParam);
			}
		}
	}

	private void updateCustViewLayout(View v, int[] prop) {
		int x = prop[0];
		int y = prop[1];
		int len = LayoutParams.WRAP_CONTENT;
		mCustView.updateViewLayout(v, new CustomizeableLayout.LayoutParams(len,
				len, x, y));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mBgBitmap != null && !mBgBitmap.isRecycled()) {
			mBgBitmap.recycle();
			System.gc();
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == RESULT_OK) {
			switch(requestCode) {
			case REQUEST_CODE_MENU_ITME:
				int item = data.getIntExtra("item_selected", 0);
				if(item == 1) {
					if(!mHasCard) {
						Toast.makeText(getApplicationContext(), "no contact to add", Toast.LENGTH_SHORT).show();
					} else {
						insertContact();
					}
				}
				break;
			}
		}
	}
	private final static int REQUEST_CODE_MENU_ITME = 1000;
}
