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
 * 文 件 名: Translation
 * 创 建 人: 陈志鹏
 * 创建日期: 2017/4/15 00:03
 * 邮   箱: ch_zh_p@qq.com
 * 修改时间:
 * 修改备注:
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef({
        Translation.INSIDE,
        Translation.OUTSIDE,
        Translation.CENTER,
        Translation.COUSTOM
})
public @interface Translation {
    /**
     * 图像始终全部显示在控件范围内
     */
    int INSIDE = 1;
    /**
     * 允许图像刚好平移到不可见
     */
    int OUTSIDE = 2;
    /**
     * 允许图像被平移，直到角落到屏幕的中心，但不再进一步。 当您想要将图像上的任何位置平移到屏幕的正确中心时很有用。
     */
    int CENTER = 3;

    /**
     * 自定义范围
     */
    int COUSTOM = 4;
}
