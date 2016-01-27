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
import org.pg.telegramchallenge.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static org.pg.telegramchallenge.utils.Utils.*;

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
    public static int mTextPadding = 16; // padding between avatar and text
    public static int mStatusPadding = 8; // padding between status drawable and time text

    // it's not finally defined yet!
    public  static final int dpAvatarRadius = 20;
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
    private float textLength, titleTextLength;

    private float[] mTextWidths, mTitleTextWidths;
    private Calendar mDate;

    private int unread = 0;
    private ChatStatus mStatus = ChatStatus.READ;
    private String avatarImageFilePath = null;
    ViewTarget<ChatListItemView, Bitmap> glideTarget;

    private static Drawable clockIcon, bageIcon;
    private Drawable avatarDrawable = null;
    private final int bageColor = ContextCompat.getColor(getContext(), R.color.accent_telegram_blue);
    private int mCounterColor;

    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy", Locale.ENGLISH);
    private final Rect bounds = new Rect();

    public ChatListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray attributes = context.obtainStyledAttributes(attrs,
                R.styleable.ChatListItemView);

        String text, title;

        try {
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

            text = getOrElse(attributes.getString(R.styleable.ChatListItemView_android_text), "");
            title = getOrElse(attributes.getString(R.styleable.ChatListItemView_titleText), "");
        } finally {
            attributes.recycle();
        }

        init();

        setText(text, false);
        setTitle(title, false);

        mDate = Calendar.getInstance();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        int heightWithoutPadding = dpToPx(dpAvatarRadius*2, getContext());
        int widthWithoutPadding = width - getPaddingLeft() - getPaddingRight();

        setMeasuredDimension(widthWithoutPadding + getPaddingLeft() + getPaddingRight(),
                heightWithoutPadding + getPaddingTop() + getPaddingBottom());
    }

    private final Utils.Computable<Drawable> clockComputable = new Utils.Computable<Drawable>() {
        @Override
        public Drawable compute() {
            return getResources().getDrawable(R.drawable.ic_clock);
        }
    };

    private final Utils.Computable<Drawable> bageComputable = new Utils.Computable<Drawable>() {
        @Override
        public Drawable compute() {
            return getResources().getDrawable(R.drawable.ic_badge);
        }
    };

    protected void init(){
        // size of avatar letters / avatar radius
        float avatarRatio = 1;

        clockIcon = getOrCompute(clockIcon, clockComputable);

        bageIcon = getOrCompute(bageIcon, bageComputable);
        bageIcon.setColorFilter(bageColor, PorterDuff.Mode.SRC_ATOP);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(mTextColor);
        if (mTextHeight == 0) {
            mTextHeight = mTextPaint.getTextSize();
        } else {
            mTextPaint.setTextSize(mTextHeight);
        }

        mTimeTextPaint = getTextPaint(Paint.ANTI_ALIAS_FLAG, mTimeTextHeight, mTextColor, Paint.Align.RIGHT, null, null);

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
        mInitialsTextPaint.setTextAlign(Paint.Align.CENTER);

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
                    left + avatarImageRadius,
                    (top + bottom) / 2 - (mInitialsTextPaint.descent() + mInitialsTextPaint.ascent())/2,
                    mInitialsTextPaint);
        } else {
            avatarDrawable.setBounds(left, top, left + avatarImageRadius*2, top + avatarImageRadius*2);
            avatarDrawable.draw(canvas);
        }

        // title and main text should begin on same position
        float textStartX = left + avatarImageRadius*2 + dpToPx(mTextPadding, context);

        canvas.drawText(displayedTime,
                right,
                top + mTitleTextHeight,
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

        float maxTextLength = textEndX - textStartX;
        String adjustedString = adjustString(mText, textLength, mTextWidths, maxTextLength, mTextPaint);
        canvas.drawText(adjustedString, textStartX, bottom - betweenText, mTextPaint);

        // constant dp for now; mb shoul change that
        float maxTitleTextLength = - textStartX + (right - dpToPx(85, context));
        String adjustedTitle = adjustString(mTitleText, titleTextLength, mTitleTextWidths, maxTitleTextLength, mTitleTextPaint);
        canvas.drawText(adjustedTitle, textStartX, top + mTitleTextHeight + betweenText, mTitleTextPaint);

    }

    public void setTitle(String s){
        setTitle(s, true);
    }

    private void setTitle(String s, boolean invalidate) {
        mTitleText = s;
        mTitleTextWidths = new float[mTitleText.length()];
        if (mTitleTextPaint!=null) {
            mTitleTextPaint.getTextWidths(mTitleText, 0, mTitleText.length(), mTitleTextWidths);
            mTitleTextPaint.getTextBounds(mTitleText, 0, mTitleText.length(), bounds);
            titleTextLength = bounds.right-bounds.left;
        }

        if (invalidate)
            invalidate();
    }

    public void setText(String s){
        setText(s, true);
    }

    private void setText(String s, boolean invalidate){
        mText = s;
        mTextWidths = new float[mText.length()];

        if (mTextPaint!=null) {
            mTextPaint.getTextWidths(mText, 0, mText.length(), mTextWidths);
            mTextPaint.getTextBounds(mText, 0, mText.length(), bounds);
            textLength = bounds.right - bounds.left;
        }

        if (invalidate)
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
}