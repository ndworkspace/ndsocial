package cn.nd.social.prishare.items;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore.Images;

public class ImageCellItem extends CellItemBase {

	private String name;
	private Drawable icon;
	private String path;
	private long id;

	public ImageCellItem(long id, Drawable icon, String path) {
		super(IMAGE_TYPE);
		this.icon = icon;
		this.path = path;
		this.id = id;
	}

	public Drawable getIcon() {
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
		Bitmap bm = Images.Thumbnails.getThumbnail(
				cn.nd.social.util.Utils.getAppContext().getContentResolver(), getId(),
				Images.Thumbnails.MICRO_KIND, null);
		BitmapDrawable bd = new BitmapDrawable(
				cn.nd.social.util.Utils.getAppContext().getResources(),bm);
		return bd;
	}

	@Override
	public String getItemPath() {
		return path;
	}

	@Override
	public String getFileShortName() {
		return "image"; // TODO : get short name
	}
}
