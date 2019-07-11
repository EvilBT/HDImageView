package xyz.zpayh.original;

import android.app.Application;
import android.content.Context;
import android.graphics.BitmapRegionDecoder;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.exifinterface.media.ExifInterface;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.FutureTarget;
import com.facebook.binaryresource.BinaryResource;
import com.facebook.binaryresource.FileBinaryResource;
import com.facebook.cache.common.CacheKey;
import com.facebook.common.internal.Closeables;
import com.facebook.common.memory.PooledByteBuffer;
import com.facebook.common.memory.PooledByteBufferInputStream;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.datasource.DataSources;
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.request.ImageRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import xyz.zpayh.hdimage.core.HDImageViewConfig;
import xyz.zpayh.hdimage.core.HDImageViewFactory;
import xyz.zpayh.hdimage.datasource.Interceptor;
import xyz.zpayh.hdimage.datasource.OrientationInterceptor;
import xyz.zpayh.hdimage.datasource.interceptor.Interceptors;
import xyz.zpayh.hdimage.state.Orientation;
import xyz.zpayh.hdimage.util.Preconditions;
import xyz.zpayh.hdimage.util.UriUtil;

import static xyz.zpayh.hdimage.state.Orientation.ORIENTATION_0;
import static xyz.zpayh.hdimage.state.Orientation.ORIENTATION_180;
import static xyz.zpayh.hdimage.state.Orientation.ORIENTATION_270;
import static xyz.zpayh.hdimage.state.Orientation.ORIENTATION_90;

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
                //.addInterceptor(new FrescoInterceptor())
                .addInterceptor(new GlideInterceptor(this))
                //.addOrientationInterceptor(new GlideOrientationInterceptor(this))
                .build();
        HDImageViewFactory.initialize(config);
    }

    public class FrescoInterceptor implements Interceptor {
        @Override
        public BitmapRegionDecoder intercept(Chain chain) throws IOException {
            final Uri uri = chain.uri();
            BitmapRegionDecoder decoder = chain.chain(uri);
            if (decoder != null){
                return decoder;
            }

            if (UriUtil.isNetworkUri(uri)){
                ImagePipeline imagePipeline = ImagePipelineFactory.getInstance().getImagePipeline();

                ImageRequest request = ImageRequest.fromUri(uri);
                DataSource<CloseableReference<PooledByteBuffer>> dataSource = imagePipeline.fetchEncodedImage(request,null);
                try {
                    CloseableReference<PooledByteBuffer> ref = DataSources.waitForFinalResult(dataSource);
                    if (ref == null){
                        return null;
                    }
                    PooledByteBuffer result = ref.get();
                    if (BuildConfig.DEBUG) {
                        Log.d("FrescoInterceptor", "从我这加载");
                    }
                    try {
                        InputStream inputStream = new PooledByteBufferInputStream(result);
                        Closeables.closeQuietly(inputStream);
                        return BitmapRegionDecoder.newInstance(inputStream,false);
                    } catch (IOException e) {
                        e.printStackTrace();
                        ImageRequest imageRequest=ImageRequest.fromUri(uri);
                        CacheKey cacheKey= DefaultCacheKeyFactory.getInstance().getEncodedCacheKey(imageRequest,null);
                        BinaryResource resource = ImagePipelineFactory.getInstance().getMainFileCache().getResource(cacheKey);
                        File file=((FileBinaryResource)resource).getFile();
                        if (BuildConfig.DEBUG) {
                            Log.d("FrescoInterceptor", file.getName());
                        }
                        return Interceptors.fixJPEGDecoder(file,e);
                    }
                } catch (Throwable throwable) {
                    if (BuildConfig.DEBUG) {
                        Log.d("FrescoInterceptor", "intercept: 加载失败了");
                    }
                    throwable.printStackTrace();
                    return null;
                }
            }

            return null;
        }
    }

    public class GlideOrientationInterceptor implements OrientationInterceptor {

        private final RequestManager mRequestManager;

        public GlideOrientationInterceptor(Context context) {
            Preconditions.checkNotNull(context);

            mRequestManager = Glide.with(context);
        }

        @Override
        public int getExifOrientation(@NonNull Context context, String sourceUri) {
            int orientation = Orientation.ORIENTATION_EXIF;
            Log.d("Sherlock", "从这儿加载: ");
            if (UriUtil.isNetworkUri(Uri.parse(sourceUri))) {
                FutureTarget<File> target = mRequestManager.downloadOnly().load(Uri.parse(sourceUri)).submit();
                try {
                    File file = target.get();
                    try {
                        ExifInterface exifInterface = new ExifInterface(new FileInputStream(file));
                        int orientationAttr = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                                ExifInterface.ORIENTATION_NORMAL);
                        if (orientationAttr == ExifInterface.ORIENTATION_NORMAL ||
                                orientationAttr == ExifInterface.ORIENTATION_UNDEFINED) {
                            orientation = ORIENTATION_0;
                        } else if (orientationAttr == ExifInterface.ORIENTATION_ROTATE_90) {
                            orientation = ORIENTATION_90;
                        } else if (orientationAttr == ExifInterface.ORIENTATION_ROTATE_180) {
                            orientation = ORIENTATION_180;
                        } else if (orientationAttr == ExifInterface.ORIENTATION_ROTATE_270) {
                            orientation = ORIENTATION_270;
                        } else {
                            Log.w("Sherlock", "Unsupported EXIF orientation: " + orientationAttr);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.w("Sherlock", "Could not get EXIF orientation of image");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mRequestManager.clear(target);
            }
            Log.d("Sherlock", "返回了："+orientation);
            return orientation;
        }
    }

    public class GlideInterceptor implements Interceptor {

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
                    try {
                        Log.d("GlideInterceptor", "用GlideInterceptor加载回来"+file.getAbsolutePath());
                        decoder = BitmapRegionDecoder.newInstance(new FileInputStream(file),false);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return Interceptors.fixJPEGDecoder(file, e);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                mRequestManager.clear(target);
            }
            return decoder;
        }
    }
}
