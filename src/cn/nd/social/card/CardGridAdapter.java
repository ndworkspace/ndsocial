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
public class CardGridAdapter extends CursorAdapter {
	protected boolean mIsScrolling = false;
	private final LayoutInflater mInflater;
	private int mHeight = 0;
	
	private int[] CARD_BG = {R.drawable.card1,R.drawable.card2,
									R.drawable.card3,R.drawable.card4};
	
	private Context mContext;
	public void setIsScrolling(boolean isScrolling) {
		this.mIsScrolling = isScrolling;
	}

	public CardGridAdapter(Context context, Cursor c, int itemHeight) {
		super(context, c, false); /* auto-requery to false */
		mInflater = LayoutInflater.from(context);
		mHeight = itemHeight;
		mContext = context;
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
		headerView.setTag(new Integer(cursor.getPosition()));
		headerView.setOnClickListener(mClickListener);
		headerView.setOnLongClickListener(mLongClickListener);
		headerView.setOnTouchListener(mOnTouchListener);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {		
		View view = mInflater.inflate(R.layout.card_item_grid, parent, false);
		//view.setBackgroundResource(CARD_BG[cursor.getPosition()%4]);
		ViewGroup.LayoutParams param = view.getLayoutParams();
		param.height = mHeight;
		return view;
	}
	
	View.OnClickListener mClickListener;
	View.OnLongClickListener mLongClickListener;
	View.OnTouchListener mOnTouchListener;
	public void setOnItemClickListener(View.OnClickListener listener) {
		mClickListener = listener;
	}
	
	public void setOnItemLongClickListener(View.OnLongClickListener listener) {
		mLongClickListener = listener;
	}
	public void setOnItemTouchListener(View.OnTouchListener listener) {
		mOnTouchListener = listener;
	}
}