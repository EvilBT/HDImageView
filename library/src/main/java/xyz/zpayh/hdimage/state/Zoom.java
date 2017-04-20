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

package xyz.zpayh.hdimage.state;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 文 件 名: Zoom
 * 创 建 人: 陈志鹏
 * 创建日期: 2017/4/15 00:18
 * 邮   箱: ch_zh_p@qq.com
 * 修改时间:
 * 修改备注:
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef({
        Zoom.ZOOM_FOCUS_FIXED,
        Zoom.ZOOM_FOCUS_CENTER,
        Zoom.ZOOM_FOCUS_CENTER_IMMEDIATE
})
public @interface Zoom {

    /**
     * 在缩放动画过程中，保持拍摄在相同位置的图像的点，并在其周围缩放图像。
     */
    int ZOOM_FOCUS_FIXED = 1;
    /**
     * 在缩放动画过程中，将拍摄的图像点移动到屏幕中央。
     */
    int ZOOM_FOCUS_CENTER = 2;
    /**
     * 立即放大并立即对中心点进行动画处理。
     */
    int ZOOM_FOCUS_CENTER_IMMEDIATE = 3;
}
