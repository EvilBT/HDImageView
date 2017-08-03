package xyz.zpayh.original;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import xyz.zpayh.hdimage.HDImageView;
import xyz.zpayh.hdimage.ImageSource;
import xyz.zpayh.hdimage.ImageSourceBuilder;

import static xyz.zpayh.hdimage.state.Orientation.ORIENTATION_0;
import static xyz.zpayh.hdimage.state.Orientation.ORIENTATION_180;
import static xyz.zpayh.hdimage.state.Orientation.ORIENTATION_270;
import static xyz.zpayh.hdimage.state.Orientation.ORIENTATION_90;
import static xyz.zpayh.hdimage.state.ScaleType.CENTER_CROP;
import static xyz.zpayh.hdimage.state.ScaleType.CENTER_INSIDE;
import static xyz.zpayh.hdimage.state.ScaleType.CUSTOM;
import static xyz.zpayh.hdimage.state.Translation.CENTER;
import static xyz.zpayh.hdimage.state.Translation.INSIDE;
import static xyz.zpayh.hdimage.state.Translation.OUTSIDE;
import static xyz.zpayh.hdimage.state.Zoom.ZOOM_FOCUS_CENTER;
import static xyz.zpayh.hdimage.state.Zoom.ZOOM_FOCUS_CENTER_IMMEDIATE;
import static xyz.zpayh.hdimage.state.Zoom.ZOOM_FOCUS_FIXED;
import static xyz.zpayh.original.UriConstants.IMAGE_11;

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

        if (savedInstanceState == null) {
            ImageSource imageSource = ImageSourceBuilder.newBuilder()
                    .setUri(IMAGE_11)
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

        Spinner spinner = (Spinner) findViewById(R.id.sp_pan_state);
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
        });

        spinner = (Spinner) findViewById(R.id.sp_orientation);
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
        });

        spinner = (Spinner) findViewById(R.id.sp_zoom);
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
        });

        spinner = (Spinner) findViewById(R.id.sp_scale_type);
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
        });

        mImageView.setScaleAnimationInterpolator(new AccelerateDecelerateInterpolator());
    }
}
