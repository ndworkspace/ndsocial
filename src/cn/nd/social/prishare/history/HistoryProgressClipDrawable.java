package cn.nd.social.prishare.history;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;

public final class HistoryProgressClipDrawable extends ClipDrawable {
	private Drawable a;
	private int b;
	private int c;
	private final Rect d = new Rect();

	public HistoryProgressClipDrawable(Drawable paramDrawable) {
		super(paramDrawable, 3, 1);
		this.a = paramDrawable;
		this.c = 3;
		this.b = 1;
	}

	public final void draw(Canvas paramCanvas) {
		if (this.a.getLevel() == 0) {
			Log.e("history", "level is 0");
			return;
		}

		Rect localRect1;
		int j;
		int k;

		localRect1 = this.d;
		Rect localRect2 = getBounds();
		int i = getLevel();
		j = localRect2.width();
		if ((0x1 & this.b) != 0)
			j -= (j + 0) * (10000 - i) / 10000;
		k = localRect2.height();
		if ((0x2 & this.b) != 0)
			k -= (k + 0) * (10000 - i) / 10000;
		Gravity.apply(this.c, j, k, localRect2, localRect1);
		if ((j <= 0) || (k <= 0)) {
			Log.e("history", "(j <= 0) || (k <= 0)");
			return;
		}

		paramCanvas.save();
		paramCanvas.clipRect(localRect1);

		Log.e("history", "Rect : top " + localRect1.top + " bottom "
				+ localRect1.bottom + " left " + localRect1.left + " right "
				+ localRect1.right);

		this.a.draw(paramCanvas);
		paramCanvas.restore();
	}
}

/*
 * Location: E:\source_code\decompile\classes-dex2jar.jar Qualified Name:
 * com.dewmobile.kuaiya.ui.p JD-Core Version: 0.6.0
 */