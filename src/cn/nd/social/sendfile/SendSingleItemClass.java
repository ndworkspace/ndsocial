package cn.nd.social.sendfile;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.Toast;
import cn.nd.social.R;
import cn.nd.social.hotspot.MsgDefine;
import cn.nd.social.net.NetworkProtocolImpl;
import cn.nd.social.prishare.PriShareSendActivity;
import cn.nd.social.prishare.items.CellItemBase;
import cn.nd.social.util.Utils;

public class SendSingleItemClass {

	private String userName;
	private String filePath;
	private String appName;
	private int type;
	private ArrayList<String> mConnectedUser;
	private int mTimeToExpire;
	NetworkProtocolImpl mNetProtocol;
	private int appInstall = 0;
	
	public SendSingleItemClass(ArrayList<String> cu, int t, int tte) {
		this.mConnectedUser = cu;
		this.type = t;
		this.mTimeToExpire = tte;      
	}
	
	public void sendFileForType(){
		switch (type) {
		case CellItemBase.IMAGE_TYPE: {
			if(mTimeToExpire == 0) {
				sendFile(userName, filePath, appName,
						MsgDefine.FILE_TYPE_IMAGE);
			} else {
				sendFile(
						userName,
						filePath,
						appName,
						MsgDefine.FILE_TYPE_IMAGE,
						MsgDefine.GRANT_FILE_AUTO_DESTROY,
						mTimeToExpire, 0);
			}
			return;
		}

		case CellItemBase.APP_TYPE:
			// APP type need help install
			
			if (appInstall == 0) {
				sendFile(userName, filePath, appName,
						MsgDefine.FILE_TYPE_APP);
			}else{
				sendFile(userName, filePath, appName,
						MsgDefine.FILE_TYPE_APP,
						MsgDefine.GRANT_APK_SILENT_INSTALL,
						0, 0);
			}		
			break;

		case CellItemBase.VIDEO_TYPE:
			sendFile(userName, filePath, appName, MsgDefine.FILE_TYPE_MEDIA);
			break;
			
		 

		default:
			sendFile(userName, filePath, appName, MsgDefine.FILE_TYPE_UNKNOWN);
			break;
		}
	}
	
	
	void sendFile(String userName, String fileName, String appName, int fileType) {
		if (mConnectedUser.size() < 1) {
			Toast.makeText(Utils.getAppContext(), "need user login at first", Toast.LENGTH_LONG)
					.show();
			return;
		}
		mNetProtocol.sendFile(userName, fileName, appName, fileType);
	}

	void sendFile(String userName, String fileName, String appName,
			int fileType, int grantType, int grantValue, int grantReserve) {
		if (mConnectedUser.size() < 1) {
			Toast.makeText(Utils.getAppContext(), "need user login at first", Toast.LENGTH_LONG)
					.show();
			return;
		}
		mNetProtocol.sendFile(userName, fileName, appName, fileType, grantType,
				grantValue, grantReserve);
	}
}
