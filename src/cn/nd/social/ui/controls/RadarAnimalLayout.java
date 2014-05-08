package cn.nd.social.ui.controls;

import cn.nd.social.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class RadarAnimalLayout extends RelativeLayout {
	
	private View mRootView;
	private Context mContext;
	
	ImageView mRadarView;
	ImageView mRadarViewInner;
	AnimationSet mAnimSet;
	AnimationSet mAnimSetInner;
	
	private boolean animaAble;
	
	public void beginAnim(){
		animaAble = true;
		startAnim();
	}
	
	public void endAnim(){
		animaAble = false;
		stopAnim();
	}

	private void initAnim() {
		mRadarView = (ImageView) findViewById(R.id.radar_view);
		mRadarViewInner = (ImageView) findViewById(R.id.radar_view_inner);
		mAnimSet = getAnimSet();
		mAnimSetInner = getAnimSet();
	}

	private void startAnim() {
		mRadarView.startAnimation(mAnimSet);
		mAnimSetInner.setStartOffset(ANIMATION_PLAY_INTERVAL / 2);
		mRadarViewInner.startAnimation(mAnimSetInner);
		mAnimSet.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				if (animaAble) {
					mRadarView.startAnimation(mAnimSet);
				}

			}
		});
		mAnimSetInner.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				if (animaAble) {
					mAnimSetInner.setStartOffset(0);
					mRadarViewInner.startAnimation(mAnimSetInner);
				}

			}
		});
	}

	private void stopAnim() {
		mRadarView.setAnimation(null);
		mRadarViewInner.setAnimation(null);
	}

	private static final int ANIMATION_PLAY_INTERVAL = 1800;

	private AnimationSet getAnimSet() {
		AnimationSet animSet = new AnimationSet(true);
		ScaleAnimation scale = new ScaleAnimation(1f, 5f, 1, 5f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		scale.setDuration(ANIMATION_PLAY_INTERVAL);
		AlphaAnimation alpha = new AlphaAnimation(1, 0);
		alpha.setDuration(ANIMATION_PLAY_INTERVAL);
		alpha.setStartOffset(0);
		animSet.addAnimation(scale);
		animSet.addAnimation(alpha);
		/* animSet.setRepeatCount(Animation.INFINITE); */
		return animSet;
	}

	public RadarAnimalLayout(Context context) {
		super(context);
		mContext = context;
		initView();
		// TODO Auto-generated constructor stub
	}

	public RadarAnimalLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		initView();
		// TODO Auto-generated constructor stub
	}

	public RadarAnimalLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		initView();
		// TODO Auto-generated constructor stub
	}

	private void initView() {
		mRootView = LayoutInflater.from(mContext).inflate(R.layout.radarlayout, this);
		initAnim();
	}
}
