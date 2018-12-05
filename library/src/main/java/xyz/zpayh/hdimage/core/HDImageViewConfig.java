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

package xyz.zpayh.hdimage.core;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.media.ExifInterface;
import android.util.Log;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import xyz.zpayh.hdimage.datasource.Interceptor;
import xyz.zpayh.hdimage.datasource.OrientationInterceptor;
import xyz.zpayh.hdimage.datasource.interceptor.AssetInterceptor;
import xyz.zpayh.hdimage.datasource.interceptor.AssetOrientationInterceptor;
import xyz.zpayh.hdimage.datasource.interceptor.ContentInterceptor;
import xyz.zpayh.hdimage.datasource.interceptor.ContentOrientationInterceptor;
import xyz.zpayh.hdimage.datasource.interceptor.FileInterceptor;
import xyz.zpayh.hdimage.datasource.interceptor.FileOrientationInterceptor;
import xyz.zpayh.hdimage.datasource.interceptor.Interceptors;
import xyz.zpayh.hdimage.datasource.interceptor.NetworkInterceptor;
import xyz.zpayh.hdimage.datasource.interceptor.NetworkOrientationInterceptor;
import xyz.zpayh.hdimage.datasource.interceptor.ResourceInterceptor;
import xyz.zpayh.hdimage.util.Preconditions;

import static android.os.Build.VERSION.SDK_INT;
import static xyz.zpayh.hdimage.datasource.BitmapDataSource.ASSET_SCHEME;
import static xyz.zpayh.hdimage.datasource.BitmapDataSource.FILE_SCHEME;
import static xyz.zpayh.hdimage.state.Orientation.ORIENTATION_0;
import static xyz.zpayh.hdimage.state.Orientation.ORIENTATION_180;
import static xyz.zpayh.hdimage.state.Orientation.ORIENTATION_270;
import static xyz.zpayh.hdimage.state.Orientation.ORIENTATION_90;
import static xyz.zpayh.hdimage.state.Orientation.ORIENTATION_EXIF;

/**
 * 文 件 名: HDImageViewConfig
 * 创 建 人: 陈志鹏
 * 创建日期: 2017/7/29 14:19
 * 邮   箱: ch_zh_p@qq.com
 * 修改时间:
 * 修改备注:
 */

public class HDImageViewConfig {
    private static final String TAG = "HDImageViewConfig";
    private final Interpolator mScaleAnimationInterpolator;
    private final Interpolator mTranslationAnimationInterpolator;

    private final List<Interceptor> mInterceptors;
    private final List<OrientationInterceptor> mOrientationInterceptors;

    public static Builder newBuilder(Context context){
        return new Builder(context);
    }

    private HDImageViewConfig(Builder builder){
        mScaleAnimationInterpolator = builder.mScaleAnimationInterpolator == null ?
                new DecelerateInterpolator() : builder.mScaleAnimationInterpolator;
        mTranslationAnimationInterpolator = builder.mTranslationAnimationInterpolator == null ?
                new DecelerateInterpolator() : builder.mTranslationAnimationInterpolator;

        mInterceptors = new ArrayList<>();


        mInterceptors.add(new ResourceInterceptor(builder.mContext.getResources()));
        mInterceptors.add(new AssetInterceptor(builder.mContext.getAssets()));
        mInterceptors.add(new ContentInterceptor(builder.mContext));
        mInterceptors.add(new FileInterceptor());

        Interceptor glideInterceptor = addGlideInterceptor(builder.mContext);
        if (glideInterceptor != null){
            mInterceptors.add(glideInterceptor);
        }

        Interceptor frescoInterceptor = addFrescoInterceptor();
        if (frescoInterceptor != null){
            mInterceptors.add(frescoInterceptor);
        }

        if (glideInterceptor == null && frescoInterceptor == null) {
            mInterceptors.add(new NetworkInterceptor(builder.mContext));
        }
        mInterceptors.addAll(builder.mInterceptors);

        mOrientationInterceptors = new ArrayList<>();
        mOrientationInterceptors.addAll(builder.mOrientationInterceptors);

        mOrientationInterceptors.add(new AssetOrientationInterceptor());
        mOrientationInterceptors.add(new ContentOrientationInterceptor());
        mOrientationInterceptors.add(new FileOrientationInterceptor());
        mOrientationInterceptors.add(new NetworkOrientationInterceptor());

        //init
        Interceptors.initDiskLruCache(builder.mContext);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private Interceptor addGlideInterceptor(Context context){
        Interceptor interceptor = null;
        try {
            Class<Interceptor> clazz =
                    (Class<Interceptor>) Class.forName("xyz.zpayh.hdimage.datasource.interceptor.GlideInterceptor");
            Constructor<Interceptor> constructor = clazz.getConstructor(Context.class);
            interceptor = constructor.newInstance(context);
        } catch (ClassNotFoundException e) {
        } catch (NoSuchMethodException e) {
        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }
        return interceptor;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private Interceptor addFrescoInterceptor(){
        Interceptor interceptor = null;

        try {
            Class<Interceptor> clazz =
                    (Class<Interceptor>) Class.forName("xyz.zpayh.hdimage.datasource.interceptor.FrescoInterceptor");
            interceptor = clazz.newInstance();
        } catch (ClassNotFoundException e) {
        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
        }
        return interceptor;
    }

    public Interpolator getScaleAnimationInterpolator() {
        return mScaleAnimationInterpolator;
    }

    public Interpolator getTranslationAnimationInterpolator() {
        return mTranslationAnimationInterpolator;
    }

    public List<Interceptor> getInterceptors() {
        return mInterceptors;
    }

    public List<OrientationInterceptor> getOrientationInterceptors() {
        return mOrientationInterceptors;
    }

    public static class Builder {
        private Interpolator mScaleAnimationInterpolator;
        private Interpolator mTranslationAnimationInterpolator;
        private Context mContext;
        private List<Interceptor> mInterceptors;
        private final List<OrientationInterceptor> mOrientationInterceptors;
        private Builder(Context context){
            mContext = Preconditions.checkNotNull(context);
            mInterceptors = new ArrayList<>();
            mOrientationInterceptors = new ArrayList<>();
        }

        public Builder setScaleAnimationInterpolator(Interpolator scaleAnimationInterpolator) {
            mScaleAnimationInterpolator = scaleAnimationInterpolator;
            return this;
        }

        public Builder setTranslationAnimationInterpolator(Interpolator translationAnimationInterpolator) {
            mTranslationAnimationInterpolator = translationAnimationInterpolator;
            return this;
        }

        public Builder addInterceptor(Interceptor interceptor){
            mInterceptors.add(interceptor);
            return this;
        }

        public Builder addOrientationInterceptor(OrientationInterceptor interceptor){
            mOrientationInterceptors.add(interceptor);
            return this;
        }

        public HDImageViewConfig build(){
            return new HDImageViewConfig(this);
        }
    }
}
