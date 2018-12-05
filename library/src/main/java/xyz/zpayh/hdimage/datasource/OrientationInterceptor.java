package xyz.zpayh.hdimage.datasource;

import android.content.Context;
import android.support.annotation.NonNull;

import xyz.zpayh.hdimage.state.Orientation;

public interface OrientationInterceptor {
    @Orientation
    int getExifOrientation(@NonNull Context context, String sourceUri);
}
