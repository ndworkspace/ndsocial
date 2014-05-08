package cn.nd.social.common;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import cn.nd.social.R;
import cn.nd.social.updater.UpdateInitiator;

public class ShareThisApp {
	Activity mActivity;
	LayoutInflater mInflater;
	public ShareThisApp(Activity activity) {
		mActivity = activity;
		mInflater = mActivity.getLayoutInflater();
	}

	
	public void share() {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/*");
		// intent.putExtra(Intent.EXTRA_SUBJECT, "Share");


		PackageManager pm = mActivity.getPackageManager();
		List<ResolveInfo> apps = pm.queryIntentActivities(intent, 0);

		List<ResolveInfo> shareList = new ArrayList<ResolveInfo>();
		for (ResolveInfo info : apps) {
			for (String pack : PRESET_SHARE_PACKAGE) {
				if (pack.equals(getPackageName(info))) {
					shareList.add(info);
					break;
				}
			}
		}

		buildShareList(shareList);
	}

	final static String[] PRESET_SHARE_PACKAGE = { "com.tencent.mm",
			"com.sina.weibo", "com.android.mms", "com.motorola.messaging" };

	private String getPackageName(ResolveInfo info) {
		return info.activityInfo.applicationInfo.packageName;
	}

	private Drawable getPackageIcon(ResolveInfo info) {
		return info.activityInfo.applicationInfo.loadIcon(mActivity
				.getPackageManager());
	}

	private CharSequence getPackageAppName(ResolveInfo info) {
		return info.activityInfo.applicationInfo.loadLabel(mActivity
				.getPackageManager());
	}

	private InviteListAdapter mAdapter;

	private void buildShareList(List<ResolveInfo> list) {
		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
		View parent = mInflater.inflate(R.layout.app_invite_list, null);
		final AlertDialog dialog = builder.create();

		TextView title = (TextView) parent.findViewById(R.id.title);
		title.setText(R.string.share_the_app);
		ListView listView = (ListView) parent.findViewById(R.id.lv_invite_list);
		mAdapter = new InviteListAdapter(list);
		listView.setAdapter(mAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> listview, View v,
					int position, long id) {
				dialog.dismiss();
				ResolveInfo info = mAdapter.getItem(position);
				String shareText = mAdapter.getShareText(position);
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setPackage(getPackageName(info));
				intent.setType("text/*");
				intent.putExtra(Intent.EXTRA_SUBJECT, "Share");
				intent.putExtra(Intent.EXTRA_TEXT, shareText);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mActivity.startActivity(intent);
			}
		});

		dialog.setView(parent, 0, 0, 0, 0);
		dialog.show();
	}

	private class InviteListAdapter extends BaseAdapter {
		List<ResolveInfo> mList;
		String mShareText;

		InviteListAdapter(List<ResolveInfo> list) {
			mList = list;
			String downloadText = mActivity.getString(R.string.share_text_prefix) + UpdateInitiator.DOWNLOAD_URL;
			mShareText = downloadText;
		}

		@Override
		public int getCount() {
			return mList.size();
		}

		@Override
		public ResolveInfo getItem(int position) {
			return mList.get(position);
		}

		public String getShareText(int position) {
			String downloadUrl = UpdateInitiator.DOWNLOAD_URL;
			if(getPackageName(getItem(position)).equals("com.tencent.mm")) {
				downloadUrl = UpdateInitiator.DOWNLOAD_URL_FOR_WECHAT;
			} 
			return mActivity.getString(R.string.share_text_prefix) + downloadUrl;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			View v;
			if (convertView == null) {
				v = mInflater.inflate(R.layout.app_invite_list_item, parent,
						false);
				holder = new ViewHolder();

				holder.image = (ImageView) v
						.findViewById(R.id.iv_invite_item_icon);
				holder.text = (TextView) v
						.findViewById(R.id.tv_invite_item_ways);
				v.setTag(holder);
			} else {
				v = convertView;
				holder = (ViewHolder) v.getTag();
			}
			bindView(holder, position);

			return v;
		}

		private void bindView(ViewHolder holder, int position) {
			ResolveInfo info = getItem(position);
			holder.image.setImageDrawable(getPackageIcon(info));
			holder.text.setText(getPackageAppName(info));
		}

	}
	
	private class ViewHolder {
		ImageView image;
		TextView text;
	}

}