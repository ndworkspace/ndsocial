package cn.nd.social.privategallery;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class CustomViewPager extends ViewPager{

	private boolean isCanScroll = true;
	
	public CustomViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CustomViewPager(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		
	}

	 public void setScanScroll(boolean isCanScroll) {  
	        this.isCanScroll = isCanScroll;  
	    }

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		 if (this.isCanScroll) {
	            return super.onInterceptTouchEvent(event);
	        }

	     return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		 if (this.isCanScroll) {
	            return super.onTouchEvent(event);
	        }

	     return false;
	} 
	 
	 
	
	
}
