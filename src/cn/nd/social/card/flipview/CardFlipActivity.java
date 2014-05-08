package cn.nd.social.card.flipview;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.Toast;
import cn.nd.social.R;
import cn.nd.social.card.CardEntity;
import cn.nd.social.card.CardUtil;
import cn.nd.social.card.flipview.FlipAdapter.Callback;
import cn.nd.social.card.flipview.FlipView.OnFlipListener;
import cn.nd.social.card.flipview.FlipView.OnOverFlipListener;

import com.nineoldandroids.view.ViewHelper;

public class CardFlipActivity extends Activity implements Callback, OnFlipListener, OnOverFlipListener {
	
	private FlipView mFlipView;
	private FrameLayout mFlipRoot;
	private FlipAdapter mAdapter;
	private ArrayList<Integer> mIdsArray;
	private int mCurrPos = 0;
	
	private int []mTargetViewSize = new int[2];
	
	private static final int MENU_ITME_ADD_CONTACT = 10000;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);		
		Intent recvIntent = getIntent();
		mIdsArray = recvIntent.getIntegerArrayListExtra("id_list");
		int id = recvIntent.getIntExtra("card_id", 0);
		int pos = recvIntent.getIntExtra("position", 0);
		
		setContentView(R.layout.card_flip_viewer);
		
		//tangtaotao@ND_20140226
		getWindow().setFlags(0x08000000, 0x08000000);//WindowsManager.FLAG_NEEDS_MENU_KEY
		
		mFlipRoot = (FrameLayout)findViewById(R.id.flip_root);
		mFlipView = (FlipView) findViewById(R.id.flip_view);
		
		initViewParam();
		
		mAdapter = new FlipAdapter(this,mIdsArray.size());
		mAdapter.setCallback(this);
		mFlipView.setAdapter(mAdapter);
		mFlipView.setOnFlipListener(this);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		if(prefs.getBoolean("card_view_first_enter", true)) {
			//tangtaotao@NetDragon_20140212 comment out
			//mFlipView.peakNext(false);// the flip hint
			SharedPreferences.Editor editor = prefs.edit();
			editor.putBoolean("card_view_first_enter", false);
			editor.commit();
		}
		
		ViewHelper.setRotation(mFlipView, 90);
		mFlipView.setOverFlipMode(OverFlipMode.GLOW);
		mFlipView.setEmptyView(findViewById(R.id.empty_view));
		mFlipView.setOnOverFlipListener(this);
		
		mFlipView.flipTo(pos); //flip to the specifiy postion(when we click the item,we should open that item)
		
	}
	
	public CardEntity getCardItem(int pos) {
		int id = mIdsArray.get(pos);
		return CardEntity.from(this, id);
	}

	public int getTargetViewWidth() {
		return mTargetViewSize[1];
	}
	
	public int getTargetViewheight() {
		return mTargetViewSize[0];
	}

	private void initViewParam() {		
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		int width = metrics.widthPixels;
		int height = metrics.heightPixels;
		int []viewSize = new int[2];
		viewSize[0] = width;
		viewSize[1] = height;

		CardUtil.adjustTargetSize(viewSize,mTargetViewSize);
		
		//note: invert width and height
		FrameLayout.LayoutParams layoutParam = new FrameLayout.LayoutParams(
				mTargetViewSize[1],mTargetViewSize[0], Gravity.CENTER);
		mFlipRoot.updateViewLayout(mFlipView, layoutParam);
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_ITME_ADD_CONTACT, 0, getString(R.string.add_to_contact));
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ITME_ADD_CONTACT:			
			insertContact(getCardItem(mCurrPos));
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	
	private void insertContact(CardEntity item) {
		Intent intent = CardUtil.getAddContactIntent(CardEntity.getCardData(item));
		try {
			startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this, "can't add contact", Toast.LENGTH_SHORT)
					.show();
		}
	}
	
	@Override
	public void onPageRequested(int page) {
		mFlipView.smoothFlipTo(page);
	}

	@Override
	public void onFlippedToPage(FlipView v, int position, long id) {
		mCurrPos = position;
	}

	@Override
	public void onOverFlip(FlipView v, OverFlipMode mode,
			boolean overFlippingPrevious, float overFlipDistance,
			float flipDistancePerPage) {
		Log.i("overflip", "overFlipDistance = "+overFlipDistance);
	}

}
