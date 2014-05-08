package cn.nd.social.prishare.component;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import cn.nd.dragdrop.DragController;
import cn.nd.dragdrop.DragSource;

public class CellItemView extends RelativeLayout implements DragSource {
	public CellItemView(Context context) {
		super(context);
	}

	public CellItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CellItemView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean allowDrag() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void setDragController(DragController dragger) {
		// TODO Auto-generated method stub

	}

	OnDragCompletedListener mListener;

	public void setDragCompletedListener(OnDragCompletedListener listener) {
		mListener = listener;
	}

	@Override
	public void onDropCompleted(View target, boolean success) {
		// TODO Auto-generated method stub
		if (mListener != null) {
			Boolean handled = mListener.onDropCompleted(this, target, success);
			if (handled) {
				return;
			}
		}
	}

}
