package cn.nd.social.syncbrowsing.codec;

public interface CodecDocument {
    CodecPage getPage(int pageNumber);

    int getPageCount();

    void recycle();
}
