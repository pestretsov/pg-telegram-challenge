package org.pg.telegramchallenge.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.ViewTarget;

/**
 * Created by roman on 24.02.2016.
 */
public class ImageUserMessageView extends BaseUserMessageView {

    private int mImageHeight, mImageWidth;
    private String mImagePath;
    private Drawable mImageDrawable;
    private ViewTarget<ImageUserMessageView, GlideDrawable> glideTarget = new ViewTarget<ImageUserMessageView, GlideDrawable>(this) {
        @Override
        public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
            mImageDrawable = resource;
            invalidate();
        }
    };

    public ImageUserMessageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    public void setImage(String path, int imageWidth, int imageHeight) {
        if (imageHeight == 0 || imageWidth == 0) {
            throw new IllegalArgumentException("Height or width cannot be zero!");
        }

        mImagePath = path;
    }
}
