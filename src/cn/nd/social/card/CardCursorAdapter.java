package cn.nd.social.card;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import cn.nd.social.R;

/**
 * Card list adapter
 */
public class CardCursorAdapter extends CursorAdapter {
	protected boolean mIsScrolling = false;
	private final LayoutInflater mInflater;

	public void setIsScrolling(boolean isScrolling) {
		this.mIsScrolling = isScrolling;
	}

	public CardCursorAdapter(Context context, Cursor c) {
		super(context, c, false); /* auto-requery to false */
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		if (!(view instanceof CardListItem)) {
			return;
		}
		CardListItem headerView = (CardListItem) view;
		if (!mIsScrolling) {
			headerView.bind(cursor);
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {		
		View view = mInflater.inflate(R.layout.card_item, parent, false);
		return view;
	}
}