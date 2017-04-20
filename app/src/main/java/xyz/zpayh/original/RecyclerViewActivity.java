package xyz.zpayh.original;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xyz.zpayh.adapter.BaseAdapter;
import xyz.zpayh.adapter.BaseViewHolder;
import xyz.zpayh.hdimage.HDImageView;
import xyz.zpayh.hdimage.ImageSizeOptions;
import xyz.zpayh.hdimage.ImageSource;
import xyz.zpayh.hdimage.ImageSourceBuilder;
import xyz.zpayh.hdimage.ImageSourceLoadListener;

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

public class RecyclerViewActivity extends AppCompatActivity {

    private static final String TAG = "RecyclerViewActivity";

    private RecyclerView mRecyclerView;
    private HDImageAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_recycler_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new HDImageAdapter();
        mRecyclerView.setAdapter(mAdapter);

        mCacheImageSize = new HashMap<>();

        initData();
    }

    private ImageSourceLoadListener mListener = new ImageSourceLoadListener() {
        @Override
        public void loadSuccess(Uri uri, ImageSizeOptions options) {
            Log.d(TAG, "uri:" + uri.toString());
            mCacheImageSize.put(uri,options);
        }
    };

    private void initData() {
        List<ImageSource> data = new ArrayList<>();
        data.add(getImageSource(IMAGE_1));
        data.add(getImageSource(IMAGE_2));
        data.add(getImageSource(IMAGE_3));
        data.add(getImageSource(IMAGE_4));
        data.add(getImageSource(IMAGE_5));
        data.add(getImageSource(IMAGE_6));
        data.add(getImageSource(IMAGE_7));
        data.add(getImageSource(IMAGE_8));
        data.add(getImageSource(IMAGE_9));
        data.add(getImageSource(IMAGE_10));

        mAdapter.setData(data);
    }

    private Map<Uri,ImageSizeOptions> mCacheImageSize;

    private ImageSource getImageSource(String uriString){
        return ImageSourceBuilder.newBuilder()
                .setUri(uriString)
                .setImageSourceLoadListener(mListener)
                .build();
    }

    private class HDImageAdapter extends BaseAdapter<ImageSource>{
        @Override
        public int getLayoutRes(int index) {
            return R.layout.item_hdimage_list;
        }

        @Override
        public void convert(BaseViewHolder holder, ImageSource data, int index) {
            HDImageView imageView = holder.find(R.id.hd_image);
            if (mCacheImageSize.containsKey(data.getUri())){
                data.setImageSizeOptions(mCacheImageSize.get(data.getUri()));
            }
            imageView.setImageSource(data);
        }

        @Override
        public void bind(BaseViewHolder holder, int layoutRes) {

        }
    }
}
