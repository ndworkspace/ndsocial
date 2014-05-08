package cn.nd.social.prishare.history;

import java.lang.reflect.Field;

import android.graphics.Canvas;
import android.graphics.NinePatch;
import android.graphics.drawable.NinePatchDrawable;

public final class HistoryNinePatchDrawable extends NinePatchDrawable {
	private NinePatch a;

	private HistoryNinePatchDrawable(NinePatch paramNinePatch) {
		super(paramNinePatch);
		this.a = paramNinePatch;
	}

	public static HistoryNinePatchDrawable a(
			NinePatchDrawable paramNinePatchDrawable) {
		return new HistoryNinePatchDrawable(b(paramNinePatchDrawable));
	}

	private static NinePatch b(NinePatchDrawable paramNinePatchDrawable) {
		try {
			Field localField = NinePatchDrawable.class
					.getDeclaredField("mNinePatch");
			localField.setAccessible(true);
			Object localObject = localField.get(paramNinePatchDrawable);
			localField.setAccessible(false);
			NinePatch localNinePatch = (NinePatch) localObject;
			return localNinePatch;
		} catch (Exception localException) {
		}

		return c(paramNinePatchDrawable);
	}

	private static NinePatch c(NinePatchDrawable ninepatchdrawable) {
		try {
			Field afield[] = NinePatchDrawable.class.getDeclaredFields();

			int i = 0;

			for (; i < afield.length; i++) {
				afield[i].setAccessible(true);
				Object obj2 = afield[i].get(ninepatchdrawable);
				afield[i].setAccessible(false);
				if (obj2 instanceof NinePatch)
					return (NinePatch) obj2;
			}

			for (int j = 0; j < afield.length; j++) {
				Object obj;
				afield[j].setAccessible(true);
				obj = afield[j].get(ninepatchdrawable);
				afield[j].setAccessible(false);
				if ((obj instanceof android.graphics.drawable.Drawable.ConstantState)) {
					Field afield1[] = obj.getClass().getDeclaredFields();
					for (int k = 0; k < afield1.length; k++) {
						NinePatch ninepatch;
						afield1[k].setAccessible(true);
						Object obj1 = afield[j].get(obj);
						afield1[k].setAccessible(false);
						if (!(obj1 instanceof NinePatch))
							break;
						ninepatch = (NinePatch) obj1;
						return ninepatch;
					}

				}
			}
		} catch (Exception e) {

		}
		return null;

	}

	public final void draw(Canvas paramCanvas) {
		this.a.draw(paramCanvas, getBounds(), getPaint());
	}
}

/*
 * Location: E:\source_code\decompile\classes-dex2jar.jar Qualified Name:
 * com.dewmobile.kuaiya.ui.bd JD-Core Version: 0.6.0
 */