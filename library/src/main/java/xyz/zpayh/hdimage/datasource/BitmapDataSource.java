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
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import androidx.annotation.NonNull;

import xyz.zpayh.hdimage.state.Orientation;

/**
 * 文 件 名: BitmapDataSource
 * 创 建 人: 陈志鹏
 * 创建日期: 2017/4/1 17:21
 * 邮   箱: ch_zh_p@qq.com
 * 修改时间:
 * 修改备注:
 */

public interface BitmapDataSource {

    String FILE_SCHEME = "file://";
    String ASSET_SCHEME = "asset:///";
    String RESOURCE_SCHEME = "res://";
    String HTTP_SCHEME = "http://";
    String HTTPS_SCHEME = "https://";

    void init(Context context, Uri uri, Point dimensions, OnInitListener listener);

    Bitmap decode(Rect sRect, int sampleSize);

    boolean isReady();

    void recycle();

    @Orientation
    int getExifOrientation(@NonNull Context context, String sourceUri);

    interface OnInitListener{
        void success();

        void failed(Throwable throwable);
    }
}
