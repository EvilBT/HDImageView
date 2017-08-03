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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import xyz.zpayh.hdimage.datasource.Interceptor;
import xyz.zpayh.hdimage.util.UriUtil;

/**
 * 文 件 名: FileInterceptor
 * 创 建 人: 陈志鹏
 * 创建日期: 2017/7/29 17:49
 * 邮   箱: ch_zh_p@qq.com
 * 修改时间:
 * 修改备注:
 */

public class FileInterceptor implements Interceptor {

    @Override
    public BitmapRegionDecoder intercept(Chain chain) throws IOException {
        final Uri uri = chain.uri();
        BitmapRegionDecoder decoder = chain.chain(uri);
        if (decoder != null){
            return decoder;
        }


        if (UriUtil.isLocalFileUri(uri)){
            File file = new File(uri.getPath());
            Log.d("FileInterceptor","从我这加载");
            try {
                return BitmapRegionDecoder.newInstance(new FileInputStream(file.toString()),false);
            } catch (IOException e) {
                return Interceptors.fixJPEGDecoder(file,e);
            }
        }
        return null;
    }
}
