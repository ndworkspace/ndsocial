package cn.nd.social.prishare.component;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import cn.nd.social.R;
import cn.nd.social.wheel.NumericWheelAdapter;
import cn.nd.social.wheel.OnWheelChangedListener;
import cn.nd.social.wheel.OnWheelClickedListener;
import cn.nd.social.wheel.OnWheelScrollListener;
import cn.nd.social.wheel.WheelView;


@SuppressLint("ResourceAsColor")
public  class CustomActivity extends Activity {

	
	private boolean timeChanged = false;
	private boolean timeScrolled = false;
	
	private ImageButton confirm;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.qe_custom);
		
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		int widthScreen = metrics.widthPixels;
			
		
		final WheelView hours = (WheelView) findViewById(R.id.hour);
		hours.setLayoutParams(new LinearLayout.LayoutParams(
				widthScreen/3,
				LinearLayout.LayoutParams.MATCH_PARENT));
		NumericWheelAdapter adapter_hours = new NumericWheelAdapter(this, 0, 23,"%02d");		
		adapter_hours.setItemResource(R.layout.time_item_layout);
		adapter_hours.setItemTextResource(R.id.time_item);
		hours.setViewAdapter(adapter_hours);					
		hours.setCyclic(true);
	
		final WheelView mins = (WheelView) findViewById(R.id.mins);
		mins.setLayoutParams(new LinearLayout.LayoutParams(
				widthScreen/3,
				LinearLayout.LayoutParams.MATCH_PARENT));
		NumericWheelAdapter adapter_mins = new NumericWheelAdapter(this, 0, 59, "%02d");		
		adapter_mins.setItemResource(R.layout.time_item_layout);
		adapter_mins.setItemTextResource(R.id.time_item);
		mins.setViewAdapter(adapter_mins);							
		mins.setCyclic(true);
		
		final WheelView seconds = (WheelView) findViewById(R.id.second);		
		seconds.setLayoutParams(new LinearLayout.LayoutParams(widthScreen/3,
				LinearLayout.LayoutParams.MATCH_PARENT));
		NumericWheelAdapter adapter_seconds = new NumericWheelAdapter(this, 0, 59,"%02d");		
		adapter_seconds.setItemResource(R.layout.time_item_layout);
		adapter_seconds.setItemTextResource(R.id.time_item);
		seconds.setViewAdapter(adapter_seconds);		
		seconds.setCyclic(true);
	
		seconds.setCurrentItem(00);
		hours.setCurrentItem(00);
		mins.setCurrentItem(00);

		// add listeners
		addChangingListener(seconds, "sec");
		addChangingListener(mins, "min");
		addChangingListener(hours, "hour");
	
		OnWheelChangedListener wheelListener = new OnWheelChangedListener() {
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				if (!timeScrolled) {
					timeChanged = true;
					timeChanged = false;
				}
			}
		};
		seconds.addChangingListener(wheelListener);
		hours.addChangingListener(wheelListener);
		mins.addChangingListener(wheelListener);
		
		OnWheelClickedListener click = new OnWheelClickedListener() {
            public void onItemClicked(WheelView wheel, int itemIndex) {
                wheel.setCurrentItem(itemIndex, true);
            }
        };
        seconds.addClickingListener(click);
        hours.addClickingListener(click);
        mins.addClickingListener(click);

		OnWheelScrollListener scrollListener = new OnWheelScrollListener() {
			public void onScrollingStarted(WheelView wheel) {
				timeScrolled = true;
			}
			public void onScrollingFinished(WheelView wheel) {
				timeScrolled = false;
				timeChanged = true;
				timeChanged = false;
			}
		};
		seconds.addScrollingListener(scrollListener);
		hours.addScrollingListener(scrollListener);
		mins.addScrollingListener(scrollListener);
		
		
		confirm = (ImageButton)findViewById(R.id.sure_button);
		confirm.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(CustomActivity.this,SetTimeActivity.class);	
				int h = hours.getCurrentItem();
				int m = mins.getCurrentItem();
				int s = seconds.getCurrentItem();
				if (h == 0 && m == 0 && s == 0) {
					Toast.makeText(CustomActivity.this, R.string.warn_time_invalidate, Toast.LENGTH_SHORT).show();
					return;
				}
				intent.putExtra("hrs", h);
				intent.putExtra("min", m);
				intent.putExtra("seconds", s);
				setResult(RESULT_OK, intent);
				finish();
			}
		});		

	}
	
	
	/**
	 * Adds changing listener for wheel that updates the wheel label
	 * @param wheel the wheel
	 * @param label the wheel label
	 */
	private void addChangingListener(final WheelView wheel, final String label) {
		wheel.addChangingListener(new OnWheelChangedListener() {
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				//wheel.setLabel(newValue != 1 ? label + "s" : label);
			}
		});
	}

	/**
	 * 返回
	 * @param v
	 */
	public void custom_back(View v) {
		this.finish();
	}
}
