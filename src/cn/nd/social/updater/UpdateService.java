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

import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import cn.nd.social.wakeful.WakefulIntentService;

public class UpdateService extends WakefulIntentService {
	public UpdateService() {
		super("UpdateService");
	}

	public static final String ACTION_CHECK_UPDATE = "cn.nd.social.update.act";
	public static final String KEY_CHECK_UPDATE_RESULT = "check_update_result";
	public static final String KEY_RELEASE_NOTE = "release_note";
	public static final String KEY_UPDATE_INTENT = "update_intent";
	

	public static final int UPDATE_CHECK_LATEST = 0; // already the lastest version
	public static final int UPDATE_CHECK_UPDATING = 1;
	public static final int UPDATE_CHECK_INSTALL = 2;
	public static final int UPDATE_CHECK_FAIL = 3; // might be server error or network error
	public static final int UPDATE_CHECK_UNKOWN = -1;
	
	private static int sUpdateStatus = 0;
	private static int sCheckType = 0;

	@Override
	protected void doWakefulWork(Intent cmd) {
		
		UpdateRequest req = new UpdateRequest(cmd);
		VersionCheckStrategy vcs = req.getVersionCheckStrategy();
		int checkType = vcs.getCheckType();
		
		sUpdateStatus = req.getPhase();
		try {
			if (req.getPhase() == UpdateRequest.PHASE_DOWNLOAD) {
				downloadAndInstall(cmd, req, req.getUpdateURL());
			} else if (req.getPhase() == UpdateRequest.PHASE_INSTALL) {
				install(req, req.getInstallUri());
				sUpdateStatus = 0;
			} else {
				int updateVersionCode = vcs.getVersionCode();
				int currentVersionCode = getPackageManager().getPackageInfo(
						getPackageName(), 0).versionCode;

				if (updateVersionCode > currentVersionCode) {
					sendUpdateMsg(vcs, UPDATE_CHECK_UPDATING,cmd);
					ConfirmationStrategy strategy = req
							.getPreDownloadConfirmationStrategy();

					if (strategy == null 
							|| strategy.confirm(
											this,
											buildDownloadPhase(cmd,
													vcs.getUpdateURL()))) {
						downloadAndInstall(cmd, req, vcs.getUpdateURL());
					}
				} else if (updateVersionCode < currentVersionCode) {
					// TODO: file an error
					sUpdateStatus = 0;
					sendUpdateCheckMsg(checkType, UPDATE_CHECK_LATEST);
				} else {
					sUpdateStatus = 0;
					sendUpdateCheckMsg(checkType, UPDATE_CHECK_LATEST);
				}
			}
		} catch (Exception e) {
			sUpdateStatus = 0;
			sendUpdateCheckMsg(checkType, UPDATE_CHECK_FAIL);
			Log.e("CWAC-Update", "Exception in applying update",e);
			// Log.e("CWAC-Update", "Exception in applying update", e);
		}
	}

	public final static void clearUpdateStatus() {
		sUpdateStatus = 0;
	}
	
	public final static int getUpdateStatus() {
		return sUpdateStatus;
	}
	
	public final static void updateCheckType(int type) {
		sCheckType = type;
	}
	
	private void sendUpdateCheckMsg(int checkType, int updateType) {
		final int USER_CHECK_TYPE = UpdateInitiator.UpdateType.USER_TRIGGER.ordinal();
		if(checkType == USER_CHECK_TYPE || sCheckType == USER_CHECK_TYPE) {
			Intent intent = new Intent(ACTION_CHECK_UPDATE);
			intent.putExtra(KEY_CHECK_UPDATE_RESULT, updateType);
			sendBroadcast(intent);
		}
	}
	
	private void sendUpdateMsg(VersionCheckStrategy vcs, int updateType,Intent cmd) {
		Intent intent = new Intent(ACTION_CHECK_UPDATE);
		intent.putExtra(KEY_CHECK_UPDATE_RESULT, updateType);
		String releaseNote = vcs.getReleaseNote();
		if (releaseNote != null) {
			intent.putExtra(KEY_RELEASE_NOTE, releaseNote);
		}
		try {
			intent.putExtra(KEY_UPDATE_INTENT,
					buildDownloadPhase(cmd, vcs.getUpdateURL()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		sendBroadcast(intent);
	}
	
	private void sendInstallMsg(int updateType,PendingIntent pendingIntent) {
		Intent intent = new Intent(ACTION_CHECK_UPDATE);
		intent.putExtra(KEY_CHECK_UPDATE_RESULT, updateType);
		intent.putExtra(KEY_UPDATE_INTENT,pendingIntent);
		sendBroadcast(intent);
	}

	private void downloadAndInstall(Intent cmd, UpdateRequest req,
			String updateURL) throws Exception {
		
		
		
		DownloadStrategy ds = req.getDownloadStrategy();
		Uri apk = ds.downloadAPK(this, updateURL);

		if (apk != null) {
			confirmAndInstall(cmd, req, apk);
		}
	}

	private void confirmAndInstall(Intent cmd, UpdateRequest req, Uri apk)
			throws Exception {
		ConfirmationStrategy strategy = req.getPreInstallConfirmationStrategy();		
		if (strategy == null
				|| strategy.confirm(this, buildInstallPhase(cmd, apk))) {
			install(req, apk);
		}
		
		//tangtaotao@NetDragon_20140213
		sendInstallMsg(UPDATE_CHECK_INSTALL,buildInstallPhase(cmd, apk));
	}

	private void install(UpdateRequest req, Uri apk) {
		Intent i;

		/*
		 * if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
		 * i=new Intent(Intent.ACTION_INSTALL_PACKAGE);
		 * i.putExtra(Intent.EXTRA_ALLOW_REPLACE, true); } else { i=new
		 * Intent(Intent.ACTION_VIEW); }
		 */
		i = new Intent(Intent.ACTION_VIEW);
		i.setDataAndType(apk, "application/vnd.android.package-archive");
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		startActivity(i);
	}

	private PendingIntent buildDownloadPhase(Intent cmd, String updateURL) {
		UpdateRequest.Builder builder = new UpdateRequest.Builder(this, cmd);

		builder.setPhase(UpdateRequest.PHASE_DOWNLOAD);
		builder.setUpdateURL(updateURL);

		return (builder.buildPendingIntent());
	}

	private PendingIntent buildInstallPhase(Intent cmd, Uri apk) {
		UpdateRequest.Builder builder = new UpdateRequest.Builder(this, cmd);

		builder.setPhase(UpdateRequest.PHASE_INSTALL);
		builder.setInstallUri(apk);

		return (builder.buildPendingIntent());
	}
}
