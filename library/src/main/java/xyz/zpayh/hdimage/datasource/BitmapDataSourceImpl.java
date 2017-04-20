/*
 *
 *  * Copyright 2017 陈志鹏
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package xyz.zpayh.hdimage.datasource;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;

import xyz.zpayh.hdimage.BuildConfig;
import xyz.zpayh.hdimage.util.DiskLruCache;
import xyz.zpayh.hdimage.util.ImageCache;

/**
 * 文 件 名: BitmapDataSourceImpl
 * 创 建 人: 陈志鹏
 * 创建日期: 2017/4/1 17:34
 * 邮   箱: ch_zh_p@qq.com
 * 修改时间:
 * 修改备注:
 */

public class BitmapDataSourceImpl implements BitmapDataSource {
    private static final String TAG = "BitmapDataSourceImpl";
    private static final int HTTP_CACHE_SIZE = 20 * 1024 * 1024; // 20MB
    private static final String HTTP_CACHE_DIR = "http";
    private static final int IO_BUFFER_SIZE = 8 * 1024;

    private static final int DISK_CACHE_INDEX = 0;

    private BitmapRegionDecoder mDecoder;
    private final Object mDecoderLock = new Object();

    private DiskLruCache mHttpDiskCache;
    private File mHttpCacheDir;
    private boolean mHttpDiskCacheStarting = true;
    private final Object mHttpDiskCacheLock = new Object();

    public BitmapDataSourceImpl(Context context){
        init(context);
        initHttpDiskCache();
    }

    private void init(Context context){
        mHttpCacheDir = ImageCache.getDiskCacheDir(context,HTTP_CACHE_DIR);
    }

    private void initHttpDiskCache() {
        if (!mHttpCacheDir.exists()) {
            mHttpCacheDir.mkdirs();
        }
        synchronized (mHttpDiskCacheLock) {
            if (ImageCache.getUsableSpace(mHttpCacheDir) > HTTP_CACHE_SIZE) {
                try {
                    mHttpDiskCache = DiskLruCache.open(mHttpCacheDir, 1, 1, HTTP_CACHE_SIZE);
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "HTTP cache initialized");
                    }
                } catch (IOException e) {
                    mHttpDiskCache = null;
                }
            }
            mHttpDiskCacheStarting = false;
            mHttpDiskCacheLock.notifyAll();
        }
    }

    /**
     * The main process method, which will be called by the ImageWorker in the AsyncTask background
     * thread.
     *
     * @param data The data to load the bitmap, in this case, a regular http URL
     * @return The downloaded and resized bitmap
     */
    private InputStream processBitmap(String data) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "processBitmap - " + data);
        }

        final String key = ImageCache.hashKeyForDisk(data);
        FileDescriptor fileDescriptor = null;
        FileInputStream fileInputStream = null;
        DiskLruCache.Snapshot snapshot;
        synchronized (mHttpDiskCacheLock) {
            // Wait for disk cache to initialize
            while (mHttpDiskCacheStarting) {
                try {
                    mHttpDiskCacheLock.wait();
                } catch (InterruptedException e) {}
            }

            if (mHttpDiskCache != null) {
                try {
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
                        snapshot = mHttpDiskCache.get(key);
                    }
                    if (snapshot != null) {
                        fileInputStream =
                                (FileInputStream) snapshot.getInputStream(DISK_CACHE_INDEX);
                        fileDescriptor = fileInputStream.getFD();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "processBitmap - " + e);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "processBitmap - " + e);
                } finally {
                    if (fileDescriptor == null && fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e) {}
                    }
                }
            }
        }

        return fileInputStream;
    }

    /**
     * Download a bitmap from a URL and write the content to an output stream.
     *
     * @param urlString The URL to fetch
     * @return true if successful, false otherwise
     */
    public boolean downloadUrlToStream(String urlString, OutputStream outputStream) {
        HttpURLConnection urlConnection = null;
        BufferedOutputStream out = null;
        BufferedInputStream in = null;

        try {
            final URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream(), IO_BUFFER_SIZE);
            out = new BufferedOutputStream(outputStream, IO_BUFFER_SIZE);

            int b;
            while ((b = in.read()) != -1) {
                out.write(b);
            }
            return true;
        } catch (final IOException e) {
            Log.e(TAG, "Error in downloadBitmap - " + e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (final IOException e) {}
        }
        return false;
    }

    @Override
    public void init(Context context, Uri uri, Point dimensions) throws Exception {
        String uriString = uri.toString();

        if (uriString.startsWith(RESOURCE_SCHEME)){
            int id = 0;
            final String idString = uri.getLastPathSegment();
            if (TextUtils.isDigitsOnly(idString)){
                id = Integer.parseInt(idString);
            }
            mDecoder = BitmapRegionDecoder.newInstance(context.getResources().openRawResource(id), false);
            if (dimensions != null){
                dimensions.set(mDecoder.getWidth(), mDecoder.getHeight());
            }
            return;
        }

        if (uriString.startsWith(ASSET_SCHEME)){
            String assetName = uriString.substring(ASSET_SCHEME.length());
            mDecoder = BitmapRegionDecoder.newInstance(
                            context.getAssets().open(assetName, AssetManager.ACCESS_RANDOM), false);
            if (dimensions != null){
                dimensions.set(mDecoder.getWidth(), mDecoder.getHeight());
            }
            return;
        }

        if (uriString.startsWith(FILE_SCHEME)){
            mDecoder = BitmapRegionDecoder.newInstance(uriString.substring(FILE_SCHEME.length()), false);
            if (dimensions != null){
                dimensions.set(mDecoder.getWidth(), mDecoder.getHeight());
            }
            return;
        }

        if (uriString.startsWith(HTTP_SCHEME) || uriString.startsWith(HTTPS_SCHEME)){
            InputStream inputStream = processBitmap(uriString);
            if (inputStream == null){
                throw new ConnectException("no connect :"+uriString);
            }
            mDecoder = BitmapRegionDecoder.newInstance(inputStream,false);

            try {
                inputStream.close();
            } catch (Exception e) {}

            if (dimensions != null){
                dimensions.set(mDecoder.getWidth(), mDecoder.getHeight());
            }
            return;
        }

        if (uriString.startsWith(ContentResolver.SCHEME_CONTENT)){
            InputStream inputStream = null;
            try {
                ContentResolver contentResolver = context.getContentResolver();
                inputStream = contentResolver.openInputStream(uri);
                mDecoder = BitmapRegionDecoder.newInstance(inputStream, false);
                if (dimensions != null){
                    dimensions.set(mDecoder.getWidth(), mDecoder.getHeight());
                }
                return;
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Exception e) {}
                }
            }
        }

        throw new Exception("Unsupported uri: " + uriString);
    }

    @Override
    public Bitmap decode(Rect sRect, int sampleSize) {
        if (mDecoder == null){
            return null;
        }
        synchronized (mDecoderLock) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = sampleSize;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            return mDecoder.decodeRegion(sRect, options);
        }
    }

    @Override
    public boolean isReady() {
        return mDecoder != null && !mDecoder.isRecycled();
    }

    @Override
    public void recycle() {
        if (mDecoder != null) {
            mDecoder.recycle();
        }
    }
}
