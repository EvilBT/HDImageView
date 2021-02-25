package xyz.zpayh.hdimage.datasource.interceptor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.exifinterface.media.ExifInterface;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import xyz.zpayh.hdimage.BuildConfig;
import xyz.zpayh.hdimage.state.Orientation;
import xyz.zpayh.hdimage.util.DiskLruCache;
import xyz.zpayh.hdimage.util.ImageCache;
import xyz.zpayh.hdimage.util.Preconditions;
import xyz.zpayh.hdimage.util.UriUtil;

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

    private static synchronized File processFile(InputStream data, String url, IOException e) throws IOException{
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "processFile - " + data);
        }
        final String key = ImageCache.hashKeyForDisk(url);
        DiskLruCache.Snapshot snapshot;

        File file = null;

        if (mHttpDiskCache != null) {
            snapshot = mHttpDiskCache.get(key);
            if (snapshot == null) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "processBitmap, not found in http cache, downloading...");
                }
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
                snapshot = mHttpDiskCache.get(key);
            }
            if (snapshot != null) {
                file = new File(mHttpDiskCache.getDirectory(), key + "." + DISK_CACHE_INDEX);
            }
        }

        if (file == null || !file.exists()){
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "下载缓存失败:" + url);
            }
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
        return fixJPEGDecoder(processFile(inputStream,uri.toString(),e),e);
    }

    public static BitmapRegionDecoder fixJPEGDecoder(File file, IOException e) throws IOException {

        if (file == null || !file.exists()){
            throw e;
        }

        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(),new BitmapFactory.Options());
        if (bitmap == null) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "加载缓存失败:" + file.getAbsolutePath());
            }
            throw e;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.WEBP, 85, baos);
        BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(baos.toByteArray(),0,baos.size(),false);
        bitmap.recycle();
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "fixJPEGDecoder: 从此处修复Bitmap");
        }
        return decoder;
    }

    static int getExifOrientation(String sourceUri) {
        if (UriUtil.isNetworkUri(Uri.parse(sourceUri))) {
            try {
                final String key = ImageCache.hashKeyForDisk(sourceUri);
                if (mHttpDiskCache != null) {
                    DiskLruCache.Snapshot snapshot = mHttpDiskCache.get(key);
                    if (snapshot != null) {
                        File file = new File(mHttpDiskCache.getDirectory(), key + "." + DISK_CACHE_INDEX);
                        ExifInterface exifInterface = new ExifInterface(new FileInputStream(file));
                        int orientationAttr = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_NORMAL);
                        switch (orientationAttr) {
                            case ExifInterface.ORIENTATION_NORMAL:
                            case ExifInterface.ORIENTATION_UNDEFINED:
                                return Orientation.ORIENTATION_0;
                            case ExifInterface.ORIENTATION_ROTATE_90:
                                return Orientation.ORIENTATION_90;
                            case ExifInterface.ORIENTATION_ROTATE_180:
                                return Orientation.ORIENTATION_180;
                            case ExifInterface.ORIENTATION_ROTATE_270:
                                return Orientation.ORIENTATION_270;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return Orientation.ORIENTATION_EXIF;
    }
}
