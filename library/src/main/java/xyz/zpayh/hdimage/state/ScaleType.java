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

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 文 件 名: ScaleType
 * 创 建 人: 陈志鹏
 * 创建日期: 2017/4/15 02:13
 * 邮   箱: ch_zh_p@qq.com
 * 修改时间:
 * 修改备注:
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef({
        ScaleType.CENTER_INSIDE,
        ScaleType.CENTER_CROP,
        ScaleType.CUSTOM
})
public @interface ScaleType {
    /**
     * 缩放图像，使图像的两个维度均等于或小于视图的相应维度。 图像然后在视图中居中。 这是默认行为，最适合画廊。
     */
    int CENTER_INSIDE = 1;
    /**
     * 缩放图像均匀，使图像的两个尺寸都等于或大于视图的相应尺寸。 图像然后在视图中居中。
     */
    int CENTER_CROP = 2;
    /**
     * 缩放图像，使图像的两个维度均等于或小于maxScale，并等于或大于minScale。 图像然后在视图中居中。
     */
    int CUSTOM = 3;
}
