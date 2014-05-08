package cn.nd.social.card;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.nd.social.R;
import cn.nd.social.SocialApplication;
import cn.nd.social.card.CardUtil.CardData;
import cn.nd.social.card.CardUtil.CardDataPacker;
import cn.nd.social.data.MsgDBHelper;
import cn.nd.social.data.MsgProviderSingleton;
import cn.nd.social.hotspot.MsgDefine;
import cn.nd.social.prishare.PriShareSendActivity;

/**
 * single instance
 * parse card data from raw sound-wave data
 */
public class ReceiveCardHandler {
	Context mContext;
	private String mLastRecvData = "";
	private long mLastTimestamp = 0;
	
	static ReceiveCardHandler sRecvHandler = new ReceiveCardHandler(SocialApplication.getAppInstance());
	private ReceiveCardHandler(Context context) {
		mContext = context;
	}
	
	public static ReceiveCardHandler getInstance() {
		return sRecvHandler;
	}
	
	public void onCardDataArrival(String rawWaveData,Activity activity) {
		long timestamp = System.currentTimeMillis();
		//repeated data in 5 seconds will be abandoned
		if (rawWaveData.equals(mLastRecvData) && mLastTimestamp != 0
				&& timestamp - mLastTimestamp < 5000) {
			return;
		} else {
			mLastRecvData = rawWaveData;
			mLastTimestamp = timestamp;
		}

		CardDataPacker cardPacker = new CardUtil.CardDataPacker();
		

		CardData holder = new CardData();
		if (!cardPacker.extractData(rawWaveData, holder)) {
			Toast.makeText(mContext, "error data format-card", Toast.LENGTH_SHORT)
					.show();
			return;
		}		
		
		showCardContent(holder,activity);
	}
	
	
	public CardData getCardData(String rawWaveData) {
		long timestamp = System.currentTimeMillis();
		//repeated data in 5 seconds will be abandoned
		if (rawWaveData.equals(mLastRecvData) && mLastTimestamp != 0
				&& timestamp - mLastTimestamp < 5000) {
			return null;
		} else {
			mLastRecvData = rawWaveData;
			mLastTimestamp = timestamp;
		}

		CardDataPacker cardPacker = new CardUtil.CardDataPacker();
		

		CardData holder = new CardData();
		if (!cardPacker.extractData(rawWaveData, holder)) {
			Toast.makeText(mContext, "error data format-card", Toast.LENGTH_SHORT)
					.show();
			return null;
		}		
		return holder;		
	}
	
	
	private void showCardContent(final CardData holder,Activity activity) {
		//need to use activity
		AlertDialog.Builder builder = new AlertDialog.Builder(activity,
				R.style.DialogSlideAnim);

		LayoutInflater inflater = LayoutInflater.from(mContext);
		View root = inflater.inflate(R.layout.received_card_dialog, null);
		LinearLayout setBg = (LinearLayout)root.findViewById(R.id.recieve_card);
		TextView tv;
		tv = (TextView) root.findViewById(R.id.cust_name_text);
		tv.setText(holder.name);
		tv = (TextView) root.findViewById(R.id.cust_company_text);
		tv.setText(holder.company);
		tv = (TextView) root.findViewById(R.id.cust_title_text);
		tv.setText(holder.title);
		tv = (TextView) root.findViewById(R.id.cust_mobile_text);
		tv.setText(holder.mobile);
		tv = (TextView) root.findViewById(R.id.cust_phone_text);
		tv.setText(holder.phone);
		tv = (TextView) root.findViewById(R.id.cust_email_text);
		tv.setText(holder.email);
		tv = (TextView) root.findViewById(R.id.cust_addr_text);
		tv.setText(holder.addr);
		
		int resId = CardUtil.getCardBgIdMini(holder.bgId);
		setBg.setBackgroundResource(resId);
	
		builder.setView(root);
		builder.setTitle(R.string.new_card_received_hint);		
		
		builder.setPositiveButton(R.string.save,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						CardUtil.storeCardFromNFC(mContext, holder);

						// xlr add
						String content = "Received card has been saved.";
						addCardRecToHisDB(holder.userId,holder.name,content);
					}
				});
		builder.setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();

						// xlr add
						String content = "Received card has been canceled.";
						addCardRecToHisDB(holder.userId,holder.name,content);
					}
				});
		AlertDialog dialog = builder.create();
		dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
		// touch outside will not cancel dialog
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}
	
	public void slientStoreCard(String rawWaveData) {
		CardDataPacker cardPacker = new CardUtil.CardDataPacker();
		

		CardData holder = new CardData();
		if (!cardPacker.extractData(rawWaveData, holder)) {
			Toast.makeText(mContext, "error data format-card", Toast.LENGTH_SHORT)
					.show();
			return;
		}
		
		CardUtil.storeCardFromNFC(mContext, holder);

		// xlr add
		String content = "Received card has been saved.";
		addCardRecToHisDB(holder.userId,holder.name,content);	
	}
	
	private void addCardRecToHisDB(long userId,String userName,String content) {
		MsgProviderSingleton.getInstance().addRecord(
				userId, 
				userName, 
				content, 
				MsgDBHelper.NullValue,
				MsgDBHelper.NullValue,
				MsgDBHelper.NullValue,
				"recv",
				MsgDefine.FILE_TYPE_CARD,
				PriShareSendActivity.INFINITE_TIME,
				MsgDefine.STATUS_DO_NOTHING);
	}
}
