/*
 * Copyright (C) 2008 Esmertec AG.
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.nd.social.prishare.history;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import cn.nd.social.hotspot.MsgDefine;

public class HistoryItemHelper {
	private String TAG = "HistoryItemHelper";

	public static class ItemData {

		public String mSendUserName;
		public String mRecvUserName;
		public String mAppName;
		public String mFileName;

		public long mFileSize;
		
		public int mFileType;

		public int mGrantType;
		public int mGrantValue;
		public int mGrantReserve;

		public long mUtc;
		public String mDate;

		public int mProgress;

		public ItemData() {
			mFileSize = 0;

			mFileType = MsgDefine.FILE_TYPE_UNKNOWN;
			
			mGrantType = 0;
			mGrantValue = 0;
			mGrantReserve = 0;

			mProgress = -1;

			mUtc = 0;
		}
	}

	private Context mContext;
	private HistoryListAdapter mListAdapter = null;
	private Vector<ItemData> mHistoryVec = null;

	public HistoryItemHelper(Context context) {
		mContext = context;

		loadHistory();
	}

	public void setListAdapter(HistoryListAdapter listAdapter) {
		mListAdapter = listAdapter;
	}

	private void loadHistory() {
		mHistoryVec = new Vector<ItemData>();

		int loadCount = 0;

		Cursor cursor = HistoryDBProviderSingleton.getInstance().getAllRecord();
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			ItemData data = new ItemData();

			data.mSendUserName = String.valueOf(cursor.getString(2));
			data.mRecvUserName = String.valueOf(cursor.getString(3));
			data.mAppName = cursor.getString(4);
			data.mFileName = cursor.getString(5);

			data.mFileSize = cursor.getInt(6);
			data.mFileType = cursor.getInt(7);

			data.mGrantType = cursor.getInt(8);
			data.mGrantValue = cursor.getInt(9);
			data.mGrantReserve = cursor.getInt(10);

			data.mProgress = cursor.getInt(11);

			data.mUtc = cursor.getLong(12);
			Date date = new Date(data.mUtc);
			data.mDate = new SimpleDateFormat("yyyy-MM-dd").format(date);

			mHistoryVec.add(data);

			loadCount++;
		}

		Log.e(TAG, "loadHistory from db, count " + loadCount);
	}

	private void storeHistory() {
		// todo
	}

	private boolean mNeedRefresh = false;

	public void refreshTimer() {
		if (mListAdapter == null) {
			return;
		}

		if (mNeedRefresh) {
			long utc = Calendar.getInstance().getTimeInMillis();

			mNeedRefresh = false;

			for (int i = 0; i < mHistoryVec.size(); i++) {
				ItemData data = mHistoryVec.get(i);

				if (data.mGrantType != MsgDefine.GRANT_FILE_AUTO_DESTROY) {
					continue;
				}

				if (data.mProgress == 100 || data.mProgress == -1) {
					long distance = (utc - data.mUtc) / 1000 - 1;

					if (distance <= data.mGrantValue) {
						mNeedRefresh = true;

						mListAdapter.notifyDataSetChanged();

						break;
					}
				}
			}
		}
	}

	public int addItem(String sendName, String recvName, String appName,
			String fileName, long fileSize, int fileType) {
		ItemData data = new ItemData();

		data.mSendUserName = sendName;
		data.mRecvUserName = recvName;
		data.mAppName = appName;
		data.mFileName = fileName;

		data.mFileSize = fileSize;
		
		data.mFileType = fileType;

		data.mProgress = 0;

		data.mUtc = Calendar.getInstance().getTimeInMillis();
		Date date = new Date();
		data.mDate = new SimpleDateFormat("yyyy-MM-dd").format(date);

		mHistoryVec.add(data);
		if(mListAdapter != null) {
			mListAdapter.notifyDataSetChanged();
		}

		return mHistoryVec.size() - 1;
	}

	public int addItem(String sendName, String recvName, String appName,
			String fileName, long fileSize, int fileType, int grantType,
			int grantValue, int grantReserve) {
		ItemData data = new ItemData();

		data.mSendUserName = sendName;
		data.mRecvUserName = recvName;
		data.mAppName = appName;
		data.mFileName = fileName;

		data.mFileSize = fileSize;
		
		data.mFileType = fileType;

		data.mGrantType = grantType;
		data.mGrantValue = grantValue;
		data.mGrantReserve = grantReserve;

		data.mProgress = 0;

		data.mUtc = Calendar.getInstance().getTimeInMillis();

		Date date = new Date();
		data.mDate = new SimpleDateFormat("yyyy-MM-dd").format(date);

		mHistoryVec.add(data);
		
		//tangtaotao@NetDragon_20140212
		if(mListAdapter != null) {
			mListAdapter.notifyDataSetChanged();
		}
		
		return mHistoryVec.size() - 1;
	}

	// public void removeItem(int idx) {
	// if ((idx >= 0) && (idx < mHistoryVec.size())) {
	// mHistoryVec.remove(idx);
	// storeHistory();
	// }
	// }
	//

	
	
	public void changeItemProgress(int index, int progress) {
		if ((index < 0) || (index >= mHistoryVec.size())) {
			// todo : change into throwing exception
			return;
		}

		ItemData data = mHistoryVec.get(index);
		if ((data.mProgress == 100 || data.mProgress == -1)) {
			return;
		}

		data.mProgress = progress;

		if (progress == 100 || progress == -1) {
			data.mUtc = Calendar.getInstance().getTimeInMillis();

			HistoryDBProviderSingleton.getInstance().addRecord(data);

			if (data.mGrantType == MsgDefine.GRANT_FILE_AUTO_DESTROY) {
				mNeedRefresh = true;
			}
		}

		Log.d(TAG, "changeItem : " + index + " " + progress);
		
		//tangtaotao@NetDragon_20140212
		if(mListAdapter != null) {
			mListAdapter.notifyDataSetChanged();
		}
	}
	
	public void changeItemFileName(int index, String fileName) {
		if ((index < 0) || (index >= mHistoryVec.size())) {
			// todo : change into throwing exception
			return;
		}

		ItemData data = mHistoryVec.get(index);
		if ((data.mProgress == 100 || data.mProgress == -1)) {
			return;
		}

		data.mFileName = fileName;

		Log.d(TAG, "changeItem : " + index + " change file path to " + fileName);
		
		//tangtaotao@NetDragon_20140212
		if(mListAdapter != null) {
			mListAdapter.notifyDataSetChanged();
		}
	}

	
	
	public void changeItem(int index, ItemData data) {
		if ((index < 0) || (index >= mHistoryVec.size())) {
			// todo : change into throwing exception
			return;
		}

		ItemData oldData = mHistoryVec.get(index);
		if ((oldData.mProgress == 100 || oldData.mProgress == -1)) {
			return;
		}

		if (data.mProgress == 100 || data.mProgress == -1) {
			data.mUtc = Calendar.getInstance().getTimeInMillis();

			HistoryDBProviderSingleton.getInstance().addRecord(data);

			if (data.mGrantType == MsgDefine.GRANT_FILE_AUTO_DESTROY) {
				mNeedRefresh = true;
			}
		}
		
		mHistoryVec.setElementAt(data, index);

		Log.d(TAG, "changeItem : " + index + " " + data.mProgress);
		
		//tangtaotao@NetDragon_20140212
		if(mListAdapter != null) {
			mListAdapter.notifyDataSetChanged();
		}
	}

	public ItemData getItem(int idx) {
		if ((idx < 0) || (idx >= mHistoryVec.size())) {
			// todo : change into throwing exception
			return null;
		}

		return mHistoryVec.get(idx);
	}

	public int getItemType(int idx) {
		if ((idx < 0) || (idx >= mHistoryVec.size())) {
			// todo : change into throwing exception
			return 0;
		}

		ItemData data = mHistoryVec.get(idx);
		if (data.mSendUserName.contentEquals("me")) {
			if (data.mFileType == MsgDefine.FILE_TYPE_IMAGE) {
				return 5;
			}
			
			// send item
			return 3;
		}
		else {
			if (data.mFileType == MsgDefine.FILE_TYPE_IMAGE) {
				return 6;
			}
			
			// recv item
			return 4;
		}
	}
	
	public String getItemFileName(int idx) {
		if ((idx < 0) || (idx >= mHistoryVec.size())) {
			// todo : change into throwing exception
			return null;
		}

		ItemData data = mHistoryVec.get(idx);
		return data.mFileName;
	}

	public int getCount() {
		return mHistoryVec.size();
	}
}
