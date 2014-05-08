package cn.nd.social.prishare.component;

public interface InterfaceForMain {

	void pagerChange(int whichPager);
	
	void showConnStatus(String info);
	
	void userKickedOutAll();
	
	void userKickedOutSingle(String name);
	
	void setMultiCount();
	
	void showQrCode();
}
