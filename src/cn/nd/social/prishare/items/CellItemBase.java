package cn.nd.social.prishare.items;

import java.lang.ref.WeakReference;

import android.graphics.drawable.Drawable;
import android.view.View;

public abstract class CellItemBase {
	public static final int APP_TYPE = 1;
	public static final int IMAGE_TYPE = 2;
	public static final int VIDEO_TYPE = 3;
	public static final int FILES_TYPE = 4;

	private int type;
	public WeakReference<View> v;
	private boolean selected = false;

	protected CellItemBase(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}
	
	public void setSelected(boolean flag) {
		selected = flag;
	}
	
	public boolean isSelected() {
		return selected;
	}

	public abstract Drawable getItemIcon();

	public abstract String getItemPath();

	public abstract String getFileShortName();
}
