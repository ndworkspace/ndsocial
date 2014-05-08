package cn.nd.social.services;


public interface ISocialService {
	// file operate
	public static class FileControlPara {
		public String fileName;
		public long startTime;
		public int controlType;
		public int expireTime;
		public int staticTime;
	}

	public abstract void AddFileControl(FileControlPara para);
	
	// voice operate
	public abstract void StartVoice();
}
