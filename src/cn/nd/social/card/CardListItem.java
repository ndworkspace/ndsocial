package cn.nd.social.card;

import android.content.Context;
import android.database.Cursor;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import cn.nd.social.R;
import cn.nd.social.data.CardOpenHelper;
import cn.nd.social.util.Utils;

public class CardListItem extends FrameLayout {

	Context mContext;
	TextView mName;
	TextView mTitle;
	ImageView mFriendInd;
	View mView;
	int mBgId;
	public CheckBox mCkbx;
	public boolean mSelected;
	private int mId = -1;
	private long mUserId = Utils.INVALID_USER_ID;
	private static final int NAME_COLUMN = 1;
	private static final int TITLE_COLUMN = 2;
	
	public CardListItem(Context context) {
		super(context);
		mContext = context;
	}

	public CardListItem(Context context, AttributeSet attrs) {
		super(context, attrs); 
		mContext = context;
	}

	public void bind(Cursor cursor) {
		mName.setText(cursor.getString(NAME_COLUMN));
		mTitle.setText(cursor.getString(TITLE_COLUMN));		
		mId = cursor.getInt(0);		
		mUserId = cursor.getLong(10);
		mBgId = cursor.getInt(cursor.getColumnIndex(CardOpenHelper.COLUMN_BG_ID));

		//TODO: tangtaotao@20140326 send multiple card
/*		if (TabCardNew.getItemIsSelected(mId)) {
			mCkbx.setChecked(true);
			mSelected = true;
		}else{
			mCkbx.setChecked(false);
			mSelected = false;
		}*/
		
		int resId = CardUtil.getCardBgIdMini(mBgId);
		setBackgroundResource(resId);
	}

	public int getId() {
		return mId;
	}

	public long getUserId() {
		return mUserId;
	}
	public boolean getCkbx(){
		return mCkbx.isChecked();
	}
	public boolean getSelected(){
		return mSelected;
	}
	public void setSelected(boolean s){
		mSelected = s;
	}
	public int getBgId(){
		return mBgId;
	}


	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mName = (TextView) findViewById(R.id.name);
		mTitle = (TextView) findViewById(R.id.title);
		mCkbx = (CheckBox)findViewById(R.id.ckbx);
		//mFriendInd = (ImageView) findViewById(R.id.friends_ind);
	}
}
