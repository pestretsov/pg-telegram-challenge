package org.pg.telegramchallenge.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.View;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.ViewTarget;
import org.pg.telegramchallenge.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static org.pg.telegramchallenge.utils.Utils.dpToPx;
import static org.pg.telegramchallenge.utils.Utils.getOrElse;

public class ChatListItemView extends View {

    private static final String TAG = ChatListItemView.class.getSimpleName();
    private Paint mCounterTextPaint;

    public ChatStatus getStatus() {
        return mStatus;
    }

    public enum ChatStatus{
        DELIVERING, READ, UNREAD;
    }

    private int mLineColor;
    private int mTextColor, mTitleTextColor, avatarColor;
    private int mCounterTextColor;

    //paddings in dp
    int mTextPadding = 16; // padding between avatar and text
    int mStatusPadding = 8; // padding between status drawable and time text

    // it's not finally defined yet!
    private final int dpAvatarRadius = 20;
    private int avatarImageRadius = dpToPx(dpAvatarRadius, getContext());

    private float mTextHeight;
    private float mTitleTextHeight;
    private float mCounterTextHeight;
    private float mTimeTextHeight;

    private Paint linePaint, mTextPaint, mTimeTextPaint;
    private Paint mTitleTextPaint;
    private Paint mInitialsTextPaint;
    private Paint avatarCirclePaint;
    private Paint avatarImagePaint;
    private Paint counterPaint;

    // mText is for message text, mTitleText is for name
    private String mText, mTitleText;
    private float[] mTextWidths, mTitleTextWidths;
    private Calendar mDate;

    private int unread = 0;
    private ChatStatus mStatus = ChatStatus.READ;
    private String avatarImageFilePath = null;
    ViewTarget<ChatListItemView, Bitmap> glideTarget;

    private Drawable clockIcon, bageIcon;
    private Drawable avatarDrawable = null;
    private final int bageColor = ContextCompat.getColor(getContext(), R.color.accent_telegram_blue);
    private final int mCounterColor;

    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy", Locale.ENGLISH);
    private final Rect bounds = new Rect();

    public ChatListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray attributes = context.obtainStyledAttributes(attrs,
                R.styleable.ChatListItemView);

        try {
            clockIcon = attributes.getDrawable(R.styleable.ChatListItemView_myDrawable);
//            mLineColor = attributes.getColor(R.styleable.BaseView_android_background, Color.WHITE);
            mLineColor = Color.BLUE;
            avatarColor =  attributes.getColor(R.styleable.ChatListItemView_avatarColor, Color.MAGENTA);
            mTextColor = attributes.getColor(R.styleable.ChatListItemView_android_textColor, Color.BLACK);
            mTitleTextColor = attributes.getColor(R.styleable.ChatListItemView_titleTextColor, Color.BLACK);

            mCounterColor = attributes.getColor(R.styleable.ChatListItemView_counterColor, Color.GREEN);
            mCounterTextHeight = attributes.getDimension(R.styleable.ChatListItemView_counterTextSize, 0.0f);
            mCounterTextColor = attributes.getColor(R.styleable.ChatListItemView_counterTextColor, Color.WHITE);

            mTextHeight = attributes.getDimension(R.styleable.ChatListItemView_android_textSize, 0.0f);
            mTitleTextHeight = attributes.getDimension(R.styleable.ChatListItemView_titleTextSize, 0.0f);
            mTimeTextHeight = attributes.getDimension(R.styleable.ChatListItemView_timeTextSize, 0.0f);


            mText = getOrElse(attributes.getString(R.styleable.ChatListItemView_android_text), "");
            mTextWidths = new float[mText.length()];
            mTitleText = getOrElse(attributes.getString(R.styleable.ChatListItemView_titleText), "");
            mTitleTextWidths = new float[mTitleText.length()];

        } finally {
            attributes.recycle();
        }

        init();

        mDate = Calendar.getInstance();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        int heightWithoutPadding = dpToPx(40f, getContext());
        int widthWithoutPadding = width - getPaddingLeft() - getPaddingRight();

        setMeasuredDimension(widthWithoutPadding + getPaddingLeft() + getPaddingRight(),
                heightWithoutPadding + getPaddingTop() + getPaddingBottom());
    }

    protected void init(){
        // size of avatar letters / avatar radius
        float avatarRatio = 1;

        clockIcon = getResources().getDrawable(R.drawable.ic_clock);
        bageIcon = getResources().getDrawable(R.drawable.ic_badge);
        bageIcon.setColorFilter(bageColor, PorterDuff.Mode.SRC_ATOP);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(mTextColor);
        if (mTextHeight == 0) {
            mTextHeight = mTextPaint.getTextSize();
        } else {
            mTextPaint.setTextSize(mTextHeight);
        }

        mTimeTextPaint = new Paint(mTextPaint);
        mTimeTextPaint.setTextSize(mTimeTextHeight);

        mTitleTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTitleTextPaint.setColor(mTitleTextColor);
        if (mTitleTextHeight == 0) {
            mTitleTextHeight = mTitleTextPaint.getTextSize();
        } else {
            mTitleTextPaint.setTextSize(mTitleTextHeight);
        }

        mInitialsTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mInitialsTextPaint.setTextSize(avatarImageRadius * avatarRatio);
        mInitialsTextPaint.setColor(Color.WHITE);

        linePaint = new Paint();
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(0);
        linePaint.setColor(mLineColor);
        linePaint.setAntiAlias(true);

        avatarCirclePaint = new Paint();
        avatarCirclePaint.setStyle(Paint.Style.FILL);
        avatarCirclePaint.setColor(avatarColor);
        avatarCirclePaint.setAntiAlias(true);

        avatarImagePaint = new Paint();
        avatarImagePaint.setAntiAlias(true);
        avatarImagePaint.setFilterBitmap(true);
        avatarImagePaint.setDither(true);

        counterPaint = new Paint();
        counterPaint.setStyle(Paint.Style.FILL_AND_STROKE);
//        counterPaint.setStrokeWidth(0);
        counterPaint.setColor(mCounterColor);
        counterPaint.setAntiAlias(true);

        mCounterTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCounterTextPaint.setTextSize(mCounterTextHeight);
        mCounterTextPaint.setColor(mCounterTextColor);

        glideTarget = new ViewTarget<ChatListItemView, Bitmap>(this) {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getContext().getResources(), resource);
                roundedBitmapDrawable.setCircular(true);
                avatarDrawable = roundedBitmapDrawable;
                view.invalidate();
            }
        };
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        Context context = getContext();

        final int left = getPaddingLeft();
        final int top = getPaddingTop();
        final int right = getWidth() - getPaddingRight();
        final int bottom = getHeight() - getPaddingBottom();

        int widthWithoutPadding = right - left;
        int heightWithoutPadding = bottom - top;

        float betweenText = 0;//(float)(heightWithoutPadding - Math.round(mTextHeight + mTitleTextHeight))/6;

        final String initials = getInitials(mTitleText);
        mInitialsTextPaint.getTextBounds(initials, 0, initials.length(), bounds);


        String displayedTime;
        if (DateUtils.isToday(mDate.getTimeInMillis())) {
           displayedTime = timeFormat.format(mDate.getTime());
        } else {
            displayedTime = dateFormat.format(mDate.getTime());
        }

        // drawing avatar
        if (avatarDrawable == null) {
            canvas.drawCircle(left + avatarImageRadius,
                    (top + bottom) / 2,
                    avatarImageRadius,
                    avatarCirclePaint);
            canvas.drawText(initials,
                    left + avatarImageRadius - mInitialsTextPaint.measureText(initials) / 2,
                    (top + bottom) / 2 + (bounds.bottom - bounds.top) / 2,
                    mInitialsTextPaint);
        } else {
            avatarDrawable.setBounds(left, top, left + avatarImageRadius*2, top + avatarImageRadius*2);
            avatarDrawable.draw(canvas);
        }

        // title and main text should begin on same position
        float textStartX = left + avatarImageRadius*2 + dpToPx(mTextPadding, context);

        canvas.drawText(displayedTime,
                right - mTimeTextPaint.measureText(displayedTime),
                top + mTimeTextPaint.getTextSize() + betweenText + (mTitleTextHeight-mTimeTextHeight),
                mTimeTextPaint);

        Drawable statusDrawable = null;
        switch (mStatus) {
            case UNREAD:
                statusDrawable = bageIcon;
                break;
            case DELIVERING:
                statusDrawable = clockIcon;
                break;
        }

        if (statusDrawable!=null) {
            mTimeTextPaint.getTextBounds("0", 0, 1, bounds);

            // drawable would be as height as text
            // bounds are NOT equal to textHeight!
            int timeTextRealHight = bounds.bottom - bounds.top;
            int statusDrawableSize = timeTextRealHight / ((mStatus==ChatStatus.UNREAD)?2:1);

            int textLeft, textTop, textRight, textBottom;

            textRight = (int) (right - mTimeTextPaint.measureText(displayedTime) - dpToPx(mStatusPadding, context));
            textLeft = textRight - statusDrawableSize;

            textBottom = (int) (top + betweenText + mTitleTextHeight - timeTextRealHight/2f + statusDrawableSize/2f);
            textTop = textBottom - statusDrawableSize;

            statusDrawable.setBounds(textLeft, textTop, textRight, textBottom);

            statusDrawable.draw(canvas);
        }

        float textEndX;

        if (unread!=0) {
            String unreadString = Integer.toString(unread);
            mCounterTextPaint.getTextBounds("0", 0, 1, bounds);
            int digitBounds = bounds.bottom - bounds.top;

            float radius = (mCounterTextHeight) * 0.7f;
            float rectLength = (unreadString.length()-1)*(bounds.right-bounds.left)*1.2f;

            float cx, cy;
            cx = right - radius;
            cy = bottom - betweenText - digitBounds/2;

            canvas.drawCircle(cx - rectLength,
                    cy,
                    radius,
                    counterPaint
            );

            canvas.drawCircle(cx,
                    cy,
                    radius,
                    counterPaint
            );

            textEndX = cx - rectLength - radius - dpToPx(mTextPadding, context);

            canvas.drawRect(cx - rectLength,
                    cy - radius,
                    cx,
                    cy + radius,
                    counterPaint);

            canvas.drawText(unreadString,
                    cx - rectLength/2 - mCounterTextPaint.measureText(unreadString)/2,
//                    cy + digitBounds/2,
                    bottom - betweenText,
                    mCounterTextPaint);
        } else {
            textEndX = right;
        }

        mTextPaint.getTextWidths(mText, 0, mText.length(), mTextWidths);
        mTitleTextPaint.getTextWidths(mTitleText, 0, mTitleText.length(), mTitleTextWidths);

        float textLength = 0;
        for (float f : mTextWidths) {
            textLength += f;
        }

        float titleTextLength = 0;
        for (float f: mTitleTextWidths) {
            titleTextLength += f;
        }

        float maxTextLength = textEndX - textStartX;
        if (textLength > maxTextLength) {
            int pos = 0;
            float dotsLenght = mTextPaint.measureText("...");
            textLength = 0;

            while (textLength + mTextWidths[pos] < (maxTextLength-dotsLenght)){
                textLength += mTextWidths[pos];
                pos++;
            }

            canvas.drawText(mText.substring(0,pos).concat("..."), textStartX, bottom - betweenText, mTextPaint);
        } else {
            canvas.drawText(mText, textStartX, bottom - betweenText, mTextPaint);
        }

        // constant dp for now; mb shoul change that
        float maxTitleTextLength = - textStartX + (right - dpToPx(85, context));
        if (titleTextLength>maxTitleTextLength) {
            int pos = 0;
            float dotsLenght = mTitleTextPaint.measureText("...");
            titleTextLength = 0;

            while (titleTextLength + mTitleTextWidths[pos] < (maxTitleTextLength-dotsLenght)){
                titleTextLength += mTitleTextWidths[pos];
                pos++;
            }

            canvas.drawText(mTitleText.substring(0,pos).concat("..."),
                    textStartX, top + mTitleTextHeight + betweenText, mTitleTextPaint);
        } else {
            canvas.drawText(mTitleText, textStartX, top + mTitleTextHeight + betweenText, mTitleTextPaint);
        }

    }

    public void setTitle(String s){
        mTitleText = s;
        mTitleTextWidths = new float[mTitleText.length()];
        invalidate();
    }

    public void setText(String s){
        mText = s;
        mTextWidths = new float[mText.length()];
        invalidate();
    }

    public void setDate(Date date){
        mDate = Calendar.getInstance();
        mDate.setTime(date);
        invalidate();
    }

    public void setDate(Calendar c) {
        mDate = c;
        invalidate();
    }

    public void setAvatarColor(int color) {
        avatarColor = color;
        avatarCirclePaint.setColor(avatarColor);
        invalidate();
    }

    public void setAvatarFilePath(@Nullable String path){
        avatarImageFilePath = path;

        if (avatarImageFilePath == null) {
            avatarDrawable = null;
            invalidate();
            return;
        }

        Glide.with(getContext())
                .load(avatarImageFilePath)
                .asBitmap()
                .fitCenter()
                .into(glideTarget);
    }

    /**
     * Sets number of unread messages in green circle
     *
     * @param unreadCount
     * if there is no unread messages, pass 0;
     */
    public void setUnreadCount(int unreadCount) {
        unread = unreadCount;
        invalidate();
    }

    /**
     * displays little mStatus icon left to the time
     *
     * @param status
     *
     */
    public void setStatus(ChatStatus status){
        mStatus = status;
        invalidate();
    }

    protected String getInitials(String s){
        if (s == null)
            return "";

        StringBuilder initials = new StringBuilder();
        for (String a: s.split(" ")){
            if (!a.isEmpty())
                initials.append(a.charAt(0));
            if (initials.length()>=2)
                break;
        }

        return initials.toString().toUpperCase();
    }
}
