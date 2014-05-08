package com.nd.voice.chatroom;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.nd.voice.MultiTalk;
import com.nd.voice.VoiceEndpoint;
import com.nd.voice.VoiceEndpoint.ConferenceCallback;

import cn.nd.social.R;
import cn.nd.social.TabLauncherUI;
import cn.nd.social.util.LogToFile;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class EnterRoomActivity extends Activity implements ConferenceCallback{

	
	private Button mBackBtn;
	private TextView mTextInput;
	private ImageView mJoinBtn;
	private TextView mTextWait;
	private ViewGroup mWaitBar;
	
	private String rName;
	public static long[] usrHold = new long[4];
	public static int index = 0;
	public static EnterRoomActivity mEnterAct;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
		setContentView(R.layout.enter_room);
		mEnterAct = this;
		
		setupView();
		setupListener();
//		VoiceEndpoint.setUID(TabLauncherUI.VOICE_UID);
		super.onCreate(savedInstanceState);
	}
	
	private void setupView(){
		mBackBtn = (Button)findViewById(R.id.enter_room_reback_btn);
		mJoinBtn = (ImageView)findViewById(R.id.enter_room_join);
		mTextInput = (TextView)findViewById(R.id.enter_room_text);
		mTextWait = (TextView)findViewById(R.id.enter_room_wait);
		mWaitBar = (ViewGroup)findViewById(R.id.enter_room_wait_bar);
		mWaitBar.setVisibility(View.INVISIBLE);

	}
	
	private void setupListener(){
		mBackBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		mJoinBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String rName = mTextInput.getText().toString();
				if (rName.isEmpty()) {
					Toast.makeText(EnterRoomActivity.this, getResources().getString(R.string.warn_input_rname), Toast.LENGTH_SHORT).show();
					return;
				}				
				mWaitBar.setVisibility(View.VISIBLE);
				mTextWait.setText(R.string.entering);
				VoiceEndpoint.setUID(TabLauncherUI.VOICE_UID);
				VoiceEndpoint.join(getApplicationContext(), rName, EnterRoomActivity.this);            

				
			}
		});
	}
	

	@Override
	public void onJoinConference(boolean success) {
		// TODO Auto-generated method stub
		if (success) {
			mWaitBar.setVisibility(View.INVISIBLE);
			LogToFile.d("Join", "congratulation to join conference");
		}else{
			mTextWait.setText(R.string.enter_room_fail);
			LogToFile.e("Join", "We feel sorry for you not join conference");
		}
		
		 rName = mTextInput.getText().toString();
		
		//record room information
		SimpleDateFormat formatter = new SimpleDateFormat(
				"MM-dd HH:mm");
		Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
		String str = formatter.format(curDate);		
		ChatRoomInfo crInfo = new ChatRoomInfo(TabLauncherUI.VOICE_UID, rName, str);


		Intent intent = new Intent(EnterRoomActivity.this,MultiTalk.class);
		intent.putExtra("room_name", rName);
		startActivity(intent);
	}

	@Override
	public void onLeaveConference() {
		LogToFile.d("Leave", "User succeeded in leaving Room:"+ mTextInput.getText().toString());
		Toast.makeText(EnterRoomActivity.this, "You left "+ mTextInput.getText().toString(),
				Toast.LENGTH_SHORT).show();
		usrHold = new long[4];
		index = 0;
	}

	@Override
	public void onConferenceMemberEnter(long uid) {
		// TODO Auto-generated method stub
		LogToFile.d("Member_Enter", "User"+ uid + " succeeded in entering Room:"+ mTextInput.getText().toString());
		if (TabLauncherUI.VOICE_UID == uid) {			
			Toast.makeText(EnterRoomActivity.this, "coming me",
					Toast.LENGTH_SHORT).show();
		}else{
			Toast.makeText(EnterRoomActivity.this, "coming you",
					Toast.LENGTH_SHORT).show();
			if (MultiTalk.mMultiAct != null) {
				MultiTalk.mMultiAct.onConferenceMemberEnter(uid);
			}else{
				usrHold[index] = uid;
				index++;
			}			
		}

	}

	@Override
	public void onConferenceMemberLeave(long uid) {
		LogToFile.d("Member_Leave", "User"+ uid + " succeeded in leaving Room:"+ mTextInput.getText().toString());
		if (TabLauncherUI.VOICE_UID == uid) {			
			Toast.makeText(EnterRoomActivity.this, "leaving me",
					Toast.LENGTH_SHORT).show();			
		}else{
			Toast.makeText(EnterRoomActivity.this, "leaving you",
					Toast.LENGTH_SHORT).show();
			if (MultiTalk.mMultiAct != null) {
				MultiTalk.mMultiAct.onConferenceMemberLeave(uid);
			}
			
		}
		
	}

	@Override
	public void onConferenceMediaOption(int cmd, int value) {
		LogToFile.i("Media_Option", "Media option is "+ cmd + ", and value is "+ value);
	}

	@Override
	public void onConferenceAudioEnergy(long uid, int energy) {
		// TODO Auto-generated method stub
		LogToFile.i("Audio_Energy", "The sound of User"+ uid + " is "+ energy);	
			if (MultiTalk.mMultiAct != null) {
				MultiTalk.mMultiAct.onConferenceAudioEnergy(uid, energy);
			}
		
	}

	@Override
	public void onSetUid(long uid, boolean success) {
		if (success) {
			LogToFile.d("Set_UID", "User"+ uid + "succeeded in setting ID");
		}else{
			LogToFile.e("Set_UID", "User"+ uid + "failed in setting ID");
		}
						
	}

	@Override
	public void onDisconnect() {
		mWaitBar.setVisibility(View.VISIBLE);
		mTextWait.setText(R.string.enter_room_disconnect);
		LogToFile.e("Disconnect", "There are problems in connecting server, it's probably network connection problem.");		
	}

	@Override
	public void onDebug(String d) {
		LogToFile.d("Debug", d);		
	}
}
