package org.pg.telegramchallenge.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import org.pg.telegramchallenge.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static org.pg.telegramchallenge.utils.Utils.*;

/**
 * Created by roman on 09.01.16.
 */
public class BaseChatItemView extends View {

    protected int mBarHeight;
    protected int mDatePlaceholderHeight;
    protected static int VERTICAL_PADDING_DP = 6;

    protected Calendar mDate;

    Paint mBarPaint;
    TextPaint mDateTextPaint;
    Paint mBarMessageTextPaint;

    float mBarMessageTextSize;
    private final float mDateTextHeight;

    private final int mBarColor;
    private final int mBarMessageTextColor;
    private final int mDateTextColor;

    protected boolean mDateVisibility = true;
    protected boolean mBarVisibility = true;
    protected int mUnreadMessagesCount = 10; // TODO remove

    private Drawable mDownArrowDrawable = getResources().getDrawable(R.drawable.ic_small_arrow);
    private final int mArrowPadding = 5;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM", Locale.ENGLISH);
    Rect rect = new Rect();

    private final String mUnreadMessagesString;

    private boolean mIsTyping = false;
    private boolean mIsChat = false;

    private String[] mAuthors = null;

    public BaseChatItemView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray attributes = context.obtainStyledAttributes(attrs,
                R.styleable.BaseChatItemView);

        try {
            mBarColor = attributes.getColor(R.styleable.BaseChatItemView_barColor, Color.parseColor("#EBF2F7"));
            mBarMessageTextColor = attributes.getColor(R.styleable.BaseChatItemView_barMessageTextColor, Color.BLUE);
            mBarMessageTextSize = attributes.getDimension(R.styleable.BaseChatItemView_barMessageTextSize, 0f);

            mDateTextColor = attributes.getColor(R.styleable.BaseChatItemView_dateTextColor, Color.BLACK);
            mDateTextHeight = attributes.getDimension(R.styleable.BaseChatItemView_dateTextSize, 0f);
        } finally {
            attributes.recycle();
        }

        init();

        mDate = Calendar.getInstance();
        mUnreadMessagesString = context.getString(R.string.unread_messages);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        Context c = getContext();
        int holdersPadding = dpToPx(VERTICAL_PADDING_DP, c);

        int width = getMeasuredWidth();
        int widthWithoutPadding = width - getPaddingLeft() - getPaddingRight();

        int heightWithoutPadding = 0;
        boolean isBarVisible = mBarVisibility && mUnreadMessagesCount>0;

        if (isBarVisible){
            heightWithoutPadding += mBarHeight;
        }

        if (mDateVisibility) {
            if (isBarVisible)
                heightWithoutPadding += holdersPadding;

            heightWithoutPadding += mDatePlaceholderHeight;
        }

        int height = getPaddingTop() + getPaddingBottom() + heightWithoutPadding;
        setMeasuredDimension(width, height);
    }

    protected static final Typeface mBoldTypeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD);
    private void init() {
        mBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBarPaint.setColor(mBarColor);
        mBarPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        mDateTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mDateTextPaint.setTextSize(mDateTextHeight);
        mDateTextPaint.setColor(mDateTextColor);
        mDateTextPaint.setTextAlign(Paint.Align.CENTER);
        mDateTextPaint.setTypeface(mBoldTypeface);

        mBarMessageTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBarMessageTextPaint.setTextSize(mBarMessageTextSize);
        mBarMessageTextPaint.setColor(mBarMessageTextColor);
        mBarMessageTextPaint.setTextAlign(Paint.Align.CENTER);
//        mBarMessageTextPaint.setFakeBoldText(true);
        mBarMessageTextPaint.setTypeface(mBoldTypeface);

        int padding = dpToPx(VERTICAL_PADDING_DP, getContext());
        mBarHeight = (int)mBarMessageTextSize*2;
        mDatePlaceholderHeight = (int)mDateTextHeight*2;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        final int left = getPaddingLeft();
        final int top = getPaddingTop();
        final int right = getWidth() - getPaddingRight();
        final int bottom = getHeight() - getPaddingBottom();

        int holdersPadding = dpToPx(VERTICAL_PADDING_DP, getContext());

        if (mBarVisibility && mUnreadMessagesCount>0) {
            canvas.drawRect(0, top, getWidth(), top + mBarHeight, mBarPaint);
            String barMessage = String.format("%d %s", mUnreadMessagesCount, mUnreadMessagesString);
            mBarMessageTextPaint.getTextBounds(barMessage, 0, barMessage.length(), rect);
            float barMessageWidth = rect.right - rect.left;
            int barMessageHeight = rect.bottom - rect.top;

            float textStartY = (top + mBarHeight/2f - (mBarMessageTextPaint.descent() + mBarMessageTextPaint.ascent())/2);
            canvas.drawText(barMessage, (left+right)/2f, textStartY, mBarMessageTextPaint);

            int arrowDrawableStart = (int)((left+right)/2f + barMessageWidth/2f + 0.5f) + dpToPx(mArrowPadding, getContext());
            int arrowHeight = (int)(mBarMessageTextSize/2f);
            rect.set(arrowDrawableStart,
                    (int)textStartY - arrowHeight,
                    arrowDrawableStart + 2*arrowHeight,
                    (int)textStartY);
            mDownArrowDrawable.setBounds(rect);
            mDownArrowDrawable.draw(canvas);
        }

        if (mDateVisibility) {
            float textStartY = (top + (mBarVisibility && mUnreadMessagesCount>0?holdersPadding + mBarHeight:0) + mDatePlaceholderHeight/2f
                    - (mDateTextPaint.descent() + mDateTextPaint.ascent())/2);
            canvas.drawText(dateFormat.format(mDate.getTime()),
                    (left+right)/2f,
                    textStartY,
                    mDateTextPaint);
        }

    }

    public void setDateVisability(boolean isShown) {
        if (isShown == mDateVisibility)
            return;

        mDateVisibility = isShown;
        invalidate();
        requestLayout();
    }

    public void setBarVisability(boolean isShown) {
        if (isShown == mBarVisibility)
            return;

        mBarVisibility = isShown;
        requestLayout(); // not sure if it's necessary
        invalidate();
    }

    public void setUnreadMessagesCount(int count) {
        if (count<0)
            throw new IllegalArgumentException("UnreadMessagesCount cannot be negative!");

        mUnreadMessagesCount = count;

        if (mBarVisibility) {
            invalidate();
            requestLayout();
        }
    }
}
