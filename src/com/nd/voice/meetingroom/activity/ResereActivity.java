package com.nd.voice.meetingroom.activity;

import java.util.Calendar;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import cn.nd.social.R;

public class ResereActivity extends FragmentActivity implements ReserveRoomFramentListener,FriendChooseListener{
	
	ReserveRoomFrament reserveRoomFrament;
	FriendChooseFrament friendListFrament;
	
	FragmentManager fragmentManager;
	FragmentTransaction transaction;
	
	Fragment currentFragment;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reseremain);
		reserveRoomFrament = new ReserveRoomFrament();
		reserveRoomFrament.setListener(this);
		fragmentManager = getSupportFragmentManager();
		transaction = fragmentManager.beginTransaction();
		transaction.add(R.id.main_layout, reserveRoomFrament, "ReserveRoom");
		transaction.show(reserveRoomFrament);
		transaction.commit();  
		fragmentManager.executePendingTransactions();
		currentFragment = reserveRoomFrament;
	}
	

	@Override
	public void reserveRoomOnNextBtnClick(String title,Calendar date) {
		transaction = fragmentManager.beginTransaction();
		transaction.setCustomAnimations(
                R.anim.push_left_in,
                R.anim.push_left_out);
		if(friendListFrament == null){
			friendListFrament = new FriendChooseFrament();
			friendListFrament.setListener(this);
			transaction.add(R.id.main_layout, friendListFrament, "FriendChoose");
		}
		friendListFrament.setMeetingTitle(title);
		friendListFrament.setMeetingDate(date);
		transaction.hide(reserveRoomFrament);
		transaction.show(friendListFrament);
		transaction.commit();  
		currentFragment = friendListFrament; 
	}
	
	@Override
	public void friendChooseOnBackBtnClick() {
		// TODO Auto-generated method stub
		transaction = fragmentManager.beginTransaction();
		transaction.setCustomAnimations(
                R.anim.push_right_in,
                R.anim.push_right_out);
		transaction.hide(friendListFrament);
		transaction.show(reserveRoomFrament);
		transaction.commit();  
		currentFragment = reserveRoomFrament; 
	}
	
	
	@Override
	public void onBackPressed() {
		if(currentFragment == friendListFrament){
			friendChooseOnBackBtnClick();
			return;
		}
		super.onBackPressed();
	}
	
}
