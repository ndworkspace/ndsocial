package com.nd.voice.meetingroom.lib;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.ListView;

public class SwipeListView extends ListView {
	

	public SwipeListView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}


	public SwipeListView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}
	
	public SwipeListView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);

		ViewConfiguration vc = ViewConfiguration.get(context);
		mSlop = vc.getScaledTouchSlop();
		mMinFlingVelocity = vc.getScaledMinimumFlingVelocity() * 8; //获取滑动的最小速度
		mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();  //获取滑动的最大速度
	}


	/**
	 * 认为是用户滑动的最小距离
	 */
	private int mSlop;
	/**
	 * 滑动的最小速度
	 */
	private int mMinFlingVelocity;
	/**
	 * 滑动的最大速度
	 */
	private int mMaxFlingVelocity;
	/**
	 * 执行动画的时间
	 */
	protected long mAnimationTime = 150;
	/**
	 * 用来标记用户是否正在滑动中
	 */
	private boolean mSwiping;
	/**
	 * 滑动速度检测类
	 */
	private VelocityTracker mVelocityTracker;
	/**
	 * 手指按下的position
	 */
	private int mDownPosition;
	/**
	 * 按下的item对应的View
	 */
	private View mDownView;
	private float mDownX;
	private float mDownY;
	/**
	 * item的宽度
	 */
	private int mViewWidth;
	
	/**
	 * 当ListView的Item滑出界面回调的接口
	 */
	private OnSwipeCallback onSwipeCallback;


	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			handleActionDown(ev);
			break;
		case MotionEvent.ACTION_MOVE:
			return handleActionMove(ev);
		case MotionEvent.ACTION_UP:
			handleActionUp(ev);
			break;
		}
		return super.onTouchEvent(ev);
	}
	
	/**
	 * 按下事件处理
	 * 
	 * @param ev
	 * @return
	 */
	private void handleActionDown(MotionEvent ev) {
		mDownX = ev.getX();
		mDownY = ev.getY();
		
		mDownPosition = pointToPosition((int) mDownX, (int) mDownY);

		if (mDownPosition == AdapterView.INVALID_POSITION) {
			return;
		}

		mDownView = getChildAt(mDownPosition - getFirstVisiblePosition());

		if (mDownView != null) {
			mViewWidth = mDownView.getWidth();
		}

		//加入速度检测
		mVelocityTracker = VelocityTracker.obtain();
		mVelocityTracker.addMovement(ev);
	}
	

	/**
	 * 处理手指滑动的方法
	 * 
	 * @param ev
	 * @return
	 */
	private boolean handleActionMove(MotionEvent ev) {
		if (mVelocityTracker == null || mDownView == null) {
			return super.onTouchEvent(ev);
		}

		float deltaX = ev.getX() - mDownX;
		float deltaY = ev.getY() - mDownY;

		// X方向滑动的距离大于mSlop并且Y方向滑动的距离小于mSlop，表示可以滑动
		if (Math.abs(deltaX) > mSlop && Math.abs(deltaY) < mSlop) {
			mSwiping = true;
			
			//当手指滑动item,取消item的点击事件，不然我们滑动Item也伴随着item点击事件的发生
			MotionEvent cancelEvent = MotionEvent.obtain(ev);
            cancelEvent.setAction(MotionEvent.ACTION_CANCEL |
                       (ev.getActionIndex()<< MotionEvent.ACTION_POINTER_INDEX_SHIFT));
            onTouchEvent(cancelEvent);
		}

		if (mSwiping) {
			// 跟谁手指移动item
//			ViewHelper.setTranslationX(mDownView, deltaX);
//			// 透明度渐变
//			ViewHelper.setAlpha(mDownView, Math.max(0f, Math.min(1f, 1f - 2f * Math.abs(deltaX)/ mViewWidth)));

			// 手指滑动的时候,返回true，表示SwipeDismissListView自己处理onTouchEvent,其他的就交给父类来处理
			return true;
		}

		return super.onTouchEvent(ev);

	}

	/**
	 * 手指抬起的事件处理
	 * @param ev
	 */
	private void handleActionUp(MotionEvent ev) {
		if (mVelocityTracker == null || mDownView == null|| !mSwiping) {
			return;
		}

		float deltaX = ev.getX() - mDownX;
		
		//通过滑动的距离计算出X,Y方向的速度
		mVelocityTracker.computeCurrentVelocity(1000);
		float velocityX = Math.abs(mVelocityTracker.getXVelocity());
		float velocityY = Math.abs(mVelocityTracker.getYVelocity());
		
		boolean dismiss = false; //item是否要滑出屏幕
		boolean dismissRight = false;//是否往右边删除
		
		//当拖动item的距离大于item的一半，item滑出屏幕
		if (Math.abs(deltaX) > mViewWidth / 3) {
			dismiss = true;
			dismissRight = deltaX > 0;
			
			//手指在屏幕滑动的速度在某个范围内，也使得item滑出屏幕
		} else if (mMinFlingVelocity <= velocityX
				&& velocityX <= mMaxFlingVelocity && velocityY < velocityX) {
			dismiss = true;
			dismissRight = mVelocityTracker.getXVelocity() > 0;
		}
		
		if (dismiss) {
			if (onSwipeCallback != null) {
				onSwipeCallback.onSwipe(mDownPosition,mDownView);
			}
		} else {
			onSwipeCallback.onCancelSwipe(mDownPosition,mDownView);
		}
		
		//移除速度检测
		if(mVelocityTracker != null){
			mVelocityTracker.recycle();
			mVelocityTracker = null;
		}
		
		mSwiping = false;
	}
	

	public OnSwipeCallback getOnSwipeCallback() {
		return onSwipeCallback;
	}


	public void setOnSwipeCallback(OnSwipeCallback onSwipeCallback) {
		this.onSwipeCallback = onSwipeCallback;
	}



	/**
	 * 删除的回调接口
	 * 
	 * @author xiaanming
	 * 
	 */
	public interface OnSwipeCallback {
		public void onSwipe(int position,View holdView);
		public void onCancelSwipe(int postion,View holdView);
	}
}
