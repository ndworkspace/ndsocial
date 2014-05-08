package cn.nd.social.common;

import android.content.Context;
import android.os.Vibrator;

public final class VibratorController {
	private static VibratorController sController;
	private Vibrator mVibrator;

	private VibratorController(Context context) {
		mVibrator = ((Vibrator) context.getSystemService("vibrator"));
	}

	synchronized public static VibratorController getController(Context context) {
		if (sController == null)
			sController = new VibratorController(context);
		return sController;
	}

	public void vibrate() {
		mVibrator.vibrate(35L);
	}

}