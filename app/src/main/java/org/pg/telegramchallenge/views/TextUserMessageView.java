package org.pg.telegramchallenge.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.text.*;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.AttributeSet;

import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;
import org.pg.telegramchallenge.R;

import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Pattern;

import static org.pg.telegramchallenge.utils.Utils.*;

/**
 * Created by dodger on 25.01.16.
 */
public class TextUserMessageView extends BaseUserMessageView {

    private static final String TAG = TextUserMessageView.class.getSimpleName();
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
        int textWidth = widthWithoutPadding
                - dpToPx(dpAvatarRadius*2, c)
                - dpToPx(mTextPadding, c)*2 // *2 because same padding between status icon and text
                - clockIcon.getIntrinsicWidth();

        if (mTextLayout == null) { // i guess it would be fine for recycler view
            mTextLayout = new DynamicLayout(mText, mTextPaint, textWidth, Layout.Alignment.ALIGN_NORMAL, 1f, 0f, false);
        }

        int height = getMeasuredHeight();
        height += mTextLayout.getHeight() - (mDetailsVisibility?mTextSize:0); // textSize is for one line

        setMeasuredDimension(width, height);
    }

    private void init() {
        mTextPaint = getTextPaint(TextPaint.ANTI_ALIAS_FLAG, mTextSize, mTextColor, null, null, null);
        mTextPaint.linkColor = mLinkColor;

        mLinkForegroundSpan = new ForegroundColorSpan(mLinkColor);
        setText(mText, false);
    }

    @Override
    protected int getStatusCenterY() {
        if (mDetailsVisibility)
            return super.getStatusCenterY();

        return (int)(mTextSize + (mTextPaint.ascent() + mTextPaint.descent())/2);
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
        float textStartY = top + (mDetailsVisibility ? (avatarDiameter - mTextSize) :0);
        canvas.translate(left + avatarDiameter + dpToPx(mTextPadding, c), textStartY);
        mTextLayout.draw(canvas);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        final Context context = getContext();
        int holdersPadding = dpToPx(VERTICAL_PADDING_DP, context);
        int avatarDiameter = dpToPx(dpAvatarRadius*2, context);

        boolean isBarVisible = mBarVisibility && mUnreadMessagesCount>0;
        int top = getPaddingTop() +
                (isBarVisible ? mBarHeight + holdersPadding:0) +
                (mDateVisibility ? mDatePlaceholderHeight + holdersPadding:0);
        final int left = getPaddingLeft();

        float x = event.getX(), y = event.getY();

        float textStartY = top + (mDetailsVisibility ? (avatarDiameter - mTextSize) :0);
        int textStartX = left + avatarDiameter + dpToPx(mTextPadding, context);
        boolean hitText = y - textStartY >0 && y - textStartY <mTextLayout.getHeight();
        hitText &= x - textStartX>0 && x-textStartX < mTextLayout.getWidth();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (hitText) {
                    Log.d(TAG, mText.toString());
//                    Toast.makeText(context, mTextLayout.getText(), Toast.LENGTH_SHORT).show();

                    int lineNum = mTextLayout.getLineForVertical(((int) (y - textStartY)));
                    int clickedTextOff = mTextLayout.getOffsetForHorizontal(lineNum, x - textStartX);

                    ClickableSpan[] spans = mText.getSpans(clickedTextOff, clickedTextOff, ClickableSpan.class);
                    if (spans.length>0) {
                        spans[0].onClick(this);
                        return true;
                    }
                }
                break;
        }

        return super.onTouchEvent(event);
    }

    public void setText(CharSequence s) {
        setText(s, true);
    }

    private void setText(CharSequence s, boolean invalidate) {

        // this is so fucking lame
        Object[] spans = mText.getSpans(0, mText.length(), Object.class);
        for (Object o: spans) {
            if (!(o instanceof SpanWatcher))
                mText.removeSpan(o);
        }

        if (s!=mText) {
            mText.replace(0, mText.length(), s);
        }

        Linkify.addLinks(mText, Linkify.WEB_URLS|Linkify.PHONE_NUMBERS);

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
