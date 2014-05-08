package cn.nd.social.prishare;



import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextView;
import cn.nd.dragdrop.DragSource;
import cn.nd.dragdrop.DragView;
import cn.nd.dragdrop.DropTarget;
import cn.nd.social.R;
import cn.nd.social.common.VibratorController;

public class DropView extends TextView implements DropTarget{
    public DropView(Context context) {
        super(context);
    }
    
    public DropView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
    }

	@Override
	public void onDrop(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
				
	}

	@Override
	public void onDragEnter(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		VibratorController.getController(this.getContext()).vibrate();
		setBackgroundResource(R.drawable.send_recv_activate);		
	}
	


	@Override
	public void onDragOver(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		
		
	}

	@Override
	public void onDragExit(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		setBackgroundDrawable(null);
	}

	@Override
	public boolean acceptDrop(DragSource source, int x, int y, int xOffset,
			int yOffset, DragView dragView, Object dragInfo) {
		return true;
	}

	@Override
	public Rect estimateDropLocation(DragSource source, int x, int y,
			int xOffset, int yOffset, DragView dragView, Object dragInfo,
			Rect recycle) {
		return null;
	}
}
