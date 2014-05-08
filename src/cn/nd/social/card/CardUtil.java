package cn.nd.social.card;

import java.util.ArrayList;

import com.nd.voice.meetingroom.manager.User;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Toast;
import cn.nd.social.R;
import cn.nd.social.SocialApplication;
import cn.nd.social.account.CAUtils;
import cn.nd.social.account.CloundServer;
import cn.nd.social.data.CardOpenHelper;
import cn.nd.social.data.CardProvider;
import cn.nd.social.util.AudioDataPacker;
import cn.nd.social.util.Utils;

public class CardUtil {
	public final static String ACTION_SELF_CARD_REFRESH = "cn.nd.social.refresh_card";

	public final static int SIZE_UNIT = TypedValue.COMPLEX_UNIT_PX;
	public final static String CARD_RECORD_SEPERATOR = "\r\n\r\n";
	
	public static final String NAME_STR = "name";
	public static final String TITLE_STR = "title";
	public static final String MOBILE_STR = "mobile";
	public static final String PHONE_STR = "phone";
	public static final String EMAIL_STR = "email";
	public static final String ADDR_STR = "address";
	public static final String COMPANY_STR = "company";
	public static final String MODEL_ID = "model";
	public static final String AVATAR_ID = "avatar";
	public static final String AVATAR_URL = "avatar_path";
	

	public static final String[] CARD_LIST_PROJECTION = {
			CardOpenHelper.COLUMN_ID, CardOpenHelper.COLUMN_NAME,
			CardOpenHelper.COLUMN_TITLE, CardOpenHelper.COLUMN_MOBILE,
			CardOpenHelper.COLUMN_PHONE, CardOpenHelper.COLUMN_EMAIL,
			CardOpenHelper.COLUMN_ADDR, CardOpenHelper.COLUMN_COMPANY,
			CardOpenHelper.COLUMN_FAVORITE, CardOpenHelper.COLUMN_NET_SYNC,
			CardOpenHelper.COLUMN_USER_ID, CardOpenHelper.COLUMN_TIME,
			CardOpenHelper.COLUMN_BG_ID, CardOpenHelper.COLUMN_AVATAR_ID,
			CardOpenHelper.COLUMN_AVATAR_URL};
	public static final String[] CARD_LIST_UNSYNC_PROJECTION = {
			CardOpenHelper.COLUMN_ID, CardOpenHelper.COLUMN_USER_ID,
			CardOpenHelper.COLUMN_NET_SYNC, CardOpenHelper.COLUMN_TIME };

	public final static int ID_INDEX = 0;
	public final static int NAME_INDEX = 1;
	public final static int TITLE_INDEX = 2;
	public final static int MOBILE_INDEX = 3;
	public final static int PHONE_INDEX = 4;
	public final static int EMAIL_INDEX = 5;
	public final static int ADDR_INDEX = 6;
	public final static int COMPANY_INDEX = 7;
	public final static int FRIEND_IND_INDEX = 8;
	
	private final static String EMPTY = "";
	private final static double TEMPLATE_HEIGHT_WIDTH_RATIO = 1.5d;	

	public static boolean storeCard(Context context, CardData card,boolean force) {
		ContentResolver cr = context.getContentResolver();
		Uri uri = CardProvider.CONTENT_URI;
		if(!force) {
			String selection = CardOpenHelper.COLUMN_USER_ID + "=?"+ " AND " + CardOpenHelper.COLUMN_MOBILE
					+ "=?"+ " AND " + CardOpenHelper.COLUMN_NAME + "=?";
			if(card.name == null) {
				card.name = "";
			}
			String []selectionArg = new String[]{String.valueOf(card.userId),
					card.mobile,card.name} ;
			Cursor cursor = cr.query(uri, CARD_LIST_PROJECTION, selection,selectionArg, null);
			if(cursor != null ) {
				if(cursor.getCount() > 0) {
					Toast.makeText(context,"名片 " + card.name + " 已经存在", Toast.LENGTH_SHORT).show();
					cursor.close();
					return false;
				}
				cursor.close();
			}
		}
		
		ContentValues values = new ContentValues();
		values.put(CardOpenHelper.COLUMN_NAME, Utils.nonNullString(card.name));
		values.put(CardOpenHelper.COLUMN_TITLE, Utils.nonNullString(card.title));
		values.put(CardOpenHelper.COLUMN_MOBILE,
				Utils.nonNullString(card.mobile));
		values.put(CardOpenHelper.COLUMN_PHONE, Utils.nonNullString(card.phone));
		values.put(CardOpenHelper.COLUMN_EMAIL, Utils.nonNullString(card.email));
		values.put(CardOpenHelper.COLUMN_ADDR, Utils.nonNullString(card.addr));
		values.put(CardOpenHelper.COLUMN_COMPANY,
				Utils.nonNullString(card.company));
		values.put(CardOpenHelper.COLUMN_FAVORITE, card.favorite);
		values.put(CardOpenHelper.COLUMN_TIME, card.time);
		if(force) {
			values.put(CardOpenHelper.COLUMN_NET_SYNC, 1);
		} else {
			values.put(CardOpenHelper.COLUMN_NET_SYNC, 0);
		}
		values.put(CardOpenHelper.COLUMN_USER_ID, card.userId);
		values.put(CardOpenHelper.COLUMN_BG_ID,card.bgId);
		values.put(CardOpenHelper.COLUMN_AVATAR_ID,card.avatarId);
		values.put(CardOpenHelper.COLUMN_AVATAR_URL,card.avatarUrl);
		cr.insert(uri, values);
		return true;
	}

	public static ContentValues buildValuesFromCursor(Cursor cursor) {
		ContentValues values = new ContentValues();
		values.put(CardOpenHelper.COLUMN_NAME, cursor.getString(1));
		values.put(CardOpenHelper.COLUMN_TITLE, cursor.getString(2));
		values.put(CardOpenHelper.COLUMN_MOBILE, cursor.getString(3));
		values.put(CardOpenHelper.COLUMN_PHONE, cursor.getString(4));
		values.put(CardOpenHelper.COLUMN_EMAIL, cursor.getString(5));
		values.put(CardOpenHelper.COLUMN_ADDR, cursor.getString(6));
		values.put(CardOpenHelper.COLUMN_COMPANY, cursor.getString(7));
		values.put(CardOpenHelper.COLUMN_FAVORITE, cursor.getInt(8));
		values.put(CardOpenHelper.COLUMN_NET_SYNC, cursor.getInt(9));
		values.put(CardOpenHelper.COLUMN_USER_ID, cursor.getLong(10));
		values.put(CardOpenHelper.COLUMN_TIME, cursor.getInt(11));
		values.put(CardOpenHelper.COLUMN_BG_ID, cursor.getInt(12));
		values.put(CardOpenHelper.COLUMN_AVATAR_ID, cursor.getInt(13));
		values.put(CardOpenHelper.COLUMN_AVATAR_URL,cursor.getString(14));
		return values;

	}

	public static void storeCardFromNFC(Context context, CardData holder) {
		holder.favorite = 1;
		holder.time = System.currentTimeMillis() / 1000;
		if(storeCard(context, holder,false)) {
			Toast.makeText(context, "来自"+ holder.name + "的名片已保存", Toast.LENGTH_LONG)
			.show();
		}
	}

	public static void storeCardFromNetSync(String jsonStr) {

	}

	
	
	public static CardData getSelfCardData() {
		SharedPreferences prefs = Utils.getAppSharedPrefs();
		CardData cardData = new CardData();
		cardData.userId = CloundServer.getInstance().getUserId();
		cardData.name = prefs.getString(CardUtil.NAME_STR, EMPTY);
		cardData.company = prefs.getString(CardUtil.COMPANY_STR, EMPTY);
		cardData.title = prefs.getString(CardUtil.TITLE_STR, EMPTY);
		cardData.mobile = prefs.getString(CardUtil.MOBILE_STR, EMPTY);
		cardData.phone = prefs.getString(CardUtil.PHONE_STR, EMPTY);
		cardData.email = prefs.getString(CardUtil.EMAIL_STR, EMPTY);
		cardData.addr = prefs.getString(CardUtil.ADDR_STR, EMPTY);
		cardData.bgId = prefs.getInt(CardUtil.MODEL_ID, 1);
		cardData.avatarId = prefs.getInt(CardUtil.AVATAR_ID, 0);
		cardData.avatarUrl = prefs.getString(CardUtil.AVATAR_URL, EMPTY);
		return cardData;
	}

	public static class CardDataPacker extends AudioDataPacker {
		private final static String FIELD_SEPARATOR = ":";
		private final static String ID_PREFIX = "id";
		

		// type is from one to nine
		CardDataPacker(int type) {
			super(type);
		}

		public CardDataPacker() {
			super(AudioDataPacker.TYPE_CARD_STRING);
		}

		@Override
		public String packAudioData(Object obj) { // data format will be like
										// id:xxx:4:tang:0:5:xxxxx: ....
			
			CardData cardData = null;
			if(obj instanceof CardData) {
				cardData = (CardData)obj;				
			} else {
				cardData = getSelfCardData();
			}
			
			StringBuilder builder = new StringBuilder();
			builder.append(packField(ID_PREFIX + FIELD_SEPARATOR + cardData.userId));
			builder.append(packField(cardData.name));
			builder.append(packField(cardData.company));
			builder.append(packField(cardData.title));
			builder.append(packField(cardData.mobile));
			builder.append(packField(cardData.phone));
			builder.append(packField(cardData.email));
			builder.append(packField(cardData.addr));
			builder.append(packField(String.valueOf(cardData.bgId)));
			builder.append(packField(String.valueOf(cardData.avatarId)));
			builder.append(packField(cardData.avatarUrl));
			return super.packAudioData(builder.toString());
		}
		

		private String packField(String fieldStr) {
			int len = 0;
			String str = "";
			if (fieldStr.equals(EMPTY)) {
				len = 0;
				str = String.valueOf(len) + FIELD_SEPARATOR;
			} else {
				len = fieldStr.length();
				str = String.valueOf(len) + FIELD_SEPARATOR + fieldStr
						+ FIELD_SEPARATOR;
			}
			return str;
		}

		private String getNextField(FieldCursor dataField) {
			String field = "";
			int index = dataField.cursorStr.indexOf(FIELD_SEPARATOR);
			int fieldLen = Integer.valueOf(dataField.cursorStr.substring(0,
					index));
			if (fieldLen > 0) {
				field = dataField.cursorStr.substring(
						index + FIELD_SEPARATOR.length(), index
								+ FIELD_SEPARATOR.length() + fieldLen);
				dataField.cursorStr = dataField.cursorStr.substring(index
						+ FIELD_SEPARATOR.length() + fieldLen
						+ FIELD_SEPARATOR.length());
			} else {
				dataField.cursorStr = dataField.cursorStr.substring(index
						+ FIELD_SEPARATOR.length());
			}
			return field;
		}		

		@Override
		public boolean extractData(String rawWaveData, Object dataHolder) {			
			String rawStr = super.unPackAudioData(rawWaveData);
			
			CardData holder = (CardData) dataHolder;
			try {
				int index, fieldLen;
				Log.e("cardUtil", rawStr);
				index = rawStr.indexOf(FIELD_SEPARATOR);
				fieldLen = Integer.valueOf(rawStr.substring(0, index));
				String idStr = rawStr.substring(
						index + FIELD_SEPARATOR.length(), index
								+ FIELD_SEPARATOR.length() + fieldLen);
				holder.userId = Long
						.parseLong(idStr.substring(idStr.indexOf(FIELD_SEPARATOR)+1));

				FieldCursor fieldCursor = new FieldCursor(
						rawStr.substring(index + FIELD_SEPARATOR.length()
								+ fieldLen + FIELD_SEPARATOR.length()));
				holder.name = getNextField(fieldCursor);
				holder.company = getNextField(fieldCursor);
				holder.title = getNextField(fieldCursor);
				holder.mobile = getNextField(fieldCursor);
				holder.phone = getNextField(fieldCursor);
				holder.email = getNextField(fieldCursor);
				holder.addr = getNextField(fieldCursor);
				holder.bgId = Integer.valueOf(getNextField(fieldCursor));
				holder.avatarId = Integer.valueOf(getNextField(fieldCursor));
				holder.avatarUrl = getNextField(fieldCursor);
			} catch (Exception e) {
				return false;
			}
			return true;
		}

		class FieldCursor {
			FieldCursor(String rawData) {
				cursorStr = rawData;
			}

			String cursorStr;
		}

	}


	public static class CardData {
		public long userId = Utils.INVALID_USER_ID;
		public String name;
		public String title;
		public String mobile;
		public String phone;
		public String email;
		public String addr;
		public String company;
		public int favorite;
		public long time;
		public int bgId = 1;
		public int avatarId = 0;
		public String avatarUrl = "";
		public void clean(){
			userId = Utils.INVALID_USER_ID;
			name = null;
			title = null;
			mobile = null;
			phone = null;
			email = null;
			addr = null;
			company = null;
			favorite = 0;
			time = 0;
			bgId = 1;
			avatarId = 0;
			avatarUrl = "";
		}
	}


	public static CardData getMyCardInfo() {
		SharedPreferences prefs = Utils.getAppSharedPrefs();
		CardData card = new CardData();
		card.name = prefs.getString(NAME_STR, EMPTY);
		card.company = prefs.getString(COMPANY_STR, EMPTY);
		card.title = prefs.getString(TITLE_STR, EMPTY);
		card.mobile = prefs.getString(MOBILE_STR, EMPTY);
		card.phone = prefs.getString(PHONE_STR, EMPTY);
		card.email = prefs.getString(EMAIL_STR, EMPTY);
		card.addr = prefs.getString(ADDR_STR, EMPTY);
		card.bgId = prefs.getInt(MODEL_ID, 1);
		card.avatarId = prefs.getInt(AVATAR_ID, 0);
		card.avatarUrl = prefs.getString(AVATAR_URL, "");
		return card;
	}
	
	/**
	 * input: viewSize[0]:width; viewSize[1]:height
	 * output: targetSize[0]:width; targetSize[1]:height
	 * */
	public static void adjustTargetSize(int []viewSize, int[]targetSize) {
		double scale = (double)viewSize[1] / viewSize[0];
		if(scale > TEMPLATE_HEIGHT_WIDTH_RATIO) {
			targetSize[0] = viewSize[0];
			targetSize[1] = (int)(targetSize[0] * TEMPLATE_HEIGHT_WIDTH_RATIO);
		} else {
			targetSize[1] = viewSize[1];
			targetSize[0] = (int)(targetSize[1] / TEMPLATE_HEIGHT_WIDTH_RATIO);
		}
		if(targetSize[0] % 2 == 1) {
			targetSize[0]++;
		} 
		if(targetSize[1] % 2 == 1) {
			targetSize[1]++;
		} 
	}

	public final static int[] sBackID = {R.drawable.card1,R.drawable.card2,
	R.drawable.card3, R.drawable.card4, R.drawable.card5};
	public final static int[] sMiniBackID = {R.drawable.card1_mini,R.drawable.card2_mini,
		R.drawable.card3_mini, R.drawable.card4_mini,R.drawable.card5_mini};
	////////////////////////////////////	

	public static int getCardBgIdMini(int index) {
		if(index > 0 && index <= sMiniBackID.length) {
			return sMiniBackID[index-1];
		}
		return sMiniBackID[0];
	}

	public static int getCardBgId(int index) {
		if(index > 0 && index <= sBackID.length) {
			return sBackID[index-1];
		}
		return sBackID[0];
	}
	
	public static Intent getAddContactIntent(CardData item) {
		String name = item.name;
		String phone = item.mobile;
		String email = item.email;
		String title = item.title;
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

		inOrUp.putExtra(ContactsContract.Data.IS_SUPER_PRIMARY, 1);
		return inOrUp;
	}


	
	public static void saveCardFromUser(User user) {
		SharedPreferences prefs = Utils.getAppSharedPrefs();
		SharedPreferences.Editor editor = prefs.edit();
		
		editor.putString(NAME_STR, user.getNickName());
		editor.putString(COMPANY_STR, user.getCompany());
		//editor.putString(TITLE_STR, user.getTitle());
		//editor.putString(MOBILE_STR, user.getMobile());
		editor.putString(MOBILE_STR, user.getUserName());
		editor.putString(PHONE_STR, user.getPhone());
		editor.putString(EMAIL_STR, user.getEmail());
		editor.putString(ADDR_STR, user.getAddress());
		editor.putInt(AVATAR_ID, user.getDefaultFace());
		editor.putString(AVATAR_URL, user.getFaceUrl());		
		editor.commit();
		
		CAUtils.setUserInfoNeedUpdate(true);
	}

	public static void saveCardByUser(ArrayList<User> userList) {
		final ArrayList<User> cardList = new ArrayList<User>();
		cardList.addAll(userList);
		new Thread ( new Runnable() {			
			@Override
			public void run() {
				deleteCardList();
				for(User user:cardList) {
					CardData cardData = new CardData();
					cardData.userId = user.getUserid();
					cardData.name = user.getNickName();
					cardData.addr = user.getAddress();
					cardData.company = user.getCompany();
					cardData.avatarId = user.getDefaultFace();
					cardData.email = user.getEmail();
					cardData.mobile = user.getMobile();
					cardData.phone = user.getPhone();	
				
					storeCard(Utils.getAppContext(),cardData,true);			
				}
				
			}
		}).start();	

	}

	public static void deleteCardList() {
		int result = Utils.getAppContext().getContentResolver().delete(CardProvider.CONTENT_URI, null, null);
		Log.e("CloundServer","delete db result:" + result);
	}

}
