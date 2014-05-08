package com.nd.voice;

import java.lang.ref.WeakReference;
import java.util.Random;

import cn.nd.social.prishare.component.MainHandler.MainMsgHandlerInterface;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;

public class UtilsForMultiTalk {

	public final static int JOIN_ROOM = 0;
	public final static int MIC_CHECKED = 1;
	private static WeakReference<MultiTalkHandler> sWkptrMultiTalk = null;

	public static MultiTalkHandler getMultiTalkHandler() {
		if (sWkptrMultiTalk != null) {
			return sWkptrMultiTalk.get();
		}
		return null;
	}

	public static void sendMessageToMulti(Message msg) {
		if (getMultiTalkHandler() != null) {
			getMultiTalkHandler().sendMessage(msg);
		}
	}

	public static class MultiTalkHandler extends Handler {

		public MultiTalkHandler() {
			sWkptrMultiTalk = new WeakReference<UtilsForMultiTalk.MultiTalkHandler>(
					this);
		}

		private MultiTalkListener mListener;

		public void setDisposer(MultiTalkListener l) {
			mListener = l;
		}

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if (msg.what == JOIN_ROOM) {
				mListener.onSetRoom(msg);
			}else if (msg.what == MIC_CHECKED) {
				mListener.onMicChecked(msg);
			}
			super.handleMessage(msg);
		}

	}

	public interface MultiTalkListener {
		void onSetRoom(Message msg);
		void onMicChecked(Message msg);
	}

	public static Bitmap toRoundBitmap(Bitmap bitmap, boolean flag) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		float roundPx;
		float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;
		if (width <= height) {
			roundPx = width / 2;
			top = 0;
			bottom = width;
			left = 0;
			right = width;
			height = width;
			dst_left = 0;
			dst_top = 0;
			dst_right = width;
			dst_bottom = width;
		} else {
			roundPx = height / 2;
			float clip = (width - height) / 2;
			left = clip;
			right = width - clip;
			top = 0;
			bottom = height;
			width = height;
			dst_left = 0;
			dst_top = 0;
			dst_right = height;
			dst_bottom = height;
		}

		Bitmap output;
		try {
			output = Bitmap.createBitmap(width, height, Config.ARGB_8888);

		} catch (OutOfMemoryError e) {
			System.gc();
			return null;
		}

		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect src = new Rect((int) left, (int) top, (int) right,
				(int) bottom);
		final Rect dst = new Rect((int) dst_left, (int) dst_top,
				(int) dst_right, (int) dst_bottom);
		final RectF rectF = new RectF(dst);

		paint.setAntiAlias(true);

		canvas.drawARGB(0, 0, 0, 0);
		
        // change img to grey-scale img		
//		if (!flag) {
//			ColorMatrix cm = new ColorMatrix();
//			cm.setSaturation(0);
//			ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
//			paint.setColorFilter(f);
//		}else{
//			
//		}
		paint.setColor(color);
		
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, src, dst, paint);
		return output;
	}

	public static int getAngleByRandom(int R, int screenW) {
		Random randDegree = new Random(System.currentTimeMillis());
		int d;
		if (2 * R < screenW) {
			d = randDegree.nextInt(90);
		}

		Double degree = (Math.asin(screenW / 2 / R));// *(180/Math.PI)
		if (degree < 1) {
			return 0;
		}
		d = randDegree.nextInt(degree.intValue());
		return d;
	}

	public static Bitmap bitmap2Gray(Bitmap bmSrc) {
		int width, height;
		height = bmSrc.getHeight();
		width = bmSrc.getWidth();
		Bitmap bmpGray = null;
		bmpGray = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		Canvas c = new Canvas(bmpGray);
		Paint paint = new Paint();
		ColorMatrix cm = new ColorMatrix();
		cm.setSaturation(0);
		ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
		paint.setColorFilter(f);
		c.drawBitmap(bmSrc, 0, 0, paint);

		return bmpGray;
	}
}
