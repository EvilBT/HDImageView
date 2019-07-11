package xyz.zpayh.hdimage.datasource.interceptor;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.annotation.NonNull;

import xyz.zpayh.hdimage.datasource.OrientationInterceptor;
import xyz.zpayh.hdimage.state.Orientation;

import static xyz.zpayh.hdimage.state.Orientation.ORIENTATION_0;
import static xyz.zpayh.hdimage.state.Orientation.ORIENTATION_180;
import static xyz.zpayh.hdimage.state.Orientation.ORIENTATION_270;
import static xyz.zpayh.hdimage.state.Orientation.ORIENTATION_90;

public class ContentOrientationInterceptor implements OrientationInterceptor {
    @Override
    public int getExifOrientation(@NonNull Context context, String sourceUri) {
        if (sourceUri.startsWith(ContentResolver.SCHEME_CONTENT)) {
            final String[] columns = {MediaStore.Images.Media.ORIENTATION};
            final Cursor cursor = context.getContentResolver()
                    .query(Uri.parse(sourceUri),columns,null,null,null);
            if (cursor != null){
                if (cursor.moveToFirst()){
                    int orientation = cursor.getInt(0);
                    if (orientation == ORIENTATION_0){
                        return ORIENTATION_0;
                    } else if (orientation == ORIENTATION_90){
                        return ORIENTATION_90;
                    } else if (orientation == ORIENTATION_180){
                        return ORIENTATION_180;
                    } else if (orientation == ORIENTATION_270){
                        return ORIENTATION_270;
                    }
                }
                cursor.close();
            }
        }
        return Orientation.ORIENTATION_EXIF;
    }
}
