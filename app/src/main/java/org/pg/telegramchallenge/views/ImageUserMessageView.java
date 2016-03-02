package org.pg.telegramchallenge.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.ViewTarget;

import org.pg.telegramchallenge.utils.Utils;

import static org.pg.telegramchallenge.utils.Utils.dpToPx;

/**
 * Created by roman on 24.02.2016.
 */
public class ImageUserMessageView extends BaseUserMessageView {

    private int mImageHeight, mImageWidth;
    private String mImagePath;
    private Drawable mImageDrawable;
    private ViewTarget<ImageUserMessageView, GlideDrawable> glideTarget = new ViewTarget<ImageUserMessageView, GlideDrawable>(this) {

        @Override
        public Request getRequest() {
            return null;
        }

        @Override
        public void setRequest(Request request) {

        }

        @Override
        public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
            mImageDrawable = resource;
            invalidate();
            requestLayout();
        }
    };

    public ImageUserMessageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final Context c = getContext();
        int avatarDiameter = Utils.dpToPx(dpAvatarRadius, c)*2;
        int textPadding = Utils.dpToPx(this.mTextPadding, c);

        int width = getMeasuredWidth() - (getPaddingLeft() + getPaddingRight());
        int heightWithPadding = getMeasuredHeight();
        int adjustedImageHeight;
        int maxImageWidth = width
                - dpToPx(dpAvatarRadius*2, c)
                - dpToPx(mTextPadding, c)*2 // *2 because same padding between status icon and text
                - clockIcon.getIntrinsicWidth();
        if (mImageWidth <= maxImageWidth) {
            adjustedImageHeight = mImageHeight;
        } else {
            adjustedImageHeight = (int) (mImageHeight * (float)maxImageWidth/mImageWidth);
        }
//
        if (mDetailsVisibility) {
            adjustedImageHeight -= ((getMeasuredHeight()-getPaddingBottom()-getPaddingTop()) - mTitleTextSize - textPadding);
        }

        if (adjustedImageHeight>0) {
            heightWithPadding += adjustedImageHeight;
        }

        setMeasuredDimension(getMeasuredWidth(), heightWithPadding);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Context c = getContext();
        int holdersPadding = dpToPx(VERTICAL_PADDING_DP, c);
        boolean isBarVisible = mBarVisibility && mUnreadMessagesCount>0;

        int avatarDiameter = Utils.dpToPx(dpAvatarRadius, c)*2;
        int textPadding = Utils.dpToPx(this.mTextPadding, c);

        int right = getPaddingRight();
        int top = getPaddingTop() +
                (isBarVisible ? mBarHeight + holdersPadding:0) +
                (mDateVisibility ? mDatePlaceholderHeight + holdersPadding:0);
        int bottom = getHeight() - getPaddingBottom();

        int imageLeft = right + avatarDiameter + textPadding;
        int imageRight;
        int maxImageWidth = getWidth()
                - getPaddingLeft() - getPaddingRight()
                - textPadding*2
                - clockIcon.getIntrinsicWidth()
                - avatarDiameter;
        if (mImageWidth>maxImageWidth) {
            imageRight = imageLeft + maxImageWidth;
        } else {
            imageRight = imageLeft + mImageWidth;
        }
//        int imageLeft = getWidth() - getPaddingLeft();
        int imageTop = top + (mDetailsVisibility?(int)mTitleTextSize + textPadding:0);
        int imageBottom = bottom;

        if (mImageDrawable == null) {
            // TODO: 25.02.2016 some animation or something.
        } else {
            mImageDrawable.setBounds(imageLeft, imageTop, imageRight, imageBottom);
            mImageDrawable.draw(canvas);
        }
    }

    public void setImage(String path, int imageWidth, int imageHeight) {
        if (imageHeight == 0 || imageWidth == 0) {
            throw new IllegalArgumentException("Height or width cannot be zero!");
        }

        mImageDrawable = null;
        mImageWidth = imageWidth;
        mImageHeight = imageHeight;
        mImagePath = path;
        requestLayout();
        invalidate();
        Glide.with(getContext()).load(path).override(imageWidth, imageHeight).into(glideTarget);
    }
}
