package cn.nd.social.common;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import cn.nd.social.MessageActivity;
import cn.nd.social.R;
import cn.nd.social.data.MsgProviderSingleton;
import cn.nd.social.hotspot.MsgDefine;
import cn.nd.social.privategallery.PrivateGalleryProvider;
import cn.nd.social.privategallery.Utils;
import cn.nd.social.privategallery.imageviewer.ImageViewTouch;
import cn.nd.social.privategallery.imageviewer.ImageViewTouchBase.DisplayType;
import cn.nd.social.services.FileControlProvider;
import cn.nd.social.ui.controls.TVOffAnimation;

public class ImageViewer extends Activity implements OnCompletionListener {
	Bitmap mBitmap;
	
	ImageView mMaskImage;
	ImageViewTouch mImage;
	
	int mType;
	int mValue;
	int mStatus;
	long mStaticTime;
	String mPath;
	
	long mUtc;
	int remain = 0;
	
	private final static int EFFECT_TV_OFF = 4;
	private final static int EFFECT_IMAGE_CRACK = 2;
	MediaPlayer mPlayer;
	Boolean mStopPlaying;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(R.layout.disappear_image_viewer);
		mImage = (ImageViewTouch) findViewById(R.id.touch_image);
		mMaskImage = (ImageView)findViewById(R.id.mask_image);
		
		Intent recvIntent = getIntent();
		mType = recvIntent.getIntExtra("type", 0);
		mValue = recvIntent.getIntExtra("value", -1);
		mStaticTime = recvIntent.getLongExtra("static", 0);
		mStatus = recvIntent.getIntExtra("status", MsgDefine.STATUS_NEED_DELETE);
		mPath = recvIntent.getStringExtra("filename");
		
		mUtc = Calendar.getInstance().getTimeInMillis();
		Log.e("Image dd", "2:"+ mUtc + "," + mStaticTime + "," + mStatus);
		
		long deleteTime = mValue * 1000 + mUtc;
		
		if (mValue != -1 && mValue < MsgProviderSingleton.STATIC_TIME && mStatus == MsgDefine.STATUS_NEED_DELETE) {	
			MsgProviderSingleton.getInstance().updateRecordBoth(mPath, deleteTime, MsgDefine.STATUS_NEED_DELETE_AND_OPENED, this);
			Log.e("Image dd", "3:"+ deleteTime + "," + 3);
		}
		
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(mPath, opt);
		opt.inJustDecodeBounds = false;
		opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
		opt.inPurgeable = true;
		opt.inInputShareable = true;
		opt.inSampleSize = getSampleSize(opt);
		try {
			mBitmap = BitmapFactory.decodeFile(mPath, opt);
		} catch(OutOfMemoryError e) {
			Toast.makeText(this, "out of memory", Toast.LENGTH_SHORT).show();
			System.gc();
			finish();
			return;
		}
		
		mImage.setImageBitmap(mBitmap);
		mImage.setDisplayType(DisplayType.FIT_IF_BIGGER);//fill full screen if big enough

		
		if (mType == MsgDefine.GRANT_FILE_AUTO_DESTROY) {
			
			
			if (mValue != -1) {

				remain = (int)(mStaticTime - mUtc / 1000);
				
				if (mValue < remain) {
					remain = mValue;
				}
				
				mHandler.postDelayed(runnable, 200);
				
				mPlayer = new MediaPlayer();
				AssetFileDescriptor afd;
				try {
					afd = getAssets().openFd("glass_break.wav");
					mPlayer.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(), afd.getLength());
					mPlayer.prepare();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				mPlayer.setOnCompletionListener(this);
				mStopPlaying = false;
			}
		} else {
			//mHandler.sendEmptyMessageDelayed(EFFECT_TV_OFF, 3000);// just for debug --- to show TV off effect
		}
		
		
	}

	

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
	}



	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			switch(msg.what) {
/*			case 0:
				mImage.zoomTo(0, 1000);
				sendEmptyMessageDelayed(1, 1000);
				break;
			case 1:
				finish();
				break;*/
			case EFFECT_IMAGE_CRACK:
				if(setMaskImage()) {
					mHandler.sendEmptyMessageDelayed(EFFECT_IMAGE_CRACK, 350);
				} else {
					mPlayer.stop();
					mStopPlaying = true;
					finish();
				}
				break;
			case EFFECT_TV_OFF:

				mImage.startAnimation(getTVAnimation());
				break;
			}
		}
	};
	
	private Animation getTVAnimation() {
		Animation anim = new TVOffAnimation(800);
		anim.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				finish();
			}
		});
		return anim;
	}
	long remainSecInView;
    Runnable runnable = new Runnable() {   
  
        @Override  
        public void run() {   
            try {
				long utc = Calendar.getInstance().getTimeInMillis();

				remainSecInView = remain - (utc - mUtc) / 1000;
				if (remainSecInView >= 0) {
					mHandler.postDelayed(this, 200);	
					if (remainSecInView <= 60) {
						TextView text = (TextView) findViewById(R.id.text);
						text.setVisibility(View.VISIBLE);
						text.setText("" + remainSecInView + " sec");
					}
					
				} else {
					startPlay();
					mHandler.sendEmptyMessageDelayed(EFFECT_IMAGE_CRACK, 100);
/*					if(setMaskImage()) {
						startPlay();
						mHandler.postDelayed(this, 350);
					} else {
						mPlayer.stop();
						finish();
					}*/
					
				}
            } catch (Exception e) {      
                e.printStackTrace();   
            }   
        }
    }; 
	

	int count = 0;
	private boolean setMaskImage() {
		if(count > 4) {
			return false;
		}
		
		if(count == 0 ) {
			mMaskImage.setImageResource(R.drawable.crack0);
		} else if(count == 1) {
			mMaskImage.setImageResource(R.drawable.crack1);
		} else if(count == 2) {
			mMaskImage.setImageResource(R.drawable.crack2);
		} else if(count == 3) {
			mMaskImage.setImageResource(R.drawable.crack3);
		} else if(count == 4) {
			mMaskImage.setImageResource(R.drawable.crack4);
		}
		
		count++;
		return true;
	}
	

	@Override
	protected void onDestroy() {
		

		//xls add: directly delete file when time is up.
		if (remainSecInView <= 0 && mValue != -1) {
			File deletefile = new File(mPath);
			deletefile.delete();
			new File(Utils.getPrivateThumbFileByFilePath(mPath))
					.delete();
			PrivateGalleryProvider.getInstance().deleteFile(mPath);
			long momentOfDelete = Calendar.getInstance().getTimeInMillis();
			if (!deletefile.exists()) {				
				MsgProviderSingleton.getInstance().updateRecordBoth(mPath, momentOfDelete, MsgDefine.STATUS_HAS_BEEN_DESTROIED, this);
			}	
		}
				
		
		if (mBitmap != null && !mBitmap.isRecycled()) {
			mBitmap.recycle();
			System.gc();
		}
		if(mPlayer != null) {
			mPlayer.release();
			mPlayer = null;
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

	private void startPlay() {
		if(!mStopPlaying) {
			mPlayer.start();
		}
	}
	@Override
	public void onCompletion(MediaPlayer mp) {
		if(!mStopPlaying){
			mPlayer.start(); 
		}
		
	}
}
