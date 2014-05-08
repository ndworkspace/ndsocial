package com.nd.voice.meetingroom.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.nd.voice.meetingroom.activity.MeetingDetailActivity;
import com.nd.voice.meetingroom.activity.MeetingInviteActivity;
import com.nd.voice.meetingroom.manager.MeetingEntity;

public class NotificationUtils {
	
	/**
     * 发送本地推送
     * @param context
     * @param title1     标题
     * @param content   内容
     * @param drawable  图标资源
     */
    public static void sendInviteNotification(Context context,int drawable,MeetingEntity meetingEntity){
    	NotificationManager nm=(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    	//1.实例化一个通知，指定图标、概要、时间
        Notification n = new Notification(drawable,"通知",System.currentTimeMillis());
        //2.指定通知的标题、内容和intent
        Intent intent = new Intent(context, MeetingInviteActivity.class);
        intent.putExtra("meetingId", meetingEntity.getMeetingid());
        intent.putExtra("meetingTitle", meetingEntity.getTitle());
        PendingIntent pi= PendingIntent.getActivity(context, 0, intent, 0);
        n.setLatestEventInfo(context,  "收到一个会议邀请", meetingEntity.getHostName() + "邀请您参加会议：" + meetingEntity.getTitle(), pi);
        //3.指定声音 
        n.defaults |= Notification.DEFAULT_SOUND;
        //指定震动
        n.defaults |= Notification.DEFAULT_VIBRATE;
        //4.发送通知
        n.flags = Notification.FLAG_AUTO_CANCEL; // 但用户点击消息后，消息自动在通知栏自动消失
        n.flags |= Notification.FLAG_NO_CLEAR;// 点击通知栏的删除，消息不会依然不会被删除
        nm.notify(NotificationUtils.getMeetingNotificationId(meetingEntity), n);
    }
    
    public static void cancelInviteNotification(Context context,MeetingEntity meetingEntity){
    	int meetingNotificationId = NotificationUtils.getMeetingNotificationId(meetingEntity);
    	NotificationManager nm=(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    	nm.cancel(meetingNotificationId);
    }
    
    private static int getMeetingNotificationId(MeetingEntity meetingEntity){
    	return Integer.parseInt(meetingEntity.getMeetingid());
    }
    
    /**
     * 发送本地推送
     * @param context
     * @param title1     标题
     * @param content   内容
     * @param drawable  图标资源
     */
    public static void sendMeetingNotification(Context context,String title,String content,int drawable,String meetingId){
    	NotificationManager nm=(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    	//1.实例化一个通知，指定图标、概要、时间
        Notification n = new Notification(drawable,"通知",System.currentTimeMillis());
        //2.指定通知的标题、内容和intent
        Intent intent = new Intent(context, MeetingDetailActivity.class);
        intent.putExtra("meetingId", meetingId);
        PendingIntent pi= PendingIntent.getActivity(context, 0, intent, 0);
        n.setLatestEventInfo(context, title, content, pi);
        //3.指定声音
        n.defaults |= Notification.DEFAULT_SOUND;
        //指定震动
        n.defaults |= Notification.DEFAULT_VIBRATE;
        //4.发送通知
        n.flags = Notification.FLAG_AUTO_CANCEL; // 但用户点击消息后，消息自动在通知栏自动消失
        n.flags |= Notification.FLAG_NO_CLEAR;// 点击通知栏的删除，消息不会依然不会被删除
        nm.notify(0, n);
    }
}
