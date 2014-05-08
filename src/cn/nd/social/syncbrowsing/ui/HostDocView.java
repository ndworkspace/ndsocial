package cn.nd.social.syncbrowsing.ui;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.ViewConfiguration;
import cn.nd.social.privategallery.imageviewer.ImageViewTouchBase;
import cn.nd.social.syncbrowsing.Document;
import cn.nd.social.syncbrowsing.ui.SyncAction.DrawState;

public class HostDocView extends ImageViewTouchBase {

	static final float SCROLL_DELTA_THRESHOLD = 1.0f;
	protected ScaleGestureDetector mScaleDetector;
	protected GestureDetector mGestureDetector;
	protected int mTouchSlop;
	protected float mScaleFactor;
	protected int mDoubleTapDirection;
	protected OnGestureListener mGestureListener;
	protected OnScaleGestureListener mScaleListener;
	protected boolean mDoubleTapEnabled = false; //disable doubleTap by tangtaotao
	protected boolean mScaleEnabled = true;
	protected boolean mScrollEnabled = true;
	private OnImageViewTouchDoubleTapListener mDoubleTapListener;
	private OnImageViewTouchSingleTapListener mSingleTapListener;

	public HostDocView(Context context) {
		super(context);
	}

	public HostDocView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public HostDocView(Context context, AttributeSet attrs, int defStyle) {
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

	
	public enum OpMode {
		NORMAL,COMMENT
	}
	
	public enum TouchState {
		NON_TOUCH,TOUCH_DOWN,TOUCH_MOVE,TOUCH_UP
	}
	
	public class ViewPoint {
		float x;
		float y;
	}

	public class Line {
		ArrayList<ViewPoint> points = new ArrayList<ViewPoint>();      
	}
	
	
	private OpMode mOpMode = OpMode.NORMAL;
	private TouchState mCommentTouchState = TouchState.NON_TOUCH;
	private Document mDoc;
	private HostSyncReadView mHostView;

	private Line current = new Line();
	private Paint mPaint;
	private float clickX,clickY;
	
	public void setOpMode(OpMode mode) {
		mOpMode = mode;
	}
	
	public OpMode getOpMode() {
		return mOpMode;
	}
	
	public void switchMode() {
		if(mOpMode == OpMode.COMMENT) {
			mOpMode = OpMode.NORMAL;
			if(mDrawListener != null) {
				mDrawListener.onDraw(DrawState.EXIT_DRAW_MODE,0, 0);
			}
		} else {
			mOpMode = OpMode.COMMENT;
			if(mDrawListener != null) {
				mDrawListener.onDraw(DrawState.ENTER_DRAW_MODE,0, 0);
			}
		}
	}
	
	public void ensureNormalMode() {
		if(mOpMode == OpMode.COMMENT) {
			mOpMode = OpMode.NORMAL;
			if(mDrawListener != null) {
				mDrawListener.onDraw(DrawState.EXIT_DRAW_MODE,0, 0);
			}
		}
	}
	
    
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
	
	public boolean onCommentTouchEvent(MotionEvent event) {
		clickX = event.getX();
		clickY = event.getY();

		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if(mDrawListener != null) {
				mDrawListener.onDraw(DrawState.START_DRAW,clickX, clickY);
			}
			current = new Line();
			mCommentTouchState = TouchState.TOUCH_DOWN;
			return true;
		} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			if(checkDrawLineOverload()) {
				return true;
			}
			if(mDrawListener != null) {
				mDrawListener.onDraw(DrawState.MOVE_DRAW,clickX, clickY);
			}
			
			ViewPoint point = new ViewPoint();
			point.x = clickX;
			point.y = clickY;
			current.points.add(point);
			mCommentTouchState = TouchState.TOUCH_MOVE;
			invalidate();
			return true;
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			if(mDrawListener != null) {
				mDrawListener.onDraw(DrawState.FINISH_DRAW,0, 0);
			}
			mCommentTouchState = TouchState.TOUCH_UP; // clear the touch event
														// in onDraw
			invalidate();
		} else {
			mCommentTouchState = TouchState.NON_TOUCH;
			if(mDrawListener != null) {
				mDrawListener.onDraw(DrawState.CANCEL_DRAW,0, 0);
			}
		}

		return super.onTouchEvent(event);
	}
    
    
    public void setDocument(Document doc) {
    	mDoc = doc;
    }
    
    public void setHostView(HostSyncReadView hostView) {
    	mHostView = hostView;
    }
    
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(mOpMode == OpMode.COMMENT) {//TODO: comment mode			
			onCommentTouchEvent(event);
			return true;
		}
		
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
	}

	private boolean isInCommentTouch() {
		return mCommentTouchState != TouchState.NON_TOUCH;
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
			} else if(isInCommentTouch()) {	
				// draw current line
				drawLine(canvas, current);
			}
			
		} else {
		}
	}
	
	@Override
	protected void onZoomAnimationCompleted(float scale) {

		if (LOG_ENABLED) {
			Log.d(LOG_TAG, "onZoomAnimationCompleted. scale: " + scale
					+ ", minZoom: " + getMinScale());
		}

		if (scale < getMinScale()) {
			zoomTo(getMinScale(), 50);
			/*tangtaotao add start*/
			if(mZoomListener != null) {
				mZoomListener.onZoom(getMinScale(), 50);
			}
			/*tangtaotao add end*/
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

		if(checkOverload()) {//not update too frequently
			return true;
		}
		
		if(Math.abs(distanceX) > 30 && !canScroll(-(int)distanceX)) {
			if(!checkFlipOverload()) {
				if (distanceX > 0) {
					mHostView.showNextPage();
				} else {
					mHostView.showPreviousPage();
				}
				return true;
			}
		}
		
		if (getScale() == 1f)
			return false;
		mUserScaled = true;
		/*tangtaotao add start*/
		if(mScrollListener != null) {
			mScrollListener.onScroll(-distanceX, -distanceY);
		}
		/*tangtaotao add end*/
		scrollBy(-distanceX, -distanceY);
		invalidate();
		return true;
	}
	

	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		if(checkOverload()) {//not update too frequently
			return true;
		}
		float diffX = e2.getX() - e1.getX();
		float diffY = e2.getY() - e1.getY();

		if (Math.abs(velocityX) > 800 || Math.abs(velocityY) > 800) {
			mUserScaled = true;
			/*tangtaotao add start*/
			if(mScrollListener != null) {
				mScrollListener.onScroll(diffX / 2, diffY / 2, 300);
			}
			/*tangtaotao add end*/
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
			/*tangtaotao add start*/
			if(mZoomListener != null) {
				mZoomListener.onZoom(getMinScale(), 50);
			}
			/*tangtaotao add end*/
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

			return HostDocView.this.onSingleTapConfirmed(e);
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
				/*tangtaotao add start*/
				if(mZoomListener != null) {
					Log.e("DocumentView","onZoom Anim");
					mZoomListener.onZoom(targetScale, e.getX(), e.getY(),DEFAULT_ANIMATION_DURATION);
				}
				/*tangtaotao add end*/
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
			return HostDocView.this.onScroll(e1, e2, distanceX, distanceY);
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

			return HostDocView.this.onFling(e1, e2, velocityX, velocityY);
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			return HostDocView.this.onSingleTapUp(e);
		}

		@Override
		public boolean onDown(MotionEvent e) {
			return HostDocView.this.onDown(e);
		}
	}

	public class ScaleListener extends
			ScaleGestureDetector.SimpleOnScaleGestureListener {

		protected boolean mScaled = false;

		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			float span = detector.getCurrentSpan() - detector.getPreviousSpan();
			float targetScale = getScale() * detector.getScaleFactor();
			
			if(checkOverload()) {//not update too frequently
				return true;
			}
			if (mScaleEnabled) {
				if (mScaled && span != 0) {
					mUserScaled = true;
					targetScale = Math.min(getMaxScale(),
							Math.max(targetScale, getMinScale() - 0.1f));
					/*tangtaotao add start*/
					if(mZoomListener != null) {
						float factor = detector.getScaleFactor();
						if(factor > 1.05f) {
							factor *= 1.1;
						} else if(targetScale < 0.95f) {
							factor *= 0.9;
						}
						targetScale = getScale() * factor;
						mZoomListener.onZoom(targetScale, detector.getFocusX(), detector.getFocusY());
					}
					/*tangtaotao add end*/
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
	
	private OnScrollListener mScrollListener;
	private OnZoomListener mZoomListener;
	private OnDrawListener mDrawListener;
	public void setOnScrollListener(OnScrollListener l) {
		mScrollListener = l;
	}
	
	public void setOnZoomListener(OnZoomListener l) {
		mZoomListener = l;
	}
	
	public void setOnDrawListener(OnDrawListener l) {
		mDrawListener = l;
	}
	
	public interface OnScrollListener {
		public void onScroll(float distanceX,float distanceY);
		public void onScroll(float distanceX,float distanceY,int timeInMills);
	}
	public interface OnZoomListener {
		public void onZoom(float scale,float centerX,float centerY,int timeInMills);
		public void onZoom(float scale,float centerX,float centerY);
		public void onZoom(float scale,int timeInMills);
	}
	
	public interface OnDrawListener {
		public void onDraw(DrawState state,float x,float y);
	}
	
	private long mTimeStamp = 0;
	private boolean checkOverload() {
		long time = System.currentTimeMillis();
		if (mTimeStamp == 0) {
			mTimeStamp = time;
		} else if (time - mTimeStamp < 50) {
			return true;
		} else {
			mTimeStamp = time;
		}
		return false;
	}
	
	private long mFlipTimeStamp = 0;
	private boolean checkFlipOverload() {
		long time = System.currentTimeMillis();
		if (mFlipTimeStamp == 0) {
			mFlipTimeStamp = time;
		} else if (time - mFlipTimeStamp < 400) {
			return true;
		} else {
			mFlipTimeStamp = time;
		}
		return false;
	}
	
	private boolean checkDrawLineOverload() {
		long time = System.currentTimeMillis();
		if (mTimeStamp == 0) {
			mTimeStamp = time;
		} else if (time - mTimeStamp < 50) {
			return true;
		} else {
			mTimeStamp = time;
		}
		return false;
	}
}
