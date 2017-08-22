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

import android.content.res.AssetManager;
import android.graphics.BitmapRegionDecoder;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

import xyz.zpayh.hdimage.BuildConfig;
import xyz.zpayh.hdimage.datasource.Interceptor;
import xyz.zpayh.hdimage.util.UriUtil;

/**
 * 文 件 名: AssetInterceptor
 * 创 建 人: 陈志鹏
 * 创建日期: 2017/7/29 17:28
 * 邮   箱: ch_zh_p@qq.com
 * 修改时间:
 * 修改备注:
 */

public class AssetInterceptor implements Interceptor{

    private final AssetManager mAssetManager;

    public AssetInterceptor(AssetManager assetManager) {
        mAssetManager = assetManager;
    }

    @Override
    public BitmapRegionDecoder intercept(Chain chain) throws IOException {
        final Uri uri = chain.uri();
        BitmapRegionDecoder decoder = chain.chain(uri);
        if (decoder != null){
            return decoder;
        }

        if (UriUtil.isLocalAssetUri(uri)){
            if (BuildConfig.DEBUG) {
                Log.d("AssetInterceptor", "从我这加载");
            }
            try {
                InputStream inputStream = mAssetManager.open(getAssetName(uri),AssetManager.ACCESS_STREAMING);
                return BitmapRegionDecoder.newInstance(inputStream,false);
            } catch (IOException e) {
                InputStream inputStream = mAssetManager.open(getAssetName(uri),AssetManager.ACCESS_STREAMING);
                return Interceptors.fixJPEGDecoder(inputStream,uri,e);
                //e.printStackTrace();
            }
        }
        return null;
    }

    private static String getAssetName(Uri uri) {
        return uri.getPath().substring(1);
    }
}
