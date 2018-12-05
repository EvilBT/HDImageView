package xyz.zpayh.hdimage.datasource.interceptor;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.media.ExifInterface;
import android.util.Log;

import java.io.IOException;

import xyz.zpayh.hdimage.datasource.OrientationInterceptor;
import xyz.zpayh.hdimage.state.Orientation;

import static xyz.zpayh.hdimage.datasource.BitmapDataSource.FILE_SCHEME;

public class NetworkOrientationInterceptor implements OrientationInterceptor {
    @Override
    public int getExifOrientation(@NonNull Context context, String sourceUri) {
        return Interceptors.getExifOrientation(sourceUri);
    }
}
