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
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;

import xyz.zpayh.hdimage.state.Orientation;

import static xyz.zpayh.hdimage.datasource.BitmapDataSource.RESOURCE_SCHEME;
import static xyz.zpayh.hdimage.state.Orientation.ORIENTATION_EXIF;

/**
 * 文 件 名: ImageSourceBuilder
 * 创 建 人: 陈志鹏
 * 创建日期: 2017/4/14 18:41
 * 邮   箱: ch_zh_p@qq.com
 * 修改时间:
 * 修改备注:
 */

public class ImageSourceBuilder {

    public static Builder newBuilder(){
        return new Builder();
    }

    public static class Builder{
        private Uri mUri;

        @Orientation
        private int mOrientation;

        @Nullable
        private ImageSizeOptions mImageSizeOptions;

        private ImageViewOptions mImageViewOptions;

        @Nullable
        private ImageSourceLoadListener mImageSourceLoadListener;

        @Nullable
        private Rect mImageSourceRegion;

        public Builder() {
            mUri = null;
            mOrientation = ORIENTATION_EXIF;
        }

        public Builder setUri(@Nullable Uri uri){
            mUri = uri;
            return this;
        }

        public Builder setUri(@Nullable String uriString){
            mUri = uriString == null ? null : Uri.parse(uriString);
            return this;
        }

        public Builder setUri(@DrawableRes int resId){
            mUri = Uri.parse(RESOURCE_SCHEME+
                    "HelloWorld/"+resId);
            return this;
        }

        public Builder setOrientation(@Orientation int orientation){
            mOrientation = orientation;
            return this;
        }

        public Builder setImageSizeOptions(@Nullable ImageSizeOptions imageSizeOptions){
            mImageSizeOptions = imageSizeOptions;
            return this;
        }

        public Builder setImageSourceLoadListener(@Nullable ImageSourceLoadListener imageSourceLoadListener){
            mImageSourceLoadListener = imageSourceLoadListener;
            return this;
        }

        public Builder setImageSourceRegion(@Nullable Rect sourceRegion){
            mImageSourceRegion = sourceRegion;
            return this;
        }

        public Builder setImageViewOptions(ImageViewOptions options){
            mImageViewOptions = options;
            return this;
        }

        public ImageSource build(){
            ImageSourceImpl imageSource = new ImageSourceImpl(mUri,mOrientation, mImageSizeOptions,
                    mImageSourceLoadListener,mImageViewOptions,mImageSourceRegion);
            return imageSource;
        }
    }

    private static class ImageSourceImpl implements ImageSource {

        private Uri mUri;

        @Orientation
        private int mOrientation;

        @Nullable
        private ImageSizeOptions mOptions;

        @Nullable
        private ImageSourceLoadListener mImageSourceLoadListener;

        @Nullable
        private Rect mImageSourceRegion;

        private ImageViewOptions mImageViewOptions;

        @Override
        public Uri getUri() {
            return mUri;
        }

        @Override
        public int getOrientation() {
            return mOrientation;
        }

        @Override
        public void setImageSizeOptions(@Nullable ImageSizeOptions imageSizeOptions) {
            mOptions = imageSizeOptions;
        }

        @Nullable
        @Override
        public ImageSizeOptions getImageSizeOptions() {
            return mOptions;
        }

        @Nullable
        @Override
        public Rect getImageSourceRegion() {
            return mImageSourceRegion;
        }

        @Override
        public ImageViewOptions getImageViewOptions() {
            return mImageViewOptions;
        }

        @Nullable
        @Override
        public ImageSourceLoadListener getImageSourceLoadListener() {
            return mImageSourceLoadListener;
        }

        @Override
        public void setOrientation(@Orientation int orientation) {
            mOrientation = orientation;
        }

        @Override
        public void setImageSourceRegion(@Nullable Rect imageSourceRegion) {
            mImageSourceRegion = imageSourceRegion;
        }

        @Override
        public void setImageViewOptions(ImageViewOptions imageViewOptions) {
            mImageViewOptions = imageViewOptions;
        }

        @Override
        public void setImageSourceLoadListener(@Nullable ImageSourceLoadListener imageSourceLoadListener) {
            mImageSourceLoadListener = imageSourceLoadListener;
        }

        private ImageSourceImpl(Uri uri,
                                int orientation,
                                @Nullable ImageSizeOptions options,
                                @Nullable ImageSourceLoadListener imageSourceLoadListener,
                                ImageViewOptions imageViewOptions,
                                @Nullable Rect imageSourceRegion) {
            mUri = uri;
            mOrientation = orientation;
            mOptions = options;
            mImageSourceLoadListener = imageSourceLoadListener;
            mImageViewOptions = imageViewOptions;
            mImageSourceRegion = imageSourceRegion;
        }
    }
}
