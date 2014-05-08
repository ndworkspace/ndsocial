package com.example.ofdmtransport;

import java.io.File;

import cn.nd.social.util.FilePathHelper;

public class SonicWaveUtils {
	private static String sCardFile;
	private static String sAckFileName;
	private static String sShareCardDir;
	private static String sWifiAccountFile;
	
	static {
		File dir = new File(sShareCardDir);
		if(dir.exists()) {
			if(!dir.isDirectory()) {
				dir.delete();
				dir.mkdirs();
			}
		} else {
			dir.mkdirs();
		}
		sCardFile = FilePathHelper.getTmpFilePath() + File.separator + "card_share.wav";
		sAckFileName = FilePathHelper.getTmpFilePath() + File.separator + "ack.wav";
		
		sWifiAccountFile = FilePathHelper.getTmpFilePath() + File.separator + "pri_share.wav";
	}
	
	public static String getCardFilePath() {
		return sCardFile;
	}
	
	public static String getWifiAccountFilePath() {
		return sWifiAccountFile;
	}
	
	public static String getWifiAccountAckFilePath() {
		return sAckFileName;
	}
	
}
