package cn.nd.social.syncbrowsing;

import android.graphics.RectF;
import cn.nd.social.syncbrowsing.codec.CodecContext;
import cn.nd.social.syncbrowsing.codec.DecodeService;
import cn.nd.social.syncbrowsing.codec.DecodeServiceBase;
import cn.nd.social.syncbrowsing.codec.LoadPageCallback;

public class LocalSource implements Source {
	private DecodeService decodeService = null;
	
	public LocalSource(CodecContext context, int targetWidth, int targetHeight) {
		this.decodeService = new DecodeServiceBase(context);			
		decodeService.setTargetSize(targetWidth, targetHeight);
	}		
	
	public void open(String fileName) {
		this.decodeService.open(fileName);
	}
	
	public int getPageCount() {
		return decodeService.getPageCount();
	}
	
	public void loadPage(Object obj, int index, LoadPageCallback callback) {
        decodeService.decodePage(obj, index, callback, 1.0f, new RectF(.0f, .0f, 1.0f, 1.0f));
	}		
}
