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

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.BitmapRegionDecoder;
import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;

import xyz.zpayh.hdimage.datasource.Interceptor;
import xyz.zpayh.hdimage.util.Preconditions;
import xyz.zpayh.hdimage.util.UriUtil;

/**
 * 文 件 名: ContentInterceptor
 * 创 建 人: 陈志鹏
 * 创建日期: 2017/7/30 20:07
 * 邮   箱: ch_zh_p@qq.com
 * 修改时间:
 * 修改备注:
 */

public class ContentInterceptor implements Interceptor {

    private final Context mContext;

    public ContentInterceptor(Context context) {
        mContext = Preconditions.checkNotNull(context);
    }

    @Override
    public BitmapRegionDecoder intercept(Chain chain) throws IOException {
        final Uri uri = chain.uri();
        BitmapRegionDecoder decoder = chain.chain(uri);
        if (decoder != null){
            return decoder;
        }

        if (UriUtil.isLocalContentUri(uri) || UriUtil.isQualifiedResourceUri(uri)){
            InputStream inputStream = null;
            try {
                ContentResolver resolver = mContext.getContentResolver();
                inputStream = resolver.openInputStream(uri);
                try {
                    decoder = BitmapRegionDecoder.newInstance(inputStream,false);
                } catch (IOException e) {
                    //e.printStackTrace();
                    return Interceptors.fixJPEGDecoder(inputStream,uri,e);
                }
            } finally {
                if (inputStream != null){
                    try { inputStream.close(); }
                    catch (Exception e){e.printStackTrace();}
                }
            }
        }

        return decoder;
    }
}
