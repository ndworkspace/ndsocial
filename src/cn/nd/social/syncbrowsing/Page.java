package cn.nd.social.syncbrowsing;

import java.lang.ref.SoftReference;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class Page {
	private int index = 0;
	private boolean loading = false;
	private Bitmap bitmap = null;
	private final Paint bitmapPaint = new Paint();
	
	public Page(int index)
	{
		this.index = index;
	}
	
	public int getIndex() {
		return index;
	}
	    
    public void setBitmap(Bitmap bitmap) {
        if (bitmap != null && bitmap.getWidth() == -1 && bitmap.getHeight() == -1) {
            return;
        }
        if (this.bitmap != bitmap) {
            if(this.bitmap != null && !this.bitmap.isRecycled()) {
            	this.bitmap.recycle();
            	System.gc();
            }
            this.bitmap = bitmap;
        }
    }
    
    public Bitmap getBitmap() {
        return bitmap;
    }
    
    public boolean isLoading() {
    	return loading;
    }
    
    public void setLoading(boolean loading) {
    	this.loading = loading;
    }    
	
	public void draw(Canvas canvas, Rect rect) {
        if (getBitmap() != null) {
            canvas.drawBitmap(getBitmap(), 
            		new Rect(0, 0, getBitmap().getWidth(), getBitmap().getHeight()),
            		rect, bitmapPaint);
        } 
	}
}
