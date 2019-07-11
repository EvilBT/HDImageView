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
 * 文 件 名: Orientation
 * 创 建 人: 陈志鹏
 * 创建日期: 2017/4/14 20:17
 * 邮   箱: ch_zh_p@qq.com
 * 修改时间:
 * 修改备注:
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef({
        Orientation.ORIENTATION_EXIF,
        Orientation.ORIENTATION_0,
        Orientation.ORIENTATION_90,
        Orientation.ORIENTATION_180,
        Orientation.ORIENTATION_270})
public @interface Orientation {
    int ORIENTATION_EXIF    = -1;
    int ORIENTATION_0       = 0;
    int ORIENTATION_90      = 1;
    int ORIENTATION_180     = 2;
    int ORIENTATION_270     = 3;
}
