package cn.nd.social.syncbrowsing.manager;

import java.io.ObjectOutputStream.PutField;

import cn.nd.social.syncbrowsing.meeting.activity.ClientPageActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * BroadcastReceiver for Meeting event Notify
 */
public class MeetingSyncEnterReceiver extends BroadcastReceiver {
	
	Context mContext;
	String mMeetingId;
	boolean mHasEnterSync = false;	
	

	public MeetingSyncEnterReceiver(Context context,String meetingId) {
		mContext = context;
		mMeetingId = meetingId;
	}

	public static IntentFilter getIntentFilter() {
		IntentFilter filter = new IntentFilter(SyncMsgFactory.ACTION_RECV_SYNC_ENTER);
		return filter;
	}
	
	public void onExitSync() {
		mHasEnterSync = false;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals(SyncMsgFactory.ACTION_RECV_SYNC_ENTER)) {
			String meetingId = intent.getStringExtra("meetingid");
			if(mMeetingId != null && mMeetingId.equals(meetingId)) {
/*				if(!mHasEnterSync) {
					mHasEnterSync = true;*/
					boolean hasExtra = intent.getBooleanExtra("hasextra",false);
					
					Intent syncIntent = new Intent(mContext,ClientPageActivity.class);
					if(hasExtra) {
						syncIntent.putExtra("hasextra", hasExtra);
						syncIntent.putExtras(intent.getExtras());
					}
					mContext.startActivity(syncIntent);					
/*				}*/
			}
		}
	}

}
