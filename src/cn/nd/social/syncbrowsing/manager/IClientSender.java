package cn.nd.social.syncbrowsing.manager;

public interface IClientSender {
	void syncRequestPage(int page,int pageVersion) ;
	
	void syncHandshake(int ver);
}
