package cn.nd.social.card;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import cn.nd.social.R;
import cn.nd.social.util.Utils;

public class CardTemplateSelector extends Activity {
	
	public final static String ACTION_SELF_CARD_REFRESH = "cn.nd.social.refresh_card";
	ListView mList;
	LayoutInflater mInflater;
	private View mBackBtn;
	private View mOkBtn;
	private int mSelectId = -1;
	
	private View mLastSelectView;

	private TemplateListAdapter mAdapter;
	private int mScreenWidth;
	private ModelItem []mItems;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		DisplayMetrics metric = getResources().getDisplayMetrics();
		mScreenWidth = metric.widthPixels;
		mInflater = getLayoutInflater();
		setContentView(R.layout.card_template_select);
		setupViews();
	}

	private void setupViews() {
		mList = (ListView) findViewById(R.id.lv_models);
		mBackBtn = findViewById(R.id.left_btn);
		mOkBtn = findViewById(R.id.right_btn);
		mBackBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});
		mOkBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mSelectId == -1) {
					Toast.makeText(CardTemplateSelector.this, "should select a template", Toast.LENGTH_SHORT).show();
				} else {					
					Intent intent = new Intent();
					intent.putExtra(MyCardEditor.KEY_MODEL_ID, mSelectId);
					setResult(RESULT_OK,intent);
					finish();
				}
				
			}
		});
		
		
		
		initAdapter();

	}
	
	private void initAdapter() {
		mItems = new ModelItem[CardUtil.sMiniBackID.length];
		for(int i=0; i < mItems.length;i++) {
			mItems[i] = new ModelItem();
			mItems[i].resId = CardUtil.sMiniBackID[i];
		}
		int currentModel = Utils.getAppSharedPrefs().getInt(CardUtil.MODEL_ID, 0);
		if(currentModel > 0 && currentModel < mItems.length + 1) {
			mItems[currentModel-1].setSelect(true);
		}
		mAdapter = new TemplateListAdapter(mItems);
		
		mList.setAdapter(mAdapter);
		mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int pos,
					long id) {
				for(int i=0; i<mItems.length;i++) {
					mItems[i].setSelect(false);
				}
				for(int i=0;i<mList.getChildCount();i++) {
					View view = mList.getChildAt(i);
					ViewHolder holder = (ViewHolder)view.getTag();
					holder.chkBox.setChecked(false);
				}
				
				ViewHolder holder = (ViewHolder)v.getTag();				
				holder.chkBox.setChecked(true);
				holder.item.setSelect(true);
				
				mLastSelectView = v;
				
				mSelectId = pos + 1;
			}
		});
	}

	private class TemplateListAdapter extends BaseAdapter {
		private ModelItem []itemArr;
		TemplateListAdapter(ModelItem []items) {
			itemArr = items;
		}
		@Override
		public int getCount() {
			return itemArr.length;
		}

		@Override
		public ModelItem getItem(int position) {
			return itemArr[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			View v;
			if (convertView == null) {
				v = mInflater.inflate(R.layout.card_template_item, parent,false);				
				holder = new ViewHolder();
				holder.image = (ImageView) v.findViewById(R.id.iv_model);
				ViewGroup.LayoutParams params = holder.image.getLayoutParams();
				params.height = (mScreenWidth-20) * 2 /3;				
				
				holder.chkBox = (CheckBox)v.findViewById(R.id.checkbox);				
				v.setTag(holder);
			} else {
				v = convertView;
				holder = (ViewHolder) v.getTag();
			}
			bindView(holder, position);

			return v;
		}

		private void bindView(ViewHolder holder, int position) {
			ModelItem item = getItem(position);
			holder.item = item;
			holder.image.setImageResource(item.resId);
			holder.chkBox.setChecked(item.isSelect());
		}

	}

	private class ViewHolder {
		ImageView image;
		CheckBox chkBox;
		ModelItem item;
	}
	
	private class ModelItem {
		private boolean selected;
		public int resId;
		public void setSelect(boolean select) {
			selected = select;
		}
		public boolean isSelect() {
			return selected;
		}
	}
}
