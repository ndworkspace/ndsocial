package cn.nd.social.util;

import java.util.ArrayList;

import cn.nd.social.util.HanziToPinyin.Token;

/**
 * 获取汉字拼音
 * 
 * @author 佚名
 * 
 */
public class PinYin {
	public static String getPinYin(String input) {
		ArrayList<Token> tokens = HanziToPinyin.getInstance().get(input);
		StringBuilder sb = new StringBuilder();
		if (tokens != null && tokens.size() > 0) {
			for (Token token : tokens) {
				if (Token.PINYIN == token.type) {
					sb.append(token.target);
				} else {
					sb.append(token.source);
				}
			}
		}
		return sb.toString().toLowerCase();
	}
}
