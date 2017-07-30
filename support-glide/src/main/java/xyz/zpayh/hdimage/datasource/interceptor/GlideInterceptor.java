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
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import xyz.zpayh.hdimage.datasource.Interceptor;
import xyz.zpayh.hdimage.util.Preconditions;
import xyz.zpayh.hdimage.util.UriUtil;

/**
 * 文 件 名: GlideInterceptor
 * 创 建 人: 陈志鹏
 * 创建日期: 2017/7/30 17:48
 * 邮   箱: ch_zh_p@qq.com
 * 修改时间:
 * 修改备注:
 */

public class GlideInterceptor implements Interceptor{

    private final RequestManager mRequestManager;

    public GlideInterceptor(Context context) {
        Preconditions.checkNotNull(context);

        mRequestManager = Glide.with(context);
    }

    @Override
    public BitmapRegionDecoder intercept(Chain chain) throws IOException {
        final Uri uri = chain.uri();
        BitmapRegionDecoder decoder = chain.chain(uri);
        if (decoder != null){
            return decoder;
        }

        FutureTarget<File> target;

        if (UriUtil.isLocalAssetUri(uri)){
            String assetsName = uri.getPath().substring(1);
            Log.d("GlideInterceptor", "名字"+assetsName);
            target = mRequestManager.downloadOnly()
                    .load("file:///android_asset/"+assetsName)
                    .submit(Target.SIZE_ORIGINAL,Target.SIZE_ORIGINAL);
        }else if (UriUtil.isLocalFileUri(uri)){
            File file = new File(uri.getPath());
            Log.d("GlideInterceptor", "路径"+file.getAbsolutePath());
            target = mRequestManager.downloadOnly()
                    .load(file)
                    .submit(Target.SIZE_ORIGINAL,Target.SIZE_ORIGINAL);
        }else if (UriUtil.isLocalResourceUri(uri)){
            int resId = Integer.parseInt(uri.getPath().substring(1));
            Log.d("GlideInterceptor", "加载Res");
            target = mRequestManager.downloadOnly()
                    .load(resId)
                    .submit(Target.SIZE_ORIGINAL,Target.SIZE_ORIGINAL);
        }else if (UriUtil.isNetworkUri(uri) ||
                UriUtil.isLocalContentUri(uri) ||
                UriUtil.isQualifiedResourceUri(uri)){
            target = mRequestManager.downloadOnly().load(uri).submit(Target.SIZE_ORIGINAL,Target.SIZE_ORIGINAL);
        }else {
            return null;
        }

        try {
            File file = target.get();
            Log.d("GlideInterceptor", "用GlideInterceptor加载回来"+file.getAbsolutePath());
            decoder = BitmapRegionDecoder.newInstance(new FileInputStream(file),false);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        mRequestManager.clear(target);
        return decoder;
    }
}
