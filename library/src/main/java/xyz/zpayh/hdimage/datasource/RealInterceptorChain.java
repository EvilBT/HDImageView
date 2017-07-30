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

import android.graphics.BitmapRegionDecoder;
import android.net.Uri;

import java.io.IOException;
import java.util.List;

/**
 * 文 件 名: RealInterceptorChain
 * 创 建 人: 陈志鹏
 * 创建日期: 2017/7/29 17:03
 * 邮   箱: ch_zh_p@qq.com
 * 修改时间:
 * 修改备注:
 */

public final class RealInterceptorChain implements Interceptor.Chain {

    private final List<Interceptor> mInterceptors;
    private final int mIndex;
    private final Uri mUri;

    RealInterceptorChain(List<Interceptor> interceptors, int index, Uri uri) {
        mInterceptors = interceptors;
        mIndex = index;
        mUri = uri;
    }

    @Override
    public Uri uri() {
        return mUri;
    }

    @Override
    public BitmapRegionDecoder chain(Uri uri) throws IOException{
        if (mIndex >= mInterceptors.size()) {
            return null;
        }

        RealInterceptorChain next = new RealInterceptorChain(mInterceptors,mIndex+1,uri);
        Interceptor interceptor = mInterceptors.get(mIndex);

        return interceptor.intercept(next);
    }
}
