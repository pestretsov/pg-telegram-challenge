package org.pg.telegramchallenge.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import org.pg.telegramchallenge.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChatListItemView extends View {

    private static final String TAG = ChatListItemView.class.getSimpleName();
    private Paint mCounterTextPaint;

    public ChatStatus getStatus() {
        return mStatus;
    }
    //TODO i guess we should write a method with whole TdApi.Chat as argument

    public static enum ChatStatus{
        DELIVERING, READ, UNREAD;
    }

    private int mLineColor;
    private int mTextColor, mTitleTextColor, avatarColor;
    private int mCounterTextColor;

    // it's not finally defined yet!
    private final int dpAvatarSize = 30;
    private int avatarImageRadius = (int) pxFromDp(dpAvatarSize, getContext());

    private float mTextHeight;
    private float mTitleTextHeight;
    private float mCounterTextHeight;

    private Paint linePaint, mTextPaint;
    private Paint mTitleTextPaint;
    private Paint mInitialsTextPaint;
    private Paint avatarPaint;
    private Paint counterPaint;

    private String mText, mTitleText;
    private Date mDate;

    private int unread = 0;
    private ChatStatus mStatus = ChatStatus.DELIVERING;

    Drawable clockIcon, bageIcon;
    private final int bageColor = ContextCompat.getColor(getContext(), R.color.accent_telegram_blue);
    private final int mCounterColor;


    private static final SimpleDateFormat localeDateFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
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

            mText = attributes.getString(R.styleable.ChatListItemView_android_text);
            mTitleText = attributes.getString(R.styleable.ChatListItemView_titleText);

        } finally {
            attributes.recycle();
        }

        init();

        mDate = new Date(System.currentTimeMillis());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        int height = Math.max(avatarImageRadius*2, Math.round(mTextHeight + mTitleTextHeight));
        int widthWithoutPadding = width - getPaddingLeft() - getPaddingRight();

        setMeasuredDimension(widthWithoutPadding + getPaddingLeft() + getPaddingRight(),
                height + getPaddingTop() + getPaddingBottom());
    }

    protected void init(){
        // size of avatar letters / avatar radius
        float avatarRatio = 2f/3;

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

        avatarPaint = new Paint();
        avatarPaint.setStyle(Paint.Style.FILL);
        avatarPaint.setColor(avatarColor);
        avatarPaint.setAntiAlias(true);

        counterPaint = new Paint();
        counterPaint.setStyle(Paint.Style.FILL_AND_STROKE);
//        counterPaint.setStrokeWidth(0);
        counterPaint.setColor(mCounterColor);
        counterPaint.setAntiAlias(true);

        mCounterTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCounterTextPaint.setTextSize(mCounterTextHeight);
        mCounterTextPaint.setColor(mCounterTextColor);
    }

    private float pxFromDp(float dp, Context context) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        int left = getPaddingLeft();
        int top = getPaddingTop();
        int right = getWidth() - getPaddingRight();
        int bottom = getHeight() - getPaddingBottom();

        int widthWithoutPadding = right - left;
        int heightWithoutPadding = bottom - top;

        float betweenText = (float)(heightWithoutPadding - Math.round(mTextHeight + mTitleTextHeight))/3;

        String initials = getInitials(mTitleText);
        mInitialsTextPaint.getTextBounds(initials, 0, initials.length() - 1, bounds);

        String displayedTime = localeDateFormat.format(mDate);

        // bounds just to see borders of view
//        canvas.drawRect(left, top, right, bottom, linePaint);
//        canvas.drawRect(left, top, left + avatarImageRadius * 2, bottom, linePaint);

        // drawing avatar
        canvas.drawCircle(left + avatarImageRadius,
                top + avatarImageRadius,
                avatarImageRadius,
                avatarPaint);
        canvas.drawText(initials,
                left + avatarImageRadius - mInitialsTextPaint.measureText(initials) / 2,
                top + avatarImageRadius + (bounds.bottom - bounds.top) / 2,
                mInitialsTextPaint);

        canvas.drawText(mText, left + avatarImageRadius*2, top + mTextHeight + mTitleTextHeight + betweenText * 2, mTextPaint);
        canvas.drawText(mTitleText, left + avatarImageRadius*2, top + mTitleTextHeight + betweenText, mTitleTextPaint);

        canvas.drawText(displayedTime, right - mTextPaint.measureText(displayedTime), top + mTextHeight + betweenText, mTextPaint);

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
            mTextPaint.getTextBounds(initials, 0, initials.length() - 1, bounds);

            // drawable would be as height as text
            // bounds are NOT equal to textHeight!
            int textBound = bounds.bottom - bounds.top;

            statusDrawable.setBounds((int) (right - mTextPaint.measureText(displayedTime) - textBound),
                    (int) (top + betweenText + mTextHeight - textBound),
                    (int) (right - mTextPaint.measureText(displayedTime)),
                    (int) (top + betweenText + mTextHeight)
            );

            statusDrawable.draw(canvas);
        }

        if (unread!=0) {
            String unreadString = Integer.toString(unread);
            mCounterTextPaint.getTextBounds("0", 0, 1, bounds);
            int digitBounds = bounds.bottom - bounds.top;

            float radius = (mCounterTextHeight);
            float rectLength = (unreadString.length()-1)*(bounds.right-bounds.left);

            float cx, cy;
            cx = right - radius;
            cy = top + betweenText + mTextHeight + radius;

            cy+=pxFromDp(5, getContext());

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

            canvas.drawRect(cx - rectLength,
                    cy - radius,
                    cx,
                    cy + radius,
                    counterPaint);

            canvas.drawText(unreadString,
                    cx - rectLength/2 - mCounterTextPaint.measureText(unreadString)/2,
                    cy + digitBounds/2,
                    mCounterTextPaint);
        }
    }

    public void setTitle(String s){
        mTitleText = s;
        invalidate();
    }

    public void setText(String s){
        mText = s;
        invalidate();
    }

    public void setDate(Date date){
        mDate = date;
        invalidate();
    }

    public void setAvatarColor(int color) {
        avatarColor = color;
        avatarPaint.setColor(avatarColor);
        invalidate();
    }

    /**
     * Sets number of unread messages in green circle
     *
     * @param unreadCount
     * if there is no unread messages, pass 0;
     */
    public void setUnreadCount(int unreadCount) {
        // TODO implement count indicator
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

        StringBuilder initials = new StringBuilder();
        for (String a: s.split(" ")){
            initials.append(a.charAt(0));
            if (initials.length()>=2)
                break;
        }

        return initials.toString().toUpperCase();
    }
}
