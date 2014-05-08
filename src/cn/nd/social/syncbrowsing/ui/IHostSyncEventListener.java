package cn.nd.social.syncbrowsing.ui;

public interface IHostSyncEventListener {
	void onExitSync();
	void notifyPage(int page);
	void notifyPageFirst(int page);
	void reselectFile();
	void showConnectedUserList();
	void sendPage(String username, byte[] image,int page);
	void notifyAction(int page,byte[]action);
}
