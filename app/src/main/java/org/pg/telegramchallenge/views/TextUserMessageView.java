package org.pg.telegramchallenge.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.text.*;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;

import org.pg.telegramchallenge.R;

import java.util.ArrayList;
import java.util.regex.Pattern;

import static org.pg.telegramchallenge.utils.Utils.*;

/**
 * Created by dodger on 25.01.16.
 */
public class TextUserMessageView extends BaseUserMessageView {

    private SpannableStringBuilder mText = new SpannableStringBuilder();

    private TextPaint mTextPaint;
    private float mTextSize;
    private int mTextColor;

    private int mLinkColor;
    private ForegroundColorSpan mLinkForegroundSpan;

    private static final Pattern mMentionPattern = Pattern.compile("\\B\\@([\\w\\-]+)", Pattern.CASE_INSENSITIVE|Pattern.MULTILINE);
    private static final Pattern mHashtagPattern = Pattern.compile("\\B\\#([\\w\\-]+)", Pattern.CASE_INSENSITIVE|Pattern.MULTILINE);

    private Layout mTextLayout;
    private ArrayList<SpanDescriptor> mMentionDescriptors;
    private ArrayList<SpanDescriptor> mHashtagDescriptors;

    private static final ArrayList<ForegroundColorSpan> mMentionForegroundSpans = new ArrayList<>();
    private static final ArrayList<ForegroundColorSpan> mHashtagForegroundSpans = new ArrayList();

    public TextUserMessageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.TextUserMessageView);

        try {
            mTextSize = attributes.getDimension(R.styleable.TextUserMessageView_android_textSize, 0f);
            mTextColor = attributes.getColor(R.styleable.TextUserMessageView_android_textColor, Color.BLACK);
            mText = new SpannableStringBuilder(getOrElse(attributes.getString(R.styleable.TextUserMessageView_android_text),""));

            mLinkColor = attributes.getColor(R.styleable.TextUserMessageView_linkColor, Color.BLUE);
        } finally {
            attributes.recycle();
        }

        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        Context c = getContext();
        int width = getMeasuredWidth();

        int widthWithoutPadding = width - getPaddingRight() - getPaddingLeft();
        int textWidth = widthWithoutPadding - dpToPx(dpAvatarRadius*2, c) - dpToPx(mTextPadding, c);

        if (mTextLayout == null) { // i guess it would be fine for recycler view
            mTextLayout = new DynamicLayout(mText, mTextPaint, textWidth, Layout.Alignment.ALIGN_NORMAL, 1f, 0f, false);
        }

        int height = getMeasuredHeight();
        height += mTextLayout.getHeight() - mTextSize; // textSize is for one line

        setMeasuredDimension(width, height);
    }

    private void init() {
        mTextPaint = getTextPaint(TextPaint.ANTI_ALIAS_FLAG, mTextSize, mTextColor, null, null, null);

        mLinkForegroundSpan = new ForegroundColorSpan(mLinkColor);
        setText(mText, false);
    }

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

        canvas.save();
        int avatarDiameter = dpToPx(dpAvatarRadius*2, c);
        canvas.translate(left + avatarDiameter + dpToPx(mTextPadding, c), top + avatarDiameter - mTextSize);
        mTextLayout.draw(canvas);
        canvas.restore();
    }

    public void setText(CharSequence s) {
        setText(s, false);
    }

    private void setText(CharSequence s, boolean invalidate) {
        mText.clearSpans();
        if (s!=mText) {
            mText.clear();
            mText.append(s);
        }

        mMentionDescriptors = getSpans(mText, mMentionPattern);
        mHashtagDescriptors = getSpans(mText, mHashtagPattern);

        while (mMentionForegroundSpans.size()<mMentionDescriptors.size()) {
            mMentionForegroundSpans.add(new ForegroundColorSpan(mLinkColor));
        }

        while (mHashtagForegroundSpans.size()<mHashtagDescriptors.size()){
            mHashtagForegroundSpans.add(new ForegroundColorSpan(mLinkColor));
        }

        for (int i = 0; i< mMentionDescriptors.size(); i++) {
            mText.setSpan(mMentionForegroundSpans.get(i),
                    mMentionDescriptors.get(i).getStart(), mMentionDescriptors.get(i).getEnd(),
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }

        for (int i = 0; i< mHashtagDescriptors.size(); i++) {
            mText.setSpan(mHashtagForegroundSpans.get(i),
                    mHashtagDescriptors.get(i).getStart(), mHashtagDescriptors.get(i).getEnd(),
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }

        if (!invalidate)
            return;

        invalidate();
        requestLayout();
    }
}
