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

import java.util.Calendar;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.NinePatchDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import cn.nd.social.R;
import cn.nd.social.hotspot.MsgDefine;

public class HistoryListAdapter extends BaseAdapter {
	private Context mContext;
	private HistoryItemHelper mItemHelper = null;

	HistoryListAdapter(Context cntx) {
		mContext = cntx;
	}

	public void setHistoryItemHelper(HistoryItemHelper itemHelper) {
		mItemHelper = itemHelper;
	}

	@Override
	public int getCount() {
		if (mItemHelper == null) {
			return 3;
		}

		return mItemHelper.getCount() + 3;
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		int ret = getItemViewType(position);
		switch (ret) {
		case 0:
			return getMemoryView(convertView);

		case 1:
			return getTrafficView(convertView, position);

		case 2:
			return getGroupView(convertView);

		case 3:
			return getFileSendView(convertView, position);

		case 4:
			return getFileRecvView(convertView, position);

		case 5:
			return getImageSendView(convertView, position);

		case 6:
			return getImageRecvView(convertView, position);

		default:
			return null;
		}
	}

	@Override
	public int getItemViewType(int paramInt) {
		if (paramInt < 3) {
			return paramInt;
		}

		return mItemHelper.getItemType(paramInt - 3);

		// // if (isMoreItem(paramInt))
		// // return 3;
		// if (isMemoryItem(paramInt))
		// return 5;
		// if (isTrafficItem(paramInt))
		// return 0;
		//
		// if (paramInt == 2) {
		// return 4;
		// }
		// // this.mPos = getPosition(paramInt);
		// // if (this.mPos.b < 0)
		// // return 4;
		// // moveCursorToIndex(this.mPos.b);
		// // if (getInt(this.mDirectionColumnId) == 0)
		// // return 2;
		// return 1;
	}

	@Override
	public int getViewTypeCount() { // tangtaotao add
		return 7;
	}

	// boolean isMemoryItem(int paramInt) {
	// return paramInt == 0;
	// }
	//
	// boolean isTrafficItem(int paramInt) {
	// return paramInt == 1;
	// }

	// boolean isMoreItem(int paramInt) {
	// if (this.mSelectionListener.isCheckBoxShow())
	// ;
	// do
	// return false;
	// while (paramInt != this.mMorePosition);
	//
	// return true;
	// }

	private View getMemoryView(View paramView) {
		if (paramView == null)
			paramView = LayoutInflater.from(mContext).inflate(
					R.layout.qe_history_list_memory_child, null);
		// dm_histoy_child_memory_text
		TextView localTextView = (TextView) paramView
				.findViewById(R.id.dm_histoy_child_memory_text);
		// if (a.a(this.mContext).k())
		// {
		// StatFs localStatFs = a.a(this.mContext).l();
		// if (localStatFs != null)
		// {
		// long l1 = localStatFs.getBlockSize();
		// long l2 = localStatFs.getBlockCount();
		// long l3 = localStatFs.getAvailableBlocks();
		// String str = this.mContext.getString(2131296653);
		// Object[] arrayOfObject = new Object[2];
		// arrayOfObject[0] = Formatter.formatFileSize(this.mContext, l2 *
		// l1);
		// arrayOfObject[1] = Formatter.formatFileSize(this.mContext, l3 *
		// l1);
		// localTextView.setText(spanString(String.format(str,
		// arrayOfObject), Formatter.formatFileSize(this.mContext, l1 *
		// l3)));
		// return paramView;
		// }
		// }
		// localTextView.setText(this.mContext.getString(2131296654));
		// return paramView;

		localTextView.setText("Memory : not support");
		return paramView;
	}

	private View getTrafficView(View paramView, int paramInt) {
		if (paramView == null)
			paramView = LayoutInflater.from(mContext).inflate(
					R.layout.qe_history_list_memory_child, null);
		// dm_histoy_child_memory_text
		TextView localTextView = (TextView) paramView
				.findViewById(R.id.dm_histoy_child_memory_text);

		localTextView.setText("File send : not support");
		return paramView;
	}

	private View getFileSendView(View paramView, int position) {
		HistoryItemHelper.ItemData itemData = mItemHelper.getItem(position - 3);
		if (itemData == null) {
			return null;
		}

		if (paramView == null) {
			paramView = LayoutInflater.from(mContext).inflate(
					R.layout.qe_history_list_send_child, null);

			ProgressBar localProgressBar3 = (ProgressBar) paramView
					.findViewById(R.id.history_progressbar_normal);
			ProgressBar localProgressBar4 = (ProgressBar) paramView
					.findViewById(R.id.history_progressbar_fail);
			localProgressBar3
					.setProgressDrawable(setProgressLayerDrawable(false));
			localProgressBar4
					.setProgressDrawable(setProgressLayerDrawable(true));
		}

		if (itemData.mProgress == 100 || itemData.mProgress == -1) {
			View localView2 = paramView.findViewById(R.id.history_buble);
			localView2.setBackgroundResource(R.drawable.history_send_bg);

			ProgressBar localProgressBar3 = (ProgressBar) paramView
					.findViewById(R.id.history_progressbar_normal);
			localProgressBar3.setVisibility(4);

			ProgressBar localProgressBar4 = (ProgressBar) paramView
					.findViewById(R.id.history_progressbar_fail);
			localProgressBar4.setVisibility(4);
		} else {
			View localView2 = paramView.findViewById(R.id.history_buble);
			localView2.setBackgroundResource(R.drawable.history_sending_bg);

			ProgressBar localProgressBar3 = (ProgressBar) paramView
					.findViewById(R.id.history_progressbar_normal);
			localProgressBar3.setVisibility(0);

			localProgressBar3.setProgress(itemData.mProgress);

			ProgressBar localProgressBar4 = (ProgressBar) paramView
					.findViewById(R.id.history_progressbar_fail);
			localProgressBar4.setVisibility(4);
		}

		View localView1 = paramView.findViewById(R.id.history_buble_content_1);
		localView1.findViewById(R.id.history_thumbnail_mask).setVisibility(4);

		localView1.setVisibility(0);

		if (true) {
			View localView2 = paramView
					.findViewById(R.id.history_buble_content_2);
			localView2.setVisibility(8);
		}

		paramView.setTag(localView1.findViewById(R.id.history_thumbnail));

		// appIcon
		ImageView localImageView1 = (ImageView) localView1
				.findViewById(R.id.history_thumbnail);

		Drawable appIconDrawable = mContext.getResources().getDrawable(
				R.drawable.ic_launcher);
		localImageView1.setImageDrawable(appIconDrawable);

		// recv nick name
		TextView sendTo = (TextView) localView1
				.findViewById(R.id.history_send_to);
		sendTo.setText("send to " + itemData.mRecvUserName);

		// app app name
		TextView appTitle = (TextView) localView1
				.findViewById(R.id.history_file_title);
		appTitle.setText(itemData.mAppName);

		// send nick name
		TextView title = (TextView) paramView
				.findViewById(R.id.history_opposide_nick);
		title.setText(itemData.mSendUserName);

		// file info
		TextView history_file_info = (TextView) localView1
				.findViewById(R.id.history_file_info);

		float fileSize = itemData.mFileSize / 1024;
		if (fileSize < 1024.0) {
			history_file_info.setText("" + (int) fileSize + "KB");
		} else {
			fileSize = fileSize / 1024;

			history_file_info.setText("" + (int) fileSize + "MB");
		}

		return paramView;
	}

	private View getFileRecvView(View paramView, int position) {
		HistoryItemHelper.ItemData itemData = mItemHelper.getItem(position - 3);
		if (itemData == null) {
			return null;
		}

		if (paramView == null) {
			paramView = LayoutInflater.from(mContext).inflate(
					R.layout.qe_history_list_recv_child, null);

			ProgressBar localProgressBar3 = (ProgressBar) paramView
					.findViewById(R.id.history_progressbar_normal);
			ProgressBar localProgressBar4 = (ProgressBar) paramView
					.findViewById(R.id.history_progressbar_fail);
			localProgressBar3
					.setProgressDrawable(setProgressLayerDrawable(false));
			localProgressBar4
					.setProgressDrawable(setProgressLayerDrawable(true));
		}

		if (itemData.mProgress == 100 || itemData.mProgress == -1) {
			View localView2 = paramView.findViewById(R.id.history_buble);
			localView2.setBackgroundResource(R.drawable.history_recv_bg);

			ProgressBar localProgressBar3 = (ProgressBar) paramView
					.findViewById(R.id.history_progressbar_normal);
			localProgressBar3.setVisibility(4);

			ProgressBar localProgressBar4 = (ProgressBar) paramView
					.findViewById(R.id.history_progressbar_fail);
			localProgressBar4.setVisibility(4);

			Log.e("History List", "GrantType " + itemData.mGrantType + " value"
					+ itemData.mGrantValue);
			// count down
			if (itemData.mGrantType == MsgDefine.GRANT_FILE_AUTO_DESTROY) {
				TextView history_count_down = (TextView) paramView
						.findViewById(R.id.history_count_down);

				history_count_down.setVisibility(0);

				long utc = Calendar.getInstance().getTimeInMillis();

				long remainSec = itemData.mGrantValue - (utc - itemData.mUtc)
						/ 1000;
				if (remainSec >= 0) {
					history_count_down.setText("" + remainSec + " sec");
				} else {
					history_count_down.setText("Expired");
				}
			}
		} else {
			View localView2 = paramView.findViewById(R.id.history_buble);
			localView2.setBackgroundResource(R.drawable.history_recving_bg);

			ProgressBar localProgressBar3 = (ProgressBar) paramView
					.findViewById(R.id.history_progressbar_normal);
			localProgressBar3.setVisibility(0);

			localProgressBar3.setProgress(itemData.mProgress);

			ProgressBar localProgressBar4 = (ProgressBar) paramView
					.findViewById(R.id.history_progressbar_fail);
			localProgressBar4.setVisibility(4);
		}

		View localView1 = paramView.findViewById(R.id.history_buble_content_1);
		localView1.findViewById(R.id.history_thumbnail_mask).setVisibility(4);

		localView1.setVisibility(0);

		if (true) {
			View localView2 = paramView
					.findViewById(R.id.history_buble_content_2);
			localView2.setVisibility(8);
		}

		paramView.setTag(localView1.findViewById(R.id.history_thumbnail));

		// appIcon
		ImageView localImageView1 = (ImageView) localView1
				.findViewById(R.id.history_thumbnail);

		Drawable appIconDrawable = mContext.getResources().getDrawable(
				R.drawable.ic_launcher);
		localImageView1.setImageDrawable(appIconDrawable);

		// app app name
		TextView appTitle = (TextView) localView1
				.findViewById(R.id.history_file_title);
		appTitle.setText(itemData.mAppName);

		// send nick name
		TextView title = (TextView) paramView
				.findViewById(R.id.history_opposide_nick);
		title.setText(itemData.mSendUserName);

		// file info
		TextView history_file_info = (TextView) localView1
				.findViewById(R.id.history_file_info);

		float fileSize = itemData.mFileSize / 1024;
		if (fileSize < 1024.0) {
			history_file_info.setText("" + (int) fileSize + "KB");
		} else {
			fileSize = fileSize / 1024;

			history_file_info.setText("" + (int) fileSize + "MB");
		}

		return paramView;
	}

	private View getImageSendView(View paramView, int position) {
		HistoryItemHelper.ItemData itemData = mItemHelper.getItem(position - 3);
		if (itemData == null) {
			return null;
		}

		if (paramView == null) {
			paramView = LayoutInflater.from(mContext).inflate(
					R.layout.qe_history_list_send_child, null);

			ProgressBar localProgressBar3 = (ProgressBar) paramView
					.findViewById(R.id.history_progressbar_normal);
			ProgressBar localProgressBar4 = (ProgressBar) paramView
					.findViewById(R.id.history_progressbar_fail);
			localProgressBar3
					.setProgressDrawable(setProgressLayerDrawable(false));
			localProgressBar4
					.setProgressDrawable(setProgressLayerDrawable(true));
		}

		if (itemData.mProgress == 100 || itemData.mProgress == -1) {
			View localView2 = paramView.findViewById(R.id.history_buble);
			localView2.setBackgroundResource(R.drawable.history_send_bg);

			ProgressBar localProgressBar3 = (ProgressBar) paramView
					.findViewById(R.id.history_progressbar_normal);
			localProgressBar3.setVisibility(4);

			ProgressBar localProgressBar4 = (ProgressBar) paramView
					.findViewById(R.id.history_progressbar_fail);
			localProgressBar4.setVisibility(4);
		} else {
			View localView2 = paramView.findViewById(R.id.history_buble);
			localView2.setBackgroundResource(R.drawable.history_sending_bg);

			ProgressBar localProgressBar3 = (ProgressBar) paramView
					.findViewById(R.id.history_progressbar_normal);
			localProgressBar3.setVisibility(0);

			localProgressBar3.setProgress(itemData.mProgress);

			ProgressBar localProgressBar4 = (ProgressBar) paramView
					.findViewById(R.id.history_progressbar_fail);
			localProgressBar4.setVisibility(4);
		}

		View localView2 = paramView.findViewById(R.id.history_buble_content_2);
		localView2.findViewById(R.id.history_thumbnail_mask).setVisibility(0);

		localView2.setVisibility(0);

		if (true) {
			View localView1 = paramView
					.findViewById(R.id.history_buble_content_1);
			localView1.setVisibility(8);
		}

		paramView.setTag(localView2.findViewById(R.id.history_thumbnail));

		// appIcon
		ImageView localImageView1 = (ImageView) localView2
				.findViewById(R.id.history_thumbnail);

		Drawable appIconDrawable = mContext.getResources().getDrawable(
				R.drawable.ic_launcher);
		localImageView1.setImageDrawable(appIconDrawable);

		// recv nick name
		TextView sendTo = (TextView) localView2
				.findViewById(R.id.history_send_to);
		sendTo.setText("send to " + itemData.mRecvUserName);

		// app app name
		TextView appTitle = (TextView) localView2
				.findViewById(R.id.history_file_title);
		appTitle.setText(itemData.mAppName);

		// send nick name
		TextView title = (TextView) paramView
				.findViewById(R.id.history_opposide_nick);
		title.setText(itemData.mSendUserName);

		// file info
		TextView history_file_info = (TextView) localView2
				.findViewById(R.id.history_file_info);

		float fileSize = itemData.mFileSize / 1024;
		if (fileSize < 1024.0) {
			history_file_info.setText("" + (int) fileSize + "KB");
		} else {
			fileSize = fileSize / 1024;

			history_file_info.setText("" + (int) fileSize + "MB");
		}

		return paramView;
	}

	private View getImageRecvView(View paramView, int position) {
		HistoryItemHelper.ItemData itemData = mItemHelper.getItem(position - 3);
		if (itemData == null) {
			return null;
		}

		if (paramView == null) {
			paramView = LayoutInflater.from(mContext).inflate(
					R.layout.qe_history_list_recv_child, null);

			ProgressBar localProgressBar3 = (ProgressBar) paramView
					.findViewById(R.id.history_progressbar_normal);
			ProgressBar localProgressBar4 = (ProgressBar) paramView
					.findViewById(R.id.history_progressbar_fail);
			localProgressBar3
					.setProgressDrawable(setProgressLayerDrawable(false));
			localProgressBar4
					.setProgressDrawable(setProgressLayerDrawable(true));
		}

		if (itemData.mProgress == 100 || itemData.mProgress == -1) {
			View localView2 = paramView.findViewById(R.id.history_buble);
			localView2.setBackgroundResource(R.drawable.history_recv_bg);

			ProgressBar localProgressBar3 = (ProgressBar) paramView
					.findViewById(R.id.history_progressbar_normal);
			localProgressBar3.setVisibility(4);

			ProgressBar localProgressBar4 = (ProgressBar) paramView
					.findViewById(R.id.history_progressbar_fail);
			localProgressBar4.setVisibility(4);

			Log.e("History List", "GrantType " + itemData.mGrantType + " value"
					+ itemData.mGrantValue);
			// count down
			if (itemData.mGrantType == MsgDefine.GRANT_FILE_AUTO_DESTROY) {
				TextView history_count_down = (TextView) paramView
						.findViewById(R.id.history_count_down);

				history_count_down.setVisibility(0);

				long utc = Calendar.getInstance().getTimeInMillis();

				long remainSec = itemData.mGrantValue - (utc - itemData.mUtc)
						/ 1000;
				if (remainSec >= 0) {
					history_count_down.setText("" + remainSec + " sec");
				} else {
					history_count_down.setText("Expired");
				}
			}
		} else {
			View localView2 = paramView.findViewById(R.id.history_buble);
			localView2.setBackgroundResource(R.drawable.history_recving_bg);

			ProgressBar localProgressBar3 = (ProgressBar) paramView
					.findViewById(R.id.history_progressbar_normal);
			localProgressBar3.setVisibility(0);

			localProgressBar3.setProgress(itemData.mProgress);

			ProgressBar localProgressBar4 = (ProgressBar) paramView
					.findViewById(R.id.history_progressbar_fail);
			localProgressBar4.setVisibility(4);
		}

		View localView2 = paramView.findViewById(R.id.history_buble_content_2);
		localView2.findViewById(R.id.history_thumbnail_mask).setVisibility(0);

		localView2.setVisibility(0);

		if (true) {
			View localView1 = paramView
					.findViewById(R.id.history_buble_content_1);
			localView1.setVisibility(8);
		}

		paramView.setTag(localView2.findViewById(R.id.history_thumbnail));

		// send nick name
		TextView title = (TextView) paramView
				.findViewById(R.id.history_opposide_nick);
		title.setText(itemData.mSendUserName);

		// appIcon
		ImageView localImageView1 = (ImageView) localView2
				.findViewById(R.id.history_thumbnail);

		Drawable appIconDrawable = mContext.getResources().getDrawable(
				R.drawable.ic_launcher);
		localImageView1.setImageDrawable(appIconDrawable);

		// app name
		TextView appTitle = (TextView) localView2
				.findViewById(R.id.history_file_title);
		appTitle.setText(itemData.mAppName);

		// file info
		TextView history_file_info = (TextView) localView2
				.findViewById(R.id.history_file_info);

		float fileSize = itemData.mFileSize / 1024;
		if (fileSize < 1024.0) {
			history_file_info.setText("" + (int) fileSize + "KB");
		} else {
			fileSize = fileSize / 1024;

			history_file_info.setText("" + (int) fileSize + "MB");
		}

		return paramView;
	}

	private LayerDrawable setProgressLayerDrawable(boolean paramBoolean) {
		Drawable localDrawable = mContext.getResources().getDrawable(
				R.drawable.history_send_progress_bg);
		NinePatchDrawable localNinePatchDrawable;
		if (paramBoolean) {
			localNinePatchDrawable = (NinePatchDrawable) mContext
					.getResources().getDrawable(
							R.drawable.history_pgbar_pause_send);
		} else {
			localNinePatchDrawable = (NinePatchDrawable) mContext
					.getResources().getDrawable(
							R.drawable.history_send_progress_fg);
		}

		// LayerDrawable localLayerDrawable = new LayerDrawable(
		// new Drawable[] { localDrawable, localNinePatchDrawable });
		LayerDrawable localLayerDrawable = new LayerDrawable(new Drawable[] {
				localDrawable,
				new HistoryProgressClipDrawable(
						HistoryNinePatchDrawable.a(localNinePatchDrawable)) });

		localLayerDrawable.setId(0, android.R.id.background);
		localLayerDrawable.setId(1, android.R.id.progress);

		return localLayerDrawable;
	}

	private View getGroupView(View paramView) {
		if (paramView == null)
			paramView = LayoutInflater.from(mContext).inflate(
					R.layout.qe_downloadmgr_group, null);
		TextView localTextView = (TextView) paramView
				.findViewById(R.id.download_mgr_group_title);
		localTextView.setVisibility(0);
		localTextView.setText("Oct 10,2013");
		paramView.setTag(null);
		return paramView;
	}

}
