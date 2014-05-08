package cn.nd.social.syncbrowsing;

import cn.nd.social.syncbrowsing.codec.LoadPageCallback;

public interface Source {
	int getPageCount();
	void loadPage(Object object, int index, LoadPageCallback callback);
}
