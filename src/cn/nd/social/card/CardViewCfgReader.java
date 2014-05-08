package cn.nd.social.card;

import java.io.InputStream;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;
import cn.nd.social.R;

public class CardViewCfgReader {
	private final static String FIELD_NAME = "name";
	private final static String FIELD_TITLE = "title";
	private final static String FIELD_MOBILE = "mobile";
	private final static String FIELD_PHONE = "phone";
	private final static String FIELD_EMAIL = "email";
	private final static String FIELD_ADDR = "address";
	private final static String FIELD_COMP = "company";
	private final static String FIELD_GLOBAL_STYLE = "global_cfg";
	private final static String FIELD_DEFAULT_SIZE = "default_size";

	private static CardLayoutCfg[] sCardLayout;
/*	private static int[]CARD_LAYOUT_RAW_RES = {
		R.raw.mycard_type_one,
		R.raw.mycard_type_two,
		R.raw.mycard_type_three,
		R.raw.mycard_type_four
	};*/
	
	private static int[]CARD_LAYOUT_RAW_RES = {
		R.raw.mycard_type_1,
		R.raw.mycard_type_2,
		R.raw.mycard_type_3,
		R.raw.mycard_type_4,
		R.raw.mycard_type_5
	};
	
	public final static int PROP_MASK = 0xFF000000;
	
	static {
		sCardLayout = new CardLayoutCfg[CARD_LAYOUT_RAW_RES.length];
		for(int i=0; i<sCardLayout.length;i++) {
			sCardLayout[i] = null;
		}
	}
	
	public static CardLayoutCfg getXmlConfig(Context context, int type) {
		int index;
		if(type<=0 || type > sCardLayout.length) {
			index = 0;
		} else {
			index = type -1;
		}
		if(sCardLayout[index] == null) {
			InputStream xmlFile = context.getResources().openRawResource(CARD_LAYOUT_RAW_RES[index]);
			sCardLayout[index] = readXml(xmlFile);
		}
		
		return sCardLayout[index].clone();
	}
	
	public static boolean isColorSet(int color) {
		return (color & PROP_MASK) != 0;
	}

	public static boolean isFontSizeSet(int size) {
		return (size & PROP_MASK) != 0;
	}

	public static int getFontSize(int size) {
		return size & (~PROP_MASK);
	}
	
	public static int getFontSize(int size,double scale) {
		return (int)((size & (~PROP_MASK))*scale);
	}

	/**
	 * location and font size is in pixel
	 * */
	public static class CardLayoutCfg {
		public static final int PROPETY_NUM = 4; // properties includes
													// coordinates x,y and
													// fontSize, fontColor
		public GlobalStyle gStyle = new GlobalStyle();
		public int defaultWidth = 720;
		public int defaultHeight = 480;
		public int[] nameProp = new int[PROPETY_NUM];// nameProp[0]:x;
														// nameProp[1]:y;
														// nameProp[2]:fontSize;
														// nameProp[3]:fontColor
		public int[] titleProp = new int[PROPETY_NUM];
		public int[] mobileProp = new int[PROPETY_NUM];
		public int[] phoneProp = new int[PROPETY_NUM];
		public int[] emailProp = new int[PROPETY_NUM];
		public int[] addrProp = new int[PROPETY_NUM];
		public int[] compProp = new int[PROPETY_NUM];
		
		public CardLayoutCfg clone() {
			CardLayoutCfg config = new CardLayoutCfg();
			config.gStyle.backgroundPic = this.gStyle.backgroundPic;
			config.gStyle.backgroundColor = this.gStyle.backgroundColor;
			config.gStyle.defFontColor = this.gStyle.defFontColor;
			config.gStyle.defFontSize = this.gStyle.defFontSize;
			
			config.defaultWidth = this.defaultWidth;
			config.defaultHeight = this.defaultHeight;
			
			for(int i=0; i<PROPETY_NUM; i++) {
				config.nameProp[i] = this.nameProp[i];
				config.titleProp[i] = this.titleProp[i];
				config.mobileProp[i] = this.mobileProp[i];
				config.phoneProp[i] = this.phoneProp[i];
				config.emailProp[i] = this.emailProp[i];
				config.addrProp[i] = this.addrProp[i];
				config.compProp[i] = this.compProp[i];
			}
			return config;
		}
	}

	public static class GlobalStyle {
		public String backgroundPic;
		public int backgroundColor;
		public int defFontColor; // default font color
		public int defFontSize; // default font size
	}


	
	
	private static CardLayoutCfg readXml(InputStream inStream) {
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser saxParser = spf.newSAXParser();
			XMLContentHandler handler = new XMLContentHandler();
			saxParser.parse(inStream, handler);
			inStream.close();
			return handler.getConfig();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	static class XMLContentHandler extends DefaultHandler {
		CardLayoutCfg config;
		private String tagName = null;
		private String tagRoot = null;
		private HashMap<String, int[]> map = new HashMap<String, int[]>();

		public CardLayoutCfg getConfig() {
			return config;
		}

		@Override
		public void startDocument() throws SAXException {
			config = new CardLayoutCfg();
			map.put(FIELD_NAME, config.nameProp);
			map.put(FIELD_TITLE, config.titleProp);
			map.put(FIELD_MOBILE, config.mobileProp);
			map.put(FIELD_PHONE, config.phoneProp);
			map.put(FIELD_EMAIL, config.emailProp);
			map.put(FIELD_ADDR, config.addrProp);
			map.put(FIELD_COMP, config.compProp);
		}

		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			if (tagRoot != null && tagName != null) {
				String data = new String(ch, start, length);
				if (map.containsKey(tagRoot)) {
					int[] prop = map.get(tagRoot);
					if (prop == null) {
						throw new RuntimeException("tagRoot:" + tagRoot
								+ "is invalid");
					}
					if (tagName.equals("loc_x")) {
						prop[0] = Integer.parseInt(data);
					} else if (tagName.equals("loc_y")) {
						prop[1] = Integer.parseInt(data);
					} else if (tagName.equals("font_size")) {
						prop[2] = PROP_MASK | Integer.parseInt(data);
					} else if (tagName.equals("font_color")) {
						prop[3] = PROP_MASK | Integer.valueOf(data, 16);
					} else {
						throw new RuntimeException("tagName" + tagName
								+ "invalid");
					}
				} else if (tagRoot.equals(FIELD_GLOBAL_STYLE)) {
					GlobalStyle elm = config.gStyle;
					if (tagName.equals("background_pic")) {
						elm.backgroundPic = data;
					} else if (tagName.equals("background_color")) {
						elm.backgroundColor = PROP_MASK
								| Integer.valueOf(data, 16);
					} else if (tagName.equals("font_color")) {
						elm.defFontColor = PROP_MASK
								| Integer.valueOf(data, 16);
					} else if (tagName.equals("font_size")) {
						elm.defFontSize = PROP_MASK | Integer.valueOf(data);
					}
				} else if(tagRoot.equals(FIELD_DEFAULT_SIZE)) {
					if (tagName.equals("width")) {
						config.defaultWidth = Integer.parseInt(data);
					} else if (tagName.equals("height")) {
						config.defaultHeight = Integer.parseInt(data);
					}
				}
			}
		}

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes atts) throws SAXException {
			if (isRootLevel(localName)) {
				tagRoot = localName;
			} else {
				if (tagRoot != null) {
					tagName = localName;
				}
			}
		}

		public boolean isRootLevel(String localName) {
			if (map.containsKey(localName)
					|| localName.equals(FIELD_GLOBAL_STYLE)
					|| localName.equals(FIELD_DEFAULT_SIZE)) {
				return true;
			}
			return false;
		}

		public void endElement(String uri, String localName, String name)
				throws SAXException {

			if (isRootLevel(localName)) {
				tagRoot = null;
			} else {
				tagName = null;
			}
		}
	}
}
