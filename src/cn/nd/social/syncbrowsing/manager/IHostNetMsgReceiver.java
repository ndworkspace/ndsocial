package cn.nd.social.syncbrowsing.manager;

public interface IHostNetMsgReceiver {	
	void onRecvHandshake(String username ,int ver);
	void onRecvPageRequest(String username,int page,int pageVer);
}
