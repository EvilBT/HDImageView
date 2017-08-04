package xyz.zpayh.original;

import android.app.Application;

import com.facebook.imagepipeline.core.ImagePipelineFactory;

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
        //HDImageViewConfig config = HDImageViewConfig.newBuilder(this)
                //.addInterceptor(new FrescoInterceptor())
        //        .addInterceptor(new GlideInterceptor(this))
        //        .build();
        //HDImageViewFactory.initialize(config);
    }

    /*public class GlideInterceptor implements Interceptor {

        private final RequestManager mRequestManager;

        public GlideInterceptor(Context context) {
            Preconditions.checkNotNull(context);

            mRequestManager = Glide.with(context);
        }

        @Override
        public BitmapRegionDecoder intercept(Chain chain) throws IOException {
            final Uri uri = chain.uri();
            BitmapRegionDecoder decoder = chain.chain(uri);
            if (decoder != null){
                return decoder;
            }

            if (UriUtil.isNetworkUri(uri)){
                FutureTarget<File> target = mRequestManager.downloadOnly().load(uri).submit();
                try {
                    File file = target.get();
                    Log.d("GlideInterceptor", "用GlideInterceptor加载回来"+file.getAbsolutePath());
                    FileInputStream inputStream = new FileInputStream(file);
                    test(inputStream);
                    decoder = BitmapRegionDecoder.newInstance(new FileInputStream(file),false);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                mRequestManager.clear(target);
            }
            return decoder;
        }
    }

    private void test(FileInputStream inputStream) throws IOException{
        ExifInterface exifInterface = new ExifInterface(inputStream);
        Log.d("Application",""+exifInterface.getAttribute(ExifInterface.TAG_COLOR_SPACE));
        Log.d("Application",""+exifInterface.getAttribute(ExifInterface.TAG_APERTURE_VALUE));
        Log.d("Application",""+exifInterface.getAttribute(ExifInterface.TAG_ARTIST));
        Log.d("Application",""+exifInterface.getAttribute(ExifInterface.TAG_BITS_PER_SAMPLE));
        Log.d("Application",""+exifInterface.getAttribute(ExifInterface.TAG_BRIGHTNESS_VALUE));
        Log.d("Application",""+exifInterface.getAttribute(ExifInterface.TAG_CFA_PATTERN));
        Log.d("Application",""+exifInterface.getAttribute(ExifInterface.TAG_COMPONENTS_CONFIGURATION));
        Log.d("Application",""+exifInterface.getAttribute(ExifInterface.TAG_COMPRESSED_BITS_PER_PIXEL));
        Log.d("Application",""+exifInterface.getAttribute(ExifInterface.TAG_COMPRESSION));
        Log.d("Application",""+exifInterface.getAttribute(ExifInterface.TAG_CONTRAST));
        Log.d("Application",""+exifInterface.getAttribute(ExifInterface.TAG_COPYRIGHT));
        Log.d("Application",""+exifInterface.getAttribute(ExifInterface.TAG_CUSTOM_RENDERED));
        Log.d("Application",""+exifInterface.getAttribute(ExifInterface.TAG_CUSTOM_RENDERED));
        Log.d("Application",""+exifInterface.getAttribute(ExifInterface.TAG_DATETIME));
    }*/
}
