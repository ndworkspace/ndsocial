package cn.nd.social.ui.controls;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import cn.nd.social.R;

public class ClearableEditTextWithIcon extends EditText implements TextWatcher,
		android.view.View.OnTouchListener {
	final Drawable deleteImage;
	Drawable icon;

	public ClearableEditTextWithIcon(Context context) {
		super(context);
		deleteImage = getResources().getDrawable(R.drawable.icon_edit_delete);
		init();
	}

	public ClearableEditTextWithIcon(Context context, AttributeSet attributeset) {
		super(context, attributeset);
		deleteImage = getResources().getDrawable(R.drawable.icon_edit_delete);
		init();
	}

	public ClearableEditTextWithIcon(Context context,
			AttributeSet attributeset, int i) {
		super(context, attributeset, i);
		deleteImage = getResources().getDrawable(R.drawable.icon_edit_delete);
		init();
	}

	private void init() {
		setOnTouchListener(this);
		addTextChangedListener(this);
		deleteImage.setBounds(0, 0, deleteImage.getIntrinsicWidth(),
				deleteImage.getIntrinsicHeight());
		manageClearButton();
	}

	void addClearButton() {
		setCompoundDrawables(icon, getCompoundDrawables()[1], deleteImage,
				getCompoundDrawables()[3]);
	}

	public void afterTextChanged(Editable editable) {
	}

	public void beforeTextChanged(CharSequence charsequence, int i, int j, int k) {
	}

	void manageClearButton() {
		if (getText().toString().equals("")) {
			removeClearButton();
			return;
		} else {
			addClearButton();
			return;
		}
	}

	public void onTextChanged(CharSequence charsequence, int i, int j, int k) {
		manageClearButton();
	}

	public boolean onTouch(View view, MotionEvent motionevent) {
		while (getCompoundDrawables()[2] == null
				|| motionevent.getAction() != 1
				|| motionevent.getX() <= (float) (getWidth()
						- getPaddingRight() - deleteImage.getIntrinsicWidth()))
			return false;
		setText("");
		removeClearButton();
		return false;
	}

	void removeClearButton() {
		setCompoundDrawables(icon, getCompoundDrawables()[1], null,
				getCompoundDrawables()[3]);
	}

	public void setIconResource(int i) {
		icon = getResources().getDrawable(i);
		icon.setBounds(0, 0, icon.getIntrinsicWidth(),
				icon.getIntrinsicHeight());
		manageClearButton();
	}

	public String getTextString() {
		return getText().toString();
	}

}