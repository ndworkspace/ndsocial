package cn.nd.social.syncbrowsing.manager;

public interface IClientSyncEventListener {
	void onExitSync();
	void requestPage(int page, int pageVer);
}
