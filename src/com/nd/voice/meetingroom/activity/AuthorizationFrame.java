package com.nd.voice.meetingroom.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import cn.nd.social.R;
import cn.nd.social.TabFramentChangeListener;
import cn.nd.social.account.activity.LoginFrameLayout;
import cn.nd.social.account.activity.LoginFrameLayoutListener;
import cn.nd.social.account.activity.RegiterFrameLayout;
import cn.nd.social.account.activity.RegiterFrameLayoutListener;
import cn.nd.social.account.usermanager.UserManager;

import com.nd.voice.meetingroom.manager.UserManagerApi;

public abstract class  AuthorizationFrame extends Fragment implements
		LoginFrameLayoutListener, RegiterFrameLayoutListener,TabFramentChangeListener {

	protected UserManagerApi mUserManager;

	protected LoginFrameLayout loginFrameLayout;

	protected RegiterFrameLayout regiterFrameLayout;

	protected Activity mActivity;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mActivity = this.getActivity();
		mUserManager = new UserManager();
		loginFrameLayout = new LoginFrameLayout(mActivity);// (LoginFrameLayout)rootView.findViewById(R.id.loginFrameLayout);
		loginFrameLayout.setListener(this);
		loginFrameLayout.setVisibility(View.GONE);
		regiterFrameLayout = new RegiterFrameLayout(mActivity);
		regiterFrameLayout.setVisibility(View.GONE);
		regiterFrameLayout.setListener(this);
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	@Override
	public void onResume() {
		if(mUserManager.hasValidUser()){
			loginFrameLayout.setVisibility(View.GONE);
			regiterFrameLayout.setVisibility(View.GONE);
			getRootFrameLayout().removeView(loginFrameLayout);
			getRootFrameLayout().removeView(regiterFrameLayout);
		}
		super.onResume();
	}

	protected void showLoginFrame() {
		// in.setDuration(500);
		// mMenu.setVisibility(View.VISIBLE);
		if(loginFrameLayout.getParent() == null){
			getRootFrameLayout().addView(loginFrameLayout);
		}
			final Animation in = AnimationUtils.loadAnimation(mActivity,
					R.anim.push_bottom_in);
			in.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation arg0) {
					// TODO Auto-generated method stub
//					loginFrameLayout.setEnabled(false);
				}

				@Override
				public void onAnimationRepeat(Animation arg0) {
					// TODO Auto-generated method stub
				}

				@Override
				public void onAnimationEnd(Animation arg0) {
				}
			});
			loginFrameLayout.setVisibility(View.VISIBLE);
			loginFrameLayout.startAnimation(in);
	}

	protected void showRegiterFrame() {
		// in.setDuration(500);
		// mMenu.setVisibility(View.VISIBLE);
		if(regiterFrameLayout.getParent() == null){
			getRootFrameLayout().addView(regiterFrameLayout);
		}
		
			final Animation in = AnimationUtils.loadAnimation(mActivity,
					R.anim.push_bottom_in);
			in.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation arg0) {
					// TODO Auto-generated method stub
//					regiterFrameLayout.setEnabled(false);
				}

				@Override
				public void onAnimationRepeat(Animation arg0) {
					// TODO Auto-generated method stub
				}

				@Override
				public void onAnimationEnd(Animation arg0) {
				}
			});
			regiterFrameLayout.setVisibility(View.VISIBLE);
			regiterFrameLayout.startAnimation(in);
		
	}

	/**
	 * Hides view
	 */
	protected void hideLoginFrame() {
		// out.setDuration(500);
			final Animation out = AnimationUtils.loadAnimation(mActivity,
					R.anim.push_bottom_out);
			out.setFillAfter(true);
			loginFrameLayout.setVisibility(View.GONE);
			out.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation arg0) {
					// TODO Auto-generated method stub
//					loginFrameLayout.setEnabled(false);
				}

				@Override
				public void onAnimationRepeat(Animation arg0) {
					// TODO Auto-generated method stub
				}

				@Override
				public void onAnimationEnd(Animation arg0) {
//					loginFrameLayout.setEnabled(true);
					// tangtaotao_20140409 use slide bar
					getRootFrameLayout().removeView(loginFrameLayout);
				}
			});
			loginFrameLayout.startAnimation(out);
		
	}
	
	protected abstract FrameLayout getRootFrameLayout();
	
	protected abstract void loadData();

	/**
	 * Hides view
	 */
	protected void hideRegiterFrame() {
		// out.setDuration(500);
			final Animation out = AnimationUtils.loadAnimation(mActivity,
					R.anim.push_bottom_out);
			out.setFillAfter(true);
			regiterFrameLayout.setVisibility(View.GONE);
			out.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation arg0) {
					// TODO Auto-generated method stub
//					regiterFrameLayout.setEnabled(false);
				}

				@Override
				public void onAnimationRepeat(Animation arg0) {
					// TODO Auto-generated method stub
				}

				@Override
				public void onAnimationEnd(Animation arg0) {
//					regiterFrameLayout.setEnabled(true);
					// tangtaotao_20140409 use slide bar
					getRootFrameLayout().removeView(regiterFrameLayout);
				}
			});
			regiterFrameLayout.startAnimation(out);
		
	}

	@Override
	public void onLoginSuccess() {
		this.hideLoginFrame();
		loadData();
	}

	@Override
	public void onRegiterSuccess() {
		this.hideRegiterFrame();
		loadData();
	}
	
	@Override
	public void onFramentViewHide() {
		loginFrameLayout.setVisibility(View.GONE);
		regiterFrameLayout.setVisibility(View.GONE);
		getRootFrameLayout().removeView(loginFrameLayout);
		getRootFrameLayout().removeView(regiterFrameLayout);
	}

	@Override
	public void onFramentViewShow() {
		if(mUserManager.isFirstUse() ) {
			this.showRegiterFrame();
			return;
		}else if(!mUserManager.hasValidUser()){
			this.showLoginFrame();
			return;
		}
		loadData();
	}

}
