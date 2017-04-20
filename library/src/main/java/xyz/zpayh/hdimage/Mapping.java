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

import android.graphics.Bitmap;
import android.graphics.Rect;

/**
 * 文 件 名: Mapping
 * 创 建 人: 陈志鹏
 * 创建日期: 2017/4/1 15:12
 * 邮   箱: ch_zh_p@qq.com
 * 修改时间:
 * 修改备注: 贴图
 */

public class Mapping {
    public Rect mSourceRect;
    public int mSampleSize;
    public Bitmap mBitmap;
    public boolean mLoading;
    public boolean mVisible;
    public Rect mViewRect;
    public Rect mFileSourceRect;
}
