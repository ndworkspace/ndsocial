package cn.nd.social.services;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import cn.nd.social.SocialApplication;
import cn.nd.social.data.MsgDBHelper;
import cn.nd.social.data.MsgProviderSingleton;
import cn.nd.social.hotspot.MsgDefine;
import cn.nd.social.privategallery.PrivateGalleryProvider;
import cn.nd.social.privategallery.Utils;
import cn.nd.social.services.ISocialService.FileControlPara;

import com.nd.voice.VoiceEndpoint;

public class SocialService extends Service {
	private HandlerThread mHandlerThread;
	private ControlHandler mControlHandler;

	private final static int EVENT_CYCLIC_CHECK = 1001;
	private final static int EVENT_ADD_CONTROLING_TO_DB = 1002;

	public final static int TYPE_ADD_FILE_CONTROL = 10;

	ServiceBinder mServicebinder;

	// ///////////////////////////////////////////////////////////////////////////
	public class ServiceBinder extends Binder implements ISocialService {
		@Override
		public void StartVoice() {
			VoiceEndpoint.instance();
		}

		public void AddFileControl(FileControlPara para) {
			Message msg = mControlHandler.obtainMessage();
			msg.what = EVENT_ADD_CONTROLING_TO_DB;
			msg.obj = para;
			mControlHandler.sendMessage(msg);
		}
	}

	// /////////////////////////////////////////////////////////////////////////////////////
	private class ControlHandler extends Handler {
		public ControlHandler(Looper loop) {
			super(loop);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case EVENT_CYCLIC_CHECK:
				// check if files is time out
//				Log.e("time is up", "warning");
//				processFileTimeout();
				deleteFileTimeUp();
				// 提醒有效时间
				// notifyFileValidTime();

				// check every 5 sec
				Message msg1 = mControlHandler.obtainMessage();
				msg1.what = EVENT_CYCLIC_CHECK;
				mControlHandler.sendMessageDelayed(msg1, 5000);

				break;
			case EVENT_ADD_CONTROLING_TO_DB:
				// 增加控制文件到数据库
				FileControlProvider fileControl = FileControlProvider
						.getInstance();
				FileControlPara fileCtrl = (FileControlPara) msg.obj;
				fileControl.addRecord(fileCtrl);

				break;
			default:
				break;
			}

		}

		//xls add: delete file, update DB regularly
		private void deleteFileTimeUp() {
			// get timeout file
			MsgProviderSingleton msgHistoryControl = MsgProviderSingleton.getInstance();
			Cursor cursorGroup = msgHistoryControl.getTimeoutFile();

			if (cursorGroup == null) {
				return;
			}

			if (cursorGroup.getCount() == 0) {
				cursorGroup.close();
				return;
			}
			int newNameColumn = cursorGroup.getColumnIndex(MsgDBHelper.MSG_LIST_NEW_NAME);
			int pathColumn = cursorGroup
					.getColumnIndex(MsgDBHelper.MSG_LIST_FILEPATH);
			ArrayList<CursorItem> recordList = new ArrayList<CursorItem>();
			for (cursorGroup.moveToFirst(); !cursorGroup.isAfterLast(); cursorGroup
					.moveToNext()) {
				CursorItem rec = new CursorItem();
				rec.newName = cursorGroup.getString(newNameColumn);
				rec.path = cursorGroup.getString(pathColumn);
				recordList.add(rec);
			}
			cursorGroup.close();

			// 删除文件
			for (CursorItem item : recordList) {

				String fullPath = item.path + File.separator + item.newName;
				File deletefile = new File(fullPath);
				deletefile.delete();
				
				new File(Utils.getPrivateThumbFileByFilePath(fullPath))
						.delete();
				PrivateGalleryProvider.getInstance().deleteFile(fullPath);
				if (!deletefile.exists()) {
					
					long strTime = Calendar.getInstance().getTimeInMillis();
					MsgProviderSingleton.getInstance().updateRecordBoth(item.newName, strTime, 
							MsgDefine.STATUS_HAS_BEEN_DESTROIED,getApplicationContext());
					
				}

				// just for now, notify user with toast
				SocialApplication.getAppInstance().sendToastMessage("private file " 
						+ new File(fullPath).getName() + "has been deleted");
				Log.e("DELETE FILE", fullPath);
				// TODO: add_delete_record
			}

			// send notify info
		}

		private void processFileTimeout() {
			// get timeout file
			FileControlProvider fileControl = FileControlProvider.getInstance();
			Cursor record = fileControl.getTimeoutFile();

			if (record == null) {
				return;
			}

			if (record.getCount() == 0) {
				record.close();
				return;
			}
			int idColumn = record.getColumnIndex(MsgDBHelper.COLUMN_FC_FILEID);
			int fileColumn = record
					.getColumnIndex(MsgDBHelper.COLUMN_FC_FILENAME);
			ArrayList<RecordItem> recordList = new ArrayList<RecordItem>();
			for (record.moveToFirst(); !record.isAfterLast(); record
					.moveToNext()) {
				RecordItem rec = new RecordItem();
				rec.fileName = record.getString(fileColumn);
				rec.id = record.getLong(idColumn);
				recordList.add(rec);
			}
			record.close();

			// 删除文件
			for (RecordItem item : recordList) {

				File deletefile = new File(item.fileName);
				deletefile.delete();
				new File(Utils.getPrivateThumbFileByFilePath(item.fileName))
						.delete();
				PrivateGalleryProvider.getInstance().deleteFile(item.fileName);
				// xls add
				if (!deletefile.exists()) {
					MsgProviderSingleton.getInstance().updateRecord(
							item.fileName, MsgDefine.STATUS_HAS_BEEN_DESTROIED);
				}
				fileControl.deleteRecord(item.id);

				// just for now, notify user with toast
				SocialApplication.getAppInstance().sendToastMessage("private file " 
						+ new File(item.fileName).getName() + "has been deleted");
				Log.d("DELETE FILE", item.fileName);
				// TODO: add_delete_record
			}

			// send notify info
		}

		// 提醒有效时间
		private void notifyFileValidTime() {
			// 查询数据库，棄1�7查所有有效文仄1�7
			FileControlProvider fileControl = FileControlProvider.getInstance();
			Cursor record = fileControl.getValidFile();
			record.getColumnIndex(MsgDBHelper.COLUMN_FC_FILEID);
			record.getColumnIndex(MsgDBHelper.COLUMN_FC_FILENAME);
			record.getColumnIndex(MsgDBHelper.COLUMN_FC_CREATE_TIME);
			record.getColumnIndex(MsgDBHelper.COLUMN_FC_EXPIRE_TIME);
			record.getColumnIndex(MsgDBHelper.COLUMN_FC_STATE);

			// 向上层发送文件有效时闄1�7
		}

	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.i("TAG", "binded");
		return mServicebinder;
	}

	@Override
	public void onCreate() {
		Log.i("TAG", "Services onCreate");
		mHandlerThread = new HandlerThread("SocialHandler");
		mHandlerThread.start();
		mControlHandler = new ControlHandler(mHandlerThread.getLooper());

		Message msg = mControlHandler.obtainMessage();
		msg.what = EVENT_CYCLIC_CHECK;
		mControlHandler.sendMessageDelayed(msg, 5000); // check every 5 secs

		mServicebinder = new ServiceBinder();

		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return 0;
	}

	public static class RecordItem {
		String fileName;
		long id;
	}
	
	public static class CursorItem {
		String newName;
		String path;
	}

}
