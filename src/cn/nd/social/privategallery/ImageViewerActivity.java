package cn.nd.social.privategallery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.WeakHashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;
import cn.nd.social.R;
import cn.nd.social.privategallery.imageviewer.ImageViewTouch;
import cn.nd.social.privategallery.imageviewer.ImageViewTouchBase.DisplayType;
import cn.nd.social.privategallery.imageviewer.ImageViewTouchViewPager;

public class ImageViewerActivity extends Activity {
	ImageView mImage;
	Context mContext;
	ArrayList<String> fileList;
	LayoutInflater mInflater;

	//WeakHashMap: the key is weakReference
	WeakHashMap<View, Bitmap> mRecycleCache = new WeakHashMap<View,Bitmap>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent receivedIntent = getIntent();

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		mContext = this;
		mInflater = LayoutInflater.from(this);
		fileList = receivedIntent.getStringArrayListExtra("file_list");
		currIndex = receivedIntent.getIntExtra("file_index", 0);
		setContentView(R.layout.private_zoom_image_viewer);

		ViewPager pager = (ViewPager) findViewById(R.id.pictures_viewPager);
		ImagePagerAdapter adapter = new ImagePagerAdapter(this);
		pager.setAdapter(adapter);
		pager.setCurrentItem(currIndex);
		pager.setOffscreenPageLimit(1);
		
	}

	int currIndex;

	@Override
	protected void onDestroy() {
		if(mRecycleCache.size() > 0) { //recycle  bitmap memory
			Collection<Bitmap> collectBm = mRecycleCache.values(); 
			for(Bitmap bm:collectBm) {
				if(bm != null && !bm.isRecycled()) {
					bm.recycle();
				}
			}
			mRecycleCache.clear();
			System.gc();
		}
		
		super.onDestroy();
	}

	private int getSampleSize(BitmapFactory.Options options) {
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		int reqWidth = metrics.widthPixels;
		int reqHeight = metrics.heightPixels;

		final int heightRatio = Math.round((float) options.outHeight
				/ (float) reqHeight);
		final int widthRatio = Math.round((float) options.outWidth
				/ (float) reqWidth);
		int inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		return inSampleSize;
	}

	public class ImagePagerAdapter extends PagerAdapter {
		ImagePagerAdapter(Activity activity) {
		}

		public final void destroyItem(ViewGroup group, int index, Object item) {
			View v = (View) item;
			group.removeView(v);
			
			//recycle bitmap
			Bitmap bm = mRecycleCache.remove(v);
			if(bm != null && !bm.isRecycled()) {
				bm.recycle();
			}
		}

		public final void finishUpdate(ViewGroup group) {
		}

		public final Object instantiateItem(ViewGroup parent, int index) {

			ImageViewTouch image;
			View layout = mInflater.inflate(R.layout.private_image_viewer_item,
					null);
			image = (ImageViewTouch) layout.findViewById(R.id.touch_image);
			image.setTag(ImageViewTouchViewPager.VIEW_PAGER_OBJECT_TAG + index);//tang newly add
			try {
				final BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;
				String filePath = fileList.get(index);
				BitmapFactory.decodeFile(filePath, options);

				BitmapFactory.Options opt = new BitmapFactory.Options();
				opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
				opt.inJustDecodeBounds = false;
				opt.inPurgeable = true;
				opt.inInputShareable = true;
				opt.inSampleSize = getSampleSize(options);
				Bitmap bm = BitmapFactory.decodeFile(filePath, opt);
				image.setImageBitmap(bm);
				image.setDisplayType( DisplayType.FIT_IF_BIGGER );
				
				mRecycleCache.put(layout, bm);//put bitmap to cache for future recycle
				
			} catch (Exception e) {
				e.printStackTrace();
			} catch (OutOfMemoryError e) {
				Toast.makeText(ImageViewerActivity.this, "out of memory",
						Toast.LENGTH_SHORT).show();
				System.gc(); //since we lack memory, release memory for future use
			}
			parent.addView(layout);
			return layout;
		}

		@Override
		public final boolean isViewFromObject(View view, Object obj) {
			return view == obj;
		}

		public final void restoreState(Parcelable parcel, ClassLoader loader) {
		}

		public final Parcelable saveState() {
			return null;
		}

		public final void startUpdate(ViewGroup group) {
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return fileList.size();
		}

	}

}
