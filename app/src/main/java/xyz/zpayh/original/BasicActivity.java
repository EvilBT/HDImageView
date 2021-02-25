package xyz.zpayh.original;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import xyz.zpayh.hdimage.HDImageView;
import xyz.zpayh.hdimage.ImageSizeOptions;
import xyz.zpayh.hdimage.ImageSource;
import xyz.zpayh.hdimage.ImageSourceBuilder;
import xyz.zpayh.hdimage.ImageSourceLoadListener;

import static xyz.zpayh.hdimage.state.ScaleType.CUSTOM;
import static xyz.zpayh.original.UriConstants.IMAGE_11;
import static xyz.zpayh.original.UriConstants.IMAGE_12;

public class BasicActivity extends AppCompatActivity {

    private static final String TAG = "BasicActivity";
    HDImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_basic);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mImageView = (HDImageView) findViewById(R.id.image);
        mImageView.setMaxScale(20f);
        mImageView.setMinScale(1f);
        mImageView.setDoubleTapZoomScale(10F);
        mImageView.setScaleType(CUSTOM);
        if (savedInstanceState == null) {
            ImageSource imageSource = ImageSourceBuilder.newBuilder()
                    .setUri(IMAGE_12)
                    .setImageSourceLoadListener(new ImageSourceLoadListener() {
                        @Override
                        public void loadSuccess(Uri uri, ImageSizeOptions options) {
                            float scaleW = mImageView.getWidth() / options.mWidth;
                            float scaleH = mImageView.getHeight() / options.mHeight;
                            mImageView.setMinScale(Math.min(1.0f,Math.min(scaleW, scaleH)));
                            mImageView.resetScaleAndCenter();
                        }
                    })
                    .build();
            mImageView.setImageSource(imageSource);
        }

        mImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(BasicActivity.this, "A Long Click Event", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(BasicActivity.this, "A Click Event", Toast.LENGTH_SHORT).show();
            }
        });

        /*Spinner spinner = (Spinner) findViewById(R.id.sp_pan_state);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0){
                    mImageView.setTranslateLimit(INSIDE);
                }else if (position == 1){
                    mImageView.setTranslateLimit(OUTSIDE);
                }else if (position == 2){
                    mImageView.setTranslateLimit(CENTER);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });*/

        /*spinner = (Spinner) findViewById(R.id.sp_orientation);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0){
                    mImageView.setOrientation(ORIENTATION_0);
                }else if (position == 1){
                    mImageView.setOrientation(ORIENTATION_90);
                }else if (position == 2){
                    mImageView.setOrientation(ORIENTATION_180);
                }else if (position == 3){
                    mImageView.setOrientation(ORIENTATION_270);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });*/

        /*spinner = (Spinner) findViewById(R.id.sp_zoom);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0){
                    mImageView.setDoubleTapZoomStyle(ZOOM_FOCUS_FIXED);
                }else if (position == 1){
                    mImageView.setDoubleTapZoomStyle(ZOOM_FOCUS_CENTER);
                }else if (position == 2){
                    mImageView.setDoubleTapZoomStyle(ZOOM_FOCUS_CENTER_IMMEDIATE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });*/

        /*spinner = (Spinner) findViewById(R.id.sp_scale_type);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0){
                    mImageView.setScaleType(CENTER_INSIDE);
                }else if (position == 1){
                    mImageView.setScaleType(CENTER_CROP);
                }else if (position == 2){
                    mImageView.setScaleType(CUSTOM);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });*/

        mImageView.setScaleAnimationInterpolator(new AccelerateDecelerateInterpolator());
    }
}
