package xyz.zpayh.hdimage.datasource.interceptor;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.media.ExifInterface;

import java.io.IOException;

import xyz.zpayh.hdimage.datasource.OrientationInterceptor;
import xyz.zpayh.hdimage.state.Orientation;

import static xyz.zpayh.hdimage.datasource.BitmapDataSource.FILE_SCHEME;

public class FileOrientationInterceptor implements OrientationInterceptor {
    @Override
    public int getExifOrientation(@NonNull Context context, String sourceUri) {
        if (sourceUri.startsWith(FILE_SCHEME)) {
            try {
                String fileName = sourceUri.substring(FILE_SCHEME.length());
                ExifInterface exifInterface = new ExifInterface(fileName);
                int orientationAttr = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_NORMAL);
                switch (orientationAttr) {
                    case ExifInterface.ORIENTATION_NORMAL:
                    case ExifInterface.ORIENTATION_UNDEFINED:
                        return Orientation.ORIENTATION_0;
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        return Orientation.ORIENTATION_90;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        return Orientation.ORIENTATION_180;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        return Orientation.ORIENTATION_270;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return Orientation.ORIENTATION_EXIF;
    }
}
