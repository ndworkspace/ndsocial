package cn.nd.social.prishare.history;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.nd.social.R;

public class HistoryListItem extends LinearLayout {
	private TextView mTextView;
	private String mText;

	public HistoryListItem(Context context) {
		super(context);
		inflate(context, R.layout.qe_history_list_item, this);
		mTextView = (TextView) findViewById(R.id.history_item_text);
	}

	public void setText(String str) {
		mText = str;
		mTextView.setText(str);
	}

	public String getText() {
		return mText;
	}
}
