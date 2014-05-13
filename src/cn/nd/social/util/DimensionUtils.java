package cn.nd.social.util;

import android.content.res.Resources;
import android.util.DisplayMetrics;
import cn.nd.social.R;

/**
 * DimensionUtils intends for providing dimension utility
 * put common dimension in this class
 * */
public class DimensionUtils {
	private final static Resources sResource; 
	private final static int sDisplayWidth;
	private final static int sDisplayHeight;
	static {
		sResource = Utils.getAppContext().getResources();
		DisplayMetrics metrics = sResource.getDisplayMetrics();
		if(metrics.widthPixels > metrics.heightPixels) {
			sDisplayWidth = metrics.heightPixels;
			sDisplayHeight = metrics.widthPixels;
		} else {
			sDisplayWidth = metrics.widthPixels;
			sDisplayHeight = metrics.heightPixels;
		}
	}
	public static int getQrCodeDimen() {
		return (int) sResource.getDimension(R.dimen.qrcode_size);
	}
	
	public static int getThumbnailDimen() {
		return UnitConverter.dpToPx(sResource, 60);
	}
	
	public static int getDisplayWidth() {
		return sDisplayWidth;		
	}
	
	public static int getDisplayHeight() {
		return sDisplayHeight;		
	}
	
}
