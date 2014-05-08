package cn.nd.social.card;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import cn.nd.social.card.CardUtil.CardData;
import cn.nd.social.data.CardOpenHelper;
import cn.nd.social.data.CardProvider;

public class CardEntity {
	private int mId;
	private final static int ID = 0;
	private final static int NAME = 1;
	private final static int TITLE = 2;
	private final static int MOBILE = 3;
	private final static int PHONE = 4;
	private final static int EMAIL = 5;
	private final static int ADDR = 6;
	private String mName;
	private String mComp;
	private String mTitle;
	private String mMobile;
	private String mPhone;
	private String mEmail;
	private String mAddr;
	
	private long mUserId;
	private int mBgId;
	private Context mContext;

	public CardEntity(Context context, int id, boolean allowQuery) {
		mContext = context;
		if (!loadFromId(id, allowQuery)) {
			mId = 0;
		}
	}

	private boolean loadFromId(int id, boolean allowQuery) {
		Cursor c = null;
		Uri uri = Uri.parse(CardProvider.CONTENT_URI + "/" + id);
		c = mContext.getContentResolver().query(uri, null, null, null, null);
		try {
			if (c.moveToFirst()) {
				fillFromCursor(mContext, this, c, allowQuery);
			} else {
				return false;
			}
		} finally {
			c.close();
		}
		return true;
	}

	private CardEntity(Context context, Cursor cursor, boolean allowQuery) {
		mContext = context;
		fillFromCursor(context, this, cursor, allowQuery);
	}

	public static CardEntity from(Context context, Cursor cursor) {
		int id = cursor.getInt(ID);
		if (id > 0) {
			CardEntity card = Cache.get(id);
			if (card != null) {
				fillFromCursor(context, card, cursor, false);// update the
																// existing
																// cardEntity
				return card;
			}

		}
		CardEntity card = new CardEntity(context, cursor, false);
		try {
			Cache.put(card);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return card;
	}

	public String getName() {
		return mName;
	}

	public String getTitle() {
		return mTitle;
	}

	public String getMobile() {
		return mMobile;
	}

	public String getPhone() {
		return mPhone;
	}

	public String getEmail() {
		return mEmail;
	}

	public String getAddr() {
		return mAddr;
	}
	
	public String getCompany() {
		return mComp;
	}
	
	public long getUserId() {
		return mUserId;
	}
	
	public int getBgId() {
		return mBgId;
	}

	public static CardEntity from(Context context, int id) {
		if (id > 0) {
			CardEntity card = Cache.get(id);
			if (card != null) {
				return card;
			}

		}
		CardEntity card = new CardEntity(context, id, false);
		try {
			Cache.put(card);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return card;
	}

	private static void fillFromCursor(Context context, CardEntity card,
			Cursor c, boolean allowQuery) {
		card.mName = c.getString(NAME);
		card.mTitle = c.getString(TITLE);
		card.mMobile = c.getString(MOBILE);
		card.mPhone = c.getString(PHONE);
		card.mEmail = c.getString(EMAIL);
		card.mAddr = c.getString(ADDR);
		card.mComp = c.getString(c.getColumnIndex(CardOpenHelper.COLUMN_COMPANY));
		card.mUserId = c.getLong(c.getColumnIndex(CardOpenHelper.COLUMN_USER_ID));
		card.mBgId = c.getInt(c.getColumnIndex(CardOpenHelper.COLUMN_BG_ID));
	}

	
	
	public int getCardId() {
		return mId;
	}

	public static CardData getCardData(CardEntity card) {
		CardData data = new CardData();
		data.userId = card.mUserId;
		data.name = card.mName;
		data.company = card.mComp;
		data.title = card.mTitle;
		data.mobile = card.mMobile;
		data.phone = card.mPhone;
		data.email = card.mEmail;
		data.addr = card.mAddr;
		data.company = card.mComp;
		data.bgId = card.mBgId;
		return data;
	}
	
	
	private static class Cache {
		private static Cache sInstance = new Cache();

		static Cache getInstance() {
			return sInstance;
		}

		// / M: use ConcurrentHashMap is better
		private final ConcurrentHashMap<Integer, CardEntity> mCache;

		private Cache() {
			// / M: use ConcurrentHashMap is better
			mCache = new ConcurrentHashMap<Integer, CardEntity>();
		}

		/**
		 * Return the CardEntity with the specified thread ID, or null if it's
		 * not in cache.
		 */
		static CardEntity get(int id) {
			CardEntity c = sInstance.mCache.get(id);
			if (c != null && c.getCardId() == id) {
				return c;
			}
			return null;
		}

		/**
		 * Put the specified CardEntity in the cache. The caller should not
		 * place an already-existing CardEntity in the cache, but rather update
		 * it in place.
		 */
		static void put(CardEntity c) {

			if (sInstance.mCache.contains(c)) {
				throw new IllegalStateException("cache already contains " + c
						+ " Id: " + c.mId);
			}
			sInstance.mCache.put(c.getCardId(), c);
		}

		/**
		 * Replace the specified CardEntity in the cache. This is used in cases
		 * where we lookup a CardEntity in the cache by threadId, but don't find
		 * it. The caller then builds a new CardEntity (from the cursor) and
		 * tries to add it, but gets an exception that the CardEntity is already
		 * in the cache, because the hash is based on the recipients and it's
		 * there under a stale threadId. In this function we remove the stale
		 * entry and add the new one. Returns true if the operation is
		 * successful
		 */
		static boolean replace(CardEntity c) {
			if (!sInstance.mCache.contains(c)) {
				return false;
			}
			sInstance.mCache.replace(c.getCardId(), c);
			return true;
		}

		static void remove(int id) {

			sInstance.mCache.remove(id);
			// / @}
		}

		/**
		 * Remove all CardEntitys from the cache that are not in the provided
		 * set of thread IDs.
		 */
		static void keepOnly(Set<Integer> ids) {
			synchronized (sInstance) {
				Iterator<CardEntity> iter = sInstance.mCache.values()
						.iterator();
				CardEntity c = null;
				while (iter.hasNext()) {
					c = iter.next();
					if (!ids.contains(c.getCardId())) {
						iter.remove();
					}
				}
			}

		}

		static void clear() {
			synchronized (sInstance) {
				sInstance.mCache.clear();
			}
		}
	}
}
