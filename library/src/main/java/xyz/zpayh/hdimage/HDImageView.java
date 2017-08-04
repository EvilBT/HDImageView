/*
Copyright 2013-2015 David Morrissey

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package xyz.zpayh.hdimage;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.animation.AnimatorListenerCompat;
import android.support.v4.animation.AnimatorUpdateListenerCompat;
import android.support.v4.animation.ValueAnimatorCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import xyz.zpayh.hdimage.core.HDImageViewFactory;
import xyz.zpayh.hdimage.datasource.BitmapDataSource;
import xyz.zpayh.hdimage.datasource.DefaultBitmapDataSource;
import xyz.zpayh.hdimage.state.Orientation;
import xyz.zpayh.hdimage.state.ScaleType;
import xyz.zpayh.hdimage.state.Translation;
import xyz.zpayh.hdimage.state.Zoom;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_POINTER_1_DOWN;
import static android.view.MotionEvent.ACTION_POINTER_2_DOWN;
import static android.view.MotionEvent.ACTION_POINTER_2_UP;
import static android.view.MotionEvent.ACTION_POINTER_UP;
import static android.view.MotionEvent.ACTION_UP;
import static xyz.zpayh.hdimage.state.Orientation.ORIENTATION_0;
import static xyz.zpayh.hdimage.state.Orientation.ORIENTATION_180;
import static xyz.zpayh.hdimage.state.Orientation.ORIENTATION_270;
import static xyz.zpayh.hdimage.state.Orientation.ORIENTATION_90;
import static xyz.zpayh.hdimage.state.Orientation.ORIENTATION_EXIF;
import static xyz.zpayh.hdimage.state.ScaleType.CENTER_CROP;
import static xyz.zpayh.hdimage.state.ScaleType.CENTER_INSIDE;
import static xyz.zpayh.hdimage.state.ScaleType.CUSTOM;
import static xyz.zpayh.hdimage.state.Translation.CENTER;
import static xyz.zpayh.hdimage.state.Translation.COUSTOM;
import static xyz.zpayh.hdimage.state.Translation.INSIDE;
import static xyz.zpayh.hdimage.state.Translation.OUTSIDE;
import static xyz.zpayh.hdimage.state.Zoom.ZOOM_FOCUS_CENTER;
import static xyz.zpayh.hdimage.state.Zoom.ZOOM_FOCUS_CENTER_IMMEDIATE;
import static xyz.zpayh.hdimage.state.Zoom.ZOOM_FOCUS_FIXED;
import static xyz.zpayh.hdimage.util.Utils.fileRect;
import static xyz.zpayh.hdimage.util.Utils.getExifOrientation;

/**
 * 文 件 名: OriginalImageView
 * 创 建 人: 陈志鹏
 * 创建日期: 2017/4/1 14:51
 * 邮   箱: ch_zh_p@qq.com
 * 修改时间:
 * 修改备注:
 *  参考于Subsampling Scale Image View
 */

public class HDImageView extends View {

    private static final String TAG = "HDImageView";

    public static final int MSG_INIT_SUCCESS = 1;
    public static final int MSG_INIT_FAILED = 2;
    public static final int MSG_TILE_LOAD_SUCCESS = 3;

    public static final String SOURCE_WIDTH = "source width";
    public static final String SOURCE_Height = "source height";
    public static final String SOURCE_ORIENTATION = "source orientation";
    private static final int DEFAULT_DURATION = 500;

    private Uri mUri;

    private int mMaxSampleSize;

    private SparseArray<List<Mapping>> mMappingMap;

    @Orientation
    private int mOrientation = ORIENTATION_EXIF;

    private float mMaxScale = 2F;

    private float mMinScale = minScale();

    /**
     * 加载更高分辨率的贴图之前达到的密度
     */
    private int mMinimumMappingDpi = -1;

    @Translation
    private int mTranslateLimit = INSIDE;

    private RectF mCustomRange = new RectF();

    @ScaleType
    private int mScaleType = CENTER_INSIDE;

    public final static int MAPPING_SIZE_AUTO = Integer.MAX_VALUE;
    private int mMaxMappingWidth = MAPPING_SIZE_AUTO;
    private int mMaxMappingHeight = MAPPING_SIZE_AUTO;

    private boolean mTranslateEnabled = true;
    private boolean mZoomEnabled = true;
    private boolean mQuickScaleEnabled = true;

    //线程处理
    private Handler mOriginalHandler;
    private Executor mOriginalExecutor;

    //双击变焦行为
    private float mDoubleTapZoomScale = 1F;
    @Zoom
    private int mDoubleTapZoomStyle = ZOOM_FOCUS_FIXED;

    // 缩放开始时的当前缩放值和缩放值
    private float mScale;
    private float mScaleStart;

    // 源图像左上角的屏幕坐标
    private PointF mViewTranslate;
    private final PointF mViewTranslateStart = new PointF(0F,0F);

    //源坐标为中心，在视图准备好之前，当外部设置新位置时使用
    private float mPendingScale = -1f;
    private PointF mSourcePendingCenter;

    // 源图像尺寸和方向 - 尺寸与未旋转图像有关
    private int mSourceWidth;
    private int mSourceHeight;
    private int mSourceOrientation;
    private Rect mSourceRegion;

    private ImageSourceLoadListener mImageSourceLoadListener;

    //双指缩放正在进行
    private boolean mIsZooming;
    //单指平移正在进行中
    private boolean mIsPanning;
    //当前手势中使用的最大触摸
    private int mMaxTouchCount;

    //快速滑动检测器
    private GestureDetector mFlingDetector;
    private GestureDetector mClickDetector;
    private GestureDetector mLongPressDetector;

    //平铺和图像解码
    private BitmapDataSource mBitmapDataSource;
    final Object mLock = new Object();

    // 调试值
    private final PointF mLastViewCenter = new PointF(0F,0F);
    private float mLastViewDistance;

    private int mDuration = DEFAULT_DURATION;
    // 缩放和中心动画跟踪
    private ValueAnimator mValueAnimator;
    private AnimatorListenerCompat mAnimatorListener;
    private AnimatorUpdateListenerCompat mAnimatorUpdateListener;
    private Interpolator mScaleAnimationInterpolator;
    private Interpolator mTranslationAnimationInterpolator;
    //是否已将通知发送到子类
    private boolean mReadySent;
    //基层加载的通知是否已发送到子类
    private boolean mImageLoadedSent;

    private OnBitmapLoadListener mOnBitmapLoadListener;

    private OnLongClickListener mOnLongClickListener;

    private Paint mBitmapPaint;
    private Paint mMappingBgPaint;

    private final ScaleAndTranslate mSatTemp = new ScaleAndTranslate();
    private final Matrix mMatrix = new Matrix();

    private float mDensity;

    private float[] mSrcArray = new float[8];
    private float[] mDstArray = new float[8];

    private final AnimatorUpdateListenerCompat mAnimatorUpdateListenerCompat = new AnimatorUpdateListenerCompat() {
        @Override
        public void onAnimationUpdate(ValueAnimatorCompat animation) {
            updateScaleAndTranslate();
        }
    };

    private final AnimatorListenerCompat mAnimatorListenerCompat = new DefaultAnimatorListenerCompat() {
        @Override
        public void onAnimationEnd(ValueAnimatorCompat animation) {
            updateScaleAndTranslate();
        }
    };

    public HDImageView(Context context) {
        this(context,null);
    }

    public HDImageView(Context context, @Nullable AttributeSet attrs) {
        this(context,attrs,0);
    }

    public HDImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {

        HDImageViewFactory.initializeDefault(context);

        //init
        mDensity = context.getResources().getDisplayMetrics().density;
        createPaints();

        mBitmapDataSource = new DefaultBitmapDataSource();
        mScaleAnimationInterpolator = HDImageViewFactory.getInstance().getScaleAnimationInterpolator();
        mTranslationAnimationInterpolator = HDImageViewFactory.getInstance().getTranslationAnimationInterpolator();

        mOriginalHandler = new OriginalHandler(this);
        mOriginalExecutor = Executors.newSingleThreadExecutor();
        setMinimumDpi(160);
        setDoubleTapZoomDpi(160);
        setWillNotDraw(false);
        setGestureDetector(context);

        if (attrs != null){
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.HDImageView,defStyleAttr,0);

            mDuration = typedArray.getInt(R.styleable.HDImageView_duration,DEFAULT_DURATION);

            int resId = typedArray.getResourceId(R.styleable.HDImageView_src,NO_ID);
            if (resId != NO_ID){
                setImageSource(ImageSourceBuilder.newBuilder().setUri(resId).build());
            }

            boolean translateEnabled = typedArray.getBoolean(R.styleable.HDImageView_translateEnabled,true);
            setTranslateEnabled(translateEnabled);

            boolean zoomEnabled = typedArray.getBoolean(R.styleable.HDImageView_zoomEnabled,true);
            setZoomEnabled(zoomEnabled);

            boolean quickScaleEnabled = typedArray.getBoolean(R.styleable.HDImageView_quickScaleEnabled,true);
            setQuickScaleEnabled(quickScaleEnabled);

            int mappingBackgroundColor = typedArray.getColor(R.styleable.HDImageView_mappingBackgroundColor, Color.TRANSPARENT);
            setMappingBackgroundColor(mappingBackgroundColor);

            typedArray.recycle();
        }
    }

    public final void setOrientation(@Orientation int orientation){
        if (mOrientation == orientation){
            return;
        }
        mOrientation = orientation;
        reset(false);
        requestLayout();
    }

    public void setImageURI(Uri uri){
        ImageSource imageSource = ImageSourceBuilder.newBuilder()
                .setUri(uri)
                .build();
        setImageSource(imageSource);
    }

    public void setImageURI(@Nullable String uriString){
        Uri uri = uriString == null ? null : Uri.parse(uriString);
        setImageURI(uri);
    }

    public void setImageSource(@NonNull ImageSource imageSource){
        reset(true);

        if (imageSource.getImageViewOptions() != null){
            restoreState(imageSource.getImageViewOptions());
        }

        mUri = imageSource.getUri();
        mOrientation = imageSource.getOrientation();
        ImageSizeOptions sizeOptions = imageSource.getImageSizeOptions();

        if (sizeOptions != null){
            mSourceWidth = sizeOptions.mWidth;
            mSourceHeight = sizeOptions.mHeight;
        }

        mImageSourceLoadListener = imageSource.getImageSourceLoadListener();
        mSourceRegion = imageSource.getImageSourceRegion();
        MappingsInit task = new MappingsInit(getContext(), mBitmapDataSource, getSourceRegion(),mUri);
        mOriginalExecutor.execute(task);
    }

    public void setScaleAnimationInterpolator(Interpolator scaleAnimationInterpolator) {
        mScaleAnimationInterpolator = scaleAnimationInterpolator;
    }

    public Interpolator getScaleAnimationInterpolator() {
        return mScaleAnimationInterpolator;
    }

    public void setTranslationAnimationInterpolator(Interpolator translationAnimationInterpolator) {
        mTranslationAnimationInterpolator = translationAnimationInterpolator;
    }

    public Interpolator getTranslationAnimationInterpolator() {
        return mTranslationAnimationInterpolator;
    }

    public void setAnimatorListener(AnimatorListenerCompat animatorListener) {
        mAnimatorListener = animatorListener;
    }

    public AnimatorListenerCompat getAnimatorListener() {
        return mAnimatorListener;
    }

    public void setAnimatorUpdateListener(AnimatorUpdateListenerCompat animatorUpdateListener) {
        mAnimatorUpdateListener = animatorUpdateListener;
    }

    public AnimatorUpdateListenerCompat getAnimatorUpdateListener() {
        return mAnimatorUpdateListener;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)){
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        if (ss.mUri == null){
            return;
        }
        ImageViewOptions imageViewOptions = new ImageViewOptions(ss.mScale,
                new PointF(ss.mCenterX,ss.mCenterY));

        setImageSource(ImageSourceBuilder.newBuilder().setUri(ss.mUri)
                .setOrientation(ss.mOrientation)
                .setImageViewOptions(imageViewOptions)
                .build());
        Log.d(TAG, "onRestoreInstanceState");
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable parcelable = super.onSaveInstanceState();
        SavedState savedState = new SavedState(parcelable);
        savedState.mScale = mScale;
        PointF center = getCenter();
        if (center != null) {
            savedState.mCenterX = center.x;
            savedState.mCenterY = center.y;
        }
        savedState.mOrientation = mOrientation;
        savedState.mUri = mUri;
        if (BuildConfig.DEBUG) Log.d(TAG, "onSaveInstanceState");
        return savedState;
    }

    public static class SavedState extends BaseSavedState{
        float mScale;
        float mCenterX;
        float mCenterY;
        int mOrientation;
        Uri mUri;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeFloat(mScale);
            out.writeFloat(mCenterX);
            out.writeFloat(mCenterY);
            out.writeInt(mOrientation);
            out.writeParcelable(mUri,flags);
        }

        @SuppressWarnings("hiding")
        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>(){
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

        private SavedState(Parcel in){
            super(in);
            mScale = in.readFloat();
            mCenterX = in.readFloat();
            mCenterY = in.readFloat();
            mOrientation = in.readInt();
            mUri = in.readParcelable(Uri.class.getClassLoader());
        }
    }

    private void reset(boolean newImage){
        mScale = 0f;
        mScaleStart = 0;
        mViewTranslate = null;
        mViewTranslateStart.set(0F,0F);
        mPendingScale = -1f;
        mSourcePendingCenter = null;
        //mSourceRequestCenter = null;
        mIsZooming = false;
        mIsPanning = false;
        mMaxTouchCount = 0;
        mMaxSampleSize = 0;
        mLastViewCenter.set(0F,0F);
        mLastViewDistance = 0;
        stopAnimator();
        mScaleAnimationInterpolator = HDImageViewFactory.getInstance().getScaleAnimationInterpolator();
        mTranslationAnimationInterpolator = HDImageViewFactory.getInstance().getTranslationAnimationInterpolator();
        mSatTemp.reset();
        mMatrix.reset();
        if (newImage){
            mUri = null;
            if (mBitmapDataSource != null){
                synchronized (mLock){
                    mBitmapDataSource.recycle();
                }
            }
            mSourceWidth = 0;
            mSourceHeight = 0;
            mSourceOrientation = ORIENTATION_0;
            mSourceRegion = null;
            mReadySent = false;
            mImageLoadedSent = false;
        }
        if (mMappingMap != null){
            for (int index = 0; index < mMappingMap.size(); index++) {
                List<Mapping> mappings = mMappingMap.valueAt(index);
                for (Mapping mapping : mappings) {
                    mapping.mVisible = false;
                    if (mapping.mBitmap != null){
                        mapping.mBitmap.recycle();
                        mapping.mBitmap = null;
                    }
                }
            }
            mMappingMap = null;
        }
        invalidate();
    }

    private void stopAnimator() {
        if (mValueAnimator != null){
            mValueAnimator.cancel();
            mValueAnimator = null;
        }
    }

    private void setGestureDetector(final Context context) {

        setLongPress(context);

        setClickGesture(context);

        setFilingGesture(context);
    }

    private void setLongPress(Context context) {
        mLongPressDetector = new GestureDetector(context,new GestureDetector.SimpleOnGestureListener(){
            @Override
            public void onLongPress(MotionEvent e) {
                if (isLongClickable() && mOnLongClickListener != null){
                    mOnLongClickListener.onLongClick(HDImageView.this);
                }
                super.onLongPress(e);
            }
        });
    }

    private void setClickGesture(Context context) {
        mClickDetector = new GestureDetector(context,new GestureDetector.SimpleOnGestureListener(){

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (BuildConfig.DEBUG) Log.d(TAG, "单击行为");
                performClick();
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (BuildConfig.DEBUG) Log.d(TAG, "双击行为");
                if (mZoomEnabled && mQuickScaleEnabled && mReadySent && mViewTranslate != null){
                    //重置快速滑动监听，解决与双击事件的冲突
                    setFilingGesture(getContext());
                    PointF sourceCenter = viewToSourceCoordinate(new PointF(e.getX(),e.getY()));
                    PointF viewFocus = new PointF(e.getX(),e.getY());
                    startDoubleTapAnimator(sourceCenter,viewFocus);
                    return true;
                }
                return false;
            }
        });
    }

    private void setFilingGesture(Context context) {
        mFlingDetector = new GestureDetector(context,new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

                if (mTranslateEnabled && mReadySent && mViewTranslate != null && e1 != null && e2 != null
                        && (Math.abs(e1.getX() - e2.getX()) > 50 || Math.abs(e1.getY() - e2.getY()) > 50)
                        && (Math.abs(velocityX) > 500 || Math.abs(velocityY) > 500) && !mIsZooming) {
                    PointF vTranslateEnd =
                            new PointF(mViewTranslate.x + (velocityX * 0.25f), mViewTranslate.y + (velocityY * 0.25f));
                    float sCenterXEnd = ((getWidth() / 2.0F) - vTranslateEnd.x) / mScale;
                    float sCenterYEnd = ((getHeight() / 2.0F) - vTranslateEnd.y) / mScale;
                    startFilingAnimation(sCenterXEnd, sCenterYEnd);
                    if (BuildConfig.DEBUG) Log.d(TAG, "onFling: 正在滑行");
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        PointF sCenter = getCenter();
        if (mReadySent && sCenter != null){
            stopAnimator();
            mPendingScale = mScale;
            mSourcePendingCenter = sCenter;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        boolean resizeWidth = widthSpecMode != MeasureSpec.EXACTLY;
        boolean resizeHeight = heightSpecMode != MeasureSpec.EXACTLY;
        int width = parentWidth;
        int height = parentHeight;
        if (mSourceWidth > 0 && mSourceHeight > 0) {
            if (resizeWidth && resizeHeight) {
                width = getShowWidth();
                height = getShowHeight();
            } else if (resizeHeight) {
                height = (int)((((double)getShowHeight()/(double)getShowWidth()) * width));
            } else if (resizeWidth) {
                width = (int)((((double)getShowWidth()/(double)getShowHeight()) * height));
            }
        }
        width = Math.max(width, getSuggestedMinimumWidth());
        height = Math.max(height, getSuggestedMinimumHeight());
        Log.d(TAG, "SourceSize:(" + mSourceWidth + "," + mSourceHeight + ")");
        Log.d(TAG, "(" + width + "," + height + ")");
        setMeasuredDimension(width, height);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean onTouchEvent(MotionEvent event) {

        if (mValueAnimator != null && !mValueAnimator.isInterrupted()){
            // 在不可中断的动画中，忽略所有触摸事件,把点击事件交由父控件处理
            getParent().requestDisallowInterceptTouchEvent(true);
            return true;
        }
        
        stopAnimator();

        // 中止如果没有准备好
        if (mViewTranslate == null){
            return true;
        }

        //检测快速滑动，点击和双击
        if (mFlingDetector.onTouchEvent(event)){
            //如果不是快速缩放状态就交由mDetector先行处理
            mIsZooming = false;
            mIsPanning = false;
            mMaxTouchCount = 0;
            return true;
        }

        if (mClickDetector.onTouchEvent(event)){
            mIsZooming = false;
            mIsPanning = false;
            mMaxTouchCount = 0;
            return true;
        }

        if (mLongPressDetector.onTouchEvent(event)){
            mIsZooming = false;
            mIsPanning = false;
            mMaxTouchCount = 0;
            return true;
        }

        final int touchCount = event.getPointerCount();
        switch (event.getAction()){
            case ACTION_DOWN:
            case ACTION_POINTER_1_DOWN:
            case ACTION_POINTER_2_DOWN:
                down(event, touchCount);
                return true;
            case ACTION_MOVE:
                boolean consumed = move(event, touchCount);
                if (consumed){
                    invalidate();
                    return true;
                }
                break;
            case ACTION_UP:
            case ACTION_POINTER_UP:
            case ACTION_POINTER_2_UP:
                if (up(event, touchCount)) return true;
                return true;
        }

        return super.onTouchEvent(event);
    }

    private boolean up(MotionEvent event, int touchCount) {

        if (mMaxTouchCount > 0 && (mIsZooming || mIsPanning)){
            if (mIsZooming && touchCount == 2){
                mIsPanning = true;
                mViewTranslateStart.set(mViewTranslate.x, mViewTranslate.y);
                if (event.getActionIndex() == 1){
                    mLastViewCenter.set(event.getX(0),event.getY(0));
                }else{
                    mLastViewCenter.set(event.getX(1),event.getY(1));
                }
            }
            if (touchCount < 3){
                mIsZooming = false;
            }
            if (touchCount < 2){
                mIsPanning = false;
                mMaxTouchCount = 0;
            }

            refreshRequiredTiles(true);
            return true;
        }
        if (touchCount == 1){
            mIsZooming = false;
            mIsPanning = false;
            mMaxTouchCount = 0;
        }
        return false;
    }

    private boolean move(MotionEvent event, int touchCount) {

        if (mMaxTouchCount == 0){
            return false;
        }

        if (touchCount >= 2 && !mZoomEnabled){
            return false;
        }

        if (touchCount >= 2){

            //计算触摸点之间的新距离，相对于起始值进行缩放和平移。
            float endDistance = distance(event.getX(0),event.getX(1),event.getY(0),event.getY(1));
            float endCenterX = (event.getX(0)+event.getX(1))/2;
            float endCenterY = (event.getY(0)+event.getY(1))/2;

            float centerDistance = distance(mLastViewCenter.x,endCenterX, mLastViewCenter.y,endCenterY);

            if (centerDistance >5 || Math.abs(endDistance- mLastViewDistance)>5 || mIsPanning){
                mIsZooming = true;
                mIsPanning = true;

                mScale = Math.min(mMaxScale,endDistance * mScaleStart / mLastViewDistance);

                if (mScale <= minScale()){
                    //达到最小规模，所以不要泛。 调整开始设置，以便任何展开都会放大。
                    mLastViewDistance = endDistance;
                    mScaleStart = minScale();
                    mLastViewCenter.set(endCenterX, endCenterY);
                    mViewTranslateStart.set(mViewTranslate);
                }else if (mTranslateEnabled){
                    //平移，将源图像坐标位于夹点中心的起始位置，即可同时进行平移+缩放。
                    float vLeftStart = mLastViewCenter.x - mViewTranslateStart.x;
                    float vTopStart = mLastViewCenter.y - mViewTranslateStart.y;
                    float vLeftNow = vLeftStart * mScale / mScaleStart;
                    float vTopNow = vTopStart * mScale / mScaleStart;
                    mViewTranslate.x = endCenterX - vLeftNow;
                    mViewTranslate.y = endCenterY - vTopNow;
                }else{
                    mViewTranslate.x = (getWidth() - mScale * getShowWidth())/2;
                    mViewTranslate.y = (getHeight() - mScale * getShowHeight())/2;
                }

                fitToBounds(true);
                refreshRequiredTiles(false);
                return true;
            }

            return false;
        }

        if (mIsZooming){
            return false;
        }

        // 获取平移值
        float dx = Math.abs(event.getX() - mLastViewCenter.x);
        float dy = Math.abs(event.getY() - mLastViewCenter.y);

        final float offset = 5 * mDensity;
        if ( dx > offset || dy > offset || mIsPanning){

            mViewTranslate.x = mViewTranslateStart.x + event.getX() - mLastViewCenter.x;
            mViewTranslate.y = mViewTranslateStart.y + event.getY() - mLastViewCenter.y;

            float lastX = mViewTranslate.x;
            float lastY = mViewTranslate.y;
            fitToBounds(true);
            boolean atXEdge = lastX != mViewTranslate.x;
            boolean atYEdge = lastY != mViewTranslate.y;
            boolean edgeXSwipe = atXEdge && dx > dy && !mIsPanning;
            boolean edgeYSwipe = atYEdge && dy > dx && !mIsPanning;
            boolean translateY = lastY == mViewTranslate.y && dy > offset * 3;
            if (!edgeXSwipe && !edgeYSwipe &&
                    (!atXEdge || !atYEdge || translateY || mIsPanning)){
                mIsPanning = true;
            }else if (dx > offset || dy > offset){
                mMaxTouchCount = 0;
                getParent().requestDisallowInterceptTouchEvent(false);
            }

            if (!mTranslateEnabled){
                mViewTranslate.x = mViewTranslateStart.x;
                mViewTranslate.y = mViewTranslateStart.y;
                getParent().requestDisallowInterceptTouchEvent(false);
            }

            refreshRequiredTiles(false);

            return true;
        }

        return false;
    }

    private void down(MotionEvent event, int touchCount) {
        stopAnimator();
        getParent().requestDisallowInterceptTouchEvent(true);
        mMaxTouchCount = Math.max(mMaxTouchCount,touchCount);

        if (touchCount < 2){
            // 开始一个手指锅
            mViewTranslateStart.set(mViewTranslate.x, mViewTranslate.y);
            mLastViewCenter.set(event.getX(),event.getY());
            return;
        }

        if (mZoomEnabled){
            // 开始捏捏缩放。 计算接触点与夹点中心点之间的距离。
            float startDistance = distance(event.getX(0),event.getX(1),event.getY(0),event.getY(1));
            float startCenterX =  (event.getX(0)+event.getX(1))/2;
            float startCenterY =  (event.getY(0)+event.getY(1))/2;
            mScaleStart = mScale;
            mLastViewDistance = startDistance;
            mViewTranslateStart.set(mViewTranslate.x, mViewTranslate.y);
            mLastViewCenter.set( startCenterX, startCenterY);
        }else{
            //中止所有二次手势
            mMaxTouchCount = 0;
        }
    }

    private void startDoubleTapAnimator(PointF sCenter, PointF vFocus) {
        Log.d(TAG, "双击动画");
        if (!mTranslateEnabled){
            sCenter.x = getShowWidth()/2.0F;
            sCenter.y = getShowHeight()/2.0F;
        }
        float doubleTapZoomScale = Math.min(mMaxScale,mDoubleTapZoomScale);
        boolean zoomIn = mScale <= doubleTapZoomScale * 0.9f;
        float targetScale = zoomIn ? doubleTapZoomScale : minScale();
        if (mDoubleTapZoomStyle == ZOOM_FOCUS_CENTER_IMMEDIATE){
            setScaleAndCenter(targetScale,sCenter);
        }else if (mDoubleTapZoomStyle == ZOOM_FOCUS_CENTER || !zoomIn || !mTranslateEnabled){
            startZoomForCenter(sCenter, targetScale);
        }else if (mDoubleTapZoomStyle == ZOOM_FOCUS_FIXED){
            startZoomForFixed(sCenter, vFocus, targetScale);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mSourceWidth == 0 || mSourceHeight == 0 || getWidth() == 0 || getHeight() == 0){
            //面积为0不画
            return;
        }

        if (mMappingMap == null && mBitmapDataSource != null){
            //初始化贴图
            initialiseBaseLayer(getMaxBitmapDimensions(canvas));
        }

        if (!checkReady()){
            return;
        }

        preDraw();

        drawMappings(canvas);
    }

    private void drawMappings(Canvas canvas) {
        if (mMappingMap == null || !isBaseLayerReady()){
            return;
        }

        int sampleSize = Math.min(mMaxSampleSize, calculateInSampleSize(mScale));

        boolean hasMissingTiles = false;
        final List<Mapping> mappings = mMappingMap.get(sampleSize);
        if (mappings != null){
            for (Mapping mapping : mappings) {
                if (mapping.mVisible && (mapping.mLoading || mapping.mBitmap == null)){
                    hasMissingTiles = true;
                    break;
                }
            }
        }

        if (hasMissingTiles){
            for (int index = 0; index < mMappingMap.size(); index++) {
                final List<Mapping> list = mMappingMap.valueAt(index);
                drawMappings(canvas,list);
            }
        } else {
            if (mappings != null){
                drawMappings(canvas, mappings);
            }
        }
    }

    private void drawMappings(Canvas canvas, List<Mapping> list) {
        for (Mapping mapping : list) {
            sourceToViewRect(mapping.mSourceRect, mapping.mViewRect);
            if (!mapping.mLoading && mapping.mBitmap != null) {
                if (mMappingBgPaint != null) {
                    canvas.drawRect(mapping.mViewRect, mMappingBgPaint);
                }
                mMatrix.reset();
                setMatrixArray(mSrcArray, 0, 0,
                        mapping.mBitmap.getWidth(), 0, mapping.mBitmap.getWidth(),
                        mapping.mBitmap.getHeight(), 0, mapping.mBitmap.getHeight());
                int orientation = getRequiredRotation();
                switch (orientation) {
                    case ORIENTATION_0:
                        setMatrixArray(mDstArray, mapping.mViewRect.left, mapping.mViewRect.top, mapping.mViewRect.right,
                                mapping.mViewRect.top, mapping.mViewRect.right, mapping.mViewRect.bottom, mapping.mViewRect.left,
                                mapping.mViewRect.bottom);
                        break;
                    case ORIENTATION_90:
                        setMatrixArray(mDstArray, mapping.mViewRect.right, mapping.mViewRect.top, mapping.mViewRect.right,
                                mapping.mViewRect.bottom, mapping.mViewRect.left, mapping.mViewRect.bottom, mapping.mViewRect.left,
                                mapping.mViewRect.top);
                        break;
                    case ORIENTATION_180:
                        setMatrixArray(mDstArray, mapping.mViewRect.right, mapping.mViewRect.bottom, mapping.mViewRect.left,
                                mapping.mViewRect.bottom, mapping.mViewRect.left, mapping.mViewRect.top, mapping.mViewRect.right,
                                mapping.mViewRect.top);
                        break;
                    case ORIENTATION_270:
                        setMatrixArray(mDstArray, mapping.mViewRect.left, mapping.mViewRect.bottom, mapping.mViewRect.left,
                                mapping.mViewRect.top, mapping.mViewRect.right, mapping.mViewRect.top, mapping.mViewRect.right,
                                mapping.mViewRect.bottom);
                        break;
                    case ORIENTATION_EXIF:
                    default:
                        break;
                }
                mMatrix.setPolyToPoly(mSrcArray, 0, mDstArray, 0, 4);
                canvas.drawBitmap(mapping.mBitmap, mMatrix, mBitmapPaint);
            }
        }
    }

    private void setMatrixArray(float[] array, float f0, float f1, float f2, float f3, float f4,
                                float f5, float f6, float f7) {
        array[0] = f0;
        array[1] = f1;
        array[2] = f2;
        array[3] = f3;
        array[4] = f4;
        array[5] = f5;
        array[6] = f6;
        array[7] = f7;
    }

    //检查贴图的基层是否准备好
    private boolean isBaseLayerReady(){
        if (mMappingMap == null){
            return false;
        }

        final List<Mapping> mappings = mMappingMap.get(mMaxSampleSize);
        if (mappings == null){
            return false;
        }

        for (Mapping mapping : mappings) {
            if (mapping.mLoading || mapping.mBitmap == null){
                return false;
            }
        }

        return true;
    }

    // 检查已经准备好绘制了
    private boolean checkReady(){
        boolean ready = getWidth() > 0 && getHeight() > 0 &&
                mSourceWidth > 0 && mSourceHeight > 0 && isBaseLayerReady();
        if (!mReadySent && ready){
            preDraw();
            mReadySent = true;
            if (mOnBitmapLoadListener != null){
                mOnBitmapLoadListener.onBitmapLoadReady();
            }
        }
        return ready;
    }

    private boolean checkImageLoaded(){
        boolean imageLoaded = isBaseLayerReady();
        if (!mImageLoadedSent && imageLoaded){
            preDraw();
            mImageLoadedSent = true;
        }
        return imageLoaded;
    }

    private void createPaints(){
        if (mBitmapPaint == null){
            mBitmapPaint = new Paint();
            mBitmapPaint.setAntiAlias(true);
            mBitmapPaint.setFilterBitmap(true);
            mBitmapPaint.setDither(true);
        }
    }

    private synchronized void initialiseBaseLayer(Point maxTileDimensions){
        fitToBounds(true,mSatTemp);

        mMaxSampleSize = calculateInSampleSize(mSatTemp.mScale);

        initialiseTileMap(maxTileDimensions);

        List<Mapping> baseGrid = mMappingMap.get(mMaxSampleSize);
        final int rotation = getRequiredRotation();
        if (BuildConfig.DEBUG) Log.d(TAG, "initialiseBaseLayer");
        for (Mapping baseMapping : baseGrid) {
            MappingLoad task = new MappingLoad(mBitmapDataSource, baseMapping, mSourceRegion,
                    mSourceWidth, mSourceHeight,rotation);
            mOriginalExecutor.execute(task);
        }
        refreshRequiredTiles(true);
    }

    // 刷新贴图
    private void refreshRequiredTiles(boolean load) {
        if (mBitmapDataSource == null || mMappingMap == null){
            return;
        }

        int sampleSize = Math.min(mMaxSampleSize, calculateInSampleSize(mScale));

        final int rotation = getRequiredRotation();
        for (int index = 0; index < mMappingMap.size(); index++) {
            final List<Mapping> mappings = mMappingMap.valueAt(index);
            for (Mapping mapping : mappings) {
                if (mapping.mSampleSize < sampleSize
                        || (mapping.mSampleSize > sampleSize && mapping.mSampleSize != mMaxSampleSize)){
                    mapping.mVisible = false;
                    if (mapping.mBitmap != null){
                        mapping.mBitmap.recycle();
                        mapping.mBitmap = null;
                    }
                }
                if (mapping.mSampleSize == sampleSize){
                    if (mappingVisible(mapping)){
                        mapping.mVisible = true;
                        if (!mapping.mLoading && mapping.mBitmap == null && load){
                            if (BuildConfig.DEBUG) Log.d(TAG, "refreshRequiredTiles");
                            MappingLoad task = new MappingLoad(mBitmapDataSource, mapping, mSourceRegion,
                                    mSourceWidth, mSourceHeight,rotation);
                            mOriginalExecutor.execute(task);
                        }
                    }else if (mapping.mSampleSize != mMaxSampleSize){
                        mapping.mVisible = false;
                        if (mapping.mBitmap != null){
                            mapping.mBitmap.recycle();
                            mapping.mBitmap = null;
                        }
                    }
                }else if (mapping.mSampleSize == mMaxSampleSize){
                    mapping.mVisible = true;
                }
            }
        }
    }

    private void updateScaleAndTranslate() {
        if (mValueAnimator == null) return;

        mScale = mValueAnimator.getScale();
        PointF viewFocus = mValueAnimator.getViewFocus();
        mViewTranslate.x -= sourceToViewX(mValueAnimator.mSourceCenter.x) - viewFocus.x;
        mViewTranslate.y -= sourceToViewY(mValueAnimator.mSourceCenter.y) - viewFocus.y;

        fitToBounds(mValueAnimator.getAnimatedFraction() >= 1f || mValueAnimator.noChangeScale());
        refreshRequiredTiles(mValueAnimator.getAnimatedFraction() >= 1f);
        invalidate();
        if (mValueAnimator.isEnded()){
            stopAnimator();
        }
    }

    /**
     * 计算贴图是否显示在屏幕上
     * @param mapping 贴图
     * @return true 显示屏幕上，false 在屏幕外
     */
    private boolean mappingVisible(Mapping mapping){
        float sVisLeft = viewToSourceX(0);
        float sVisRight = viewToSourceX(getWidth());
        float sVisTop = viewToSourceY(0);
        float sVisBottom = viewToSourceY(getHeight());
        return !(sVisLeft > mapping.mSourceRect.right ||
                 mapping.mSourceRect.left > sVisRight ||
                 sVisTop > mapping.mSourceRect.bottom ||
                 mapping.mSourceRect.top > sVisBottom);
    }

    private void preDraw(){
        if (getWidth() == 0 || getHeight() == 0 || mSourceWidth <= 0 || mSourceHeight <= 0){
            return;
        }

        if (mSourcePendingCenter != null && mPendingScale > -1f){

            if (mViewTranslate == null){
                mViewTranslate = new PointF();
            }

            mScale = mPendingScale;
            mViewTranslate.x = getWidth()/2.0F - mScale * mSourcePendingCenter.x;
            mViewTranslate.y = getHeight()/2.0F - mScale * mSourcePendingCenter.y;
            mSourcePendingCenter = null;
            mPendingScale = -1f;
            fitToBounds(true);
            refreshRequiredTiles(true);
        }

        fitToBounds(false);
    }

    private int calculateInSampleSize(float scale){
        if (mMinimumMappingDpi > 0){
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            float averageDpi = (metrics.xdpi + metrics.ydpi)/2;
            scale = mMinimumMappingDpi * scale / averageDpi;
        }

        int reqWidth = (int)(getShowWidth()*scale);
        int reqHeight = (int)(getShowHeight()*scale);

        int inSampleSize = 1;
        if (reqWidth == 0 || reqHeight == 0){
            return 32;
        }

        if (getShowHeight() > reqHeight || getShowWidth() > reqWidth){
            final int heightRatio = Math.round(getShowHeight()/(float)reqHeight);
            final int widthRatio = Math.round(getShowWidth()/(float)reqWidth);

            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        int power = 1;
        while ( power * 2 < inSampleSize){
            power *= 2;
        }
        return power;
    }

    //适应边界
    private void fitToBounds(boolean center){
        boolean init = false;
        if (mViewTranslate == null){
            init = true;
            mViewTranslate = new PointF(0,0);
        }

        mSatTemp.mScale = mScale;
        mSatTemp.mViewTranslate.set(mViewTranslate);
        fitToBounds(center,mSatTemp);
        mScale = mSatTemp.mScale;
        mViewTranslate.set(mSatTemp.mViewTranslate);
        if (init){
            mViewTranslate.set(getTranslateForSourceCenter(getShowWidth()/2.0F, getShowHeight()/2.0F,mScale));
        }
    }

    public void setCustomRange(RectF range){
        this.mCustomRange.set(range);
    }

    private void fitToBounds(boolean center, ScaleAndTranslate sat){
        if (mTranslateLimit == OUTSIDE && isReady()){
            center = false;
        }

        // 计算padding的偏移效果
        final float xPaddingRatio = getPaddingLeft() > 0 || getPaddingRight() > 0
                ? getPaddingLeft() / (float)(getPaddingRight()+getPaddingLeft())
                : 0.5f;
        final float yPaddingRatio = getPaddingTop() > 0 || getPaddingBottom() > 0
                ? getPaddingTop() / (float)(getPaddingTop()+getPaddingBottom())
                : 0.5f;

        // 限制缩放的大小
        float scale = limitedScale(sat.mScale);
        sat.mScale = scale;
        // 获取缩放后的图片宽高
        float scaleWidth = scale * getShowWidth();
        float scaleHeight = scale * getShowHeight();

        if (mTranslateLimit == COUSTOM && isReady() && !mCustomRange.isEmpty()){
            if (scaleWidth < mCustomRange.width()){
                scale =  (mCustomRange.width()*1.0f) / (getShowWidth() * 1.0f);
                sat.mScale = scale;
                // 获取缩放后的图片宽高
                scaleWidth = scale * getShowWidth();
                scaleHeight = scale * getShowHeight();
            }

            if (scaleHeight < mCustomRange.height()){
                scale = (mCustomRange.height()*1.0f) / (getShowHeight() * 1.0f);
                sat.mScale = scale;
                // 获取缩放后的图片宽高
                scaleWidth = scale * getShowWidth();
                scaleHeight = scale * getShowHeight();
            }

            float translateX = limitTranslate(
                    mCustomRange.right - scaleWidth,
                    sat.mViewTranslate.x,
                    Math.max(0F,mCustomRange.left)
            );

            float translateY = limitTranslate(
                    mCustomRange.bottom - scaleHeight,
                    sat.mViewTranslate.y,
                    Math.max(0F,mCustomRange.top)
            );
            sat.mViewTranslate.x = translateX;
            sat.mViewTranslate.y = translateY;
            return;
        }

        if (mTranslateLimit == CENTER && isReady()){
            float translateX = limitTranslate(getWidth()/2.0F-scaleWidth,
                    sat.mViewTranslate.x,
                    Math.max(0F,getWidth()/2.0F));
            float translateY = limitTranslate(getHeight()/2.0F-scaleHeight,
                    sat.mViewTranslate.y,
                    Math.max(0F,getHeight()/2.0F));
            sat.mViewTranslate.x = translateX;
            sat.mViewTranslate.y = translateY;
            return;
        }

        if (center){
            float translateX = limitTranslate(getWidth()-scaleWidth,
                    sat.mViewTranslate.x,
                    Math.max(0F,(getWidth()-scaleWidth)*xPaddingRatio));
            float translateY = limitTranslate(getHeight()-scaleHeight,
                    sat.mViewTranslate.y,
                    Math.max(0F,(getHeight()-scaleHeight)*yPaddingRatio));
            sat.mViewTranslate.x = translateX;
            sat.mViewTranslate.y = translateY;
            return;
        }
        float translateX = limitTranslate(-scaleWidth,
                sat.mViewTranslate.x,
                Math.max(0F,getWidth()));
        float translateY = limitTranslate(-scaleHeight,
                sat.mViewTranslate.y,
                Math.max(0F,getHeight()));
        sat.mViewTranslate.x = translateX;
        sat.mViewTranslate.y = translateY;
    }

    private float limitTranslate(float min, float current, float max){
        return Math.min(max,Math.max(min,current));
    }

    private void initialiseTileMap(Point maxTileDimensions){
        mMappingMap = new SparseArray<>();
        int sampleSize = mMaxSampleSize;
        int xTiles = 1;
        int yTiles = 1;
        while (true){
            int sTileWidth = getShowWidth() / xTiles;
            int sTileHeight = getShowHeight() / yTiles;
            int subTileWidth = sTileWidth / sampleSize;
            int subTileHeight = sTileHeight / sampleSize;

            while (subTileWidth + xTiles + 1 > maxTileDimensions.x
                    || (subTileWidth > getWidth()*1.25 && sampleSize < mMaxSampleSize)){
                xTiles++;
                sTileWidth = getShowWidth() / xTiles;
                subTileWidth = sTileWidth / sampleSize;
            }

            while (subTileHeight + yTiles + 1 > maxTileDimensions.y
                    || (subTileHeight > getHeight()*1.25 && sampleSize < mMaxSampleSize)){
                yTiles++;
                sTileHeight = getShowHeight() / yTiles;
                subTileHeight = sTileHeight / sampleSize;
            }
            List<Mapping> mappingGrid = new ArrayList<>(xTiles*yTiles);
            for (int x = 0; x < xTiles; x++) {
                for (int y = 0; y < yTiles; y++) {
                    Mapping mapping = new Mapping();
                    mapping.mSampleSize = sampleSize;
                    mapping.mVisible = sampleSize == mMaxSampleSize;
                    mapping.mSourceRect = new Rect( x * sTileWidth,y*sTileHeight,
                            x == xTiles - 1 ? getShowWidth() : (x+1)*sTileWidth,
                            y == yTiles - 1 ? getShowHeight() : (y+1)*sTileHeight);
                    mapping.mViewRect = new Rect(0, 0 ,0 ,0);
                    mapping.mFileSourceRect = new Rect(mapping.mSourceRect);
                    mappingGrid.add(mapping);
                }
            }
            mMappingMap.put(sampleSize, mappingGrid);
            if (sampleSize == 1){
                break;
            }else {
                sampleSize /=2;
            }
        }
        if (BuildConfig.DEBUG){
            for (int index = 0; index < mMappingMap.size(); index++) {
                Log.d(TAG, "[sampleSize]"+ mMappingMap.keyAt(index)+" [tiles]"+
                        mMappingMap.valueAt(index).size());
            }
        }
    }

    public synchronized void onTilesInitialized(int sWidth, int sHeight, int sOrientation){
        if (mSourceWidth > 0 && mSourceHeight > 0 && (mSourceWidth != sWidth || mSourceHeight != sHeight)){
            reset(false);
        }
        mSourceWidth = sWidth;
        mSourceHeight = sHeight;
        mSourceOrientation = sOrientation;

        if (mImageSourceLoadListener != null){
            mImageSourceLoadListener.loadSuccess(mUri,new ImageSizeOptions(mSourceWidth,mSourceHeight));
        }

        if (mOnBitmapLoadListener != null){
            mOnBitmapLoadListener.onBitmapLoaded(mSourceWidth,mSourceHeight);
        }

        checkReady();
        checkImageLoaded();
        requestLayout();
        invalidate();
        if (BuildConfig.DEBUG) Log.d(TAG, "onTilesInitialized");
    }

    synchronized void onTileLoaded(){
        checkReady();
        checkImageLoaded();
        invalidate();
    }

    private void restoreState(ImageViewOptions state){
        if (state != null && state.getCenter() != null){
            mPendingScale = state.getScale();
            mSourcePendingCenter = state.getCenter();
            invalidate();
        }
    }

    private void setMaxTileSize(int maxPixels){
        mMaxMappingWidth = maxPixels;
        mMaxMappingHeight = maxPixels;
    }

    private void setMaxTileSize(int maxPixelsX, int maxPixelsY){
        mMaxMappingWidth = maxPixelsX;
        mMaxMappingHeight = maxPixelsY;
    }

    private Point getMaxBitmapDimensions(Canvas canvas){
        int maxWidth = 2048;
        int maxHeight = 2048;
        if (Build.VERSION.SDK_INT >= 14){
            try {
                maxWidth = (int) Canvas.class.getMethod("getMaximumBitmapWidth").invoke(canvas);
                maxHeight = (int) Canvas.class.getMethod("getMaximumBitmapHeight").invoke(canvas);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new Point(Math.min(maxWidth, mMaxMappingWidth),Math.min(maxHeight, mMaxMappingHeight));
    }

    @SuppressWarnings("SuspiciousNameCombination")
    private int getShowWidth(){
        int rotation = getRequiredRotation();
        if (rotation == ORIENTATION_90 || rotation == ORIENTATION_270){
            return mSourceHeight;
        }
        return mSourceWidth;
    }

    @SuppressWarnings("SuspiciousNameCombination")
    private int getShowHeight(){
        int rotation = getRequiredRotation();
        if (rotation == ORIENTATION_90 || rotation == ORIENTATION_270){
            return mSourceWidth;
        }
        return mSourceHeight;
    }

    @Orientation
    private int getRequiredRotation(){
        if (mOrientation == ORIENTATION_EXIF){
            return mSourceOrientation;
        }
        return mOrientation;
    }

    private float distance(float x0, float x1, float y0, float y1){
        float dx = x0 - x1;
        float dy = y0 - y1;
        return (float) Math.sqrt(dx*dx+dy*dy);
    }

    public void recycle(){
        if (BuildConfig.DEBUG) Log.d(TAG, "recycle" + mUri + " id" + System.identityHashCode(this));
        reset(true);
        setOnBitmapLoadListener(null);
    }

    private float viewToSourceX(float vx){
        if (mViewTranslate == null)
            return Float.NaN;
        return (vx - mViewTranslate.x) / mScale;
    }

    private float viewToSourceY(float vy){
        if (mViewTranslate == null)
            return Float.NaN;
        return (vy - mViewTranslate.y) / mScale;
    }

    /**
     * 将屏幕坐标转换为资源坐标。
     * @param viewCoordinate view的坐标
     * @return
     *  返回图片的坐标
     */
    public final PointF viewToSourceCoordinate(@NonNull PointF viewCoordinate){
        return viewToSourceCoordinate(viewCoordinate.x, viewCoordinate.y, new PointF());
    }

    /**
     * 将屏幕坐标转换为资源坐标。
     * @param viewX view的x坐标
     * @param viewY view的y坐标
     * @return
     *  返回图片的坐标
     */
    public final PointF viewToSourceCoordinate(float viewX, float viewY){
        return viewToSourceCoordinate(viewX, viewY, new PointF());
    }

    /**
     * 将屏幕坐标转换为资源坐标。
     * @param viewCoordinate view的坐标
     * @param sourceTarget 目标资源坐标
     * @return
     * 返回图片的坐标
     */
    public final PointF viewToSourceCoordinate(@NonNull PointF viewCoordinate, @NonNull PointF sourceTarget){
        return viewToSourceCoordinate(viewCoordinate.x,viewCoordinate.y,sourceTarget);
    }

    /**
     * 将屏幕坐标转换为资源坐标。
     * @param viewX view的x坐标
     * @param viewY view的y坐标
     * @param sourceTarget 目标资源坐标
     * @return
     * 返回图片的坐标
     */
    private PointF viewToSourceCoordinate(float viewX, float viewY, @NonNull PointF sourceTarget) {
        if (mViewTranslate == null)
            return null;

        sourceTarget.set(viewToSourceX(viewX),viewToSourceY(viewY));
        return sourceTarget;
    }

    private float sourceToViewX(float sx){
        if (mViewTranslate == null){
            return Float.NaN;
        }
        return (sx * mScale) + mViewTranslate.x;
    }

    private float sourceToViewY(float sy){
        if (mViewTranslate == null){
            return Float.NaN;
        }
        return (sy * mScale) + mViewTranslate.y;
    }

    /**
     * 将资源坐标转换为屏幕坐标。
     * @param sourceCoordinate 资源坐标
     * @return
     *  返回屏幕上的位置
     */
    public final PointF sourceToViewCoordinate(@NonNull PointF sourceCoordinate){
        return sourceToViewCoordinate(sourceCoordinate.x,sourceCoordinate.y,new PointF());
    }

    public final PointF sourceToViewCoordinate(float sx, float sy){
        return sourceToViewCoordinate(sx,sy,new PointF());
    }

    /**
     * 将资源坐标转换为屏幕坐标。
     * @param sourceCoordinate 资源坐标
     * @param viewTarget 目标view坐标
     * @return
     *  返回屏幕上的位置
     */
    public final PointF sourceToViewCoordinate(@NonNull PointF sourceCoordinate, @NonNull PointF viewTarget){
        return sourceToViewCoordinate(sourceCoordinate.x,sourceCoordinate.y,viewTarget);
    }

    /**
     * 将资源坐标转换为屏幕坐标。
     * @param sourceX 资源的x坐标
     * @param sourceY 资源的y坐标
     * @param viewTarget 目标view坐标
     * @return
     *  返回屏幕上的位置
     */
    private PointF sourceToViewCoordinate(float sourceX, float sourceY, @NonNull PointF viewTarget) {
        if (mViewTranslate == null) {
            return null;
        }
        viewTarget.set(sourceToViewX(sourceX),sourceToViewY(sourceY));
        return viewTarget;
    }

    private Rect sourceToViewRect(Rect sourceRect, Rect viewTarget){
        viewTarget.set((int)sourceToViewX(sourceRect.left),
                (int)sourceToViewY(sourceRect.top),
                (int)sourceToViewX(sourceRect.right),
                (int)sourceToViewY(sourceRect.bottom));
        return viewTarget;
    }

    private PointF getTranslateForSourceCenter(float sourceCenterX, float sourceCenterY, float scale){
        int vxCenter = (getPaddingLeft() + getWidth() - getPaddingRight())/2;
        int vyCenter = (getPaddingTop() + getHeight() - getPaddingBottom())/2;
        mSatTemp.mScale = scale;
        mSatTemp.mViewTranslate.set(vxCenter - sourceCenterX*scale,vyCenter-sourceCenterY*scale);
        fitToBounds(true,mSatTemp);
        return mSatTemp.mViewTranslate;
    }

    private PointF limitedSourceCenter(float sourceCenterX, float sourceCenterY, float scale, @NonNull PointF sourceTarget){
        PointF vTranslate = getTranslateForSourceCenter(sourceCenterX, sourceCenterY, scale);
        int vxCenter = (getPaddingLeft() + getWidth() - getPaddingRight())/2;
        int vyCenter = (getPaddingTop() + getHeight() - getPaddingBottom())/2;
        float sx = (vxCenter - vTranslate.x) / scale;
        float sy = (vyCenter - vTranslate.y) / scale;
        sourceTarget.set(sx,sy);
        return sourceTarget;
    }

    private float minScale() {
        int width = getWidth() - getPaddingLeft() - getPaddingRight();
        int height = getHeight() - getPaddingTop() - getPaddingBottom();
        if (mScaleType == CENTER_CROP){
            return Math.max(width/(float) getShowWidth(), height/(float) getShowHeight());
        }
        if (mScaleType == CUSTOM && mMinScale > 0){
            return mMinScale;
        }
        return Math.min(width/(float) getShowWidth(),height/(float) getShowHeight());
    }

    private float limitedScale(float targetScale){
        return Math.min(mMaxScale,Math.max(minScale(),targetScale));
    }

    public final void setBitmapDataSource(BitmapDataSource decoder){
        mBitmapDataSource = decoder;
    }

    public final void setTranslateLimit(@Translation int translateLimit){
        mTranslateLimit = translateLimit;
        if (isReady()){
            fitToBounds(true);
            invalidate();
        }
    }

    public final void setScaleType(@ScaleType int scaleType){
        if (mScaleType == scaleType){
            return;
        }
        mScaleType = scaleType;
        if (isReady()){
            fitToBounds(true);
            invalidate();
        }
    }

    public final void setMaxScale(float maxScale) {
        mMaxScale = maxScale;
    }

    public final void setMinScale(float minScale) {
        mMinScale = minScale;
    }

    public final void setMinimumDpi(int dpi){
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float averageDpi = (metrics.xdpi + metrics.ydpi)/2;
        setMaxScale(averageDpi / dpi);
    }

    public final void setMaximumDpi(int dpi){
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float averageDpi = (metrics.xdpi + metrics.ydpi)/2;
        setMinScale(averageDpi / dpi);
    }

    public float getMaxScale() {
        return mMaxScale;
    }

    public float getMinScale() {
        return mMinScale;
    }

    public void setMinimumMappingDpi(int minimumMappingDpi) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float averageDpi = (metrics.xdpi + metrics.ydpi)/2;
        mMinimumMappingDpi =(int)Math.min(minimumMappingDpi,averageDpi);
        if (isReady()){
            reset(false);
            invalidate();
        }
    }

    public final PointF getCenter(){
        int mX = getWidth() / 2;
        int mY = getHeight() / 2;
        return viewToSourceCoordinate(mX,mY);
    }

    public float getScale() {
        return mScale;
    }

    public final void setScaleAndCenter(float scale, PointF sCenter){
        stopAnimator();
        mPendingScale = scale;
        mSourcePendingCenter = sCenter;
        invalidate();
    }

    public final void resetScaleAndCenter(){
        stopAnimator();
        mPendingScale = limitedScale(0);
        if (isReady()){
            mSourcePendingCenter = new PointF(getShowWidth()/2.0F, getShowHeight()/2.0F);
        }else{
            mSourcePendingCenter = new PointF(0,0);
        }
        invalidate();
    }

    /**
     * 下一帧是否已经能绘制图像
     * @return true 已经能显示成View上
     * false 还在加载图层中
     */
    public final boolean isReady(){
        return mReadySent;
    }

    public final boolean isImageLoaded(){
        return mImageLoadedSent;
    }

    public final int getSourceWidth() {
        return mSourceWidth;
    }

    public final int getSourceHeight() {
        return mSourceHeight;
    }

    @Orientation
    public final int getOrientation() {
        return mOrientation;
    }

    public ImageSource getImageSource(){
        if (mViewTranslate != null && mSourceWidth > 0 && mSourceHeight > 0){
            return ImageSourceBuilder.newBuilder()
                    .setUri(mUri)
                    .setImageSourceRegion(mSourceRegion)
                    .setImageSourceLoadListener(mImageSourceLoadListener)
                    .setImageSizeOptions(new ImageSizeOptions(mSourceWidth,mSourceHeight))
                    .setOrientation(mOrientation)
                    .setImageViewOptions(new ImageViewOptions(mScale,getCenter()))
                    .build();
        }
        return null;
    }

    public final boolean isZoomEnabled(){
        return mZoomEnabled;
    }

    public final void setZoomEnabled(boolean zoomEnabled) {
        mZoomEnabled = zoomEnabled;
    }

    public final boolean isQuickScaleEnabled() {
        return mQuickScaleEnabled;
    }

    public final void setQuickScaleEnabled(boolean quickScaleEnabled) {
        mQuickScaleEnabled = quickScaleEnabled;
    }

    public final boolean isTranslateEnabled() {
        return mTranslateEnabled;
    }

    public final void setTranslateEnabled(boolean translateEnabled) {
        if (mTranslateEnabled == translateEnabled){
            return;
        }
        mTranslateEnabled = translateEnabled;
        if (!translateEnabled && mViewTranslate != null){
            mViewTranslate.x = (getWidth()-mScale* getShowWidth())/2;
            mViewTranslate.y = (getHeight()-mScale* getShowHeight())/2;
            if (isReady()){
                refreshRequiredTiles(true);
                invalidate();
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.d(TAG, "onAttachedToWindow");
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.d(TAG, "onDetachedFromWindow");
        //recycle();
    }

    public final void setMappingBackgroundColor(@ColorInt int tileBgColor){
        if (Color.alpha(tileBgColor) == 0){
            mMappingBgPaint = null;
        }else{
            mMappingBgPaint = new Paint();
            mMappingBgPaint.setStyle(Paint.Style.FILL);
            mMappingBgPaint.setColor(tileBgColor);
        }
        invalidate();
    }

    public final void setDoubleTapZoomScale(float doubleTapZoomScale) {
        mDoubleTapZoomScale = doubleTapZoomScale;
    }

    public final void setDoubleTapZoomDpi(int dpi){
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float averageDpi = (metrics.xdpi + metrics.ydpi)/2;
        setDoubleTapZoomScale(averageDpi / dpi);
    }

    public final void setDoubleTapZoomStyle(@Zoom int doubleTapZoomStyle) {
        mDoubleTapZoomStyle = doubleTapZoomStyle;
    }

    public final void setDuration(int duration) {
        mDuration = Math.max(0,duration);
    }

    public Rect getSourceRegion() {
        return mSourceRegion;
    }

    @Override
    public void setOnLongClickListener(@Nullable OnLongClickListener l) {
        if (!isLongClickable()) {
            setLongClickable(true);
        }
        mOnLongClickListener = l;
    }

    public void setOnBitmapLoadListener(OnBitmapLoadListener listener){
        mOnBitmapLoadListener = listener;
    }

    public OnBitmapLoadListener getOnBitmapLoadListener(){
        return mOnBitmapLoadListener;
    }

    private void startFilingAnimation(float sCenterXEnd, float sCenterYEnd) {
        if (mValueAnimator != null){
            mValueAnimator.cancel();
        }

        final int vxCenter = (getWidth()+getPaddingLeft()-getPaddingRight())/2;
        final int vyCenter = (getHeight()+getPaddingTop()-getPaddingBottom())/2;
        
        final PointF sourceCenter = new PointF(sCenterXEnd, sCenterYEnd);
        mValueAnimator = new AnimationBuilder(sourceCenter)
                .setTarget(this)
                .setScaleStart(mScale)
                .setScaleEnd(mScale)
                .setViewFocusStart(sourceToViewCoordinate(sourceCenter))
                .setViewFocusEnd(new PointF(vxCenter,vyCenter))
                .setTranslateInterpolator(mTranslationAnimationInterpolator)
                .setScaleInterpolator(mScaleAnimationInterpolator)
                .setDuration(mDuration)
                .addAnimationListener(mAnimatorListener)
                .addAnimationListener(mAnimatorListenerCompat)
                .addAnimationUpdateListener(mAnimatorUpdateListener)
                .addAnimationUpdateListener(mAnimatorUpdateListenerCompat)
                .build();
        mValueAnimator.start();

        Log.d(TAG, "startFilingAnimation");
    }

    private void startZoomForCenter(PointF sCenter, float scaleEnd) {
        if (mValueAnimator != null){
            mValueAnimator.cancel();
        }
        final int vxCenter = (getWidth()+getPaddingLeft()-getPaddingRight())/2;
        final int vyCenter = (getHeight()+getPaddingTop()-getPaddingBottom())/2;

        final float limitScale = limitedScale(scaleEnd);

        final PointF limitSourceCenter = limitedSourceCenter(sCenter.x,sCenter.y,limitScale,new PointF());

        mValueAnimator = new AnimationBuilder(limitSourceCenter)
                .setTarget(this)
                .setScaleStart(mScale)
                .setScaleEnd(limitScale)
                .setViewFocusStart(sourceToViewCoordinate(limitSourceCenter))
                .setViewFocusEnd(new PointF(vxCenter,vyCenter))
                .setDuration(mDuration)
                .setInterrupt(false)
                .setTranslateInterpolator(mTranslationAnimationInterpolator)
                .setScaleInterpolator(mScaleAnimationInterpolator)
                .addAnimationListener(mAnimatorListener)
                .addAnimationListener(mAnimatorListenerCompat)
                .addAnimationUpdateListener(mAnimatorUpdateListener)
                .addAnimationUpdateListener(mAnimatorUpdateListenerCompat)
                .build();

        mValueAnimator.start();

        Log.d(TAG, "startZoomForCenter");
    }

    private void startZoomForFixed(PointF sCenter, PointF vFocus, float scaleEnd) {

        if (mValueAnimator != null){
            mValueAnimator.cancel();
        }

        final float limitScale = limitedScale(scaleEnd);

        final PointF limitSourceCenter = limitedSourceCenter(sCenter.x,sCenter.y,limitScale,new PointF());

        PointF focusEnd;
        if (vFocus != null){
            PointF center = getCenter();
            float vTranslateXEnd = vFocus.x - (limitScale * center.x);
            float vTranslateYEnd = vFocus.y - (limitScale * center.y);
            ScaleAndTranslate satEnd = new ScaleAndTranslate(limitScale,new PointF(vTranslateXEnd,vTranslateYEnd));
            fitToBounds(true,satEnd);
            focusEnd = new PointF(
                    vFocus.x + (satEnd.mViewTranslate.x - vTranslateXEnd),
                    vFocus.y + (satEnd.mViewTranslate.y - vTranslateYEnd));
        }else{
            final int vxCenter = (getWidth()+getPaddingLeft()-getPaddingRight())/2;
            final int vyCenter = (getHeight()+getPaddingTop()-getPaddingBottom())/2;
            focusEnd = new PointF(vxCenter,vyCenter);
        }

        mValueAnimator = new AnimationBuilder(limitSourceCenter)
                .setTarget(this)
                .setScaleStart(mScale)
                .setScaleEnd(limitScale)
                .setViewFocusStart(sourceToViewCoordinate(limitSourceCenter))
                .setViewFocusEnd(focusEnd)
                .setDuration(mDuration)
                .setInterrupt(false)
                .setTranslateInterpolator(mTranslationAnimationInterpolator)
                .setScaleInterpolator(mScaleAnimationInterpolator)
                .addAnimationListener(mAnimatorListener)
                .addAnimationListener(mAnimatorListenerCompat)
                .addAnimationUpdateListener(mAnimatorUpdateListener)
                .addAnimationUpdateListener(mAnimatorUpdateListenerCompat)
                .build();

        mValueAnimator.start();

        Log.d(TAG, "startZoomForFixed");
    }



    private class MappingsInit implements Runnable{

        private BitmapDataSource mDecoder;
        private Rect mRect;
        private Uri mUri;
        private Context mContext;
        MappingsInit(@NonNull Context context,
                     @NonNull BitmapDataSource decoder,
                     Rect rect,
                     @NonNull Uri uri) {
            mContext = context;
            mDecoder = decoder;
            mRect = rect;
            mUri = uri;
        }

        @Override
        public void run() {
            if (mContext == null || mDecoder == null || mUri == null){
                return;
            }
            final Point dimensions = new Point();
            mDecoder.init(mContext, mUri, dimensions, new BitmapDataSource.OnInitListener() {
                @Override
                public void success() {
                    int exifOrientation = getExifOrientation(mContext,mUri.toString());
                    Bundle bundle = new Bundle();
                    bundle.putInt(SOURCE_ORIENTATION,exifOrientation);
                    Message msg = Message.obtain();
                    msg.what = MSG_INIT_SUCCESS;
                    if (mRect != null){

                        if (mRect.left > mRect.right){
                            int tmp = mRect.left;
                            mRect.left = mRect.right;
                            mRect.right = tmp;
                        }

                        if (mRect.left < 0 || mRect.left >= dimensions.x){
                            mRect.left = 0;
                        }

                        if (mRect.right < 0 || mRect.right > dimensions.x){
                            mRect.right = dimensions.x;
                        }

                        if (mRect.top > mRect.bottom){
                            int tmp = mRect.bottom;
                            mRect.bottom = mRect.top;
                            mRect.top = tmp;
                        }

                        if (mRect.top < 0 || mRect.top >= dimensions.y){
                            mRect.top = 0;
                        }

                        if (mRect.bottom < 0 || mRect.bottom > dimensions.y){
                            mRect.bottom = dimensions.y;
                        }

                        bundle.putInt(SOURCE_WIDTH,mRect.width());
                        bundle.putInt(SOURCE_Height,mRect.height());
                    }else {
                        bundle.putInt(SOURCE_WIDTH,dimensions.x);
                        bundle.putInt(SOURCE_Height,dimensions.y);
                    }
                    msg.setData(bundle);
                    mOriginalHandler.sendMessage(msg);
                }

                @Override
                public void failed(Throwable throwable) {
                    Message msg = Message.obtain();
                    msg.what = MSG_INIT_FAILED;
                    msg.obj = throwable;
                    mOriginalHandler.sendMessage(msg);
                    throwable.printStackTrace();
                }
            });
            /*try {
                Point dimensions = new Point();
                mDecoder.init(mContext, mUri,dimensions);
                int exifOrientation = getExifOrientation(mContext,mUri.toString());
                Bundle bundle = new Bundle();
                bundle.putInt(SOURCE_ORIENTATION,exifOrientation);
                Message msg = Message.obtain();
                msg.what = MSG_INIT_SUCCESS;
                if (mRect != null){

                    if (mRect.left > mRect.right){
                        int tmp = mRect.left;
                        mRect.left = mRect.right;
                        mRect.right = tmp;
                    }

                    if (mRect.left < 0 || mRect.left >= dimensions.x){
                        mRect.left = 0;
                    }

                    if (mRect.right < 0 || mRect.right > dimensions.x){
                        mRect.right = dimensions.x;
                    }

                    if (mRect.top > mRect.bottom){
                        int tmp = mRect.bottom;
                        mRect.bottom = mRect.top;
                        mRect.top = tmp;
                    }

                    if (mRect.top < 0 || mRect.top >= dimensions.y){
                        mRect.top = 0;
                    }

                    if (mRect.bottom < 0 || mRect.bottom > dimensions.y){
                        mRect.bottom = dimensions.y;
                    }

                    bundle.putInt(SOURCE_WIDTH,mRect.width());
                    bundle.putInt(SOURCE_Height,mRect.height());
                }else {
                    bundle.putInt(SOURCE_WIDTH,dimensions.x);
                    bundle.putInt(SOURCE_Height,dimensions.y);
                }
                msg.setData(bundle);
                mOriginalHandler.sendMessage(msg);
            } catch (Exception e) {
                Message msg = Message.obtain();
                msg.what = MSG_INIT_FAILED;
                msg.obj = e;
                mOriginalHandler.sendMessage(msg);
                e.printStackTrace();
            }*/
        }
    }

    private class MappingLoad implements Runnable{
        private BitmapDataSource mDecoder;
        private Mapping mMapping;
        private Rect mRegion;
        private int mSourceWidth;
        private int mSourceHeight;
        private int mRotation;

        public MappingLoad(BitmapDataSource decoder, Mapping mapping, Rect region,
                           int sourceWidth, int sourceHeight, int rotation) {
            mDecoder = decoder;
            mMapping = mapping;
            mRegion = region;
            mSourceWidth = sourceWidth;
            mSourceHeight = sourceHeight;
            mRotation = rotation;
        }

        @Override
        public void run() {
            if (mMapping == null || mDecoder == null){
                return;
            }
            if (mDecoder.isReady() && mMapping.mVisible){
                fileRect(mMapping.mSourceRect, mMapping.mFileSourceRect,mSourceWidth,mSourceHeight,mRotation);
                if (mRegion != null){
                    mMapping.mFileSourceRect.offset(mRegion.left,mRegion.top);
                }

                Bitmap bitmap = mDecoder.decode(mMapping.mFileSourceRect, mMapping.mSampleSize);
                if (bitmap != null){
                    mMapping.mBitmap = bitmap;
                    mMapping.mLoading = false;
                    if (!mOriginalHandler.hasMessages(MSG_TILE_LOAD_SUCCESS)) {
                        mOriginalHandler.sendEmptyMessage(MSG_TILE_LOAD_SUCCESS);
                    }else {
                        Log.d(TAG, "已经有相同的消息了");
                    }
                }
            } else {
                mMapping.mLoading = false;
            }
        }
    }

    private static class OriginalHandler extends Handler{

        private final WeakReference<HDImageView> mWef;

        public OriginalHandler(@NonNull HDImageView view) {
            mWef = new WeakReference<>(view);
        }

        @Override
        public void handleMessage(Message msg) {
            HDImageView view = mWef.get();
            if (view == null){
                return;
            }

            switch (msg.what){
                case MSG_INIT_SUCCESS:
                    Bundle data = msg.getData();
                    view.onTilesInitialized(data.getInt(SOURCE_WIDTH),
                            data.getInt(SOURCE_Height),
                            data.getInt(SOURCE_ORIENTATION));
                    return;
                case MSG_INIT_FAILED:
                    Exception e = (Exception) msg.obj;
                    OnBitmapLoadListener listener = view.getOnBitmapLoadListener();
                    if (e != null && listener != null){
                        listener.onBitmapLoadError(e);
                    }
                    return;
                case MSG_TILE_LOAD_SUCCESS:
                    removeMessages(MSG_TILE_LOAD_SUCCESS);
                    view.onTileLoaded();
                    return;
                default:break;
            }
        }
    }
}
