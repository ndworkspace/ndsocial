package cn.nd.social.syncbrowsing;

import cn.nd.social.syncbrowsing.codec.LoadPageCallback;
import cn.nd.social.syncbrowsing.ui.ClientSyncReadView;
import cn.nd.social.syncbrowsing.ui.ClientSyncReadView.NetworkPageLoader;

public class NetworkSource implements Source {

	private int pageCount;
	private ClientSyncReadView.NetworkPageLoader mPageLoader;
	public NetworkSource(int pageCount,ClientSyncReadView.NetworkPageLoader loader) {
		this.pageCount = pageCount;
		mPageLoader = loader;
	}
	
	public int getPageCount() {
		return pageCount;
	}
	
	public void loadPage(Object object, int index, LoadPageCallback callback) {
		//TODO:send network request
		mPageLoader.requestPage(object, index, callback);
	}
}
