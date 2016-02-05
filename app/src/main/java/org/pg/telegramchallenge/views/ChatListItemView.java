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

import static org.pg.telegramchallenge.utils.Utils.*;

public class ChatListItemView extends View {

    private static final String TAG = ChatListItemView.class.getSimpleName();
    private static String SINGLE_TYPING_MESSAGE ,CHAT_SINGLE_TYPING_MESSAGE, CHAT_FEW_TYPING_MESSAGE;

    private Paint mCounterTextPaint;
    private boolean mIsGroupChat = false;
    private boolean mIsTyping = false;
    private String[] mAuthors = null;

    public ChatStatus getStatus() {
        return mStatus;
    }

    public enum ChatStatus {
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
    private float titleTextLength;

    private float[] mTitleTextWidths;
    private Calendar mDate;

    private int unread = 0;
    private ChatStatus mStatus = ChatStatus.READ;
    private String avatarImageFilePath = null;
    ViewTarget<ChatListItemView, Bitmap> glideTarget;

    private static Drawable clockIcon, bageIcon, groupIcon;
    private Drawable avatarDrawable = null;
    private final int bageColor = ContextCompat.getColor(getContext(), R.color.accent_telegram_blue);
    private int mCounterColor;

    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy", Locale.ENGLISH);
    private final Rect bounds = new Rect();
    private final StringBuilder mBuilder = new StringBuilder();

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

        if (SINGLE_TYPING_MESSAGE==null || CHAT_SINGLE_TYPING_MESSAGE == null || CHAT_FEW_TYPING_MESSAGE==null) {
            SINGLE_TYPING_MESSAGE = context.getString(R.string.single_typing_message);
            CHAT_SINGLE_TYPING_MESSAGE = context.getString(R.string.chat_single_typing_message);
            CHAT_FEW_TYPING_MESSAGE = context.getString(R.string.chat_few_typing_message);
        }
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

    protected void init(){
        // size of avatar letters / avatar radius
        float avatarRatio = 1;

        if (clockIcon == null) {
            clockIcon = getResources().getDrawable(R.drawable.ic_clock);
        }

        if (bageIcon == null) {
            bageIcon = getResources().getDrawable(R.drawable.ic_badge);
            bageIcon.setColorFilter(bageColor, PorterDuff.Mode.SRC_ATOP);
        }

        if (groupIcon == null) {
            groupIcon = getResources().getDrawable(R.drawable.ic_group);
        }

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
            case READ:
                statusDrawable = null;
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

        String adjustedString, unadjustedString;
        float maxTextLength = textEndX - textStartX;

        if (mIsTyping) {
            mTextPaint.setColor(bageColor);
            if (mIsGroupChat){
                mBuilder.setLength(0);
                for (String s : mAuthors){
                    if (mBuilder.length()!=0) {
                        mBuilder.append(',');
                        mBuilder.append(' ');
                    }
                    mBuilder.append(s);
                }
                mBuilder.append(' ');
                mBuilder.append(mAuthors.length>1 ? CHAT_FEW_TYPING_MESSAGE:CHAT_SINGLE_TYPING_MESSAGE);

                unadjustedString = mBuilder.toString();
            } else {
                unadjustedString = SINGLE_TYPING_MESSAGE;
            }
        } else {
            mTextPaint.setColor(mTextColor);
            unadjustedString = mText;
        }

        adjustedString = adjustString(unadjustedString, maxTextLength, mTextPaint);
        canvas.drawText(adjustedString, textStartX, bottom, mTextPaint);

        // constant dp for now; mb should change that
        if (mIsGroupChat) {
            groupIcon.setBounds((int)textStartX,
                    (int)(top + mTitleTextHeight - groupIcon.getIntrinsicHeight()),
                    (int)(textStartX+groupIcon.getIntrinsicWidth()),
                    (int)(top + mTitleTextHeight));
            groupIcon.draw(canvas);
        }

        float maxTitleTextLength = - (textStartX + (mIsGroupChat?mTitleTextHeight:0)) + (right - dpToPx(85, context));
        String adjustedTitle = adjustString(mTitleText, maxTitleTextLength, mTitleTextPaint);
        canvas.drawText(adjustedTitle, textStartX + (mIsGroupChat?mTitleTextHeight:0), top + mTitleTextHeight + betweenText, mTitleTextPaint);

    }

    public void setTitle(String s){
        setTitle(s, true);
    }

    private void setTitle(String s, boolean invalidate) {
        mTitleText = s;

        if (invalidate)
            invalidate();
    }

    public void setText(String s){
        setText(s, true);
    }

    private void setText(String s, boolean invalidate){
        mText = s;

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

    public void setIsGroupChat(boolean isChat){
        if(mIsGroupChat == isChat)
            return;

        mIsGroupChat = isChat;

        if (!mIsGroupChat) {
            mAuthors = null;
        }

        invalidate();
        requestLayout();
    }

    /**
     * Sets view to display that user is typing.
     *
     * @param isTyping
     * @param authors   array of persons which are typing
     *
     * @throws IllegalArgumentException if authors are not set but it's chat and it's supposed to be in typing mode
     */

    public void setTyping(boolean isTyping, @Nullable String... authors) {
        if (mIsTyping == isTyping)
            return;

        mIsTyping = isTyping;

        if (mIsTyping && mIsGroupChat && authors == null)
            throw new IllegalArgumentException("It's chat, but authors are not specified!");

        mAuthors = authors;

        invalidate();
        requestLayout();
    }
}