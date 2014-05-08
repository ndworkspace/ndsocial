package cn.nd.social.prishare.component;

import cn.nd.social.hotspot.MsgDefine;

import com.example.ofdmtransport.Modulation;

import android.os.Handler;
import android.os.Message;

public class MainHandler extends Handler{

	
	private MainMsgHandlerInterface mListener;
	public void setDisposer(MainMsgHandlerInterface l){
		mListener = l;
	}
	
	
	@Override
	public void handleMessage(Message msg) {
		// TODO Auto-generated method stub
		if (msg.what == MsgDefine.HANDLER_NOTIFY_INFO) {
			mListener.onShowInfo(msg);
		} else if (msg.what == MsgDefine.HANDLER_MSG_CREATE_HOTSPOT) {
			mListener.onHandlerMsgCreateHotSpot(msg);
		} else if (msg.what == MsgDefine.HANDLER_MSG_CONNECT_WIFI) {
			mListener.onHandlerMsgConnectWifi(msg);
		}

		// modulation operator
		if (msg.what == Modulation.MODULATION_HANDLER_RECV_NOTIFY) {
			mListener.onShowInfo(msg);
			mListener.onModulationHandlerRecvNotify(msg);
		} else if (msg.what == Modulation.MODULATION_HANDLER_RECV_NOTIFY_ACK) {
			mListener.onModulationHandlerRecvNotifyAck(msg);
			mListener.onShowInfo(msg);
	
		}else if (msg.what == Modulation.MODULATION_HANDLER_PLAY_FINISH) {
			mListener.onModulationHandlerPlayFinish(msg);
		}

		super.handleMessage(msg);
	}
	
	public interface MainMsgHandlerInterface{
		
		void onHandlerMsgCreateHotSpot(Message msg);
		void onHandlerMsgConnectWifi(Message msg);
		void onModulationHandlerRecvNotify(Message msg);
		void onModulationHandlerRecvNotifyAck(Message msg);
		void onModulationHandlerPlayFinish(Message msg);
		void onShowInfo(Message msg);
	}

}
