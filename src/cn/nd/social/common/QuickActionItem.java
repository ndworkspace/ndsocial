package cn.nd.social.common;

public class QuickActionItem {
	private int itemId;
	private int titleRes;
	private int iconRes;
	
	public QuickActionItem(int itemId, int strRes,int iconRes) {		
		super();
		this.itemId = itemId;
		this.titleRes = strRes;
		this.iconRes = iconRes;
	}
 
	public int getItemId() {
		return itemId;
	}
 
	public int getTextRes() {
		return titleRes;
	}
	
	public int getIconRes() {
		return iconRes;
	}
}
