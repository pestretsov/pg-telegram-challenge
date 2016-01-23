package org.pg.telegramchallenge.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.TextPaint;
import android.util.AttributeSet;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.ViewTarget;
import org.pg.telegramchallenge.utils.Utils;

import static org.pg.telegramchallenge.utils.Utils.dpToPx;

/**
 * Created by dodger on 20.01.16.
 */
public class BaseUserMessageView extends BaseChatItemView {

    int mTextPadding = 16; // padding between avatar and text

    private final int dpAvatarRadius = 20;
    private Drawable mAvatarDrawable = null;

    private boolean mAvatarIsDisplayed = true;
    private Paint mAvatarCirclePaint;
    private TextPaint mInitialsTextPaint;
    private int mAvatarColor = Color.BLUE;

    public void setAvatarIsDisplayed(boolean avatarIsDisplayed) {

        if (mAvatarIsDisplayed == avatarIsDisplayed)
            return;

        this.mAvatarIsDisplayed = avatarIsDisplayed;
        requestLayout();
        invalidate();
    }

    ViewTarget<BaseUserMessageView, Bitmap> glideTarget = new ViewTarget<BaseUserMessageView, Bitmap>(this) {
        @Override
        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
            RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getContext().getResources(), resource);
            roundedBitmapDrawable.setCircular(true);
            mAvatarDrawable = roundedBitmapDrawable;
            view.invalidate();
        }
    };


    public BaseUserMessageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    private void init() {
        mAvatarCirclePaint = new Paint();
        mAvatarCirclePaint.setStyle(Paint.Style.FILL);
        mAvatarCirclePaint.setColor(mAvatarColor);
        mAvatarCirclePaint.setAntiAlias(true);

        int avatarImageRadius = Utils.dpToPx(dpAvatarRadius, getContext());
        float avatarRatio = 1;

        mInitialsTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mInitialsTextPaint.setTextSize(avatarImageRadius * avatarRatio);
        mInitialsTextPaint.setColor(Color.WHITE);
        mInitialsTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int holdersPadding = dpToPx(HORIZONTAL_PADDING_DP, getContext());

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        if (mAvatarIsDisplayed) {
            boolean isBarVisible = mBarVisibility && mUnreadMessagesCount>0;
            if (isBarVisible || mDateVisibility)
                height += holdersPadding;

            height += 2*Utils.dpToPx(dpAvatarRadius, getContext());
        }

        setMeasuredDimension(width, height);
    }

    @SuppressWarnings("Duplicates")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int holdersPadding = dpToPx(HORIZONTAL_PADDING_DP, getContext());
        boolean isBarVisible = mBarVisibility && mUnreadMessagesCount>0;

        int top = getPaddingTop() +
                (isBarVisible ? mBarHeight + holdersPadding:0) +
                (mDateVisibility ? mDatePlaceholderHeight + holdersPadding:0);

        final int left = getPaddingLeft();
        final int right = getWidth() - getPaddingRight();
        final int bottom = getHeight() - getPaddingBottom();

        String initials = "KK";
        int avatarImageRadius = Utils.dpToPx(dpAvatarRadius, getContext());
        if (mAvatarIsDisplayed) {
            if (mAvatarDrawable == null) {
                canvas.drawCircle(left + avatarImageRadius,
                        (top + bottom) / 2,
                        avatarImageRadius,
                        mAvatarCirclePaint);
                canvas.drawText(initials,
                        left + avatarImageRadius,
                        (top + bottom) / 2 - (mInitialsTextPaint.descent() + mInitialsTextPaint.ascent())/2,
                        mInitialsTextPaint);
            } else {
                mAvatarDrawable.setBounds(left, top, left + avatarImageRadius*2, top + avatarImageRadius*2);
                mAvatarDrawable.draw(canvas);
            }
        }    }
}
