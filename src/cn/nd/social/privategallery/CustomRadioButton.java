package cn.nd.social.privategallery;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RadioButton;

public class CustomRadioButton extends RadioButton{


	private boolean isCanClick = true; 
	
	public CustomRadioButton(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public CustomRadioButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	public CustomRadioButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}


	@Override
	public boolean performClick() {
		// TODO Auto-generated method stub
		if (isCanClick) {
			return super.performClick();
		}
		return false;
	}
	
	public void setIsCanClick(boolean iscanclick) {  
        this.isCanClick = iscanclick;  
    }  
	

}
