package cn.nd.social.account.usermanager;

public interface CloundEventCallback {
	void onReturn(boolean success,Object data);
	void onError(int errorCode);
}
