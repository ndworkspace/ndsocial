package cn.nd.social.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import cn.nd.social.prishare.component.InterfaceHandlerForMain;

public class CommonUtils {

	/**
	 * Draw the view into a bitmap.
	 */
	public static Bitmap getViewBitmap(View v) {
		v.clearFocus();
		v.setPressed(false);
	
		boolean willNotCache = v.willNotCacheDrawing();
		v.setWillNotCacheDrawing(false);
	
		// Reset the drawing cache background color to fully transparent
		// for the duration of this operation
		int color = v.getDrawingCacheBackgroundColor();
		v.setDrawingCacheBackgroundColor(0);
	
		if (color != 0) {
			v.destroyDrawingCache();
		}
		v.buildDrawingCache();
		Bitmap cacheBitmap = v.getDrawingCache();
		if (cacheBitmap == null) {
			Log.e(InterfaceHandlerForMain.TAG, "failed getViewBitmap(" + v + ")",
					new RuntimeException());
			return null;
		}
	
		Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);
	
		// Restore the view
		v.destroyDrawingCache();
		v.setWillNotCacheDrawing(willNotCache);
		v.setDrawingCacheBackgroundColor(color);
	
		return bitmap;
	}

	/** 
	 * regular methods to install apk, 
	 * */
	public static void installApkNormal(String apkPath) {
		Intent intent = new Intent(android.content.Intent.ACTION_VIEW);	
		// Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
	
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// intent.setFlags(Intent.ACTION_PACKAGE_REPLACED);	
		// intent.setAction(Settings. ACTION_APPLICATION_SETTINGS);
	
		intent.setDataAndType(
				Uri.fromFile(new File(apkPath)),
				"application/vnd.android.package-archive");
	
		Utils.getAppContext().startActivity(intent);
	
	}

	/** static functions **/
	
	/** 
	 * silent install apk for rooted device
	 * */
	public static boolean installApkRooted(File file, Context context) {
		boolean result = false;
		Process process = null;
		OutputStream out = null;
		InputStream in = null;
		String state = null;
		try {
			// request for root
			process = Runtime.getRuntime().exec("su");
			out = process.getOutputStream();
	
			// write install to process
			out.write(("pm install -r " + file + "\n").getBytes());
	
			// start install
			in = process.getInputStream();
			int len = 0;
			byte[] bs = new byte[256];
			while (-1 != (len = in.read(bs))) {
				state = new String(bs, 0, len);
				if (state.equals("Success\n")) {
					result = true;
					Log.e("success", state);
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null) {
					out.flush();
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	
	}

}
