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

package xyz.zpayh.hdimage;

import android.graphics.Rect;
import android.net.Uri;
import android.support.annotation.Nullable;

import xyz.zpayh.hdimage.state.Orientation;

/**
 * 文 件 名: ImageSource
 * 创 建 人: 陈志鹏
 * 创建日期: 2017/4/14 18:28
 * 邮   箱: ch_zh_p@qq.com
 * 修改时间:
 * 修改备注:
 */

public interface ImageSource {

    Uri getUri();

    @Orientation
    int getOrientation();

    void setOrientation(@Orientation int orientation);

    @Nullable
    ImageSizeOptions getImageSizeOptions();

    void setImageSizeOptions(@Nullable ImageSizeOptions imageSizeOptions);

    Rect getImageSourceRegion();

    void setImageSourceRegion(Rect imageSourceRegion);

    ImageViewOptions getImageViewOptions();

    void setImageViewOptions(ImageViewOptions imageViewOptions);

    ImageSourceLoadListener getImageSourceLoadListener();

    void setImageSourceLoadListener(ImageSourceLoadListener imageSourceLoadListener);
}
