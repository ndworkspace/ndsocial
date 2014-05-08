package cn.nd.social.util;

public class WifiInfoDataPacket extends AudioDataPacker {
	public WifiInfoDataPacket(int type) {
		super(type);
	}
	
	@Override
	public boolean extractData(String rawWaveData, Object obj) {
		String wifiInfo = super.unPackAudioData(rawWaveData);
		String[] temp = wifiInfo.split(" ");
		WifiInfoHolder holder = (WifiInfoHolder)obj;
		holder.ssid = temp[0];
		holder.passwd = temp[1];
		return true;
	}
	
	@Override
	public String packAudioData(Object obj) {
		WifiInfoHolder holder = (WifiInfoHolder)obj;
		String data = holder.ssid + " " + holder.passwd;
		return super.packAudioData(data);
	}
	
	public static class WifiInfoHolder {
		public WifiInfoHolder() {
			
		}
		public WifiInfoHolder(String ssid,String passwd) {
			this.ssid = ssid;
			this.passwd = passwd;
		}
		public String ssid;
		public String passwd;
	}	
}
