package cn.nd.social.syncbrowsing.ui;

import java.util.ArrayList;

import cn.nd.social.privategallery.imageviewer.ImageViewTouchBase;
import cn.nd.social.syncbrowsing.Document;
import cn.nd.social.syncbrowsing.ui.SyncAction.DrawState;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.ViewConfiguration;

public class ClientDocView extends ImageViewTouchBase {

	static final float SCROLL_DELTA_THRESHOLD = 1.0f;
	protected ScaleGestureDetector mScaleDetector;
	protected GestureDetector mGestureDetector;
	protected int mTouchSlop;
	protected float mScaleFactor;
	protected int mDoubleTapDirection;
	protected OnGestureListener mGestureListener;
	protected OnScaleGestureListener mScaleListener;
	protected boolean mDoubleTapEnabled = false;
	protected boolean mScaleEnabled = true;
	protected boolean mScrollEnabled = true;
	private OnImageViewTouchDoubleTapListener mDoubleTapListener;
	private OnImageViewTouchSingleTapListener mSingleTapListener;

	public ClientDocView(Context context) {
		super(context);
	}

	public ClientDocView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ClientDocView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void init(Context context, AttributeSet attrs, int defStyle) {
		super.init(context, attrs, defStyle);
		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
		mGestureListener = getGestureListener();
		mScaleListener = getScaleListener();

		mScaleDetector = new ScaleGestureDetector(getContext(), mScaleListener);
		mGestureDetector = new GestureDetector(getContext(), mGestureListener,
				null, true);

		mDoubleTapDirection = 1;
		
		/**tangtaotao add*/
		mPaint = new Paint();
		mPaint.setColor(Color.RED);
		mPaint.setStrokeWidth(3);
	}

	public void setDoubleTapListener(OnImageViewTouchDoubleTapListener listener) {
		mDoubleTapListener = listener;
	}

	public void setSingleTapListener(OnImageViewTouchSingleTapListener listener) {
		mSingleTapListener = listener;
	}

	public void setDoubleTapEnabled(boolean value) {
		mDoubleTapEnabled = value;
	}

	public void setScaleEnabled(boolean value) {
		mScaleEnabled = value;
	}

	public void setScrollEnabled(boolean value) {
		mScrollEnabled = value;
	}

	public boolean getDoubleTapEnabled() {
		return mDoubleTapEnabled;
	}

	protected OnGestureListener getGestureListener() {
		return new GestureListener();
	}

	protected OnScaleGestureListener getScaleListener() {
		return new ScaleListener();
	}

	@Override
	protected void _setImageDrawable(final Drawable drawable,
			final Matrix initial_matrix, float min_zoom, float max_zoom) {
		super._setImageDrawable(drawable, initial_matrix, min_zoom, max_zoom);
		mScaleFactor = getMaxScale() / 3;
	}


	
	
/*	@Override
	public boolean onTouchEvent(MotionEvent event) {
		mScaleDetector.onTouchEvent(event);

		if (!mScaleDetector.isInProgress()) {
			mGestureDetector.onTouchEvent(event);
		}

		int action = event.getAction();
		switch (action & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_UP:
			return onUp(event);
		}
		return true;
	}*/

	@Override
	protected void onZoomAnimationCompleted(float scale) {

		if (LOG_ENABLED) {
			Log.d(LOG_TAG, "onZoomAnimationCompleted. scale: " + scale
					+ ", minZoom: " + getMinScale());
		}

		if (scale < getMinScale()) {
			zoomTo(getMinScale(), 50);
		}
	}

	protected float onDoubleTapPost(float scale, float maxZoom) {
		if (mDoubleTapDirection == 1) {
			if ((scale + (mScaleFactor * 2)) <= maxZoom) {
				return scale + mScaleFactor;
			} else {
				mDoubleTapDirection = -1;
				return maxZoom;
			}
		} else {
			mDoubleTapDirection = 1;
			return 1f;
		}
	}

	public boolean onSingleTapConfirmed(MotionEvent e) {
		return true;
	}

	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		if (getScale() == 1f)
			return false;
		mUserScaled = true;
		scrollBy(-distanceX, -distanceY);
		invalidate();
		return true;
	}

	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		float diffX = e2.getX() - e1.getX();
		float diffY = e2.getY() - e1.getY();

		if (Math.abs(velocityX) > 800 || Math.abs(velocityY) > 800) {
			mUserScaled = true;
			scrollBy(diffX / 2, diffY / 2, 300);
			invalidate();
			return true;
		}
		return false;
	}

	public boolean onDown(MotionEvent e) {
		return true;
	}

	public boolean onUp(MotionEvent e) {
		if (getScale() < getMinScale()) {
			zoomTo(getMinScale(), 50);
		}
		return true;
	}

	public boolean onSingleTapUp(MotionEvent e) {
		return true;
	}

	/**
	 * Determines whether this ImageViewTouch can be scrolled.
	 * 
	 * @param direction
	 *            - positive direction value means scroll from right to left,
	 *            negative value means scroll from left to right
	 * 
	 * @return true if there is some more place to scroll, false - otherwise.
	 */
	public boolean canScroll(int direction) {
		RectF bitmapRect = getBitmapRect();
		updateRect(bitmapRect, mScrollRect);
		Rect imageViewRect = new Rect();
		getGlobalVisibleRect(imageViewRect);

		if (null == bitmapRect) {
			return false;
		}

		if (bitmapRect.right >= imageViewRect.right) {
			if (direction < 0) {
				return Math.abs(bitmapRect.right - imageViewRect.right) > SCROLL_DELTA_THRESHOLD;
			}
		}

		double bitmapScrollRectDelta = Math.abs(bitmapRect.left
				- mScrollRect.left);
		return bitmapScrollRectDelta > SCROLL_DELTA_THRESHOLD;
	}

	public class GestureListener extends
			GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {

			if (null != mSingleTapListener) {
				mSingleTapListener.onSingleTapConfirmed();
			}

			return ClientDocView.this.onSingleTapConfirmed(e);
		}

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			Log.i(LOG_TAG, "onDoubleTap. double tap enabled? "
					+ mDoubleTapEnabled);
			if (mDoubleTapEnabled) {
				mUserScaled = true;
				float scale = getScale();
				float targetScale = scale;
				targetScale = onDoubleTapPost(scale, getMaxScale());
				targetScale = Math.min(getMaxScale(),
						Math.max(targetScale, getMinScale()));
				zoomTo(targetScale, e.getX(), e.getY(),
						DEFAULT_ANIMATION_DURATION);
				invalidate();
			}

			if (null != mDoubleTapListener) {
				mDoubleTapListener.onDoubleTap();
			}

			return super.onDoubleTap(e);
		}

		@Override
		public void onLongPress(MotionEvent e) {
			if (isLongClickable()) {
				if (!mScaleDetector.isInProgress()) {
					setPressed(true);
					performLongClick();
				}
			}
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {

			if (!mScrollEnabled)
				return false;
			if (e1 == null || e2 == null)
				return false;
			if (e1.getPointerCount() > 1 || e2.getPointerCount() > 1)
				return false;
			if (mScaleDetector.isInProgress())
				return false;
			return ClientDocView.this.onScroll(e1, e2, distanceX, distanceY);
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			if (!mScrollEnabled)
				return false;

			if (e1.getPointerCount() > 1 || e2.getPointerCount() > 1)
				return false;
			if (mScaleDetector.isInProgress())
				return false;
			if (getScale() == 1f)
				return false;

			return ClientDocView.this.onFling(e1, e2, velocityX, velocityY);
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			return ClientDocView.this.onSingleTapUp(e);
		}

		@Override
		public boolean onDown(MotionEvent e) {
			return ClientDocView.this.onDown(e);
		}
	}

	public class ScaleListener extends
			ScaleGestureDetector.SimpleOnScaleGestureListener {

		protected boolean mScaled = false;

		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			float span = detector.getCurrentSpan() - detector.getPreviousSpan();
			float targetScale = getScale() * detector.getScaleFactor();

			if (mScaleEnabled) {
				if (mScaled && span != 0) {
					mUserScaled = true;
					targetScale = Math.min(getMaxScale(),
							Math.max(targetScale, getMinScale() - 0.1f));
					zoomTo(targetScale, detector.getFocusX(),
							detector.getFocusY());
					mDoubleTapDirection = 1;
					invalidate();
					return true;
				}

				// This is to prevent a glitch the first time
				// image is scaled.
				if (!mScaled)
					mScaled = true;
			}
			return true;
		}

	}

	public interface OnImageViewTouchDoubleTapListener {

		void onDoubleTap();
	}

	public interface OnImageViewTouchSingleTapListener {

		void onSingleTapConfirmed();
	}
	
	/*tangtaotao add start scroll and zoom*/
	public void onZoom(float targetScale,float centerX,float centerY) {
		mUserScaled = true;
		zoomTo(targetScale, centerX,centerY);
	}
	
	public void onZoom(float targetScale,int timeInMills) {
		mUserScaled = true;
		zoomTo(targetScale, timeInMills);
	}
	
	public void onZoom(float targetScale,float centerX,float centerY,int timeInMills) {
		mUserScaled = true;
		zoomTo(targetScale, centerX,centerY,timeInMills);
		invalidate();
	}
	
	public void onScroll(float distanceX, float distanceY, int timeInMills) {
		mUserScaled = true;
		scrollBy(distanceX,distanceY,timeInMills);
		invalidate();
		
	}
	public void onScroll(float distanceX, float distanceY) {
		mUserScaled = true;
		scrollBy(distanceX,distanceY);
		invalidate();
	}
	/*tangtaotao add end scroll and zoom*/
	
	
	/**
	 * add for comment-mode synchronization
	 * */
	public class ViewPoint {
		float x;
		float y;
	}

	public class Line {
		ArrayList<ViewPoint> points = new ArrayList<ViewPoint>();      
	}
	
	public enum OpMode {
		NORMAL,COMMENT
	}
	
	public enum TouchState {
		NON_TOUCH,TOUCH_DOWN,TOUCH_MOVE,TOUCH_UP
	}
	
	
	
	private OpMode mOpMode = OpMode.NORMAL;
	private TouchState mCommentTouchState = TouchState.NON_TOUCH;
	
	private Line current = new Line();
	private Paint mPaint;
	private Document mDoc;
	
	private void drawLine(Canvas canvas, Line line) {
		for (int i = 0; i < line.points.size() - 1; i++) {
			float x = line.points.get(i).x;
			float y = line.points.get(i).y;

			float nextX = line.points.get(i + 1).x;
			float nextY = line.points.get(i + 1).y;

			canvas.drawLine(x, y, nextX, nextY, mPaint);
		}

	}
    
	/**
	 * transform the coordination and draw the line
	 * */
	private void drawLineTrans(Canvas canvas, Line line) {
		
		Matrix matrix = new Matrix();
		getImageMatrix().invert(matrix);
		float []points = new float[line.points.size() * 2];
		for (int i = 0; i < line.points.size(); i++) {
			points[2*i] = line.points.get(i).x;
			points[2*i+1] = line.points.get(i).y;
		}
		
		matrix.mapPoints(points);
		
		for (int i = 0; i < line.points.size() - 1; i++) {//cut the last point
			float x = points[2*i];
			float y = points[2*i+1];

			float nextX = points[2*(i+1)];
			float nextY = points[2*(i+1)+1];

			canvas.drawLine(x, y, nextX, nextY, mPaint);
		}
	}
	
    public void setDocument(Document doc) {
    	mDoc = doc;
    }
    
    public void onDrawAction(DrawState state,float x1, float y1) {		
    	switch(state) {
    	case ENTER_DRAW_MODE:
    		mOpMode = OpMode.COMMENT;
    		break;
    		
    	case START_DRAW:
    		mCommentTouchState = TouchState.TOUCH_DOWN;
    		break;
    		
    	case MOVE_DRAW:
    		mCommentTouchState = TouchState.TOUCH_MOVE;
    		onMove(x1,y1);
    		break;
    		
    	case FINISH_DRAW:
    		mCommentTouchState = TouchState.TOUCH_UP;
    		invalidate();
    		break;
    		
    	case CANCEL_DRAW:
    		mCommentTouchState = TouchState.NON_TOUCH;
    		break;
    		
    	case EXIT_DRAW_MODE:
    		mOpMode = OpMode.NORMAL;
    		break;
    	}
	}
    
    private void onMove(float x1, float y1) {		
		ViewPoint point = new ViewPoint();
		point.x = x1;
		point.y = y1;
		current.points.add(point);
		invalidate();
	}
    
	/**
	 * save the line
	 * 
	 * */
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if(mOpMode == OpMode.COMMENT) {
			if(mCommentTouchState == TouchState.TOUCH_UP) {
				Bitmap bmp = mDoc.getCurrent();				
				Bitmap newbmp = bmp.copy(Config.RGB_565, true);				
				Canvas c = new Canvas(newbmp);
				drawLineTrans(c, current);
				mDoc.setCurrPageImage(newbmp);
				mCommentTouchState = TouchState.NON_TOUCH;
				current = new Line();
			} else if(mCommentTouchState != TouchState.NON_TOUCH) {	
				// draw current line
				drawLine(canvas, current);
			}
			
		}
	}
}
