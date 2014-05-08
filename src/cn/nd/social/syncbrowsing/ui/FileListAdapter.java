/*
 * Copyright (c) 2010-2011, The MiCode Open Source Community (www.micode.net)
 *
 * This file is part of FileExplorer.
 *
 * FileExplorer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FileExplorer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SwiFTP.  If not, see <http://www.gnu.org/licenses/>.
 */

package cn.nd.social.syncbrowsing.ui;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import cn.nd.social.R;
import cn.nd.social.util.FormatUtils;
import cn.nd.social.util.NDConfig;
import cn.nd.social.util.file.FileIconHelper;
import cn.nd.social.util.file.FileInfo;

public class FileListAdapter extends ArrayAdapter<FileInfo> {
    private LayoutInflater mInflater;
    private Context mContext;
    List<FileInfo> mFileInfoList;
    private FileIconHelper mIconHelper;

    public FileListAdapter(Context context, int resource,
            List<FileInfo> list, FileIconHelper iconHelper) {
        super(context, resource, list);
        mInflater = LayoutInflater.from(context);
        mContext = context;
        mFileInfoList = list;
        mIconHelper = iconHelper;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        ViewHolder holder;
        if (convertView != null) {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        } else {
            view = mInflater.inflate(R.layout.sync_file_browser_item, parent, false);
            holder = new ViewHolder();
            holder.filename = (TextView)view.findViewById(R.id.file_name);
            holder.fileCount = (TextView)view.findViewById(R.id.file_count);
            holder.modifiedTime = (TextView)view.findViewById(R.id.modified_time);
            holder.fileSize = (TextView)view.findViewById(R.id.file_size);
            holder.fileImage = (ImageView)view.findViewById(R.id.file_image);
            holder.fileImageFrame = (ImageView)view.findViewById(R.id.file_image_frame);
            view.setTag(holder);
        }
        
        bindView(position,holder);
        return view;
    }
    
    private void bindView(int pos, ViewHolder holder) {
    	 FileInfo fileInfo = mFileInfoList.get(pos);
         holder.filename.setText(fileInfo.fileName);
         if(NDConfig.FILE_BROWSER_SHOW_FILE_COUNT) {
        	 holder.fileCount.setText(fileInfo.IsDir ? "(" + fileInfo.Count + ")" : "");
         } else {
        	 holder.fileCount.setVisibility(View.GONE);
         }
         holder.modifiedTime.setText(FormatUtils.formatDateString(mContext, fileInfo.ModifiedDate));
         holder.fileSize.setText(fileInfo.IsDir ? "" : FormatUtils.convertStorage(fileInfo.fileSize));
    	 if(fileInfo.IsDir) {
    		 holder.fileImageFrame.setVisibility(View.GONE);
    		 holder.fileImage.setImageResource(R.drawable.sync_file_icon_folder);
    	 } else {
    		 mIconHelper.setIcon(fileInfo, holder.fileImage, holder.fileImageFrame);
    	 }
    }
    

    
    public class ViewHolder {
    	TextView filename;
    	TextView fileCount;
    	TextView modifiedTime;
    	TextView fileSize;
    	ImageView fileImage;
    	ImageView fileImageFrame;
    }
}
