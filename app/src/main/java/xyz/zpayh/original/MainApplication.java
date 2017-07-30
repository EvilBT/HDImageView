package xyz.zpayh.original;

import android.app.Application;

import com.facebook.imagepipeline.core.ImagePipelineFactory;

import xyz.zpayh.hdimage.core.HDImageViewConfig;
import xyz.zpayh.hdimage.core.HDImageViewFactory;
import xyz.zpayh.hdimage.datasource.interceptor.FrescoInterceptor;
import xyz.zpayh.hdimage.datasource.interceptor.GlideInterceptor;

/**
 * 文 件 名: MainApplication
 * 创 建 人: 陈志鹏
 * 创建日期: 2017/4/3 16:14
 * 邮   箱: ch_zh_p@qq.com
 * 修改时间:
 * 修改备注:
 */

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ImagePipelineFactory.initialize(this);

        // 与Fresco加载库结合，共享缓存
        HDImageViewConfig config = HDImageViewConfig.newBuilder(this)
                .addInterceptor(new FrescoInterceptor())
                .addInterceptor(new GlideInterceptor(this))
                .build();
        HDImageViewFactory.initialize(config);
    }
}
