package cn.nd.social.prishare.component;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.nd.dragdrop.DragSource;
import cn.nd.dragdrop.DragView;
import cn.nd.dragdrop.DropTarget;
import cn.nd.social.R;
import cn.nd.social.common.VibratorController;
import cn.nd.social.hotspot.MsgDefine;
import cn.nd.social.hotspot.Utils;

public class UserHead extends LinearLayout implements DropTarget,
		View.OnClickListener, View.OnLongClickListener {

	private final static String TAG = "UserHead";

	private Context mContext;

	private Handler mParentHandler = null;

	private String mUserName;

	public UserHead(Context context) {
		super(context);

		mContext = context;

		setOnClickListener(this);

		setOnLongClickListener(this);
	}

	public UserHead(Context context, AttributeSet attrs) {
		super(context, attrs);

		mContext = context;

		setOnClickListener(this);

		setOnLongClickListener(this);
	}

	public UserHead(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		mContext = context;

		setOnClickListener(this);

		setOnLongClickListener(this);
	}

	public void setHandler(Handler handler) {
		mParentHandler = handler;
	}

	@Override
	public void onDrop(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {

		// fire up a transfer command
	}

	public void onClick(View paramView) {
		Log.e(TAG, "user head click");
		// try {
		// showActions();
		// label4: setOnShock();
		// return;
		// } catch (Exception localException) {
		// break label4;
		// }
	}

	public boolean onLongClick(View view) {
		return false;
	}

	@Override
	public void onDragEnter(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		VibratorController.getController(this.getContext()).vibrate();
		scaleImage(1.4f);
		dragView.setScale(0.55f);
	}

	@Override
	public void onDragOver(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDragExit(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		// TODO Auto-generated method stub
		scaleImage(1.0f);
		dragView.setScale(1.0f);
	}

	@Override
	public boolean acceptDrop(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		// TODO Auto-generated method stub
		return true;
	}

	ImageView image;

	private void scaleImage(float scale) {
		if (Build.VERSION.SDK_INT > 10) {
			image.setScaleX(scale);
			image.setScaleY(scale);
		}
	}

	protected void onFinishInflate() {
		super.onFinishInflate();
		image = (ImageView) findViewById(R.id.image_id);
	}

	public void setUserLabel(String name) {
		mUserName = name;
		((TextView) findViewById(R.id.user_id)).setText(name);
	}

	public String getUserName() {
		return mUserName;
	}

	@Override
	public Rect estimateDropLocation(DragSource source, int x, int y,
			int xOffset, int yOffset, DragView dragView, Object dragInfo,
			Rect recycle) {
		return null;
	}
}
