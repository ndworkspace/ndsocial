package com.thirdparty.barcode.core;

import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import cn.nd.social.R;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.EncodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ResultParser;
import com.google.zxing.common.HybridBinarizer;
import com.thirdparty.barcode.camera.CameraManager;
import com.thirdparty.barcode.executor.ResultHandler;

public final class CaptureActivity extends Activity implements
		SurfaceHolder.Callback {

	private static final String TAG = CaptureActivity.class.getSimpleName();
	private CameraManager cameraManager;
	private CaptureActivityHandler handler;
	private ViewfinderView viewfinderView;
	private TextView statusView;
	private Result lastResult;
	private boolean hasSurface;
	private IntentSource source;
	private Collection<BarcodeFormat> decodeFormats;
	private String characterSet;
	private InactivityTimer inactivityTimer;
	
	private ImageView mBackToUltraSound;
	private ImageView mBackBtn;

	// private Button from_gallery;
	private final int from_photo = 010;
	static final int PARSE_BARCODE_SUC = 3035;
	static final int PARSE_BARCODE_FAIL = 3036;
	String photoPath;
	ProgressDialog mProgress;

	public static CaptureActivity mCapture;
	private Bundle bundleInput;

	// Dialog dialog;

	enum IntentSource {

		ZXING_LINK, NONE

	}

	Handler barHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case PARSE_BARCODE_SUC:
				break;
			case PARSE_BARCODE_FAIL:
				// showDialog((String) msg.obj);
				if (mProgress != null && mProgress.isShowing()) {
					mProgress.dismiss();
				}
				new AlertDialog.Builder(CaptureActivity.this)
						.setTitle("Warning")
						.setMessage("Scan Failed!")
						.setPositiveButton("Sure",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
									}
								}).show();
				break;
			}
			super.handleMessage(msg);
		}

	};

	ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	CameraManager getCameraManager() {
		return cameraManager;
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		// Window window = getWindow();
		// window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.capture);

		mCapture = this;

		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);

		cameraManager = new CameraManager(getApplication());

		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
		viewfinderView.setCameraManager(cameraManager);
		statusView = (TextView) findViewById(R.id.status_view);
		
		
		mBackBtn = (ImageView)findViewById(R.id.back_btn);
		mBackToUltraSound = (ImageView) findViewById(R.id.capture_switch_to_sound);
		setupListener();
		
	}
	
	private void setupListener(){

		mBackToUltraSound.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				inactivityTimer.shutdown();
				finish();
			}
		});
		mBackBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				inactivityTimer.shutdown();
				finish();
			}
		});
	}


	public String parsLocalPic(String path) {
		String parseOk = null;
		Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
		hints.put(EncodeHintType.CHARACTER_SET, "UTF8");

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true; // get the original one size
		Bitmap bitmap = BitmapFactory.decodeFile(path, options);
		options.inJustDecodeBounds = false; // get the new one size
		// ��ű�
		int be = (int) (options.outHeight / (float) 200);
		if (be <= 0)
			be = 1;
		options.inSampleSize = be;
		bitmap = BitmapFactory.decodeFile(path, options);
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		System.out.println(w + "   " + h);
		RGBLuminanceSource source = new RGBLuminanceSource(bitmap);
		BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
		QRCodeReader reader2 = new QRCodeReader();
		Result result;
		try {
			result = reader2.decode(bitmap1, hints);
			android.util.Log.i("steven", "result:" + result);
			parseOk = result.getText();

		} catch (NotFoundException e) {
			parseOk = null;
		} catch (ChecksumException e) {
			parseOk = null;
		} catch (FormatException e) {
			parseOk = null;
		}
		return parseOk;
	}

	/**
	 *  play Beep
	 */
	private boolean playBeep;
	private boolean vibrate;
	private MediaPlayer mediaPlayer;
	private static final float BEEP_VOLUME = 0.10f;
	
	private void initBeepSound() {
		if (playBeep && mediaPlayer == null) {
			// The volume on STREAM_SYSTEM is not adjustable, and users found it
			// too loud,
			// so we now play on the music stream.
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setOnCompletionListener(beepListener);

			AssetFileDescriptor file = getResources().openRawResourceFd(
					R.raw.beep);
			try {
				mediaPlayer.setDataSource(file.getFileDescriptor(),
						file.getStartOffset(), file.getLength());
				file.close();
				mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
				mediaPlayer.prepare();
			} catch (IOException e) {
				mediaPlayer = null;
			}
		}
	}
	
	/**
	 * When the beep has finished playing, rewind to queue up another one.
	 */
	private final OnCompletionListener beepListener = new OnCompletionListener() {
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.seekTo(0);
		}
	};
	
	private static final long VIBRATE_DURATION = 200L;

	private void playBeepSoundAndVibrate() {
		if (playBeep && mediaPlayer != null) {
			mediaPlayer.start();
		}
		if (vibrate) {
			Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			vibrator.vibrate(VIBRATE_DURATION);
		}
	}
	
	
	//////////////////////////////////////
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		android.util.Log.i("steven", "data.getData()" + data);
		if (data != null) {
			mProgress = new ProgressDialog(CaptureActivity.this);
			mProgress.setMessage("Scaning...");
			mProgress.setCancelable(false);
			mProgress.show();
			final ContentResolver resolver = getContentResolver();
			if (requestCode == from_photo) {
				if (resultCode == RESULT_OK) {
					Cursor cursor = getContentResolver().query(data.getData(),
							null, null, null, null);
					if (cursor.moveToFirst()) {
						photoPath = cursor.getString(cursor
								.getColumnIndex(MediaStore.Images.Media.DATA));
					}
					cursor.close();
					new Thread(new Runnable() {
						@Override
						public void run() {
							Looper.prepare();
							String result = parsLocalPic(photoPath);
							if (result != null) {
								Message m = Message.obtain();
								m.what = PARSE_BARCODE_SUC;
								m.obj = result;
								barHandler.sendMessage(m);
							} else {
								Message m = Message.obtain();
								m.what = PARSE_BARCODE_FAIL;
								m.obj = "Scan Failed!";
								barHandler.sendMessage(m);
							}
							Looper.loop();
						}
					}).start();
				}

			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		handler = null;
		lastResult = null;
		resetStatusView();
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			initCamera(surfaceHolder);
		} else {
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		inactivityTimer.onResume();
		source = IntentSource.NONE;
		decodeFormats = null;
		
		
		//add for play beep
		playBeep = true;
		AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
		if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
			playBeep = false;
		}
		initBeepSound();
		vibrate = true;
	}

	@Override
	protected void onPause() {
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		inactivityTimer.onPause();
		cameraManager.closeDriver();
		if (!hasSurface) {
			SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
			SurfaceHolder surfaceHolder = surfaceView.getHolder();
			surfaceHolder.removeCallback(this);
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		inactivityTimer.shutdown();
		if (mProgress != null) {
			mProgress.dismiss();
		}
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if ((source == IntentSource.NONE || source == IntentSource.ZXING_LINK)
					&& lastResult != null) {
				restartPreviewAfterDelay(0L);
				return true;
			}
			break;
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			cameraManager.setTorch(false);
			return true;
		case KeyEvent.KEYCODE_VOLUME_UP:
			cameraManager.setTorch(true);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	// initial UI, use Camera
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (holder == null) {
			Log.e(TAG,
					"*** WARNING *** surfaceCreated() gave us a null surface!");
		}
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	private static ParsedResult parseResult(Result rawResult) {
		return ResultParser.parseResult(rawResult);
	}

	// analysis QRcode
	public void handleDecode(Result rawResult, Bitmap barcode) {
		inactivityTimer.onActivity();
		
		playBeepSoundAndVibrate();
		
		lastResult = rawResult;

		ResultHandler resultHandler = new ResultHandler(parseResult(rawResult));

		boolean fromLiveScan = barcode != null;
		if (barcode == null) {
			android.util.Log.i("steven",
					"rawResult.getBarcodeFormat().toString():"
							+ rawResult.getBarcodeFormat().toString());
			android.util.Log.i("steven", "resultHandler.getType().toString():"
					+ resultHandler.getType().toString());
			android.util.Log.i("steven", "resultHandler.getDisplayContents():"
					+ resultHandler.getDisplayContents());
			Toast.makeText(CaptureActivity.this, "Scan failed!", Toast.LENGTH_SHORT).show();
		} else {
			String str = resultHandler.getDisplayContents().toString();
			
			Intent resultIntent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putString("result", str);
			resultIntent.putExtras(bundle);
			this.setResult(RESULT_OK, resultIntent);
		}
		
		CaptureActivity.this.finish();
	}

	// initialization camera, CaptureActivityHandler decoding
	private void initCamera(SurfaceHolder surfaceHolder) {
		if (surfaceHolder == null) {
			throw new IllegalStateException("No SurfaceHolder provided");
		}
		if (cameraManager.isOpen()) {
			Log.w(TAG,
					"initCamera() while already open -- late SurfaceView callback?");
			return;
		}
		try {
			cameraManager.openDriver(surfaceHolder);
			if (handler == null) {
				handler = new CaptureActivityHandler(this, decodeFormats,
						characterSet, cameraManager);
			}
		} catch (IOException ioe) {
			Log.w(TAG, ioe);
			displayFrameworkBugMessageAndExit();
		} catch (RuntimeException e) {
			Log.w(TAG, "Unexpected error initializing camera", e);
			displayFrameworkBugMessageAndExit();
		}
	}

	private void displayFrameworkBugMessageAndExit() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.app_name));
		builder.setMessage(getString(R.string.msg_camera_framework_bug));
		builder.setPositiveButton(R.string.confirm, new FinishListener(this));
		builder.setOnCancelListener(new FinishListener(this));
		builder.show();
	}

	public void restartPreviewAfterDelay(long delayMS) {
		if (handler != null) {
			handler.sendEmptyMessageDelayed(R.id.restart_preview, delayMS);
		}
		resetStatusView();
	}

	private void resetStatusView() {
		statusView.setText(R.string.msg_default_status);
		statusView.setVisibility(View.VISIBLE);
		viewfinderView.setVisibility(View.VISIBLE);
		lastResult = null;
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();
	}

}
