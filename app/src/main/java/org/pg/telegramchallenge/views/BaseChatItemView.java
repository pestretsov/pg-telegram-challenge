package org.pg.telegramchallenge.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import org.pg.telegramchallenge.R;
import org.pg.telegramchallenge.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by roman on 09.01.16.
 */
public class BaseChatItemView extends View {

    public static int mBarHeightDP = 20;
    Calendar mDate;

    Paint mBarPaint;
    TextPaint mDateTextPaint;
    Paint mBarMessageTextPaint;

    float mBarMessageTextSize;
    private final float mDateTextHeight;

    private final int mBarColor;
    private final int mBarMessageTextColor;
    private final int mDateTextColor;

    private boolean mDateVisibility = true;
    private boolean mBarVisibility = true;
    private int mUnreadMessagesCount = 10; // TODO remove

    private Drawable mDownArrowDrawable = getResources().getDrawable(R.drawable.ic_small_arrow);
    private final int mArrowPadding = 5;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM", Locale.ENGLISH);
    Rect rect = new Rect();

    private final String mUnreadMessagesString;

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

        int width = getMeasuredWidth();
        int widthWithoutPadding = width - getPaddingLeft() - getPaddingRight();

        int heightWithoutPadding = 0;
        if (mDateVisibility) {
            heightWithoutPadding += Utils.dpToPx(20,c);
        }

        if (mBarVisibility)
            heightWithoutPadding += Utils.dpToPx(mBarHeightDP, c);

        int height = getPaddingTop() + getPaddingBottom() + heightWithoutPadding;
        setMeasuredDimension(width, height);
    }

    protected void init(){
        Typeface boldTypeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD);

        mBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBarPaint.setColor(mBarColor);
        mBarPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        mDateTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mDateTextPaint.setTextSize(mDateTextHeight);
        mDateTextPaint.setColor(mDateTextColor);
        mDateTextPaint.setTextAlign(Paint.Align.CENTER);
        mDateTextPaint.setTypeface(boldTypeface);

        mBarMessageTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBarMessageTextPaint.setTextSize(mBarMessageTextSize);
        mBarMessageTextPaint.setColor(mBarMessageTextColor);
        mBarMessageTextPaint.setTextAlign(Paint.Align.CENTER);
//        mBarMessageTextPaint.setFakeBoldText(true);
        mBarMessageTextPaint.setTypeface(boldTypeface);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        final int left = getPaddingLeft();
        final int top = getPaddingTop();
        final int right = getWidth() - getPaddingRight();
        final int bottom = getHeight() - getPaddingBottom();

        if (mBarVisibility && mUnreadMessagesCount>0) {
            canvas.drawRect(0, top, getWidth(), top + Utils.dpToPx(mBarHeightDP, getContext()), mBarPaint);
            String barMessage = String.format("%d %s", mUnreadMessagesCount, mUnreadMessagesString);
            mBarMessageTextPaint.getTextBounds(barMessage, 0, barMessage.length(), rect);
            float barMessageWidth = rect.right - rect.left;
            canvas.drawText(barMessage, (left+right)/2f, top + mBarMessageTextSize, mBarMessageTextPaint);

            int arrowDrawableStart = (int)((left+right)/2f + barMessageWidth/2f + 0.5f) + Utils.dpToPx(mArrowPadding, getContext());
            int arrowHeight = (int)(mBarMessageTextSize/2f);
            rect.set(arrowDrawableStart,
                    top + (int)mBarMessageTextSize - arrowHeight,
                    arrowDrawableStart + 2*arrowHeight,
                    top + (int)mBarMessageTextSize);
            mDownArrowDrawable.setBounds(rect);
            mDownArrowDrawable.draw(canvas);
        }

        if (mDateVisibility) {
            canvas.drawText(dateFormat.format(mDate.getTime()),
                    (left+right)/2f,
                    0 + Utils.dpToPx(mBarHeightDP, getContext()) + mDateTextHeight,
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
