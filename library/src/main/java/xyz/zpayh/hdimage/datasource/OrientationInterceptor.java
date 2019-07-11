package xyz.zpayh.hdimage.datasource;

import android.content.Context;
import androidx.annotation.NonNull;

import xyz.zpayh.hdimage.state.Orientation;

public interface OrientationInterceptor {
    @Orientation
    int getExifOrientation(@NonNull Context context, String sourceUri);
}
