package com.nd.voice.meetingroom.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import cn.nd.social.R;

/**
 * Created by Administrator on 14-4-16.
 */
public class AlarmReceiver extends BroadcastReceiver {
	
    @Override
    public void onReceive(Context context, Intent intent) {
    	String meetingId = intent.getStringExtra("meetingId");
    	String title  = intent.getStringExtra("title");
        NotificationUtils.sendMeetingNotification(context,"会议提醒","会议:" + title + "即将于五分钟后开始", R.drawable.icon,meetingId);
    }

    
}