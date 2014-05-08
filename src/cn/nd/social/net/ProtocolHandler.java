package cn.nd.social.net;

import android.os.Handler;
import android.os.Message;
import cn.nd.social.hotspot.MsgDefine;

public class ProtocolHandler extends Handler {

	private HandlerInterface mListener;

	public void setDisposer(HandlerInterface l) {
		mListener = l;
	}

	@Override
	public void handleMessage(Message msg) {
		// TODO Auto-generated method stub

		if (hostMsgHandler(msg)) {
			return;
		}

		if (clientMsgHandler(msg)) {
			return;
		}

		if (fileTransMsgHandler(msg)) {
			return;
		}

		if (userMgrMsgHandler(msg)) {
			return;
		}

		if (syncMsgHandler(msg)) {
			return;
		}
		
	}

	public static abstract class DefaultDisposal implements HandlerInterface {

		/** msg.arg1: int version */
		@Override
		public void onSyncShakehand(Message msg) {

		}

		/**
		 * int[] ackArray = (int[]) msg.obj; int state = ackArray[0]; int width
		 * = ackArray[1]; int height = ackArray[2]; int pageCount = ackArray[3];
		 * int curPage = ackArray[4];
		 */
		@Override
		public void onSyncShakehandAck(Message msg) {
		}

		/**
		 * msg.arg1: state; msg.obj: NetworkServerThread.SyncBrowsingInfo
		 * */
		@Override
		public void onSyncGetPageAck(Message msg) {

		}

		/**
		 * msg.arg1: page number; msg.arg2: pageVersion
		 */
		@Override
		public void onSyncGetPageRequest(Message msg) {

		}

		/**
		 * msg.obj: byte[]action
		 * */
		@Override
		public void onSynActioncAck(Message msg) {

		}

		/**
		 * msg.arg1: page number; msg.arg2: pageVersion; msg.obj: byte[]action
		 * */
		@Override
		public void onSyncActionBroadcast(Message msg) {

		}

		/**
		 * msg.arg1: page number; msg.arg2: pageVersion
		 * */
		@Override
		public void onSyncPageBroadcast(Message msg) {

		}

		/**
		 * msg.obj: NetworkServerThread.FileInfo;
		 * */
		@Override
		public void onReceiveFileStart(Message msg) {

		}

		/**
		 * msg.obj: NetworkServerThread.FileInfo;
		 * */
		@Override
		public void onReceiveFileEnd(Message msg) {

		}

		/**
		 * msg.obj: NetworkServerThread.FileInfo;
		 * */
		@Override
		public void onSendFileStart(Message msg) {

		}

		/**
		 * msg.obj: NetworkServerThread.FileInfo;
		 * */
		@Override
		public void onSendFileEnd(Message msg) {

		}

		@Override
		public void onShowInfo(Message msg) {

		}

	}

	public interface HandlerInterface {

		/** sync start */
		// msg.arg1: int version;
		void onSyncShakehand(Message msg);

		/*
		 * int[] ackArray = (int[]) msg.obj; int state = ackArray[0]; int width
		 * = ackArray[1]; int height = ackArray[2]; int pageCount = ackArray[3];
		 * int curPage = ackArray[4];
		 */
		void onSyncShakehandAck(Message msg);

		// msg.arg1:state; msg.obj:NetworkServerThread.SyncBrowsingInfo
		void onSyncGetPageAck(Message msg);

		// msg.arg1: page number; msg.arg2: pageVersion
		void onSyncGetPageRequest(Message msg);

		// msg.obj: byte[]action
		void onSynActioncAck(Message msg);

		// msg.arg1: page number; msg.arg2: pageVersion; msg.obj: byte[]action
		void onSyncActionBroadcast(Message msg);

		// msg.arg1: page number; msg.arg2: pageVersion
		void onSyncPageBroadcast(Message msg);

		/** sync end */

		/** file transfer start */
		// msg.obj: NetworkServerThread.FileInfo;
		void onReceiveFileStart(Message msg);

		// msg.obj: NetworkServerThread.FileInfo;
		void onReceiveFileEnd(Message msg);

		// msg.obj: NetworkServerThread.FileInfo;
		void onSendFileStart(Message msg);

		// msg.obj: NetworkServerThread.FileInfo;
		void onSendFileEnd(Message msg);

		/** file transfer end */

		/** user manager */

		// msg.obj: String userName;
		void onUserLogin(Message msg);

		// msg.obj: String userName;
		void onUserLoginAck(Message msg);

		// null
		void onNewConnect(Message msg);

		// msg.obj: String userName;
		void onUserLogout(Message msg);
		
		// msg.obj: String userName;
		void onUserLogoutBroadCastMsg(Message msg);


		// msg.obj: String userName; msg.arg1: int isKickoutSelf
		void onKickedOut(Message msg);

		/** user manager end*/

		/** host msg **/
		
		void onHandleerNewConnect(Message msg);
		
		void onNetworkMsgLogin(Message msg);
		
		/** host msg end **/
		
		// msg.obj: String info
		void onShowInfo(Message msg);
	}

	private boolean syncMsgHandler(Message msg) {
		boolean handled = true;
		switch (msg.what) {
		case MsgDefine.NETWORK_SYNC_BROWSING_SHAKEHAND:
			mListener.onSyncShakehand(msg);
			break;

		case MsgDefine.NETWORK_SYNC_BROWSING_SHAKEHAND_ACK:
			mListener.onSyncShakehandAck(msg);
			break;

		case MsgDefine.NETWORK_SYNC_BROWSING_BROADCAST_ACTION:
			mListener.onSyncActionBroadcast(msg);
			break;

		case MsgDefine.NETWORK_SYNC_BROWSING_BROADCAST_ACTION_ACK:
			mListener.onSynActioncAck(msg);
			break;

		case MsgDefine.NETWORK_SYNC_BROWSING_PAGE_SYNC_BROADCAST:
			mListener.onSyncPageBroadcast(msg);
			break;

		case MsgDefine.NETWORK_SYNC_BROWSING_PAGE_REQUEST:
			mListener.onSyncGetPageRequest(msg);
			break;

		case MsgDefine.NETWORK_SYNC_BROWSING_PAGE_REQUEST_ACK:
			mListener.onSyncGetPageAck(msg);
			break;

		default:
			handled = false;
			break;
		}

		return handled;
	}

	private boolean userMgrMsgHandler(Message msg) {
		boolean handled = true;
		switch (msg.what) {
		case MsgDefine.NETWORK_MSG_KICKOUT:
			mListener.onKickedOut(msg);
			break;

		case MsgDefine.NETWORK_MSG_LOGOUT:
			mListener.onUserLogout(msg);
			break;

		default:
			handled = false;
			break;
		}

		return handled;
	}

	private boolean clientMsgHandler(Message msg) {
		boolean handled = true;
		switch (msg.what) {
		case MsgDefine.HANDLER_MSG_CONNECTED:
			mListener.onNewConnect(msg);
			break;

		case MsgDefine.NETWORK_MSG_LOGIN_ACK:
			mListener.onUserLoginAck(msg);
			break;

		case MsgDefine.NETWORK_BROADCAST_MSG_LOGIN:
			// /xlr todo : login broadcast
			mListener.onUserLogin(msg);
			break;

		case MsgDefine.NETWORK_BROADCAST_MSG_LOGOUT: {
			// /xlr todo : logout broadcast
			// / todo : add branch for hotspot
			mListener.onUserLogoutBroadCastMsg(msg);
		}
			break;
		default:
			handled = false;
			break;
		}

		return handled;
	}

	private boolean hostMsgHandler(Message msg) {
		boolean handled = true;
		switch (msg.what) {
		case MsgDefine.HANDLER_MSG_NEW_CONNECT:
			mListener.onHandleerNewConnect(msg);
			break;

		case MsgDefine.NETWORK_MSG_LOGIN:
			mListener.onNetworkMsgLogin(msg);
			break;

		default:
			handled = false;
			break;
		}
		return handled;
	}

	private boolean fileTransMsgHandler(Message msg) {
		boolean handled = true;

		switch (msg.what) {
		case MsgDefine.HANDLER_MSG_FILE_SEND_REQ:
			mListener.onSendFileStart(msg);
			break;

		case MsgDefine.HANDLER_MSG_FILE_SEND_FIN:
			mListener.onSendFileEnd(msg);
			break;

		case MsgDefine.NETWORK_MSG_FILE_SEND_REQ:
			mListener.onReceiveFileStart(msg);
			break;

		case MsgDefine.NETWORK_MSG_FILE_SEND_FIN:
			mListener.onReceiveFileEnd(msg);
			break;

		default:
			handled = false;
			break;
		}

		return handled;
	}

}
