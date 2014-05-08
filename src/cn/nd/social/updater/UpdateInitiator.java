package cn.nd.social.updater;

import android.app.Notification;
import android.os.Build;
import android.widget.Toast;
import cn.nd.social.R;
import cn.nd.social.util.Utils;

public class UpdateInitiator {
	private final static String SERVER_URL = "http://121.207.254.44/777Server/";
	
	private final static String UPDATE_URL = SERVER_URL + "update.json";
	public final static String DOWNLOAD_URL = SERVER_URL + "NDSocial.apk";
	public final static String DOWNLOAD_URL_FOR_WECHAT = SERVER_URL + "ndsocial_down.html";
	
	public static enum UpdateType {
		AUTO_CHECK,
		USER_TRIGGER
	}
	
	public static void checkAutoUpdate(UpdateType type) {
		UpdateService.updateCheckType(type.ordinal());
		if(UpdateService.getUpdateStatus() > 0) {
			Toast.makeText(Utils.getAppContext(), R.string.new_version_checking, Toast.LENGTH_SHORT).show();
			return;
		}
	    UpdateRequest.Builder builder=new UpdateRequest.Builder(Utils.getAppContext());
	    builder.setVersionCheckStrategy(internalVersionCheckStrategy(type))
	           .setPreDownloadConfirmationStrategy(internalPreDownloadConfirmationStrategy())
	           .setDownloadStrategy(buildDownloadStrategy())
	           .setPreInstallConfirmationStrategy(internalPreInstallConfirmationStrategy())
	           .execute();
	}
	
	private static VersionCheckStrategy internalVersionCheckStrategy(UpdateType type) {
		return (new InternalVersionCheckStrategy(UPDATE_URL,type.ordinal()));
	}
	
	@SuppressWarnings("unused")
	private static VersionCheckStrategy buildVersionCheckStrategy() {
		return (new SimpleHttpVersionCheckStrategy(UPDATE_URL));
	}

	private static String getStr(int id) {
		return Utils.getAppContext().getString(id);
	}
	
	private static ConfirmationStrategy internalPreDownloadConfirmationStrategy() {
		return (new InternalConfirmationStrategy());
	}
	
	@SuppressWarnings({"deprecation" , "unused"})
	private static ConfirmationStrategy buildPreDownloadConfirmationStrategy() {
		// return(new ImmediateConfirmationStrategy());
		Notification n = new Notification(android.R.drawable.stat_notify_chat,
				getStr(R.string.new_version_notify), System.currentTimeMillis());

		n.setLatestEventInfo(Utils.getAppContext(), getStr(R.string.new_version_notify),
				getStr(R.string.new_version_hint), null);
		n.flags |= Notification.FLAG_AUTO_CANCEL;

		return (new NotificationConfirmationStrategy(n));
	}

	private static DownloadStrategy buildDownloadStrategy() {
		if (Build.VERSION.SDK_INT >= 11) {
			return (new InternalHttpDownloadStrategy());
		}

		return (new SimpleHttpDownloadStrategy());
	}

	
	private static ConfirmationStrategy internalPreInstallConfirmationStrategy() {
		return (new InternalConfirmationStrategy());
	}
	
	@SuppressWarnings({"deprecation","unused"})
	private static ConfirmationStrategy buildPreInstallConfirmationStrategy() {
		// return(new ImmediateConfirmationStrategy());

		Notification n = new Notification(android.R.drawable.stat_notify_chat,
				getStr(R.string.new_version_ready_install), System.currentTimeMillis());

		n.setLatestEventInfo(Utils.getAppContext(), getStr(R.string.new_version_ready_install),
				getStr(R.string.install_new_version_hint), null);
		n.flags |= Notification.FLAG_AUTO_CANCEL;

		return (new NotificationConfirmationStrategy(n));
	}

	
}
