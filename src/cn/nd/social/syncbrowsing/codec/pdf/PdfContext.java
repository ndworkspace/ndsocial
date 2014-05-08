package cn.nd.social.syncbrowsing.codec.pdf;

import cn.nd.social.syncbrowsing.codec.CodecContext;
import cn.nd.social.syncbrowsing.codec.CodecDocument;
import cn.nd.social.syncbrowsing.codec.CodecLibraryLoader;

public class PdfContext implements CodecContext
{
    static
    {
        CodecLibraryLoader.load();
    }

    public CodecDocument openDocument(String fileName)
    {
        return PdfDocument.openDocument(fileName, "");
    }

    public void recycle() {
    }
}
