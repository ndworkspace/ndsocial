package cn.nd.social.prishare.items;

import java.io.File;
import java.text.DecimalFormat;

import android.graphics.drawable.Drawable;
import cn.nd.social.R;
import cn.nd.social.SocialApplication;

public class AudioCellItem extends CellItemBase {

	private String name;
	private Drawable icon;
	private String path;
	private long id;
	private String fileSize;
	private String artist;
	private String title;

	public AudioCellItem(long id, Drawable icon, String path,String artist,String title) {
		super(VIDEO_TYPE);
		this.icon = icon;
		this.path = path;
		this.id = id;
		this.artist = artist;
		this.title = title;
		File f = new File(path);
		if (f.exists()) {
			long size = f.length();
			DecimalFormat df = new DecimalFormat("#0.00");
			double d = size / 1024.0 / 1024.0;
			fileSize = df.format(d) + "M";
		}
	}

	public Drawable getIcon() {
		if(icon == null) {
			icon = SocialApplication.getAppInstance().getResources().getDrawable(R.drawable.zapya_data_audio);
		}
		return icon;
	}

	public String getPath() {
		return path;
	}

	public long getId() {
		return id;
	}

	@Override
	public Drawable getItemIcon() {
		return getIcon();
	}

	@Override
	public String getItemPath() {
		return path;
	}

	@Override
	public String getFileShortName() {
		File f = new File(path);
		if(f.exists()) {
			return f.getName();
		}
		return "audio"; // TODO : get short name
	}
	
	public String getFileSize() {
		return fileSize;
	}
	
	public String getArtist() {
		return artist;
	}
	
	public String getTitle() {
		return title;
	}

}
