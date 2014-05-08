package cn.nd.social.ui.controls;

import java.util.regex.Pattern;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import cn.nd.social.R;

public class MobileEditText extends ClearableEditTextWithIcon {
	private TextWatcher watcher;

	public MobileEditText(Context paramContext) {
		super(paramContext);
		addMobileTextWatcher();
	}

	public MobileEditText(Context paramContext, AttributeSet paramAttributeSet) {
		super(paramContext, paramAttributeSet);
		addMobileTextWatcher();
	}

	public MobileEditText(Context paramContext, AttributeSet paramAttributeSet,
			int paramInt) {
		super(paramContext, paramAttributeSet, paramInt);
		addMobileTextWatcher();
	}

	private void addMobileTextWatcher() {
		setIconResource(R.drawable.g_ic_mobile);
		watcher = new MobileEditWatcher(this);
		addTextChangedListener(watcher);
	}

	public static boolean isCanonicalMobile(String phoneStr) {
		if (TextUtils.isEmpty(phoneStr))
			return false;
		String str = "\\d*";
		if (phoneStr.length() < 4)
			str = "\\d*";
		else {
			if (phoneStr.length() < 9) {
				str = "\\d{3}-\\d+";
			} else if (phoneStr.length() < 14) {
				str = "\\d{3}-\\d{4}-\\d+";
			} else if (phoneStr.length() < 20) {
				str = "\\d{3}-\\d{4}-\\d{4}-\\d+";
			}
		}
		return Pattern.matches(str, phoneStr);
	}

	public static final String makeCanonicalMobile(String phoneNum) {
		String str = "";
		if (!TextUtils.isEmpty(phoneNum)) {
			for (int i = 0; i < phoneNum.length(); i++) {
				if ((i == 3) || (i == 7) || (i == 11))
					str = str + "-";
				str = str + phoneNum.charAt(i);
			}
		}
		return str;
	}

	public String getMobile() {
		return getText().toString();
	}

	class MobileEditWatcher implements TextWatcher {
		boolean delete;
		private MobileEditText thiz;

		MobileEditWatcher(MobileEditText editor) {
			thiz = editor;
		}

		public void afterTextChanged(Editable editable) {
		}

		public void beforeTextChanged(CharSequence paramCharSequence,
				int paramInt1, int paramInt2, int paramInt3) {
		}

		public void onTextChanged(CharSequence paramCharSequence,
				int paramInt1, int paramInt2, int paramInt3) {
		}
	}
}
