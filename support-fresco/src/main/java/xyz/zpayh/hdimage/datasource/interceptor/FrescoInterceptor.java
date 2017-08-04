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

import android.graphics.BitmapRegionDecoder;
import android.net.Uri;
import android.util.Log;

import com.facebook.binaryresource.BinaryResource;
import com.facebook.binaryresource.FileBinaryResource;
import com.facebook.cache.common.CacheKey;
import com.facebook.common.internal.Closeables;
import com.facebook.common.memory.PooledByteBuffer;
import com.facebook.common.memory.PooledByteBufferInputStream;
import com.facebook.common.references.CloseableReference;
import com.facebook.common.util.UriUtil;
import com.facebook.datasource.DataSource;
import com.facebook.datasource.DataSources;
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.request.ImageRequest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import xyz.zpayh.hdimage.datasource.Interceptor;

/**
 * 文 件 名: FrescoInterceptor
 * 创 建 人: 陈志鹏
 * 创建日期: 2017/7/30 16:07
 * 邮   箱: ch_zh_p@qq.com
 * 修改时间:
 * 修改备注: 只加载网络图片
 */

public class FrescoInterceptor implements Interceptor {
    @Override
    public BitmapRegionDecoder intercept(Chain chain) throws IOException {
        final Uri uri = chain.uri();
        BitmapRegionDecoder decoder = chain.chain(uri);
        if (decoder != null){
            return decoder;
        }

        if (UriUtil.isNetworkUri(uri)){
            ImagePipeline imagePipeline = ImagePipelineFactory.getInstance().getImagePipeline();

            ImageRequest request = ImageRequest.fromUri(uri);
            DataSource<CloseableReference<PooledByteBuffer>> dataSource = imagePipeline.fetchEncodedImage(request,null);
            try {
                CloseableReference<PooledByteBuffer> ref = DataSources.waitForFinalResult(dataSource);
                if (ref == null){
                    return null;
                }
                PooledByteBuffer result = ref.get();
                if (BuildConfig.DEBUG) {
                    Log.d("FrescoInterceptor", "从我这加载");
                }
                try {
                    InputStream inputStream = new PooledByteBufferInputStream(result);
                    Closeables.closeQuietly(inputStream);
                    return BitmapRegionDecoder.newInstance(inputStream,false);
                } catch (IOException e) {
                    ImageRequest imageRequest=ImageRequest.fromUri(uri);
                    CacheKey cacheKey= DefaultCacheKeyFactory.getInstance().getEncodedCacheKey(imageRequest,null);
                    BinaryResource resource = ImagePipelineFactory.getInstance().getMainFileCache().getResource(cacheKey);
                    File file=((FileBinaryResource)resource).getFile();
                    if (BuildConfig.DEBUG) {
                        Log.d("FrescoInterceptor", file.getName());
                    }
                    return Interceptors.fixJPEGDecoder(file,e);
                }
            } catch (Throwable throwable) {
                if (BuildConfig.DEBUG) {
                    Log.d("FrescoInterceptor", "intercept: 加载失败了");
                }
                throwable.printStackTrace();
                return null;
            }
        }

        return null;
    }
}
