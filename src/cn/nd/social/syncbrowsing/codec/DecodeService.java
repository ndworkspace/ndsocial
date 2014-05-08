package cn.nd.social.syncbrowsing.codec;

import android.graphics.RectF;

public interface DecodeService
{
    void setTargetSize(int width, int height);

    void open(String fileName);

    void decodePage(Object decodeKey, int pageNum, LoadPageCallback decodeCallback, float zoom, RectF pageSliceBounds);

    void stopDecoding(Object decodeKey);

    int getEffectivePagesWidth();

    int getEffectivePagesHeight();

    int getPageCount();

    int getPageWidth(int pageIndex);

    int getPageHeight(int pageIndex);

    void recycle();
}
