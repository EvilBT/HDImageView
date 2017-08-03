package xyz.zpayh.hdimage.datasource.interceptor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.net.Uri;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import xyz.zpayh.hdimage.util.DiskLruCache;
import xyz.zpayh.hdimage.util.ImageCache;
import xyz.zpayh.hdimage.util.Preconditions;

import static android.R.attr.key;

/**
 * 创建人： zp
 * 创建时间：2017/8/3
 */

public class Interceptors {

    private static final String TAG = "Interceptors";

    private static final int FIX_CACHE_SIZE = 10 * 1024 * 1024; // 20MB
    private static final String FIX_CACHE_DIR = "fixJPEG";
    private static final int IO_BUFFER_SIZE = 8 * 1024;

    private static final int DISK_CACHE_INDEX = 0;

    private static DiskLruCache mHttpDiskCache;

    public static void initDiskLruCache(Context context){
        if (mHttpDiskCache != null){
            return;
        }
        Preconditions.checkNotNull(context);
        File httpCacheDir = ImageCache.getDiskCacheDir(context, FIX_CACHE_DIR);
        if (!httpCacheDir.exists()){
            if (!httpCacheDir.mkdirs()){
                mHttpDiskCache = null;
                return;
            }
        }
        if (ImageCache.getUsableSpace(httpCacheDir) > FIX_CACHE_SIZE){
            try {
                mHttpDiskCache = DiskLruCache.open(httpCacheDir,1,1, FIX_CACHE_SIZE);
            } catch (IOException e) {
                e.printStackTrace();
                mHttpDiskCache = null;
            }
        }
    }

    /**
     * The main process method, which will be called by the ImageWorker in the AsyncTask background
     * thread.
     *
     * @param data The data to load the bitmap, in this case, a regular http URL
     * @return The downloaded and resized bitmap
     */
    private static synchronized File processBitmap(InputStream data,String url, IOException e) throws IOException{

        final String key = ImageCache.hashKeyForDisk(url);
        File file = null;

        if (mHttpDiskCache != null) {
            DiskLruCache.Editor editor = mHttpDiskCache.edit(key);
            if (editor != null) {
                if (downloadUrlToStream(data,
                        editor.newOutputStream(DISK_CACHE_INDEX))) {
                    editor.commit();
                } else {
                    editor.abort();
                }
            }
            mHttpDiskCache.flush();
            file = getCleanFile(mHttpDiskCache.getDirectory(),DISK_CACHE_INDEX);
        }

        if (file == null || !file.exists()){
            throw e;
        }

        return file;
    }

    private static boolean downloadUrlToStream(InputStream inputStream, OutputStream outputStream) throws IOException{

        BufferedInputStream in = new BufferedInputStream(inputStream, IO_BUFFER_SIZE);
        BufferedOutputStream out = new BufferedOutputStream(outputStream, IO_BUFFER_SIZE);

        int b;
        while ((b = in.read()) != -1) {
            out.write(b);
        }
        try {
            out.close();
            in.close();
        } catch (final IOException e) {e.printStackTrace();}

        return true;
    }

    public static BitmapRegionDecoder fixJPEGDecoder(InputStream inputStream, Uri uri, IOException e) throws IOException {
        return fixJPEGDecoder(processBitmap(inputStream,uri.toString(),e),e);
    }

    public static BitmapRegionDecoder fixJPEGDecoder(File file, IOException e) throws IOException {

        if (file == null || !file.exists()){
            throw e;
        }

        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(),new BitmapFactory.Options());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(baos.toByteArray(),0,baos.size(),false);
        bitmap.recycle();
        Log.d(TAG, "fixJPEGDecoder: 从此修复Bitmap");
        return decoder;
    }

    private static File getCleanFile(File directory, int i) {
        return new File(directory, key + "." + i);
    }
}
