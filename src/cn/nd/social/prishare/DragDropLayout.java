package cn.nd.social.prishare;

import cn.nd.dragdrop.DragController;
import cn.nd.dragdrop.DragSource;
import cn.nd.social.util.UnitConverter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.ArcShape;
import android.graphics.drawable.shapes.Shape;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.KeyEvent;
import android.view.View;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**
 * A ViewGroup that supports dragging within it. Dragging starts in an object
 * that implements the DragSource interface and ends in an object that
 * implements the DropTarget interface.
 * 
 * <p>
 * This class used DragLayer in the Android Launcher activity as a model. It is
 * a bit different in several respects: (1) it supports dragging to a grid view
 * and trash area; (2) it dynamically adds drop targets when a drag-drop
 * sequence begins. The child views of the GridView are assumed to implement the
 * DropTarget interface.
 */
public class DragDropLayout extends RelativeLayout implements
		DragController.DragListener {
	DragController mDragController;
	
/*	Shape mArcShape;
	
	Paint mPaint = new Paint();*/

	/**
	 * Used to create a new DragLayer from XML.
	 * 
	 * @param context
	 *            The application's context.
	 * @param attrs
	 *            The attribtues set containing the Workspace's customization
	 *            values.
	 */
	public DragDropLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
/*		mPaint.setColor(0x1affffff);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(UnitConverter.dpToPx(getResources(), 2));
		mArcShape = new ArcShape(0, 3f);*/
	}

	public void setDragController(DragController controller) {
		mDragController = controller;
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		return mDragController.dispatchKeyEvent(event)
				|| super.dispatchKeyEvent(event);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return mDragController.onInterceptTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		return mDragController.onTouchEvent(ev);
	}

	@Override
	public boolean dispatchUnhandledMove(View focused, int direction) {
		return mDragController.dispatchUnhandledMove(focused, direction);
	}

	/**
 */
	// DragListener Interface Methods

	/**
	 * A drag has begun.
	 * 
	 * @param source
	 *            An object representing where the drag originated
	 * @param info
	 *            The data associated with the object that is being dragged
	 * @param dragAction
	 *            The drag action: either
	 *            {@link DragController#DRAG_ACTION_MOVE} or
	 *            {@link DragController#DRAG_ACTION_COPY}
	 */

	public void onDragStart(DragSource source, Object info, int dragAction) {
		// We are starting a drag.
	}

	/**
	 * A drag-drop operation has eneded.
	 */

	public void onDragEnd() {
		mDragController.removeAllDropTargets();
	}

	/**
 */
	// Other Methods

	/**
	 * Show a string on the screen via Toast.
	 * 
	 * @param msg
	 *            String
	 * @return void
	 */

	public void toast(String msg) {
		Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
	} // end toast
	
	@Override
	protected void dispatchDraw(Canvas canvas) {
/*		mArcShape.resize(280*1.5f, 280*1.5f);
		mArcShape.draw(canvas,mPaint);*/
		super.dispatchDraw(canvas);		
	}
} 
