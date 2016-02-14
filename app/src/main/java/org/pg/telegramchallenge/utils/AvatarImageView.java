package org.pg.telegramchallenge.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ImageView;

import java.util.jar.Attributes;

import static org.pg.telegramchallenge.utils.Utils.dpToPx;

/**
 * Created by artemypestretsov on 2/13/16.
 */
public class AvatarImageView extends ImageView {

    private String initials;
    private Paint avatarImagePaint;
    private Paint avatarCirclePaint;
    private Paint mInitialsTextPaint;
    private int avatarImageViewColor;

    public  static final int dpAvatarRadius = 38;
    private int avatarImageRadius = dpToPx(dpAvatarRadius, getContext());

    private final Rect bounds = new Rect();

    public AvatarImageView(Context context) {
        super(context);

        init();
    }

    public AvatarImageView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        init();
    }


    public void setInitials(String initials) {
        this.initials = initials;
    }

    protected void init() {
        avatarImageViewColor = 2;
        float avatarRatio = 1;
        avatarCirclePaint = new Paint();
        avatarCirclePaint.setStyle(Paint.Style.FILL);
        avatarCirclePaint.setColor(avatarImageViewColor);
        avatarCirclePaint.setAntiAlias(true);

        avatarImagePaint = new Paint();
        avatarImagePaint.setAntiAlias(true);
        avatarImagePaint.setFilterBitmap(true);
        avatarImagePaint.setDither(true);

        mInitialsTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mInitialsTextPaint.setTextSize(avatarImageRadius * avatarRatio);
        mInitialsTextPaint.setColor(Color.WHITE);
        mInitialsTextPaint.setTextAlign(Paint.Align.CENTER);

    }

    @Override
    protected void onDraw(Canvas canvas) {

        final int left = getPaddingLeft();
        final int top = getPaddingTop();
        final int right = getWidth() - getPaddingRight();
        final int bottom = getHeight() - getPaddingBottom();

        mInitialsTextPaint.getTextBounds(initials, 0, initials.length(), bounds);

        if (getDrawable() == null) {
            canvas.drawCircle(left + avatarImageRadius,
                    (top + bottom) / 2,
                    avatarImageRadius,
                    avatarCirclePaint);
            canvas.drawText(initials,
                    left + avatarImageRadius,
                    (top + bottom) / 2 - (mInitialsTextPaint.descent() + mInitialsTextPaint.ascent())/2,
                    mInitialsTextPaint);
        } else {
            getDrawable().setBounds(left, top, left + avatarImageRadius*2, top + avatarImageRadius*2);
            getDrawable().draw(canvas);
        }
    }
}
