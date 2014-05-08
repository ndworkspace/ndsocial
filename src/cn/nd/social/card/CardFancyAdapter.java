package cn.nd.social.card;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import cn.nd.social.R;

import com.nineoldandroids.view.ViewHelper;

/**
 * fancy Card list adapter
 */
public class CardFancyAdapter extends CursorAdapter {
	protected boolean mIsScrolling = false;
	private final LayoutInflater mInflater;
	private int mHeight = 0;
	private Context mContext;

	private final static int ROTATION_DEGREE = -5;	
	
	private final static int[] BG_RES = { R.drawable.card1,R.drawable.card2,
									R.drawable.card3,R.drawable.card4};
	
	public void setIsScrolling(boolean isScrolling) {
		this.mIsScrolling = isScrolling;
	}

	public CardFancyAdapter(Context context, Cursor c, int itemHeight) {
		super(context, c, false); /** auto-requery to false */
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
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {		
		View view = mInflater.inflate(R.layout.card_item_fancy, parent, false);
		ViewGroup.LayoutParams param = view.getLayoutParams();		

		param.height = mHeight * 2 / 5;
		View itemView = view.findViewById(R.id.list_item);
		ViewGroup.LayoutParams itemParam = itemView.getLayoutParams();		

		itemParam.height = mHeight;
		itemView.setBackgroundResource(BG_RES[cursor.getPosition()%BG_RES.length]);
		ViewHelper.setPivotX(itemView, mHeight*3/2/2);//
		ViewHelper.setPivotY(itemView, 0);
		ViewHelper.setRotationX(itemView, ROTATION_DEGREE);

		return view;
	}
	
	View.OnClickListener mClickListener;
	View.OnLongClickListener mLongClickListener;
	public void setOnItemClickListener(View.OnClickListener listener) {
		mClickListener = listener;
	}
	
	public void setOnItemLongClickListener(View.OnLongClickListener listener) {
		mLongClickListener = listener;
	}
}