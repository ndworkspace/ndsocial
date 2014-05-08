package cn.nd.social.syncbrowsing.ui;

import java.io.Serializable;

public class SyncAction {

	public static enum SyncActionType {
		SCROLL, SCROOL_ANIM,ZOOM_ANIM,ZOOM_IN_CENTER,ZOOM_IN_CENTER_ANIM,COMMENT_MODE,UPDATE_DOC
	}
	
	public static enum DrawState {
		ENTER_DRAW_MODE,
		START_DRAW,  /**launch a draw action*/
		MOVE_DRAW,
		FINISH_DRAW, /**only finish one time draw, one can launch another draw action*/
		CANCEL_DRAW,
		EXIT_DRAW_MODE;
		
		public static DrawState fromInt(int index) {
			DrawState state = ENTER_DRAW_MODE;
			if(index ==ENTER_DRAW_MODE.ordinal()) {
				state = ENTER_DRAW_MODE;
			} else if(index ==START_DRAW.ordinal()) {
				state = START_DRAW;
			} else if(index ==MOVE_DRAW.ordinal()) {
				state = MOVE_DRAW;
			} else if(index ==FINISH_DRAW.ordinal()) {
				state = FINISH_DRAW;
			} else if(index == CANCEL_DRAW.ordinal()) {
				state = CANCEL_DRAW;
			} else if(index ==EXIT_DRAW_MODE.ordinal()) {
				state = EXIT_DRAW_MODE;
			}
			return state;
		}
	}
	
	
	@SuppressWarnings("serial")
	public abstract static class SyncActionBase implements Serializable {
		public int action = 0;
	}
	
	/**
	 * transfer the action through network;
	 * update document
	 * */
	@SuppressWarnings("serial")
	public static class UpdateDocAction extends SyncActionBase {
		public int pageCount = 0;
		public int currPage = 0;
	}
	
	/**
	 * transfer the action through network;
	 * scroll and zoom action
	 * */
	@SuppressWarnings("serial")
	public static class PageTransAction extends SyncActionBase {
		public float zoomScale = 1.0f;
		public float distanceX = 0;
		public float distanceY = 0;
		public float centerX = 0;
		public float centerY = 0;
		public int timeMills = 0;
	}
	
	/**
	 * draw action in comment-mode
	 * transfer the action through network;	 
	 * */
	@SuppressWarnings("serial")
	public static class PageDrawAction extends SyncActionBase {
		public float x1 = 0;
		public float y1 = 0;
		public int state = 0;		
	}
	
	
	public static UpdateDocAction getUpdateDocAction(int pageCount,int currPage) {
		UpdateDocAction updateAct = new UpdateDocAction();
		updateAct.action = SyncActionType.UPDATE_DOC.ordinal();
		updateAct.pageCount = pageCount;
		updateAct.currPage = currPage;
		return updateAct;
	}
	
	public static PageDrawAction getDrawAction(DrawState state,float x1,float y1) {
		PageDrawAction drawAct = new PageDrawAction();
		drawAct.action = SyncActionType.COMMENT_MODE.ordinal();
		drawAct.state = state.ordinal();
		drawAct.x1 = x1;
		drawAct.y1 = y1;
		return drawAct;
	}
	
	
	public static PageTransAction getScrollAction(float distanceX, float distanceY,int timeInMills) {
		PageTransAction transAct = new PageTransAction();
		if(timeInMills == 0) {
			transAct.action = SyncActionType.SCROLL.ordinal();
		} else {
			transAct.action = SyncActionType.SCROOL_ANIM.ordinal();
			transAct.timeMills = timeInMills;
		}
		transAct.distanceX = distanceX;
		transAct.distanceY = distanceY;
		return transAct;
	}
	
	public static PageTransAction getZoomAction(float targetScale,int timeInMills) {
		PageTransAction transAct = new PageTransAction();
		transAct.action = SyncActionType.ZOOM_ANIM.ordinal();
		transAct.zoomScale = targetScale;
		transAct.timeMills = timeInMills;		
		return transAct;
	}
	
	public static PageTransAction getZoomAction(float targetScale,float centerX, float centerY,int timeInMills) {
		PageTransAction transAct = new PageTransAction();
		if(timeInMills == 0) {
			transAct.action = SyncActionType.ZOOM_IN_CENTER.ordinal();
		} else {
			transAct.action = SyncActionType.ZOOM_IN_CENTER_ANIM.ordinal();
			transAct.timeMills = timeInMills;
		}
		transAct.zoomScale = targetScale;
		transAct.centerX = centerX;
		transAct.centerY = centerY;
		return transAct;
	}
	
	/**
	 * to uniform coordinate;(multiple by 1000 to avoid accuracy loss)
	 * host side
	 * */
	public static float toUniformCoor(float x,int viewSize) {
		return viewSize == 0 ? x:(x * 1000 / viewSize);
	}
	
	/**
	 * transform to local view coordinate
	 * user together with toUniformCoor
	 * client side
	 * */
	public static float toNativeCoor(float x, int viewSize) {
		return x * viewSize /  1000;
	}
	
}
