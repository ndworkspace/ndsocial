package cn.nd.social.common;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import cn.nd.social.R;

public class Feedback extends Activity {
	
	private EditText mContent;
	private View mSendBtn;
	private View mBackBtn;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feedback);
		setupViews();
		registerEvent();
	}


	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void setupViews() {
		mContent = (EditText)findViewById(R.id.content);
		mSendBtn = findViewById(R.id.tl_right_btn);
		mBackBtn = findViewById(R.id.back_btn);
	}

	
	private void registerEvent() {
		mSendBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {				
				String text = mContent.getText().toString();
				if(text.equals("")) {
					Toast.makeText(getApplicationContext(), 
							R.string.send_content_empty_hint, Toast.LENGTH_SHORT).show();
				} else {
					sendFeedback(text);
				}
			}
		});
		
		mBackBtn.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				finish();				
			}
		});
		
	}
	
	private void sendFeedback(String text) {
		
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}
}