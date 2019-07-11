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

package xyz.zpayh.hdimage.datasource.interceptor;

import android.content.Context;
import android.graphics.BitmapRegionDecoder;
import android.net.Uri;
import androidx.annotation.CheckResult;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import xyz.zpayh.hdimage.BuildConfig;
import xyz.zpayh.hdimage.datasource.Interceptor;
import xyz.zpayh.hdimage.util.DiskLruCache;
import xyz.zpayh.hdimage.util.ImageCache;
import xyz.zpayh.hdimage.util.Preconditions;
import xyz.zpayh.hdimage.util.UriUtil;

/**
 * 文 件 名: NetworkInterceptor
 * 创 建 人: 陈志鹏
 * 创建日期: 2017/7/30 01:37
 * 邮   箱: ch_zh_p@qq.com
 * 修改时间:
 * 修改备注:
 */

public class NetworkInterceptor implements Interceptor{
    private static final String TAG = "NetworkInterceptor";

    private static final int HTTP_CACHE_SIZE = 20 * 1024 * 1024; // 20MB
    private static final String HTTP_CACHE_DIR = "http";
    private static final int IO_BUFFER_SIZE = 8 * 1024;

    private static final int DISK_CACHE_INDEX = 0;

    private DiskLruCache mHttpDiskCache;

    public NetworkInterceptor(Context context) {
        Preconditions.checkNotNull(context);
        initDiskLruCache(context);
    }

    private void initDiskLruCache(Context context){
        File httpCacheDir = ImageCache.getDiskCacheDir(context,HTTP_CACHE_DIR);
        if (!httpCacheDir.exists()){
            if (!httpCacheDir.mkdirs()){
                mHttpDiskCache = null;
                return;
            }
        }
        if (ImageCache.getUsableSpace(httpCacheDir) > HTTP_CACHE_SIZE){
            try {
                mHttpDiskCache = DiskLruCache.open(httpCacheDir,1,1,HTTP_CACHE_SIZE);
            } catch (IOException e) {
                e.printStackTrace();
                mHttpDiskCache = null;
            }
        }
    }

    @Override
    public BitmapRegionDecoder intercept(Chain chain) throws IOException {
        final Uri uri = chain.uri();
        BitmapRegionDecoder decoder = chain.chain(uri);
        if (decoder != null){
            return decoder;
        }

        if (UriUtil.isNetworkUri(uri)){
            if (BuildConfig.DEBUG) {
                Log.d("NetworkInterceptor", "从我这加载");
            }
            File file = processFile(uri.toString());
            if (file == null) {
                return null;
            }
            try {
                //InputStream inputStream = processBitmap(uri.toString());
                return BitmapRegionDecoder.newInstance(new FileInputStream(file),false);
            } catch (IOException e) {
                //e.printStackTrace();
                return Interceptors.fixJPEGDecoder(file,e);
            }
        }
        return null;
    }

    @CheckResult
    private synchronized File processFile(String data) throws IOException{
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "processFile - " + data);
        }

        final String key = ImageCache.hashKeyForDisk(data);
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

        return file;
    }

    /**
     * Download a bitmap from a URL and write the content to an output stream.
     *
     * @param urlString The URL to fetch
     * @return true if successful, false otherwise
     */
    private boolean downloadUrlToStream(String urlString, OutputStream outputStream) throws IOException{
        final URL url = new URL(urlString);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        BufferedInputStream in = new BufferedInputStream(urlConnection.getInputStream(), IO_BUFFER_SIZE);
        BufferedOutputStream out = new BufferedOutputStream(outputStream, IO_BUFFER_SIZE);

        int b;
        while ((b = in.read()) != -1) {
            out.write(b);
        }

        urlConnection.disconnect();

        try {
            out.close();
            in.close();
        } catch (final IOException e) {e.printStackTrace();}

        return true;
    }
}
