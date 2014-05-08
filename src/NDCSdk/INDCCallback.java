package NDCSdk;

public interface INDCCallback {
	void OnError(int nError, String ErrorInfo);
	void OnNotify(int nNotifyCode, Object Notify);
	void OnMessage(String RequestCode, int nReturnCode, Object Value);
	
	void OnMessage(short bsType, byte[] Data, int nLen);
}
