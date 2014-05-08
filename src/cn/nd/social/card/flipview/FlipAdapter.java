package cn.nd.social.card.flipview;

import java.util.ArrayList;
import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.nd.social.R;
import cn.nd.social.card.CardEntity;
import cn.nd.social.card.CardUtil;
import cn.nd.social.card.CardViewCfgReader;
import cn.nd.social.card.CardViewCfgReader.CardLayoutCfg;

import cn.nd.social.ui.controls.CustomizeableLayout;

public class FlipAdapter extends BaseAdapter implements OnClickListener {

	public interface Callback{
		public void onPageRequested(int page);
	}
	
	static class Item {
		static long id = 0;
		
		long mId;
		
		public Item() {
			mId = id++;
		}
		
		long getId(){
			return mId;
		}
	}
	
	private LayoutInflater inflater;
	private Callback callback;
	private List<Item> items = new ArrayList<Item>();
	private CardFlipActivity mFlipActivity;
	private CardLayoutCfg mXmlConfig;
	private double mScale = 1.0;
	
	public FlipAdapter(CardFlipActivity flipActivity,int count) {
		mFlipActivity = flipActivity;
		inflater = LayoutInflater.from(flipActivity);
		for(int i = 0 ; i<count ; i++){
			items.add(new Item());
		}
	}

	public void setCallback(Callback callback) {
		this.callback = callback;
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return items.get(position).getId();
	}
	
	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		

		CardEntity cardItem = mFlipActivity.getCardItem(position);
		int bgID = cardItem.getBgId();		
		mXmlConfig = CardViewCfgReader.getXmlConfig(mFlipActivity, bgID);
		
		mScale = (double)mFlipActivity.getTargetViewWidth() / mXmlConfig.defaultWidth;
		scaleXYProp(mXmlConfig);
		if(convertView == null){
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.card_flip_page, parent, false);			
			initViewRefs(convertView,holder);			
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		
		setupView(holder, position);
		bindData(position,holder);
		
		return convertView;
	}
	
	
	private void initViewRefs(View parent,ViewHolder holder) {
		//holder.mCardViewRoot = (FrameLayout)parent.findViewById(R.id.card_view_root);
		holder.mCustView = (CustomizeableLayout) parent.findViewById(R.id.customize_view);
		holder.mNameText = (TextView) parent.findViewById(R.id.cust_name_text);
		holder.mCompanyText = (TextView) parent.findViewById(R.id.cust_company_text);
		holder.mTitleText = (TextView) parent.findViewById(R.id.cust_title_text);
		holder.mMobileText = (TextView) parent.findViewById(R.id.cust_mobile_text);
		holder.mPhoneText = (TextView) parent.findViewById(R.id.cust_phone_text);
		holder.mEmailText = (TextView) parent.findViewById(R.id.cust_email_text);
		holder.mAddrText = (TextView) parent.findViewById(R.id.cust_addr_text);
		
		
		holder.mMobileLabel = (TextView) parent.findViewById(R.id.cust_mobile_label);
		holder.mPhoneLabel = (TextView) parent.findViewById(R.id.cust_phone_label);
		holder.mEmailLabel = (TextView) parent.findViewById(R.id.cust_email_label);
		holder.mAddrLabel = (TextView) parent.findViewById(R.id.cust_addr_label);

		holder.mMobileLayout = (LinearLayout) parent.findViewById(R.id.mobile_layout);
		holder.mPhoneLayout = (LinearLayout) parent.findViewById(R.id.phone_layout);
		holder.mEmailLayout = (LinearLayout) parent.findViewById(R.id.email_layout);
		holder.mAddrLayout = (LinearLayout) parent.findViewById(R.id.addr_layout);
	}

	
	private void setupView(ViewHolder holder,int position) {
		
		

		setLabelProp(holder.mMobileLabel, mXmlConfig.mobileProp);
		setLabelProp(holder.mPhoneLabel, mXmlConfig.phoneProp);
		setLabelProp(holder.mEmailLabel, mXmlConfig.emailProp);
		setLabelProp(holder.mAddrLabel, mXmlConfig.addrProp);

		setTextProp(holder.mNameText, mXmlConfig.nameProp);
		setTextProp(holder.mCompanyText, mXmlConfig.compProp);
		
		setTextProp(holder.mTitleText, mXmlConfig.titleProp);
		setTextProp(holder.mMobileText, mXmlConfig.mobileProp, true);
		setTextProp(holder.mPhoneText, mXmlConfig.phoneProp, true);
		setTextProp(holder.mEmailText, mXmlConfig.emailProp, true);
		setTextProp(holder.mAddrText, mXmlConfig.addrProp, true);

		updateCustViewLayout(holder,holder.mNameText, mXmlConfig.nameProp);
		updateCustViewLayout(holder,holder.mCompanyText, mXmlConfig.compProp);
		updateCustViewLayout(holder,holder.mTitleText, mXmlConfig.titleProp);

		updateCustViewLayout(holder,holder.mMobileLayout, mXmlConfig.mobileProp);
		updateCustViewLayout(holder,holder.mPhoneLayout, mXmlConfig.phoneProp);
		updateCustViewLayout(holder,holder.mEmailLayout, mXmlConfig.emailProp);
		updateCustViewLayout(holder,holder.mAddrLayout, mXmlConfig.addrProp);
		
		
		
		
		
	}
	
	private void bindData(int pos, ViewHolder holder) {
		CardEntity item = mFlipActivity.getCardItem(pos);
		holder.mNameText.setText(item.getName());
		holder.mTitleText.setText(item.getTitle());
		holder.mCompanyText.setText(item.getCompany());
		holder.mMobileText.setText(item.getMobile());
		holder.mPhoneText.setText(item.getPhone());
		holder.mEmailText.setText(item.getEmail());
		holder.mAddrText.setText(item.getAddr());
		holder.mCustView.setBackgroundResource(CardUtil.getCardBgId(item.getBgId()));		
		
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

	private void updateCustViewLayout(ViewHolder holder,View v, int[] prop) {
		int x = prop[0];
		int y = prop[1];
		int len = LayoutParams.WRAP_CONTENT;
		holder.mCustView.updateViewLayout(v, new CustomizeableLayout.LayoutParams(len,
				len, x, y));
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

	static class ViewHolder{
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

		private LinearLayout mMobileLayout;
		private LinearLayout mPhoneLayout;
		private LinearLayout mEmailLayout;
		private LinearLayout mAddrLayout;
		private CustomizeableLayout mCustView;
	
	}

	@Override
	public void onClick(View v) {

	}

	public void addItems(int amount) {
		for(int i = 0 ; i<amount ; i++){
			items.add(new Item());
		}
		notifyDataSetChanged();
	}

	public void addItemsBefore(int amount) {
		for(int i = 0 ; i<amount ; i++){
			items.add(0, new Item());
		}
		notifyDataSetChanged();
	}

}
