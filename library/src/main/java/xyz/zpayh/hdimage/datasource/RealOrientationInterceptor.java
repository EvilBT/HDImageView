package xyz.zpayh.hdimage.datasource;

import android.content.Context;
import androidx.annotation.NonNull;

import java.util.List;

import xyz.zpayh.hdimage.state.Orientation;

public class RealOrientationInterceptor implements OrientationInterceptor{

    private final List<OrientationInterceptor> mInterceptors;

    RealOrientationInterceptor(List<OrientationInterceptor> interceptors) {
        mInterceptors = interceptors;
    }

    @Override
    public int getExifOrientation(@NonNull Context context, String sourceUri) {
        for (OrientationInterceptor interceptor : mInterceptors) {
            int orientation = interceptor.getExifOrientation(context, sourceUri);
            if (orientation != Orientation.ORIENTATION_EXIF) {
                return orientation;
            }
        }
        return Orientation.ORIENTATION_0;
    }
}
