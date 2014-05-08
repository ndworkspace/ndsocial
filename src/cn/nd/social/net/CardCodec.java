package cn.nd.social.net;

import org.json.JSONException;
import org.json.JSONObject;

import cn.nd.social.card.CardUtil;
import cn.nd.social.card.CardUtil.CardData;

public class CardCodec {
	private static JSONObject constructCard2Json(CardData card) {
		JSONObject cardObj = new JSONObject();
		try {
			cardObj.put(CardUtil.AVATAR_ID, String.valueOf(card.avatarId));
			cardObj.put(CardUtil.AVATAR_URL, card.avatarUrl);
			cardObj.put(CardUtil.MODEL_ID, String.valueOf(card.bgId));
			cardObj.put(CardUtil.NAME_STR, card.name);
			cardObj.put(CardUtil.TITLE_STR, card.title);
			cardObj.put(CardUtil.MOBILE_STR, card.mobile);
			cardObj.put(CardUtil.PHONE_STR, card.phone);
			cardObj.put(CardUtil.EMAIL_STR, card.email);
			cardObj.put(CardUtil.ADDR_STR, card.addr);
			cardObj.put(CardUtil.COMPANY_STR, card.company);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return cardObj;
	}

	public static String buildCardJsonString(CardData card) {
		JSONObject jobj = constructCard2Json(card);
		if (jobj == null) {
			return null;
		}
		return jobj.toString();
	}

	public static CardData parseCardJson(JSONObject cardObj) {
		CardData card = new CardData();
		try {
			card.name = cardObj.getString(CardUtil.NAME_STR);
			card.title = cardObj.getString(CardUtil.TITLE_STR);
			card.mobile = cardObj.getString(CardUtil.MOBILE_STR);
			card.phone = cardObj.getString(CardUtil.PHONE_STR);
			card.email = cardObj.getString(CardUtil.EMAIL_STR);
			card.addr = cardObj.getString(CardUtil.ADDR_STR);
			card.company = cardObj.getString(CardUtil.COMPANY_STR);
			card.bgId = Integer.valueOf(cardObj.optString(CardUtil.MODEL_ID));
			card.avatarId = Integer.valueOf(cardObj.getString(CardUtil.AVATAR_ID));
			card.avatarUrl = cardObj.optString(CardUtil.AVATAR_URL);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return card;

	}
}
