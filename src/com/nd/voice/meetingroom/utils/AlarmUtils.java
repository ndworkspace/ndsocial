package com.nd.voice.meetingroom.utils;

import java.util.Calendar;
import java.util.Date;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import cn.nd.social.util.DateStringUtils;

import com.nd.voice.meetingroom.manager.MeetingEntity;

public class AlarmUtils {
	
	public static void addAlarm(Context context,MeetingEntity meetingEntity){
		 // 进行闹铃注册
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("meetingId", meetingEntity.getMeetingid());
        intent.putExtra("title", meetingEntity.getTitle());
        intent.putExtra("hostname", meetingEntity.getHostName());
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        // 过10s 执行这个闹铃
        String meetingTime = meetingEntity.getMeetingTime();
        Date date = DateStringUtils.strToDate(meetingTime,null);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, 5);//提前5分钟提醒

        AlarmManager manager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        manager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
	}
	
}

