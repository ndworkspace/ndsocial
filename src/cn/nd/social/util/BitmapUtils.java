package cn.nd.social.util;

import java.io.FileOutputStream;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ThumbnailUtils;
import android.widget.Toast;

public class BitmapUtils {
	
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

	
	/**
	 * get Thumbnail
	 * filePath: the input image file path
	 * thumbPath: the output thumbnail path
	 * */
	public final static boolean extractThumbnail(String filePath,String thumbPath) {
		try {
	
			final BitmapFactory.Options opt = new BitmapFactory.Options();
			opt.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(filePath, opt);
			
		
			//set sample size to avoid oom
			opt.inSampleSize = calcSampleSize(opt, 
					DimensionUtils.getDisplayWidth() /2 , DimensionUtils.getDisplayHeight() /2);
			
			opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
			opt.inPurgeable = true;
			opt.inInputShareable = true;
			opt.inJustDecodeBounds = false;
			
			Bitmap fileBmp = BitmapFactory.decodeFile(filePath, opt);
			
			Bitmap thumb = ThumbnailUtils.extractThumbnail(fileBmp,
					DimensionUtils.getThumbnailDimen(), DimensionUtils.getThumbnailDimen());
			fileBmp.recycle();
			
			FileOutputStream out = new FileOutputStream(thumbPath);
			thumb.compress(Bitmap.CompressFormat.PNG, 90, out);
			thumb.recycle();
			out.flush();
			out.close();
			System.gc();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} catch (OutOfMemoryError e) {
			Toast.makeText(Utils.getAppContext(), "out of memory",
					Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}
	
	public static int calcSampleSize(BitmapFactory.Options options,int targetWidth,int targetHeigth) {
		final int heightRatio = Math.round((float) options.outHeight
				/ (float) targetHeigth);
		final int widthRatio = Math.round((float) options.outWidth
				/ (float) targetWidth);
		int inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		return inSampleSize;
	}
}
