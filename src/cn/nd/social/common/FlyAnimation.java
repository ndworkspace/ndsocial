package cn.nd.social.common;

import cn.nd.social.util.CommonUtils;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;


/**
 * flying animation from srcView to destView;
 *  using srcView's viewBitmap
 * */
public class FlyAnimation {
	private ImageView flyView;
	private ViewGroup refView;
	private boolean useInternalView = false;
	
	/**
	 * create a new flyView
	 * 
	 * */
	public FlyAnimation(Context context) {
		this.flyView = new ImageView(context);		
	}
	
	/**
	 * using internal View which alreay exist and is a child of the refView
	 * 
	 * */
	public FlyAnimation(ImageView fly) {
		useInternalView = true;
		this.flyView = fly;		
	}
	
	/**
	 * srcView: the view which need to be animated
	 * destView: destination view which srcView will fly to
	 * refViewLayer:the flyview attach to the refView which define the coordination base
	 * */
	public void startFly(View srcView,View destView,ViewGroup refViewLayer) {

		refView = refViewLayer;
		int[] srcPos = new int[2];
		int[] destPos = new int[2];
		int[] refPos = new int[2];
		
		if(!useInternalView) {
			Bitmap bmp = CommonUtils.getViewBitmap(srcView);
			if (bmp == null) {
				return;
			}
			flyView.setImageBitmap(bmp);
			refView.addView(flyView);
		}
		
		refView.getLocationOnScreen(refPos);
		
		srcView.getLocationOnScreen(srcPos);
		destView.getLocationOnScreen(destPos);
		
		
		
		//obsolete method
/*		ViewGroup.MarginLayoutParams x = (ViewGroup.MarginLayoutParams) flyView
				.getLayoutParams();
		x.leftMargin = srcPos[0] - refPos[0];
		x.topMargin = srcPos[1] - refPos[1];
		flyView.setLayoutParams(x);*/

		

		
		AnimationSet animateSet = new AnimationSet(true);
		animateSet.setDuration(800);
		
		int fromX = srcPos[0] - refPos[0];
		int fromY = srcPos[1] - refPos[1];

		int toX = destPos[0] + (destView.getWidth() / 2) - refPos[0];
		int toY = destPos[1] + (destView.getHeight() / 2) - refPos[1];



		TranslateAnimation flyToCollect = new TranslateAnimation(fromX, toX, fromY, toY);
		ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 0.0f, 1.0f,
				0.0f, Animation.ABSOLUTE, toX, Animation.ABSOLUTE, toY);
		animateSet.addAnimation(flyToCollect);
		animateSet.addAnimation(scaleAnimation);
		setAnimListener(animateSet);
		if(useInternalView) {
			flyView.setVisibility(View.VISIBLE);
		}
		flyView.startAnimation(animateSet);		
		
		//return animateSet;
	}

	private void setAnimListener(AnimationSet animateSet) {
		animateSet.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationEnd(Animation arg0) {
				if(useInternalView) {
					flyView.setVisibility(View.GONE);
				} else {
					//directly use refView.removeView(flyView); will cause RuntimeException
					//because ViewGroup.dispatchDraw will get null to draw;
					//onAnimationEnd may be called in ViewGroup.dispatchDraw ? 
					refView.post(new Runnable() {						
						@Override
						public void run() {
							refView.removeView(flyView);							
						}
					});
					
				}
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationStart(Animation animation) {

			}
		});

	}
}
