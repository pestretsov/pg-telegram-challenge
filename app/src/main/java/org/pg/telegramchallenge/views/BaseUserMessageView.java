package org.pg.telegramchallenge.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.TextPaint;
import android.util.AttributeSet;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.ViewTarget;
import org.pg.telegramchallenge.R;

import java.text.SimpleDateFormat;
import java.util.Locale;

import static org.pg.telegramchallenge.utils.Utils.*;

/**
 * Created by dodger on 20.01.16.
 */
public class BaseUserMessageView extends BaseChatItemView {

    int mTextPadding = 16; // padding between avatar and text
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);

    protected final int dpAvatarRadius = 20;
    private Drawable mAvatarDrawable = null;

    protected boolean mDetailsVisibility = true;
    private boolean mTimeIsVisible = true;

    private Paint mAvatarCirclePaint;
    private TextPaint mInitialsTextPaint;
    private int mAvatarColor = Color.BLUE;

    public void setTitle(String mTitleText) {
        this.mTitleText = mTitleText;
    }

    private String mTitleText;

    private TextPaint mTitleTextPaint;
    private TextPaint mTimeTextPaint;

    private float mTitleTextSize;
    private float mTimeTextSize;

    private int mTitleTextColor;
    private int mTimeTextColor;

    private String mAvatarImageFilePath;

    public void setDetailsVisibility(boolean isVisible) {

        if (mDetailsVisibility == isVisible)
            return;

        this.mDetailsVisibility = isVisible;
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

        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.BaseUserMessageView);

        try {
            mTitleText = getOrElse(attributes.getString(R.styleable.BaseUserMessageView_titleText), "");

            mTitleTextSize = attributes.getDimension(R.styleable.BaseUserMessageView_titleTextSize, 0f);
            mTimeTextSize = attributes.getDimension(R.styleable.BaseUserMessageView_timeTextSize, 0f);

            mTimeTextColor = attributes.getColor(R.styleable.BaseUserMessageView_timeTextColor, Color.BLACK);
            mTitleTextColor = attributes.getColor(R.styleable.BaseUserMessageView_titleTextColor, Color.BLACK);
        } finally {
            attributes.recycle();
        }

        init();
    }

    private void init() {
        mAvatarCirclePaint = new Paint();
        mAvatarCirclePaint.setStyle(Paint.Style.FILL);
        mAvatarCirclePaint.setColor(mAvatarColor);
        mAvatarCirclePaint.setAntiAlias(true);

        int avatarImageRadius = dpToPx(dpAvatarRadius, getContext());
        float avatarRatio = 1;

        mInitialsTextPaint = getTextPaint(Paint.ANTI_ALIAS_FLAG, avatarImageRadius * avatarRatio, Color.WHITE,
                Paint.Align.CENTER, null, null);

        mTitleTextPaint = getTextPaint(Paint.ANTI_ALIAS_FLAG, mTitleTextSize, mTitleTextColor, null, null, mBoldTypeface);

        mTimeTextPaint = getTextPaint(Paint.ANTI_ALIAS_FLAG, mTimeTextSize, mTimeTextColor, Paint.Align.RIGHT, null, null);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int holdersPadding = dpToPx(VERTICAL_PADDING_DP, getContext());

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        boolean isBarOrDateVisible = (mBarVisibility && mUnreadMessagesCount>0) || mDateVisibility;
        if (mDetailsVisibility) {
            if (isBarOrDateVisible)
                height += holdersPadding;

            height += 2*dpToPx(dpAvatarRadius, getContext());
        }

        setMeasuredDimension(width, height);
    }

    @SuppressWarnings("Duplicates")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Context c = getContext();
        int holdersPadding = dpToPx(VERTICAL_PADDING_DP, c);
        boolean isBarVisible = mBarVisibility && mUnreadMessagesCount>0;

        int top = getPaddingTop() +
                (isBarVisible ? mBarHeight + holdersPadding:0) +
                (mDateVisibility ? mDatePlaceholderHeight + holdersPadding:0);

        final int left = getPaddingLeft();
        final int right = getWidth() - getPaddingRight();
        final int bottom = getHeight() - getPaddingBottom();

        String initials = getInitials(mTitleText);
        int avatarImageRadius = dpToPx(dpAvatarRadius, c);
        if (mDetailsVisibility) {
            if (mAvatarDrawable == null) {
                canvas.drawCircle(left + avatarImageRadius,
                        (top + avatarImageRadius),
                        avatarImageRadius,
                        mAvatarCirclePaint);
                canvas.drawText(initials,
                        left + avatarImageRadius,
                        (top + avatarImageRadius) - (mInitialsTextPaint.descent() + mInitialsTextPaint.ascent())/2,
                        mInitialsTextPaint);
            } else {
                mAvatarDrawable.setBounds(left, top, left + avatarImageRadius*2, top + avatarImageRadius*2);
                mAvatarDrawable.draw(canvas);
            }

            int titleStartX = left + avatarImageRadius*2 + dpToPx(mTextPadding, c);
            int titleStartY = top + (int)mTitleTextSize;
            canvas.drawText(mTitleText, titleStartX, titleStartY, mTitleTextPaint);

            int timeStartY = (int) (top + mTitleTextSize); // to align them
            canvas.drawText(timeFormat.format(mDate.getTime()), right, timeStartY, mTimeTextPaint);
        }
    }

    public void setAvatarFilePath(@Nullable String path){
        mAvatarImageFilePath = path;

        if (mAvatarImageFilePath == null) {
            mAvatarDrawable = null;
            invalidate();
            return;
        }

        Glide.with(getContext())
                .load(mAvatarImageFilePath)
                .asBitmap()
                .fitCenter()
                .into(glideTarget);
    }
}
