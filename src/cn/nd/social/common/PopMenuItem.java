package cn.nd.social.common;

public class PopMenuItem {
	private int itemId;
	private String titleText;
	private int iconId;
	
	public PopMenuItem(int itemId, String text,int resId) {		
		super();
		this.itemId = itemId;
		this.titleText = text;
		iconId = resId;
	}
 
	public int getItemId() {
		return itemId;
	}
 
	public String getText() {
		return titleText;
	}
	
	public int getIconId() {
		return iconId;
	}

}
