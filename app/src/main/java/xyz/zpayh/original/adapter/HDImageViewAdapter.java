package xyz.zpayh.original.adapter;

import android.content.Context;
import android.graphics.Point;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import xyz.zpayh.hdimage.HDImageView;
import xyz.zpayh.original.R;

/**
 * 文 件 名: HDImageViewAdapter
 * 创 建 人: 陈志鹏
 * 创建日期: 2017/4/17 00:11
 * 邮   箱: ch_zh_p@qq.com
 * 修改时间:
 * 修改备注:
 */

public class HDImageViewAdapter extends PagerAdapter{

    private static final int LONG_IMG_ASPECT_RATIO = 3;
    private static final int LONG_IMG_MINIMUM_LENGTH = 1500;

    private final List<Uri> mHDImageUris;

    private final LinkedList<View> mCacheViews;

    private Point mDisplaySize;

    public HDImageViewAdapter(Context context) {
        mHDImageUris = new ArrayList<>();
        mCacheViews = new LinkedList<>();
        mDisplaySize = new Point();


        initDisplaySize(context);
    }

    private void initDisplaySize(Context context) {
        try {
            WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            if (manager != null) {
                Display display = manager.getDefaultDisplay();
                if (display != null) {
                    display.getSize(mDisplaySize);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setUris(List<Uri> hdImageUris) {
        mHDImageUris.clear();
        if (hdImageUris != null) {
            mHDImageUris.addAll(hdImageUris);
        }

        notifyDataSetChanged();
    }

    public void addUri(Uri hdImageUri) {
        if (hdImageUri != null) {
            mHDImageUris.add(hdImageUri);
            notifyDataSetChanged();
        }
    }

    public void addUris(List<Uri> hdImageUris){
        if (hdImageUris != null && !hdImageUris.isEmpty()) {
            mHDImageUris.addAll(hdImageUris);
            notifyDataSetChanged();
        }
    }

    public Uri getHDImage(int position){
        if (position < 0 || position >= mHDImageUris.size()){
            return null;
        }
        return mHDImageUris.get(position);
    }

    @Override
    public int getCount() {
        return mHDImageUris.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        HDImageView imageView;
        View view;
        if (mCacheViews.isEmpty()){
            view = LayoutInflater.from(container.getContext())
                    .inflate(R.layout.item_hdimage_pager,container,false);
            PageViewHolder holder = new PageViewHolder(view);
            holder.position = position;
            imageView = holder.mHDImageView;
        }else{
            view = mCacheViews.remove();
            PageViewHolder holder = (PageViewHolder) view.getTag();
            holder.position = position;
            imageView = holder.mHDImageView;
        }

        final Uri pictureUri = mHDImageUris.get(position);

        imageView.setImageURI(pictureUri);

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        View view = (View) object;
        container.removeView(view);
        mCacheViews.add(view);
    }

    private class PageViewHolder{
        HDImageView mHDImageView;
        int position;

        public PageViewHolder(View view) {
            mHDImageView = (HDImageView) view.findViewById(R.id.hd_image);

            view.setTag(this);
        }
    }
}
