package cn.nd.social.ui.controls;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.TextView;
import cn.nd.social.R;

public class TextGradientView extends TextView {
    private String TAG = "TextGradientView";
    private Handler mTimeTickHandler;
    private int mIndex = 20;
    private Shader mShader;
    private int mMinWidth = 0;
    private int mMaxWidth ;
    private int mMaxHeight;
    private int mUpdateStep = 15;
  
    

    private Context mContext;
    public TextGradientView(Context context) {
        super(context);
        mContext = context;
    }
    
    public TextGradientView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mTimeTickHandler = new Handler();                        
        setFocusable(true);
        mTimeTickHandler.post(mTimeTickRunnable);
        color = getResources().getColor(R.color.light_red);
    }
  
    
    
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		mMaxWidth = MeasureSpec.getSize(widthMeasureSpec);
		mMaxHeight = MeasureSpec.getSize(heightMeasureSpec);
	}
	
    @Override
    protected void onDraw(Canvas canvas) {

    	if(mShader != null) {
    		getPaint().setShader(mShader);
    	}    	
        super.onDraw(canvas);
    }
    
    public void removeHandlerGradient(){
        mTimeTickHandler.removeCallbacks(mTimeTickRunnable);        
    }
    
    private int color = 0x60ff0000;
    
    private boolean leftToRight = true;
    private boolean hitEdge = false;
    private  Runnable mTimeTickRunnable = new Runnable(){

        public void run() {                            
        	if(leftToRight) {
        		mIndex += mUpdateStep;
        	} else {
        		mIndex -=mUpdateStep;
        	}
            if(mIndex >= mMaxWidth){
            	leftToRight = false;
            	hitEdge = true;
            } else if(mIndex <= mMinWidth){
            	leftToRight = true;
            	hitEdge = true;
            } else {
            	hitEdge = false;
            }

            mShader = new LinearGradient(-40, 100, mIndex, 100, new int[] { color,
                    Color.RED }, null, Shader.TileMode.CLAMP);
            
            postInvalidate();
            if(hitEdge) {
            	mTimeTickHandler.postDelayed(mTimeTickRunnable, 300);
            } else {
            	mTimeTickHandler.postDelayed(mTimeTickRunnable, 150);
            }
            
        }        
    };
}
