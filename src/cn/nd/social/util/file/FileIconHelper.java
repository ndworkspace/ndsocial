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

package cn.nd.social.util.file;



import java.util.HashMap;

import android.view.View;
import android.widget.ImageView;
import cn.nd.social.R;
import cn.nd.social.util.FilePathHelper;

public class FileIconHelper {

    private static final String LOG_TAG = "FileIconHelper";

    private static HashMap<ImageView, ImageView> imageFrames = new HashMap<ImageView, ImageView>();

    private static HashMap<String, Integer> fileExtToIcons = new HashMap<String, Integer>();

    static {
        addItem(new String[] {
            "mp3"
        }, R.drawable.file_icon_mp3);
        addItem(new String[] {
            "wma"
        }, R.drawable.file_icon_wma);
        addItem(new String[] {
            "wav"
        }, R.drawable.file_icon_wav);
        addItem(new String[] {
            "mid"
        }, R.drawable.file_icon_mid);
        addItem(new String[] {
                "mp4", "wmv", "mpeg", "m4v", "3gp", "3gpp", "3g2", "3gpp2", "asf"
        }, R.drawable.file_icon_video);
        addItem(new String[] {
                "jpg", "jpeg", "gif", "png", "bmp", "wbmp"
        }, R.drawable.file_icon_picture);
        addItem(new String[] {
                "txt", "log", "xml", "ini", "lrc"
        }, R.drawable.file_icon_txt);
        addItem(new String[] {
                "doc", "ppt", "docx", "pptx", "xsl", "xslx",
        }, R.drawable.file_icon_doc);
        addItem(new String[] {
            "pdf"
        }, R.drawable.file_icon_pdf);
        addItem(new String[] {
            "zip"
        }, R.drawable.file_icon_zip);
        addItem(new String[] {
            "rar"
        }, R.drawable.file_icon_rar);
    }

    public FileIconHelper() {
    }

    private static void addItem(String[] exts, int resId) {
        if (exts != null) {
            for (String ext : exts) {
                fileExtToIcons.put(ext.toLowerCase(), resId);
            }
        }
    }

    public static int getFileIcon(String ext) {
        Integer i = fileExtToIcons.get(ext.toLowerCase());
        if (i != null) {
            return i.intValue();
        } else {
            return R.drawable.file_icon_default;
        }

    }

    public void setIcon(FileInfo fileInfo, ImageView fileImage, ImageView fileImageFrame) {
        String filePath = fileInfo.filePath;
        String extFromFilename = FilePathHelper.getExtFromFilename(filePath);
        fileImageFrame.setVisibility(View.GONE);
        int id = getFileIcon(extFromFilename);
        fileImage.setImageResource(id);
    }

    public void setIcon(String filePath,ImageView fileImage) {
        String extFromFilename = FilePathHelper.getExtFromFilename(filePath);
        int id = getFileIcon(extFromFilename);
        fileImage.setImageResource(id);
    }
}
