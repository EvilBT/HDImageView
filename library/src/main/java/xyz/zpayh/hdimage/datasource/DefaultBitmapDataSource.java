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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.io.IOException;

import xyz.zpayh.hdimage.HDImageView;
import xyz.zpayh.hdimage.core.HDImageViewFactory;

/**
 * 文 件 名: DefaultBitmapDataSource
 * 创 建 人: 陈志鹏
 * 创建日期: 2017/7/29 13:26
 * 邮   箱: ch_zh_p@qq.com
 * 修改时间:
 * 修改备注:
 */

public class DefaultBitmapDataSource implements BitmapDataSource{

    private BitmapRegionDecoder mDecoder;
    private final Object mDecoderLock = new Object();
    private OrientationInterceptor mOrientationInterceptor;

    @Override
    public void init(Context context, Uri uri, Point dimensions, OnInitListener listener) {

        try {
            mDecoder = getDecoderWithInterceptorChain(uri);
            if (mDecoder != null) {
                if (dimensions != null){
                    dimensions.set(mDecoder.getWidth(), mDecoder.getHeight());
                }
                if (listener != null){
                    listener.success();
                }
            }else{
                if (listener != null){
                    listener.failed(new IOException("init failed"));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (listener != null){
                listener.failed(e);
            }
        }
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

    @Override
    public int getExifOrientation(@NonNull Context context, String sourceUri) {
        if (mOrientationInterceptor == null) {
            mOrientationInterceptor = new RealOrientationInterceptor(HDImageViewFactory.getInstance().getOrientationInterceptor());
        }
        return mOrientationInterceptor.getExifOrientation(context, sourceUri);
    }

    private BitmapRegionDecoder getDecoderWithInterceptorChain(Uri uri) throws IOException{
        Interceptor.Chain chain = new RealInterceptorChain(HDImageViewFactory.getInstance().getDataSourceInterceptor(),0,uri);
        return chain.chain(uri);
    }
}
