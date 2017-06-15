package xyz.zpayh.crop;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import xyz.zpayh.hdimage.HDImageView;

/**
 * Created on 2017/6/15.
 */

public class CropHDImageView extends HDImageView{

    private RectF mCropRectF;
    private Paint mCropPaint;
    private Paint mTPaint;

    Xfermode mode;

    private Paint mLinePaint;

    public CropHDImageView(Context context) {
        super(context);
        init(context);
    }

    public CropHDImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CropHDImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context){
        mCropRectF = new RectF();
        mCropPaint = new Paint();
       // mCropPaint.setColor(context.getResources().getColor(R.color.crop_background));
        mTPaint = new Paint();
        mTPaint.setColor(Color.TRANSPARENT);

        mode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);

        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setColor(Color.WHITE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawCrop(canvas);
        drawAuxiliaryLine(canvas);
    }

    private void drawAuxiliaryLine(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        // 求边长
        int length = Math.min(width,height);
        length -= 80;//留白

        int thickness = 6;

        for (int i = 0; i < 3; i++){
            //横线
            canvas.drawRect((width-length)/2,(height-length-thickness)/2+length*i/3,
                    (width+length)/2,(height-length+thickness)/2+length*i/3,mLinePaint);

            canvas.drawRect((width-length-thickness)/2+length*i/3,
                    (height-length)/2,
                    (width-length+thickness)/2+length*i/3,
                    (height+length)/2,
                    mLinePaint);
        }
    }

    private void drawCrop(Canvas canvas) {

        Bitmap cropBitmap = getCrop();
        canvas.drawBitmap(cropBitmap,0,0,mCropPaint);
    }

    private Bitmap getCrop() {
        int width = getWidth();
        int height = getHeight();
        Bitmap bm = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bm);
        //Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

        //p.setColor(getResources().getColor(R.color.crop_background));
        //c.drawRect(0,0,getWidth(),getHeight(),p);
        c.drawColor(getResources().getColor(R.color.crop_background));

        mCropPaint.setXfermode(mode);

        calCropRect(width,height,mCropRectF);

        c.drawRect(mCropRectF,mCropPaint);

        mCropPaint.setXfermode(null);

        return bm;
    }

    private void calCropRect(int width, int height, RectF rectF) {
        // 求边长
        int length = Math.min(width,height);

        length -= 80;//留白

        // 居中
        rectF.set((width-length)/2,(height-length)/2,(width+length)/2,(height+length)/2);
    }
}
