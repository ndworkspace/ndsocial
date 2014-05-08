package cn.nd.social.ui.controls;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class FrameLayoutEx extends FrameLayout {

	private int mMesureWidth = 0;
	private int mMesureHeight = 0;

	public FrameLayoutEx(Context context) {
		super(context);
	}

	public FrameLayoutEx(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public FrameLayoutEx(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		mMesureWidth = MeasureSpec.getSize(widthMeasureSpec);
		mMesureHeight = MeasureSpec.getSize(heightMeasureSpec);
		if (mListener != null) {
			mListener.onDimenMesured();
			mListener = null;
		}
	}

	public int getWidthEx() {
		return mMesureWidth;
	}

	public int getHeightEx() {
		return mMesureHeight;
	}

	OnMeasureListener mListener;

	public void setOnMeasureListener(OnMeasureListener listener) {
		mListener = listener;
	}

	public interface OnMeasureListener {
		void onDimenMesured();
	}
}
