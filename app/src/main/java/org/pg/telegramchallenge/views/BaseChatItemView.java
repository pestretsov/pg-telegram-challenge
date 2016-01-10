package org.pg.telegramchallenge.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import org.pg.telegramchallenge.R;

import java.util.Date;

/**
 * Created by roman on 09.01.16.
 */
public class BaseChatItemView extends View {

    Date mDate;

    Paint mBarPaint;
    TextPaint mDateTextPaint;
    TextPaint mBarMessageTextPaint;

    float mBarMessageTextSize;
    private final float mDateTextHeight;

    private final int mBarColor;
    private final int mBarMessageTextColor;
    private final int mDateTextColor;

    public BaseChatItemView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray attributes = context.obtainStyledAttributes(attrs,
                R.styleable.ChatListItemView);

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
    }

    private void init(){
        mBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBarPaint.setColor(mBarColor);
        mBarPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        mDateTextPaint = new TextPaint();
        mDateTextPaint.setTextSize(mBarMessageTextSize);
        mDateTextPaint.setColor(mBarMessageTextColor);

        mBarMessageTextPaint = new TextPaint();
        mBarMessageTextPaint.setTextSize(mBarMessageTextSize);
        mBarMessageTextPaint.setColor(mBarMessageTextColor);
    }
}
