package cn.nd.social.card;

import cn.nd.social.R;
import cn.nd.social.card.CardViewCfgReader.CardLayoutCfg;
import cn.nd.social.ui.controls.CustomizeableLayout;
import cn.nd.social.util.Utils;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;


public class CardViewLayout extends CustomizeableLayout {

	private TextView mMobileLabel;
	private TextView mPhoneLabel;
	private TextView mEmailLabel;
	private TextView mAddrLabel;

	private TextView mNameText;
	private TextView mTitleText;
	private TextView mCompanyText;
	private TextView mMobileText;
	private TextView mPhoneText;
	private TextView mEmailText;
	private TextView mAddrText;

	private LinearLayout mMobileLayout;
	private LinearLayout mPhoneLayout;
	private LinearLayout mEmailLayout;
	private LinearLayout mAddrLayout;
	
	private Context mContext;
	private Bitmap mBgBitmap;
	private CardLayoutCfg mConfig;
	private float mScale;
	private float mFontScale;
	private boolean mDisposed  = false;
	public CardViewLayout(Context context) {
		super(context);
		mContext = context;
	}
	
	public CardViewLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}
	
	public CardViewLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
	}

	public void onDestroyCardView() {
		if(mBgBitmap != null && !mBgBitmap.isRecycled()) {
			mBgBitmap.recycle();		
		}
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if(!mDisposed) {
			updateView();
			mDisposed = true;
		}
	}
	@Override
	protected void onFinishInflate() {		
		mNameText = (TextView) findViewById(R.id.cust_name_text);
		mTitleText = (TextView) findViewById(R.id.cust_title_text);
		mCompanyText = (TextView)findViewById(R.id.cust_company_text);
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

		super.onFinishInflate();
	}
	
	
	private void updateView() {
		refreshView();
	}
	
	public void refreshView() {
		int width = getWidth();
		int modelId = Utils.getAppSharedPrefs().getInt(CardUtil.MODEL_ID,1);
		mConfig = CardViewCfgReader.getXmlConfig(Utils.getAppContext(), modelId);
		mScale = (float)width / mConfig.defaultWidth;
		if(mScale < 0.7) {
			mFontScale = mScale * 1.2f;
		} else if(mScale > 1.3)  {
			mFontScale = mScale / 1.2f;
		} else {
			mFontScale = mScale;
		}
		updateViewFromConfig(mConfig);
		updateItem();
		postInvalidate();
	}

	private void updateItem() {
		mNameText.setText(getPrefString(CardUtil.NAME_STR));
		mTitleText.setText(getPrefString(CardUtil.TITLE_STR));
		mMobileText.setText(getPrefString(CardUtil.MOBILE_STR));
		mPhoneText.setText(getPrefString(CardUtil.PHONE_STR));
		mEmailText.setText(getPrefString(CardUtil.EMAIL_STR));
		mAddrText.setText(getPrefString(CardUtil.ADDR_STR));
		mCompanyText.setText(getPrefString(CardUtil.COMPANY_STR));
		setBg();
	}
	
	private void setBg() {
		int modelId = Utils.getAppSharedPrefs().getInt(CardUtil.MODEL_ID, 1);
		int resId = CardUtil.getCardBgIdMini(modelId);
		setBackgroundResource(resId);	
	}
	
	private String getPrefString(String key) {
		return Utils.getAppSharedPrefs().getString(key, "");
	}
	
	private void updateViewFromConfig(CardLayoutCfg config) {

		if (config != null) {

			scaleProp(config);
			
			setBg();

			setLabelProp(mMobileLabel, config.mobileProp);
			setLabelProp(mPhoneLabel, config.phoneProp);
			setLabelProp(mEmailLabel, config.emailProp);
			setLabelProp(mAddrLabel, config.addrProp);

			setTextProp(mNameText, config.nameProp);
			setTextProp(mTitleText, config.titleProp);
			setTextProp(mCompanyText, config.compProp);
			setTextProp(mMobileText, config.mobileProp, true);
			setTextProp(mPhoneText, config.phoneProp, true);
			setTextProp(mEmailText, config.emailProp, true);
			setTextProp(mAddrText, config.addrProp, true);

			updateViewLayout(mNameText, config.nameProp);
			updateViewLayout(mTitleText, config.titleProp);
			updateViewLayout(mCompanyText, config.compProp);

			updateViewLayout(mMobileLayout, config.mobileProp);
			updateViewLayout(mPhoneLayout, config.phoneProp);
			updateViewLayout(mEmailLayout, config.emailProp);
			updateViewLayout(mAddrLayout, config.addrProp);

		}
	}
	
	private void setLabelProp(TextView tv, int[] prop) {
		if (CardViewCfgReader.isColorSet(mConfig.gStyle.defFontColor)) {
			tv.setTextColor(prop[3]);
		} else if (CardViewCfgReader.isColorSet(mConfig.gStyle.defFontColor)) {
			tv.setTextColor(mConfig.gStyle.defFontColor);
		}
		if (CardViewCfgReader.isFontSizeSet(prop[2])) {
			tv.setTextSize(CardUtil.SIZE_UNIT, CardViewCfgReader.getFontSize(prop[2],mFontScale));
		} else if (CardViewCfgReader.isFontSizeSet(mConfig.gStyle.defFontSize)) {
			tv.setTextSize(CardUtil.SIZE_UNIT,
					CardViewCfgReader.getFontSize(mConfig.gStyle.defFontSize,mFontScale));
		}
	}

	private void setTextProp(TextView tv, int[] prop) {
		setTextProp(tv, prop, false);
	}

	private void setTextProp(TextView tv, int[] prop, boolean needMargin) {
		if (CardViewCfgReader.isColorSet(mConfig.gStyle.defFontColor)) {
			tv.setTextColor(prop[3]);
		} else if (CardViewCfgReader.isColorSet(mConfig.gStyle.defFontColor)) {
			tv.setTextColor(mConfig.gStyle.defFontColor);
		}
		if (CardViewCfgReader.isFontSizeSet(prop[2])) {
			tv.setTextSize(CardUtil.SIZE_UNIT, CardViewCfgReader.getFontSize(prop[2],mFontScale));
		} else if (CardViewCfgReader.isFontSizeSet(mConfig.gStyle.defFontSize)) {
			tv.setTextSize(CardUtil.SIZE_UNIT,
					CardViewCfgReader.getFontSize(mConfig.gStyle.defFontSize,mFontScale));
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

	private void updateViewLayout(View v, int[] prop) {
		int x = prop[0];
		int y = prop[1];
		int len = LayoutParams.WRAP_CONTENT;
		updateViewLayout(v, new CustomizeableLayout.LayoutParams(len,len, x, y));
	}
	
	private void scaleProp(CardLayoutCfg config) {
		scalePropInt(config.nameProp);
		scalePropInt(config.titleProp);
		scalePropInt(config.compProp);
		scalePropInt(config.mobileProp);
		scalePropInt(config.phoneProp);
		scalePropInt(config.emailProp);
		scalePropInt(config.addrProp);
	}

	private void scalePropInt(int[] prop) {
		prop[0] = (int) (prop[0] * mScale);
		prop[1] = (int) (prop[1] * mScale);
		//prop[2] = (int) (prop[2] * mScale);
	}
}
