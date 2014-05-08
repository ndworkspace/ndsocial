package com.example.ofdmtransport;

import android.os.Handler;
import android.os.Message;
import cn.nd.social.util.AudioDataPacker;

public interface RecordingListener {
	public boolean interceptRecording();
	public void onValidWaveArrival(String data);
	
	public static class PrivateShareRecord implements RecordingListener {
		private Handler mHandler;		
		public PrivateShareRecord(Handler handler) {
			mHandler = handler;
		}
		
		@Override
		public boolean interceptRecording() {
			return false;
		}

		@Override
		public void onValidWaveArrival(String data) {			
			Message msg = mHandler.obtainMessage();
			msg.what = Modulation.MODULATION_HANDLER_RECV_NOTIFY;
			msg.obj = data;
			mHandler.sendMessage(msg);
			playModulationAck();
		}
		
		private void playModulationAck() {
			AudioDataPacker.playModulationAck();
		}
		
	}
	
	public static class CardShareRecord implements RecordingListener {
		private Handler mHandler;
		private int mEventId;
		private boolean mIntercept = false;
		
		/**
		 * API start
		 */
		public CardShareRecord(Handler handler,int eventId) {
			mHandler = handler;
			mEventId = eventId;
		}
		
		public void setIntercept(boolean intercept) {
			mIntercept = intercept;
		}
		
		/**
		 * API end
		 */
		
		@Override
		public boolean interceptRecording() {
			return mIntercept;
		}
		
		@Override
		public void onValidWaveArrival(String data) {			
			Message msg = mHandler.obtainMessage();
			msg.what = mEventId;
			msg.obj = data;
			mHandler.sendMessage(msg);
		}		
	}
	
	
	
}