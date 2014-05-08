package cn.nd.social.account.business;

import com.nd.voice.meetingroom.activity.RoomListFrame;
import com.nd.voice.meetingroom.manager.MeetingEntity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;

/**
 * BroadcastReceiver for Meeting event Notify
 */
public class MeetingNotifierReceiver extends BroadcastReceiver {
	Context mContext;

	public MeetingNotifierReceiver(Context context) {
		mContext = context;
	}

	public static IntentFilter getIntentFilter() {
		IntentFilter filter = new IntentFilter(
				MeetingEventNotify.ACTION_MEETING_INVITE_NOTIFY);
		filter.addAction(MeetingEventNotify.ACTION_MEETING_INVITE_RESULT_NOTIFY);
		filter.addAction(MeetingEventNotify.ACTION_MEETING_START_NOTIFY);
		filter.addAction(MeetingEventNotify.ACTION_MEETING_CANCEL_NOTIFY);
		filter.addAction(MeetingEventNotify.ACTION_MEETING_APPEND_MEMBER_NOTIFY);
		return filter;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals(MeetingEventNotify.ACTION_MEETING_INVITE_NOTIFY)) {
			String title = intent.getStringExtra("title");
			String time = intent.getStringExtra("time");
			String hostname = intent.getStringExtra("hostname");
			String roomid = intent.getStringExtra("roomid");
			String idlist = intent.getStringExtra("idlist");
			String hostid =  intent.getStringExtra("hostid");
			showInviteNotify(title, time, hostname,hostid, roomid,idlist);
		} else if (action
				.equals(MeetingEventNotify.ACTION_MEETING_INVITE_RESULT_NOTIFY)) {

			String title = intent.getStringExtra("title");
			String uid = intent.getStringExtra("uid");
			String roomid = intent.getStringExtra("roomid");
			String status = intent.getStringExtra("status");
			//showInviteResultNotify(title, uid, roomid, status);

		} else if (action
				.equals(MeetingEventNotify.ACTION_MEETING_START_NOTIFY)) {
			String title = intent.getStringExtra("title");
			showStartNotify(title);
		} else if (action
				.equals(MeetingEventNotify.ACTION_MEETING_CANCEL_NOTIFY)) {
			String title = intent.getStringExtra("title");
			String roomid = intent.getStringExtra("roomid");
			showMeetingCancelNotify(title, roomid);
		} else if (action
				.equals(MeetingEventNotify.ACTION_MEETING_APPEND_MEMBER_NOTIFY)) {
			String title = intent.getStringExtra("title");
			String roomid = intent.getStringExtra("roomid");
			String idlist = intent.getStringExtra("idlist");
			if(title == null) {
				title = "";
			}
			Toast.makeText(mContext, "有新成员加入会议:" + title, Toast.LENGTH_SHORT).show();
		}

	}

	private void showMeetingCancelNotify(String title, final String roomid) {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		if (title == null) {
			title = "";
		}
		builder.setTitle("会议提醒");

		String str;
		str = title + " 会议被取消";

		builder.setMessage(str);
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				BusinessMeetingManager.removeMeeting(roomid);
				mContext.sendBroadcast(new Intent(RoomListFrame.ACTION_MEETING_LIST_CHANGE));
			}
		});

		builder.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				BusinessMeetingManager.removeMeeting(roomid);
				mContext.sendBroadcast(new Intent(RoomListFrame.ACTION_MEETING_LIST_CHANGE));
			}

		});
		builder.create().show();
	}

	private void showInviteResultNotify(String title, String user,
			final String roomid, String status) {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		if (user == null) {
			user = "";
		}
		if (title == null) {
			title = "";
		}
		builder.setTitle("会议提醒:" + title);

		String str;

		if (status.equals("accept")) {
			str = user + "接受了会议邀请";			
		} else {
			str = user + "拒绝了您的会议邀请";
		}
		builder.setMessage(str);
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		builder.create().show();
	}

	private void showInviteNotify(final String title, final String time, final String hostname,
			final String hostid,final String roomid,final String idlist) {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle("会议提醒");
		String str = "你有一个会议邀请";
		str += "  邀请人" + hostname + "  时间:" + time + "   主题:" + title;
		builder.setMessage(str);
		builder.setPositiveButton("接受", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				BusinessMeetingManager meetingMgr = new BusinessMeetingManager();
				meetingMgr.accpetMeeting(roomid, "available");
				MeetingEntity entity = new MeetingEntity();
				entity.setTitle(title);
				entity.setMeetingid(roomid);
				entity.setReplyState(MeetingEntity.REPLYSTATE_ACCEPTED);
				entity.setMeetingTime(time);
				entity.setHostName(hostname);
				entity.setHostUserId(Long.parseLong(hostid));
				BusinessMeetingManager.addToLocalMeeting(entity, false);
				mContext.sendBroadcast(new Intent(RoomListFrame.ACTION_MEETING_LIST_CHANGE));
				
			}
		});

		builder.setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				new BusinessMeetingManager().refuseMeeting(roomid,
						"not avaiable");
			}
		});
		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				new BusinessMeetingManager().refuseMeeting(roomid,
						"not avaiable");
			}
		});
		builder.create().show();
	}

	private void showStartNotify(String title) {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle("会议提醒");
		String str;
		str = "会议  " + title + " 即将开始";
		builder.setMessage(str);
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();

			}
		});

		builder.create().show();
	}

}
