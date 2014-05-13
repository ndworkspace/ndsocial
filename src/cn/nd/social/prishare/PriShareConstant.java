package cn.nd.social.prishare;

import android.os.Environment;
import cn.nd.social.R;

public class PriShareConstant {

	// /////////////////////////////////////////////
	public final static int TAB_COUNT = 5;
	public final static int[] TAB_TITLE = { 
		R.string.qe_main_pic, 
		R.string.qe_main_audio,
		R.string.qe_main_file, 
		R.string.qe_main_app,										
		R.string.qe_main_more
	};
	
	public final static int[] TAB_TITLE_LOGO = { 
		R.drawable.pri_tab_gallery, 
		R.drawable.pri_tab_music,
		R.drawable.pri_tab_file,
		R.drawable.pri_tab_app,													 
		R.drawable.pri_tab_file 
	};
	
	public final static int GALLERY_INDEX = 0;
	public final static int AUDIO_INDEX = 1;
	public final static int APP_INDEX = 3;
	public final static int FILE_INDEX = 2;
	public final static int HISTORY_INDEX = 4;
	public final static int INFINITE_TIME = -1;
	
	public final static String ROOT_FILE_DIRECTORY = Environment.getExternalStorageDirectory().getAbsolutePath();
	
	public final static String KEY_HIDE_PRIVATE_HIDE = "show_enter_private_hint";
	
	public final static int REQ_CODE_SET_EXPIRE_TIME = 100;

}
