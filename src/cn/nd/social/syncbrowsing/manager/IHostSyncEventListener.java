package cn.nd.social.syncbrowsing.manager;

public interface IHostSyncEventListener {
	void onExitSync();
	void syncReady();
	void notifyPage(int page,byte[]image);
	void notifyPageFirst(int page);
	void reselectFile();
	void showConnectedUserList();
	void sendPage(int state,String username, byte[] image,int page);
	void notifyAction(int page,byte[]action);
}
