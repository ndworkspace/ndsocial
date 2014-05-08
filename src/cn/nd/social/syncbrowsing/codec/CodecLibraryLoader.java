package cn.nd.social.syncbrowsing.codec;

public class CodecLibraryLoader
{
    private static boolean alreadyLoaded = false;

    public static void load()
    {
        if (alreadyLoaded)
        {
            return;
        }
        System.loadLibrary("pdf");
        alreadyLoaded = true;
    }
}
