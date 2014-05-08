package cn.nd.social.syncbrowsing.codec;

public interface CodecContext
{
    CodecDocument openDocument(String fileName);

    void recycle();
}
