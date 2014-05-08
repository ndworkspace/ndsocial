package cn.nd.social.syncbrowsing.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class ConnectUserView extends View{
	
	private final static int CIRCLE_RADIUS = 180;
	private final static int INNER_CIRCLE_RADIUS = 40;
	
	private View mStubVew;
	private CountSolver mCountSolver;

	
	public ConnectUserView(Context context,View stubView,CountSolver countSolver) {
		super(context);
		mStubVew = stubView;
		mCountSolver = countSolver;

	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		Paint p = new Paint();	
		p.setStyle(Paint.Style.STROKE);
		p.setStrokeWidth(2);
		p.setColor(Color.BLACK);
		
		int l = mStubVew.getLeft();
		int w = mStubVew.getWidth();
		int t = mStubVew.getTop();
		int h = mStubVew.getHeight();			
		canvas.drawCircle(l+w/2, t+h/2, CIRCLE_RADIUS, p);
		drawConnector(canvas);
	}
	
	private void drawConnector(Canvas canvas) {
		int l = mStubVew.getLeft();
		int w = mStubVew.getWidth();
		int t = mStubVew.getTop();
		int h = mStubVew.getHeight();			
		
		int connected = mCountSolver.getConnectCount();
		int notifyCount = mCountSolver.getNotifyCount();
		int count = Math.max(connected, notifyCount);
		if(count == 0) {
			return;
		}
		int degree = 360 / count;
		Paint p = new Paint();	
		p.setColor(0xff82cce5);
		for(int i=0; i<count; i++) {
			if(i >= connected) {
				p.setColor(0xffcfe2e8);
			}
			int x = (int)((l+w/2) +  CIRCLE_RADIUS * Math.cos(degree*i));
			int y = (int)((t+h/2) - CIRCLE_RADIUS * Math.sin(degree*i));
			canvas.drawCircle(x, y, INNER_CIRCLE_RADIUS, p);
		}
	}
	
	public static interface CountSolver {
		int getNotifyCount();
		int getConnectCount();
	}
}
