package cn.nd.social.syncbrowsing.manager;

public interface IClientNetMsgReceiver {	
	void onRecvEnterSync();	
	
	void onRecvHandshake(int state,int pageCount,int curPage,int hostWidth,int hostHeight);
	
	void onRecvNotifyPage(int page,byte[]pageData, int pageVer);
	
	void onRecvAction(int page,int pageVer,byte[]action);
	
	void onRecvExitSync();
	
	//not using this currently
	void onRecvReselectFile();
	
	//not using this currently
	void onRecvPageRequestAck(int state,int pageNum,int pageVer,String pageData);
}
