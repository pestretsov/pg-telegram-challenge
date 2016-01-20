package org.pg.telegramchallenge.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.graphics.drawable.Drawable;
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

    private int mBarHeight;
    private int mDatePlaceholderHeight;
    private static int barPaddingDp = 6;

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
            heightWithoutPadding += mDatePlaceholderHeight;
        }

        if (mBarVisibility)
            heightWithoutPadding += mBarHeight;

        int height = getPaddingTop() + getPaddingBottom() + heightWithoutPadding;
        setMeasuredDimension(width, height);
    }

    private static final Typeface boldTypeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD);
    protected void init(){

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

        int padding = dpToPx(barPaddingDp, getContext());
        mBarHeight = (int)mBarMessageTextSize + 2*padding;
        mDatePlaceholderHeight = (int)mDateTextHeight + 2*padding;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        final int left = getPaddingLeft();
        final int top = getPaddingTop();
        final int right = getWidth() - getPaddingRight();
        final int bottom = getHeight() - getPaddingBottom();

        int barPadding = dpToPx(barPaddingDp, getContext());

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

            float textStartY = (top + mBarHeight + mDatePlaceholderHeight/2f
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
