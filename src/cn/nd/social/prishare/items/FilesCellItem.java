package cn.nd.social.prishare.items;

import java.io.File;

import android.graphics.drawable.Drawable;
import cn.nd.social.R;
import cn.nd.social.util.Utils;

public class FilesCellItem extends CellItemBase {

	private String name;
	private Drawable icon;
	private String path;
	private String fileShortName;
	private int directoryFlag;
	private long id;

	public FilesCellItem(long id, Drawable icon, String fileShortName, String path, int directoryFlag) {
		super(FILES_TYPE);
		
		this.id = id;
		
		this.icon = icon;
		
		this.fileShortName = fileShortName;
		this.path = path;
		
		this.directoryFlag = directoryFlag;
	}

	public static FilesCellItem getFilesCellItem(String path){
		File f = new File(path);
		Drawable localDrawable = Utils.getAppContext().getResources()
				.getDrawable(R.drawable.zapya_data_folder_folder);
		return (new FilesCellItem(0,localDrawable,
				f.getName(),f.getPath(),(f.isDirectory()?1:0)));
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

		Drawable fileDrawable = Utils.getAppContext().getResources()
				.getDrawable(R.drawable.zapya_data_folder_doc);		
		return fileDrawable;
	}
	
	@Override
	public String getItemPath() {
		return path;
	}

	public boolean isDirectory() {
		return directoryFlag != 0;
	}
	
	@Override
	public String getFileShortName() {
		return fileShortName;
	}
}
