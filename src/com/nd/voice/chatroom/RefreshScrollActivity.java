package com.nd.voice.chatroom;

import cn.nd.social.R;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

/**
 * 
 * @author Nono
 *
 */
public class RefreshScrollActivity extends Activity implements
RefreshableView.RefreshListener {
    /** Called when the activity is first created. */
	private RefreshableView mRefreshableView;
	private Context mContext;
	

	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.room_history);
        mContext = this;
        initView();
    }
    
    
	private void initView() {
		// TODO Auto-generated method stub
		mRefreshableView = (RefreshableView) findViewById(R.id.refresh_root);
		initData();
	}
	
	private void initData() {
		mRefreshableView.setRefreshListener(this);
		
	}
	
	
	
	Handler mRefreshHandler = new Handler() {
		public void handleMessage(Message message) {
			super.handleMessage(message);
			mRefreshableView.finishRefresh();
			Toast.makeText(mContext, R.string.toast_text, Toast.LENGTH_SHORT).show();
		};
	};
	
	
	//实现刷新RefreshListener 中方法
	public void onRefresh(RefreshableView view) {
		//伪处理
		mRefreshHandler.sendEmptyMessageDelayed(1, 2000);
		
	}
}