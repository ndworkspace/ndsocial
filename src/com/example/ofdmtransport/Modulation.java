package com.example.ofdmtransport;

import android.util.Log;

//ultrasonic codec
public class Modulation {
	public static final int MODULATION_HANDLER_RECV_NOTIFY = 100407;
	public static final int MODULATION_HANDLER_RECV_NOTIFY_ACK = 100408;
	public static final int MODULATION_HANDLER_PLAY_FINISH = 100409;

	private static boolean sEnInitialed = false;
	private static boolean sDeInitialed = false;
	
	public final static boolean MODULATION_OPTION_LISTEN_REPLY = false;
	public final static boolean MODULATION_OPTION_PLAY_ACK = true;
	
	static {
		System.loadLibrary("OfdmTransport");
	}
	
	public static void initEncoder() {
		if(sEnInitialed) {
			return;
		}
		sEnInitialed = true;
		CreateModulator();
		
	}
	public static void releaseEncoder() {
		if(!sEnInitialed) {
			return;
		}
		sEnInitialed = false;
		DestroyModulator();
	}

	public static void initDecoder() {
		if(sDeInitialed) {
			return;
		}
		sDeInitialed = true;
		CreateDemodulator();
	}
	public static void releaseDecoder() {
		if(!sDeInitialed) {
			return;
		}
		sDeInitialed = false;
		DestroyDemodulator();		
	}


	public static int process(short[] data, int datalen) {
		if(sDeInitialed == false) {
			Log.e("Modulation","process: decoder is not initilized");
			return ProcessData(data,datalen,0);
		} else {
			return ProcessData(data,datalen,0);
		}
	}
	
	public static void setListenMode(char mode) {
		if(sDeInitialed) {
			SetMode(mode);
		}
	}
	
	
	public static boolean genWavFile(String str, String filename) {
		return GenWavFile(str,filename,0);
	}

	public static boolean genWavFile(String str, int ncopies, String filename) {
		return GenWavFile(str,filename,1);//add effect
	}
	
	public static byte[] getResult() {
		return GetResult(0);
	}
	
	//temprory fix the problem
	//TODO: fix the libOfdmTransport
	public static boolean genReplyFile(String filename) {
		return true;
		//initEncoder();
		//boolean flag =  GenReplyFile(filename);
		//releaseEncoder();
		//return flag;
	}
	
	public static void initProcess() {
		InitProcess();
	}

	
	//deprecated method
	//public static native void InitModulator();
	//public static native void ReleaseModulator();
	
	//Modulator related methods
	private static native void CreateModulator();
	private static native void DestroyModulator();
	//public static native short[] GenWavData(String str, int scheme);
	private static native boolean GenWavFile(String str, String filename, int scheme);
	private static native boolean GenReplyFile(String filename);
	
	//Demodulator related methods
	private static native void CreateDemodulator();
	private static native void DestroyDemodulator();
	private static native void InitProcess();
	private static native void SetMode(char mode);
	private static native char GetMode();
	private static native int ProcessData(short[] data, int datalen, int scheme);
	//public static native byte[] DetectFromWavFile(short[] data, int datalen, int scheme);
	private static native byte[] GetResult(int scheme);
	
	
	
	
}
