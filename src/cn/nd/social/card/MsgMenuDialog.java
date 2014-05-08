package cn.nd.social.card;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import cn.nd.social.R;
public class MsgMenuDialog extends Activity {
	//private MyDialog dialog;

	private View mAddToContact;
	private int mItemSelected = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.msg_menu_dialog);

		mAddToContact = findViewById(R.id.add_to_contact);
		mAddToContact.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				mItemSelected = 1;
				returnTarget();
			}
		});
		
	}

	public void returnTarget() {
		Intent returnIntent = new Intent();
		returnIntent.putExtra("item_selected", mItemSelected);
		setResult(RESULT_OK, returnIntent);
		finish();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event){
		finish();
		return true;
	}
}
