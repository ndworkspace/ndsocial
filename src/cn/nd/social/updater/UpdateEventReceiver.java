package cn.nd.social.updater;

import cn.nd.social.R;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * BroadcastReceiver for Meeting event Notify
 */
public class UpdateEventReceiver extends BroadcastReceiver {
	Context mContext;

	public UpdateEventReceiver(Context context) {
		mContext = context;
	}

	public static IntentFilter getIntentFilter() {
		IntentFilter filter = new IntentFilter(UpdateService.ACTION_CHECK_UPDATE);
		return filter;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals(UpdateService.ACTION_CHECK_UPDATE)) {
			int result = intent.getIntExtra(
					UpdateService.KEY_CHECK_UPDATE_RESULT,
					UpdateService.UPDATE_CHECK_UNKOWN);

			boolean confirmUpdate = (result == UpdateService.UPDATE_CHECK_UPDATING);
			boolean confirmInstall = (result == UpdateService.UPDATE_CHECK_INSTALL);
			if (confirmUpdate || confirmInstall) {
				int textId;
				if (confirmUpdate) {
					textId = R.string.update_new_version_msg;
				} else {
					textId = R.string.install_new_version_msg;
				}
				String releaseNote = intent
						.getStringExtra(UpdateService.KEY_RELEASE_NOTE);
				PendingIntent pendingIntent = intent
						.getParcelableExtra(UpdateService.KEY_UPDATE_INTENT);
				showUpdateConfirm(confirmUpdate, textId, releaseNote,
						pendingIntent);
			} else {
				String currVer = mContext.getString(R.string.version_code);
				try {
					currVer += mContext.getPackageManager().getPackageInfo(
							mContext.getPackageName(), 0).versionName;
				} catch (NameNotFoundException e) {
					e.printStackTrace();
				}

				if (result == UpdateService.UPDATE_CHECK_FAIL) {
					String message = mContext
							.getString(R.string.update_network_error)
							+ "\n"
							+ currVer;

					showUpdateCheckResult(message);
				} else if (result == UpdateService.UPDATE_CHECK_LATEST) {
					String message = mContext
							.getString(R.string.update_lattest_version_msg)
							+ "\n" + currVer;
					showUpdateCheckResult(message);
				} else {
					showUpdateCheckResult("unknow error" + "\n" + currVer);
				}
			}
		}
	}

	private void showUpdateConfirm(boolean isDownload, int textId,
			String releaseNote, final PendingIntent pendingIntent) {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle(R.string.hint);
		String text = (String) mContext.getText(textId);
		if (releaseNote != null) {
			text = text + "\n" + releaseNote;
		}
		builder.setMessage(text);
		final int positiveStr;
		if (isDownload) {
			positiveStr = R.string.start_download;
		} else {
			positiveStr = R.string.start_install;
		}
		builder.setPositiveButton(mContext.getString(positiveStr),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						try {
							pendingIntent.send();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});

		builder.setNegativeButton(mContext.getString(R.string.cancel),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						UpdateService.clearUpdateStatus();
					}
				});
		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				UpdateService.clearUpdateStatus();
			}
		});
		builder.create().show();
	}

	private void showUpdateCheckResult(String text) {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle(R.string.update_hint);
		builder.setMessage(text);
		builder.setPositiveButton(mContext.getString(R.string.ok),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.create().show();
	}

}
