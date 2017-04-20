/*
Copyright 2014 David Morrissey

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package xyz.zpayh.hdimage;

import android.graphics.PointF;

/**
 * 文 件 名: ImageViewOptions
 * 创 建 人: 陈志鹏
 * 创建日期: 2017/4/1 19:52
 * 邮   箱: ch_zh_p@qq.com
 * 修改时间:
 * 修改备注:
 */

public class ImageViewOptions {
    private float mScale;
    private float mCenterX;
    private float mCenterY;

    public ImageViewOptions(float scale, PointF center) {
        mScale = scale;
        mCenterX = center.x;
        mCenterY = center.y;
    }

    public float getScale() {
        return mScale;
    }

    public PointF getCenter(){
        return new PointF(mCenterX,mCenterY);
    }
}
