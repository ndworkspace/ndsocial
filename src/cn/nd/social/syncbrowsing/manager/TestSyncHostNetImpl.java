package cn.nd.social.syncbrowsing.manager;

import cn.nd.social.account.CAConstant;
import cn.nd.social.account.CloundServer;
import cn.nd.social.hotspot.NetworkServerThread;

public class TestSyncHostNetImpl implements IHostSender{
	
	NetworkServerThread mNetworkThread;

	private String mMeetingId;
	
	private int mWidth = 0;
	private int mHeight = 0;
	private int mPageCount = 0;
	
	
	public TestSyncHostNetImpl(String meetingId) {
		mMeetingId = meetingId;
	}


	@Override
	public void syncEnter(String meetingId) {
		byte []msg = SyncMsgFactory.getSyncEnterMsg(meetingId);
		sendSyncMsg(msg, CAConstant.LOCAL_MSG_ID_SYNC_ENTER);
	}
	
	@Override
	public void syncHandShake(String user, int state, int width, int height,
			int pageCount, int curPage) {
		
		byte []msg = SyncMsgFactory.getSyncHandshakeMsg(width, height, pageCount, curPage);
		
		mWidth = width;
		mHeight = height;
		mPageCount = pageCount;
		
		sendSyncMsg(msg, CAConstant.LOCAL_MSG_ID_SYNC_HANDSHAKE);		
	}



	@Override
	public void syncNotifyPage(int page,byte[]pageData, int pageVersion) {
		
		byte []msg = SyncMsgFactory.getSyncNotifyPageMsg(page, pageData,mWidth,mHeight,mPageCount);
		
		sendSyncMsg(msg, CAConstant.LOCAL_MSG_ID_SYNC_NOTIFY_PAGE);	
		
	}



	@Override
	public void syncSendAction(int pageNum, int pageVersion, byte[] action) {
		
		byte []msg = SyncMsgFactory.getSyncSendAction(pageNum, action);		
		sendSyncMsg(msg, CAConstant.LOCAL_MSG_ID_SYNC_ACTION);		
	}

	@Override
	public void syncExit() {
		byte []msg = SyncMsgFactory.getSyncExitAction();		
		sendSyncMsg(msg, CAConstant.LOCAL_MSG_ID_SYNC_EXIT);			
	}

	
	@Override
	public void syncRequestPageAck(String user, int state, int pageNum,
			int pageVer, String pageInfo) {
				
	}
	
	
	
	@Override
	public void syncSwitchFile() {
		// TODO Auto-generated method stub
		
	}

	
	
	@Override
	public void syncSendPage(int state,String userName, String fileName, int pageNum) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void syncSendPage(int state,String userName, byte[] imageData, int pageNum) {
				
	}

	
	private void sendSyncMsg(byte []content, int msgId) {
		if(SyncMsgFactory.ADD_JSON_HEADER) {
			byte[] header = SyncMsgFactory.getSyncPacketHeader(mMeetingId);
			byte []msg = SyncMsgFactory.contactHeadAndContent(header,content);			
			CloundServer.getInstance().sendSyncMsg(msg, msgId);
		} else {
			CloundServer.getInstance().sendSyncMsg(content, msgId);
		}
	}

}
