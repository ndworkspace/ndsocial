package cn.nd.social.syncbrowsing.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import cn.nd.social.R;

public class DialogUtil {
	public static void  showExitDialog(final Activity activity) {
		new AlertDialog.Builder(activity).setTitle(R.string.hint).setMessage(R.string.exit_hint)
		.setNegativeButton(R.string.Cancel,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,
							int which) {
					}
				})
		.setPositiveButton(R.string.OK,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int whichButton) {
						activity.finish();
					}
				}).show();
	}
}
