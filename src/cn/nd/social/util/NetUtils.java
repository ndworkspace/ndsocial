package cn.nd.social.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetUtils {
	
	//判断网络是否可用
	public static boolean checkNetWorkStatus(Context context) 
    {
        boolean result;
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo = cm.getActiveNetworkInfo();
        if (netinfo != null && netinfo.isConnected()) {
            result = true;
            Log.i("NetStatus", "The net was connected");
        } else {
            result = false;
            Log.i("NetStatus", "The net was bad!");
        }
        return result;
    }
	
}
