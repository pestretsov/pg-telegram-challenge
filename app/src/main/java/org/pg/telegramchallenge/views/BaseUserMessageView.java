package org.pg.telegramchallenge.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.TextPaint;
import android.util.AttributeSet;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.Request;
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

    private Paint mAvatarCirclePaint;
    private TextPaint mInitialsTextPaint;
    private int mAvatarColor = Color.BLUE;

    private String mFirstName, mSecondName;

    private TextPaint mTitleTextPaint;

    private TextPaint mTimeTextPaint;
    protected float mTitleTextSize;

    private float mTimeTextSize;
    private int mTitleTextColor;

    private int mTimeTextColor;
    private String mAvatarImageFilePath;

    private ChatListItemView.MessageStatus mStatus = ChatListItemView.MessageStatus.UNREAD;
    protected static Drawable clockIcon, badgeIcon;
    private static int badgeColor;

    {
        final Context context = getContext();
        if (clockIcon == null)
            clockIcon = ContextCompat.getDrawable(context, R.drawable.ic_clock);
        if (badgeColor == 0) {
            badgeColor = ContextCompat.getColor(context, R.color.accent_telegram_blue);
        }
        if (badgeIcon == null){
            badgeIcon = ContextCompat.getDrawable(context, R.drawable.ic_badge);
            badgeIcon.setColorFilter(badgeColor, PorterDuff.Mode.SRC_ATOP);
        }
    }

    public void setDetailsVisibility(boolean isVisible) {

        if (mDetailsVisibility == isVisible)
            return;

        this.mDetailsVisibility = isVisible;
        requestLayout();
        invalidate();
    }

    private ViewTarget<BaseUserMessageView, Bitmap> glideTarget = new ViewTarget<BaseUserMessageView, Bitmap>(this) {

        @Override
        public Request getRequest() {
            return null;
        }

        @Override
        public void setRequest(Request request) {

        }

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
            setTitle(getOrElse(attributes.getString(R.styleable.BaseUserMessageView_titleText), ""),
                        "",
                        false);

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

        mTimeTextPaint = getTextPaint(Paint.ANTI_ALIAS_FLAG, mTimeTextSize, mTimeTextColor, Paint.Align.LEFT, null, null);
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

    protected int getStatusCenterY(){
        return (int)(mTitleTextSize + (mTimeTextPaint.ascent() + mTimeTextPaint.descent())/2);
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

        String initials = initialsBuilder.toString().toUpperCase();
        int avatarImageRadius = dpToPx(dpAvatarRadius, c);

        float statusCenterY = top + getStatusCenterY();
        float iconRadius = 0;
        switch (mStatus) {
            case UNREAD:
                iconRadius = mTitleTextSize/4;
//                    iconRadius = clockIcon.getIntrinsicHeight()/2;
                badgeIcon.setBounds((int)(right - iconRadius*2),
                        (int)(statusCenterY-iconRadius),
                        right,
                        (int)(statusCenterY+iconRadius));
                badgeIcon.draw(canvas);
                break;
            case DELIVERING:
                iconRadius = clockIcon.getIntrinsicHeight()/2;
                clockIcon.setBounds(right - (int)(iconRadius*2),
                        (int)(statusCenterY - iconRadius),
                        right,
                        (int)(statusCenterY + iconRadius));
                clockIcon.draw(canvas);
                break;
        }

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

            int textPadding = dpToPx(mTextPadding, c);
            int titleStartX = left + avatarImageRadius*2 + textPadding;
            int titleStartY = top + (int)mTitleTextSize;

            final String timeString = timeFormat.format(mDate.getTime());
            int timeLength = (int) mTimeTextPaint.measureText(timeString);
            int titleMaxLength = right
                    - (iconRadius==0?0:((int)(iconRadius*2) + holdersPadding))
                    - (timeLength + holdersPadding) - titleStartX;

            String adjustedTitle = adjustString(mFirstName, titleMaxLength, mTitleTextPaint);
            canvas.drawText(adjustedTitle, titleStartX, titleStartY, mTitleTextPaint);

            int timeStartY = (int) (top + mTitleTextSize); // to align them
            int timeStartX = (int) (titleStartX + mTitleTextPaint.measureText(adjustedTitle) + holdersPadding);
            canvas.drawText(timeString, timeStartX, timeStartY, mTimeTextPaint);
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

    private StringBuilder initialsBuilder = new StringBuilder();
    public void setTitle(@NonNull String firstName, @Nullable String secondName) {
        setTitle(firstName, secondName, true);
    }
    private void setTitle(@NonNull String firstName, @Nullable String secondName, boolean toInvalidate) {
        if (firstName == null) {
            throw new IllegalArgumentException("firstName cannot be null!");
        }

        initialsBuilder.setLength(0);
        mFirstName = firstName;
        if (!mFirstName.isEmpty()) {
            initialsBuilder.append(mFirstName.charAt(0));
        }
        if (secondName == null || secondName.isEmpty()) {
            if (mFirstName.length()>=2)
                initialsBuilder.append(mFirstName.charAt(1));
        } else {
            initialsBuilder.append(secondName.charAt(0));
        }
        mSecondName = secondName;

        if (toInvalidate)
            invalidate();
    }

    public void setStatus(ChatListItemView.MessageStatus status) {
        if (mStatus == status)
            return;

        mStatus = status;
        invalidate();
    }
}
