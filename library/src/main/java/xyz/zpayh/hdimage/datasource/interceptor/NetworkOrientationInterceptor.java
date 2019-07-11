package xyz.zpayh.hdimage.datasource.interceptor;

import android.content.Context;
import androidx.annotation.NonNull;

import xyz.zpayh.hdimage.datasource.OrientationInterceptor;

public class NetworkOrientationInterceptor implements OrientationInterceptor {
    @Override
    public int getExifOrientation(@NonNull Context context, String sourceUri) {
        return Interceptors.getExifOrientation(sourceUri);
    }
}
