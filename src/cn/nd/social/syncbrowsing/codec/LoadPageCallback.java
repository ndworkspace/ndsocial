package cn.nd.social.syncbrowsing.codec;

import android.graphics.Bitmap;

public interface LoadPageCallback {
	void onLoadComplete(Object objectKey, int pageNumber, Bitmap bitmap);
}
