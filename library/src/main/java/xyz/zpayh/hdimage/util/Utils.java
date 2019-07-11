/*
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.zpayh.hdimage.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.exifinterface.media.ExifInterface;
import android.util.Log;

import java.io.IOException;

import xyz.zpayh.hdimage.state.Orientation;

import static android.os.Build.VERSION.SDK_INT;
import static xyz.zpayh.hdimage.datasource.BitmapDataSource.ASSET_SCHEME;
import static xyz.zpayh.hdimage.datasource.BitmapDataSource.FILE_SCHEME;
import static xyz.zpayh.hdimage.state.Orientation.ORIENTATION_0;
import static xyz.zpayh.hdimage.state.Orientation.ORIENTATION_180;
import static xyz.zpayh.hdimage.state.Orientation.ORIENTATION_270;
import static xyz.zpayh.hdimage.state.Orientation.ORIENTATION_90;
import static xyz.zpayh.hdimage.state.Orientation.ORIENTATION_EXIF;

/**
 * Class containing some static utility methods.
 */
public class Utils {
    private static final String TAG = "Utils";
    private Utils() {}

    @Orientation
    public static int getExifOrientation(@NonNull Context context, String sourceUri){
        int exifOrientation = ORIENTATION_0;
        if (sourceUri.startsWith(ContentResolver.SCHEME_CONTENT)){
            final String[] columns = {MediaStore.Images.Media.ORIENTATION};
            final Cursor cursor = context.getContentResolver()
                    .query(Uri.parse(sourceUri),columns,null,null,null);
            if (cursor != null){
                if (cursor.moveToFirst()){
                    int orientation = cursor.getInt(0);
                    if (orientation == ORIENTATION_0){
                        exifOrientation = ORIENTATION_0;
                    } else if (orientation == ORIENTATION_90){
                        exifOrientation = ORIENTATION_90;
                    } else if (orientation == ORIENTATION_180){
                        exifOrientation = ORIENTATION_180;
                    } else if (orientation == ORIENTATION_270){
                        exifOrientation = ORIENTATION_270;
                    } else{
                        Log.w(TAG, "Unsupported orientation: " + orientation);
                    }
                }
                cursor.close();
            }
            return exifOrientation;
        }

        if (sourceUri.startsWith(FILE_SCHEME)){
            try {
                ExifInterface exifInterface = new ExifInterface(sourceUri.substring(FILE_SCHEME.length()));
                int orientationAttr = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL);
                if (orientationAttr == ExifInterface.ORIENTATION_NORMAL ||
                        orientationAttr == ExifInterface.ORIENTATION_UNDEFINED){
                    exifOrientation = ORIENTATION_0;
                }else if (orientationAttr == ExifInterface.ORIENTATION_ROTATE_90){
                    exifOrientation = ORIENTATION_90;
                }else if (orientationAttr == ExifInterface.ORIENTATION_ROTATE_180){
                    exifOrientation = ORIENTATION_180;
                }else if (orientationAttr == ExifInterface.ORIENTATION_ROTATE_270){
                    exifOrientation = ORIENTATION_270;
                }else{
                    Log.w(TAG, "Unsupported EXIF orientation: " + orientationAttr);
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.w(TAG, "Could not get EXIF orientation of image");
            }

            return exifOrientation;
        }

        if (sourceUri.startsWith(ASSET_SCHEME) && SDK_INT >= 24){
            try {
                String assetName = sourceUri.substring(ASSET_SCHEME.length());
                ExifInterface exifInterface = new ExifInterface(context.getAssets().open(assetName, AssetManager.ACCESS_RANDOM));
                int orientationAttr = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL);
                if (orientationAttr == ExifInterface.ORIENTATION_NORMAL ||
                        orientationAttr == ExifInterface.ORIENTATION_UNDEFINED){
                    exifOrientation = ORIENTATION_0;
                }else if (orientationAttr == ExifInterface.ORIENTATION_ROTATE_90){
                    exifOrientation = ORIENTATION_90;
                }else if (orientationAttr == ExifInterface.ORIENTATION_ROTATE_180){
                    exifOrientation = ORIENTATION_180;
                }else if (orientationAttr == ExifInterface.ORIENTATION_ROTATE_270){
                    exifOrientation = ORIENTATION_270;
                }else{
                    Log.w(TAG, "Unsupported EXIF orientation: " + orientationAttr);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return exifOrientation;
    }

    @SuppressWarnings("SuspiciousNameCombination")
    public static void fileRect(@NonNull Rect rect, @NonNull Rect target, int width,
                          int height, int rotation){
        switch (rotation){
            case ORIENTATION_0:
                target.set(rect);
                return;
            case ORIENTATION_90:
                target.set(rect.top,height - rect.right,
                        rect.bottom,height - rect.left);
                return;
            case ORIENTATION_180:
                target.set(width - rect.right, height - rect.bottom,
                        width - rect.left, height - rect.top);
                return;
            case ORIENTATION_270:
            case ORIENTATION_EXIF:
            default:
                target.set(width-rect.bottom,rect.left,width-rect.top,
                        rect.right);
        }
    }

    public static boolean hasHoneycombMR1() {
        return Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB_MR1;
    }

    public static boolean hasKitKat() {
        return Build.VERSION.SDK_INT >= VERSION_CODES.KITKAT;
    }
}
