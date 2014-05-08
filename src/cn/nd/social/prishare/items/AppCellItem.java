package cn.nd.social.prishare.items;

import java.io.File;
import java.text.DecimalFormat;


import android.graphics.drawable.Drawable;

public class AppCellItem extends CellItemBase {

	private String appName;
	private Drawable appIcon;
	private String packageName;
	private String appPath;
	private String appSize;

	public AppCellItem(String name, Drawable icon, String pack, String path) {
		super(APP_TYPE);

		this.appName = name;
		this.appIcon = icon;
		this.packageName = pack;
		this.appPath = path;
		File f = new File(path);
		if (f.exists()) {
			long size = f.length();
			DecimalFormat df = new DecimalFormat("#0.00");
			double d = size / 1024.0 / 1024.0;
			appSize = df.format(d) + "M";
		}
	}

	@Override
	public String getFileShortName() {
		return appName;
	}

	@Override
	public Drawable getItemIcon() {
		return appIcon;
	}

	@Override
	public String getItemPath() {
		return appPath;
	}

	public String getAppSize() {
		return appSize;
	}

	public String getAppPackage() {
		return packageName;
	}
}
