package xyz.zpayh.original;

import android.net.Uri;
import android.os.Bundle;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

import xyz.zpayh.original.adapter.HDImageViewAdapter;

import static xyz.zpayh.original.UriConstants.IMAGE_1;
import static xyz.zpayh.original.UriConstants.IMAGE_10;
import static xyz.zpayh.original.UriConstants.IMAGE_2;
import static xyz.zpayh.original.UriConstants.IMAGE_3;
import static xyz.zpayh.original.UriConstants.IMAGE_4;
import static xyz.zpayh.original.UriConstants.IMAGE_5;
import static xyz.zpayh.original.UriConstants.IMAGE_6;
import static xyz.zpayh.original.UriConstants.IMAGE_7;
import static xyz.zpayh.original.UriConstants.IMAGE_8;
import static xyz.zpayh.original.UriConstants.IMAGE_9;

public class ViewPagerActivity extends AppCompatActivity {

    private ViewPager mViewPager;
    private HDImageViewAdapter mViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_view_pager);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mViewPager.setOffscreenPageLimit(5);
        mViewAdapter = new HDImageViewAdapter(this);
        mViewPager.setAdapter(mViewAdapter);

        initData();
    }

    private void initData() {

        List<Uri> data = new ArrayList<>();
        data.add(Uri.parse(IMAGE_1));
        data.add(Uri.parse(IMAGE_2));
        data.add(Uri.parse(IMAGE_3));
        data.add(Uri.parse(IMAGE_4));
        data.add(Uri.parse(IMAGE_5));
        data.add(Uri.parse(IMAGE_6));
        data.add(Uri.parse(IMAGE_7));
        data.add(Uri.parse(IMAGE_8));
        data.add(Uri.parse(IMAGE_9));
        data.add(Uri.parse(IMAGE_10));

        mViewAdapter.addUris(data);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}
