package cn.nd.social.syncbrowsing.manager;

public interface IHostSender {
	
	void syncEnter(String meetingId);

	void syncHandShake(String user,int state,int width,int height,int pageCount,int curPage);
	
	void syncNotifyPage(int page,byte[]pageData,int pageVersion);
	
	void syncSendAction(int pageNum, int pageVersion, byte[]action);
	
	void syncExit();
	
	//not using this currently
	void syncSwitchFile();
	
	//not using this currently
	void syncRequestPageAck(String user,int state,int pageNum, int pageVer,String pageInfo);
	//not using this currently
	void syncSendPage(int state,String userName, String fileName, int pageNum);
	
	//not using this currently
	void syncSendPage(int state,String userName, byte []imageData, int pageNum);
	
}
