/***
Copyright (c) 2012 CommonsWare, LLC

Licensed under the Apache License, Version 2.0 (the "License"); you may
not use this file except in compliance with the License. You may obtain
a copy of the License at
  http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package cn.nd.social.updater;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.RemoteViews;
import cn.nd.social.R;
import cn.nd.social.util.Utils;

public class SimpleHttpDownloadStrategy implements DownloadStrategy {

	private Notification mDownNotification;
	private RemoteViews mContentView;
	private NotificationManager mNotifManager;

	@Override
	public Uri downloadAPK(Context ctxt, String url) throws Exception {
		File apk = getDownloadFile(ctxt);

		if (apk.exists()) {
			apk.delete();
		}

		HttpURLConnection conn = (HttpURLConnection) new URL(url)
				.openConnection();

		try {
			//tangtaotao@NetDragon_20140213
			int fileSize = -1;
			int downloadSize = 0;
			int progress = -1;
			int tempProgress = -1;

			conn.connect();
			int status = conn.getResponseCode();

			if (status == HttpURLConnection.HTTP_OK) {
				//tangtaotao@NetDragon_20140213
				showDownloadProgress();
				fileSize = conn.getContentLength();
				
				InputStream is = conn.getInputStream();
				OutputStream f = openDownloadFile(ctxt, apk);
				byte[] buffer = new byte[4096];
				int len1 = 0;

				
				while ((len1 = is.read(buffer)) > 0) {
					f.write(buffer, 0, len1);
					
					//tangtaotao@NetDragon_20140213
					downloadSize = downloadSize + len1;
					progress = (int) (downloadSize * 100.0 / fileSize);			
					if (tempProgress != progress) {
						updateDownloadProgress(progress);
						tempProgress = progress;
					}
				}
				
				f.close();
				is.close();

				//tangtaotao@NetDragon_20140213
				mNotifManager.cancel(R.id.iv_download);
				//downloadSuccess();
				
			} else {
				throw new RuntimeException(String.format(
						"Received %d from server", status));
			}
		} finally {
			conn.disconnect();
		}

		return (getDownloadUri(ctxt, apk));
	}

	@SuppressWarnings("deprecation")
	private void showDownloadProgress() {
		Context ctx = Utils.getAppContext();
		mNotifManager = (NotificationManager) ctx
				.getSystemService(Context.NOTIFICATION_SERVICE);
		mDownNotification = new Notification(
				android.R.drawable.stat_sys_download,
				ctx.getString(R.string.download_progress),
				System.currentTimeMillis());
		mDownNotification.flags = Notification.FLAG_ONGOING_EVENT;
		mDownNotification.flags = Notification.FLAG_AUTO_CANCEL;

		mContentView = new RemoteViews(ctx.getPackageName(),
				R.layout.download_notification);
		mContentView.setImageViewResource(R.id.iv_download,
				android.R.drawable.stat_sys_download);

	}
	
	private void updateDownloadProgress(int progress) {
		mContentView.setTextViewText(R.id.progress_percent,
				progress + "%");
		mContentView.setProgressBar(R.id.download_progress,
				100, progress, false);
		mDownNotification.contentView = mContentView;
		mNotifManager.notify(R.id.iv_download,
				mDownNotification);
	}

	@SuppressWarnings("deprecation")
	private void downloadSuccess() {
		Context ctx = Utils.getAppContext();
		NotificationManager ntMgr = (NotificationManager) ctx
				.getSystemService(Context.NOTIFICATION_SERVICE);

		Notification notification = new Notification(
				android.R.drawable.stat_sys_download_done,
				ctx.getString(R.string.download_success),
				System.currentTimeMillis());
		notification.flags = Notification.FLAG_ONGOING_EVENT;
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		notification.setLatestEventInfo(ctx,
				ctx.getString(R.string.download_success), null, null);
		ntMgr.notify(R.drawable.icon, notification);
	}

	@Override
	public int describeContents() {
		return (0);
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// no-op
	}

	protected File getDownloadFile(Context ctxt) {
		File updateDir = new File(ctxt.getExternalFilesDir(null),
				".ndsocial-Update");

		updateDir.mkdirs();

		return (new File(updateDir, "update.apk"));
	}

	protected OutputStream openDownloadFile(Context ctxt, File apk)
			throws FileNotFoundException {
		return (new FileOutputStream(apk));
	}

	protected Uri getDownloadUri(Context ctxt, File apk) {
		return (Uri.fromFile(apk));
	}

	public static final Parcelable.Creator<SimpleHttpDownloadStrategy> CREATOR = new Parcelable.Creator<SimpleHttpDownloadStrategy>() {
		public SimpleHttpDownloadStrategy createFromParcel(Parcel in) {
			return (new SimpleHttpDownloadStrategy());
		}

		public SimpleHttpDownloadStrategy[] newArray(int size) {
			return (new SimpleHttpDownloadStrategy[size]);
		}
	};
}
