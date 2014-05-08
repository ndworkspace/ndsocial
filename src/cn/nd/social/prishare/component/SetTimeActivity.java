package cn.nd.social.prishare.component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Paint;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.nd.social.R;
import cn.nd.social.prishare.PriShareSendActivity;
import cn.nd.social.sendfile.SendFilesActivity;
import cn.nd.social.util.AudioDataPacker;
import cn.nd.social.util.Utils;

public class SetTimeActivity extends Activity implements
		ViewPager.OnPageChangeListener {

	private RelativeLayout setTime_frame;
	private ViewPager setTime_pager;
	private STPagerAdapter setTime_pagerAdapter;
	private LayoutInflater setTime_inflater;
	private int pageSize = 1;

	private TextView st_eternity;
	private TextView st_hrs;
	private ImageView st_img_bg;
	private ImageView st_img_bg_empty;
	private ImageView st_img_delete;
	private ImageButton st_btn_sure;
	
	private TextView st_txt_custom;

	private int hrs;
	private int mins;
	private int secs;
	
	private final static Object ST_TAG = new Object();

	Context mContext;
	PhraseItemHelper mItemHelper;
	
	public final static String EXPIRE_TIME = "expire_time";
	public final static String INDEX = "index";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		mContext = this;
		
		setContentView(R.layout.qe_set_time);
		setTime_inflater = LayoutInflater.from(this);		
		setFindId();
		
		st_txt_custom.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
		
		mItemHelper = new PhraseItemHelper();
		pageSize = mItemHelper.mPhraseVec.size()+1;

		if (pageSize > 1) {
			setupPager(1);
		}else{
			setupPager(0);
		}
		
		setListenerEvent();
		
		

	}
	public void setListenerEvent(){
		st_btn_sure.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(SetTimeActivity.this,PriShareSendActivity.class);
				int ind = setTime_pager.getCurrentItem()-1;
				if(ind != -1) {
					intent.putExtra(EXPIRE_TIME, mItemHelper.getItem(setTime_pager.getCurrentItem()-1));
				}
				setResult(RESULT_OK, intent);
				finish();
			}
		});
		
		st_txt_custom.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(SetTimeActivity.this,
						CustomActivity.class);
				startActivityForResult(intent, 10);
			}
		});
		
	}
	private void setupPager(int index) {
		setupPager(index, false);

	}
	private void setupPager(int index,boolean deleteState) {
		// TODO Auto-generated method stub

		setTime_pagerAdapter = new STPagerAdapter(this);
		setTime_pager.setAdapter(setTime_pagerAdapter);	
		if (deleteState) {
			Animation fade_in = AnimationUtils.loadAnimation(this, R.anim.fade_in_2);
			setTime_pager.startAnimation(fade_in);
		}

		setTime_pager.setCurrentItem(index);
		setTime_pager.setOffscreenPageLimit(1);
		setTime_pager.setOnPageChangeListener(this);
	}

	
	public void setFindId() {

		setTime_pager = (ViewPager) findViewById(R.id.settime_pager);
		st_btn_sure = (ImageButton) findViewById(R.id.st_sure_button);
		st_txt_custom = (TextView) findViewById(R.id.custom_text);
		
	}


	
	public void setFrameLongListenerEvent(final int pos) {
		setTime_frame.setOnLongClickListener(new View.OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				
				ImageView currentDelete = (ImageView)v.findViewById(R.id.set_time_delete);
				currentDelete.setVisibility(View.VISIBLE);
				currentDelete.setTag(ST_TAG);
				currentDelete.setOnClickListener(new StListener(pos));	
				Animation shake = AnimationUtils.loadAnimation(SetTimeActivity.this, R.anim.delete_shake);
				
				v.findViewById(R.id.shake_frame).startAnimation(shake);
			
				return false;
			}
		});


	}
	
	public class StListener implements View.OnClickListener{

		public int index;
		
		public StListener(int ind){
			this.index = ind;
		}
		@Override
		public void onClick(View v) {
			
			mItemHelper.removeItem(index-1);
			pageSize--;

			Animation fade_out = AnimationUtils.loadAnimation(SetTimeActivity.this, R.anim.fade_out_2);
		
			((View)v.getParent()).startAnimation(fade_out);			
			fade_out.setAnimationListener(new AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation arg0) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onAnimationRepeat(Animation arg0) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onAnimationEnd(Animation arg0) {
					// TODO Auto-generated method stub
					setupPager(index-1,true);//����ǰһҳ��page����
					st_img_delete.setVisibility(View.GONE);
				}
			});
		}
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 10) {
			if (resultCode == RESULT_OK) {			
				hrs = data.getIntExtra("hrs", 0);
				mins = data.getIntExtra("min", 0);
				secs = data.getIntExtra("seconds", 0);
				mItemHelper.addItem(String.valueOf(hrs+","+mins+","+secs));
				pageSize++;
				setupPager(pageSize - 1,false);

			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * 返回
	 * 
	 * @param v
	 */
	public void setTime_back(View v) {
		this.finish();
	}

	/**
	 * @param index
	 * @return
	 */
	View loadView(int index) {
		View v;
		v = setTime_inflater.inflate(R.layout.qe_adapter_settime, null);
		setTime_frame = (RelativeLayout) v.findViewById(R.id.set_sub_layout);// qe_adapter_settime


		if (index == 0) {
	
				st_eternity = (TextView) v.findViewById(R.id.set_time_eternity);
				st_eternity.setVisibility(View.VISIBLE);
				st_img_bg_empty = (ImageView) v
					  	.findViewById(R.id.set_time_bg_empty);
				st_img_bg_empty.setVisibility(View.VISIBLE);

		}else{
			String fact_time = mItemHelper.getItem(index-1);
			String sarray[]=fact_time.split(",");

			st_img_delete = (ImageView)v.findViewById(R.id.set_time_delete);
			st_img_delete.setVisibility(View.GONE);
			st_hrs = (TextView) v.findViewById(R.id.set_time_hours);
			st_hrs.setText(sarray[0] + getString(R.string.hours) + 
					sarray[1] + getString(R.string.mins) +
					sarray[2] + getString(R.string.secs_single));
			st_hrs.setVisibility(View.VISIBLE);
			st_img_bg = (ImageView) v.findViewById(R.id.set_time_bg);
			st_img_bg.setVisibility(View.VISIBLE);
			setFrameLongListenerEvent(index);
		}


		return v;
	}

	/**
	 * Page Adapter
	 * 
	 * @author xls
	 * 
	 */
	public class STPagerAdapter extends PagerAdapter {
		
		private int lastTime;

		public STPagerAdapter(SetTimeActivity setTimeActivity) {
			// TODO Auto-generated constructor stub
			lastTime = 0;
		}

		@Override
		public void destroyItem(ViewGroup group, int position, Object item) {
			((ViewPager) group).removeView((View) item);
		}

		@Override
		public void finishUpdate(ViewGroup container) {
			// TODO Auto-generated method stub
			super.finishUpdate(container);
		}

		@Override
		public Object instantiateItem(ViewGroup parent, int position) {
			// TODO Auto-generated method stub
			View v = loadView(position);
			parent.addView(v);
			return v;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return pageSize;
		}

		@Override
		public boolean isViewFromObject(View view, Object obj) {
			// TODO Auto-generated method stub
			return view == obj;
		}

	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
		

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub

		
	}

	@Override
	public void onPageSelected(int arg0) {
		// TODO Auto-generated method stub

		View v = setTime_pager.findViewWithTag(ST_TAG);
		if (v != null) {
			v.setVisibility(View.GONE);
			v.setTag(null);
		}

	}

	/**
	 * 
	 * @author xls
	 *
	 */
	public class PhraseItemHelper {
		private SharedPreferences mSp;
		private final String mSeparator = "\t";
		private Vector<String> mPhraseVec;
		private final static String preference_label = "data";
		private final static String preference_flag = "count";

		public PhraseItemHelper() {
			mSp = PreferenceManager.getDefaultSharedPreferences(Utils
					.getAppContext());
			String str = mSp.getString(preference_label, "");
			if (mSp.getInt(preference_flag, 0) == 0) {
				// phrase is not in shared preference in the first time,load it
				// from file
				loadPhraseFromFile();
				storePhrase();
				
				SharedPreferences.Editor editor = mSp.edit();
				editor.putInt(preference_flag, 1);
				editor.commit();
			} else {
				loadPhrase(str);
			}
//			loadPhrase(str);
		}

		private void loadPhraseFromFile() {
			mPhraseVec = new Vector<String>();
			InputStream phraseFile;
			BufferedReader br = null;
			try {
				phraseFile = mContext.getResources().openRawResource(
						R.raw.record_set_time);
				br = new BufferedReader(new InputStreamReader(phraseFile));
				String str;
				while ((str = br.readLine()) != null) {
					mPhraseVec.add(str);
				}
				br.close();
			} catch (Resources.NotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
		}

		private void loadPhrase(String str) {
			mPhraseVec = new Vector<String>();
			StringTokenizer strToken = new StringTokenizer(str, mSeparator);
			while (strToken.hasMoreTokens()) {
				mPhraseVec.add(strToken.nextToken());
			}
		}

		private void storePhrase() {
			StringBuilder phrases = new StringBuilder();
			Iterator<String> iter = mPhraseVec.iterator();
			while (iter.hasNext()) {
				phrases.append(iter.next());
				phrases.append("\t");
			}
			if (phrases.length() != 0) {
				phrases.delete(phrases.lastIndexOf(mSeparator),
						phrases.length());
			}
			SharedPreferences.Editor editor = mSp.edit();
			editor.putString(preference_label, phrases.toString());
			editor.commit();

		}

		public void removeItem(int idx) {
			if ((idx >= 0) && (idx < mPhraseVec.size())) {
				mPhraseVec.remove(idx);
				storePhrase();
			}
		}

		public void addItem(String str) {
			mPhraseVec.add(mPhraseVec.size(), str);
			storePhrase();
		}

		public void editItem(int idx, String str) {
			if (idx >= 0 && idx < mPhraseVec.size()) {
				mPhraseVec.set(idx, str);
				storePhrase();
			}
		}

		public String getItem(int idx) {
			if ((idx < 0) || (idx >= mPhraseVec.size()))
				return null;
			return mPhraseVec.get(idx);
		}

		public int getCount() {
			return mPhraseVec.size();
		}
	}

}
