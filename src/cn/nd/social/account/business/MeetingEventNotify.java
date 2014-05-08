package cn.nd.social.account.business;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import cn.nd.social.R;
import cn.nd.social.account.business.MeetingUtils.AppendMemberNotify;
import cn.nd.social.account.business.MeetingUtils.CancelMeetingNotify;
import cn.nd.social.account.business.MeetingUtils.InviteMeetingNotify;
import cn.nd.social.account.business.MeetingUtils.InviteResultNotify;
import cn.nd.social.account.business.MeetingUtils.MeetingRsp;
import cn.nd.social.account.business.MeetingUtils.StartMeetingNotify;
import cn.nd.social.util.Utils;

import com.nd.voice.meetingroom.manager.MeetingEntity;
import com.nd.voice.meetingroom.utils.NotificationUtils;

public class MeetingEventNotify {

	public static final String ACTION_MEETING_INVITE_NOTIFY = "cn.nd.social.meeting-invite-notity";
	public static final String ACTION_MEETING_INVITE_RESULT_NOTIFY = "cn.nd.social.meeting-invite-result-notity";
	public static final String ACTION_MEETING_CANCEL_NOTIFY = "cn.nd.social.meeting-cancel-notity";
	public static final String ACTION_MEETING_START_NOTIFY = "cn.nd.social.meeting-start-notity";
	public static final String ACTION_MEETING_APPEND_MEMBER_NOTIFY = "cn.nd.social.meeting-append-member-notity";

	public static void onNotify(MeetingRsp rsp) {
		if (rsp == null || rsp.action == null) {
			Log.e("ServerEventNotify", "server notify error,action null");
			return;
		}
		
		String action = rsp.action;
		if (action.equals(MeetingUtils.MEETING_INVITE_NOTIFY)) {
			InviteMeetingNotify notify = (InviteMeetingNotify) rsp;
			if (isTopActivity()) {
				Intent intent = new Intent(ACTION_MEETING_INVITE_NOTIFY);
				intent.putExtra("title", notify.title);
				intent.putExtra("time", notify.timeStr);
				intent.putExtra("hostname", notify.hostname);
				intent.putExtra("roomid", notify.roomid);
				intent.putExtra("idlist", notify.idlist);
				intent.putExtra("hostid", notify.hostuid);
				sendBroadcast(intent);
			} else {
				MeetingEntity meetingEntity = new MeetingEntity();
				meetingEntity.setMeetingid(notify.roomid);
				meetingEntity.setHostName(notify.hostname);
				meetingEntity.setTitle(notify.title);
				NotificationUtils.sendInviteNotification(Utils.getAppContext(),
						R.drawable.ic_launcher, meetingEntity);
			}
		} else if (action.equals(MeetingUtils.MEETING_START_NOTIFY)) {
			StartMeetingNotify notify = (StartMeetingNotify) rsp;
			Intent intent = new Intent(ACTION_MEETING_START_NOTIFY);
			intent.putExtra("title", notify.title);
			intent.putExtra("roomid", notify.roomid);
			sendBroadcast(intent);

		} else if (action.equals(MeetingUtils.MEETING_INVITE_RESULT_NOTIFY)) {
			InviteResultNotify notify = (InviteResultNotify) rsp;
			Intent intent = new Intent(ACTION_MEETING_INVITE_RESULT_NOTIFY);
			intent.putExtra("title", notify.title);
			intent.putExtra("roomid", notify.roomid);
			intent.putExtra("status", notify.ackType);
			intent.putExtra("uid", notify.hostid);
			sendBroadcast(intent);
		} else if (action.equals(MeetingUtils.MEETING_CANCEL_NOTIFY)) {
			CancelMeetingNotify notify = (CancelMeetingNotify) rsp;
			Intent intent = new Intent(ACTION_MEETING_CANCEL_NOTIFY);
			intent.putExtra("title", notify.title);
			intent.putExtra("roomid", notify.roomid);
			sendBroadcast(intent);
			
		} else if (action.equals(MeetingUtils.MEETING_APPEND_MEMBER_NOTIFY)) {
			AppendMemberNotify notify = (AppendMemberNotify) rsp;
			if (isTopActivity()) {
				Intent intent = new Intent(ACTION_MEETING_APPEND_MEMBER_NOTIFY);
				intent.putExtra("title", notify.title);
				intent.putExtra("roomid", notify.roomid);
				intent.putExtra("idlist", notify.idlist);

				sendBroadcast(intent);
			} else {
				MeetingEntity meetingEntity = new MeetingEntity();
				meetingEntity.setMeetingid(notify.roomid);
				meetingEntity.setTitle(notify.title);
				NotificationUtils.sendInviteNotification(Utils.getAppContext(),
						R.drawable.ic_launcher, meetingEntity);
			}
		}

	}

	private static void sendBroadcast(Intent intent) {
		Utils.getAppContext().sendBroadcast(intent);
	}

	private static boolean isTopActivity() {
		String packageName = "cn.nd.social";
		ActivityManager activityManager = (ActivityManager) Utils
				.getAppContext().getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> tasksInfo = activityManager.getRunningTasks(1);
		if (tasksInfo.size() > 0) {
			System.out.println("---------------包名-----------"
					+ tasksInfo.get(0).topActivity.getPackageName());
			// 应用程序位于堆栈的顶层
			if (packageName.equals(tasksInfo.get(0).topActivity
					.getPackageName())) {
				return true;
			}
		}
		return false;
	}
	
	
	private static boolean isActivityAtTop(Class<?> activityCls) {
		String packageName = Utils.getAppContext().getPackageName();
		ActivityManager activityManager = (ActivityManager) Utils
				.getAppContext().getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> tasksInfo = activityManager.getRunningTasks(1);
		if (tasksInfo.size() > 0) {
			// 应用程序位于堆栈的顶层
			//MeetingDetailActivity.class.
			if (packageName.equals(tasksInfo.get(0).topActivity.getPackageName())
				 && activityCls.getName().equals(tasksInfo.get(0).topActivity.getClassName())) {
				return true;
			}
		}
		return false;
	}

}
