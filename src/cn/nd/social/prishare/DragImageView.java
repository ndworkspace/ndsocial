package cn.nd.social.prishare;

import cn.nd.dragdrop.DragController;
import cn.nd.dragdrop.DragSource;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

public class DragImageView extends ImageView implements DragSource {
	public DragImageView(Context context) {
		super(context);
	}
	
	public DragImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public DragImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean allowDrag() {
		return true;
	}
	
	@Override
	public void setDragController(DragController dragger) {
		
	}
	
	private OnDragCompletedListener mListener;
	public void setDragCompletedListener(OnDragCompletedListener listener) {
		mListener = listener;
	}
	
	@Override
	public void onDropCompleted(View target, boolean success) {
		// TODO Auto-generated method stub
		if(mListener != null) {
			Boolean handled = mListener.onDropCompleted(this,target, success);
			if(handled) {
				return;
			}
		}
	}
}
