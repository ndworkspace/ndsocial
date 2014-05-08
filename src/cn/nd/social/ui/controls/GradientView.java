package cn.nd.social.ui.controls;

import java.io.InputStream;

import cn.nd.social.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

public class GradientView extends View {
    private String TAG = "GradientView";
    private Handler mTimeTickHandler;
    private int mIndex = 190;
    private Shader mShader;
    private Bitmap mBitmapBg;
    private Bitmap mBitmapWord;
    private int mMinWidth = 190;
    private int mMaxWidth ;
    private int mUpdateStep = 20;
    private Paint mPaint = new Paint();
    
    private int mStrResId;
    private int mBgResId;
    private Context mContext;
    public GradientView(Context context) {
        super(context);
        mContext = context;
    }
    
    public GradientView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTimeTickHandler = new Handler();                        
        setFocusable(true);
        
    }
    
    
    public void setRes(int strRes,int bgResId) {
    	mStrResId = strRes;
    	mBgResId = bgResId;
    	InputStream is = mContext.getResources().openRawResource(bgResId);
        mBitmapBg = BitmapFactory.decodeStream(is);    
        mMaxWidth = mBitmapBg.getWidth();
        mBitmapWord = Bitmap.createBitmap(mBitmapBg.getWidth(), mBitmapBg.getHeight(), Bitmap.Config.ALPHA_8);            
        drawIntoBitmap(mBitmapWord, mContext);
        mTimeTickHandler.post(mTimeTickRunnable);
    }
    
    private void drawIntoBitmap(Bitmap bm, Context context) {
        float x = bm.getWidth();
        float y = bm.getHeight();
        Canvas c = new Canvas(bm);
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setColor(Color.WHITE);
        p.setTextSize(45);            
        p.setTextAlign(Paint.Align.CENTER);
        c.drawText(context.getResources().getString(mBgResId), x/2+10, y/2, p);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {

        mPaint.setShader(mShader);
        canvas.drawBitmap(mBitmapWord, 50, 13, mPaint);
    }
    
    public void removeHandlerGradient(){
        mTimeTickHandler.removeCallbacks(mTimeTickRunnable);        
    }
    
    private  Runnable mTimeTickRunnable = new Runnable(){

        public void run() {
                            
            mIndex += mUpdateStep;
            if(mIndex >= mMaxWidth){
                
                mIndex = mMinWidth;
            }            
            mShader = new LinearGradient(0, 150, mIndex, 150, new int[] { Color.GRAY,Color.GRAY,Color.GRAY, Color.GRAY,
                    Color.WHITE }, null, Shader.TileMode.MIRROR);
            
            postInvalidate();
            mTimeTickHandler.postDelayed(mTimeTickRunnable, 100);
        }        
    };
}
