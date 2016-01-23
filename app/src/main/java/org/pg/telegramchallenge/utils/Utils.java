package org.pg.telegramchallenge.utils;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.text.TextPaint;

/**
 * Created by roman on 05.01.16.
 */
public class Utils {

    public static int dpToPx(float dp, Context context) {
        return (int)(dp * context.getResources().getDisplayMetrics().density+0.5f);
    }

    /**
     *
     * @param primary
     * @param another
     * @param <T>
     * @return primary if not null, otherwise another; another cannot be null!
     */
    public static<T> T getOrElse(T primary, T another){
        if (another == null) {
            throw new NullPointerException( "You cannot pass null as another object!");
        }

        return (primary == null)?another:primary;
    }


    /**
     * Interface to pass some computations or calls
     * that need to be done only if necessary
     * @param <T>
     */
    public interface Computable <T>{
        T compute();
    }

    public static<T> T getOrCompute(T primary, Computable<? extends T> another) {
        if (another == null) {
            throw new NullPointerException( "You cannot pass null as computable object!");
        }

        if (primary==null){
            T anotherT = another.compute();
            if (anotherT == null)
                throw new NullPointerException( "compute method returned null!");

            return anotherT;
        }

        return primary;
    }

    public static long timestampToMillis(long timestamp) {
        return timestamp * 1000L;
    }

    public static TextPaint getTextPaint(int flags, float size, int color,
                                   @Nullable TextPaint.Align align,
                                   @Nullable TextPaint.Style style,
                                   @Nullable Typeface typeface){
        TextPaint result = new TextPaint(flags);

        result.setTextSize(size);
        result.setColor(color);

        if (style != null) {
            result.setStyle(style);
        }

        if (typeface!=null){
            result.setTypeface(typeface);
        }

        if (align!=null) {
            result.setTextAlign(align);
        }

        return result;
    }

    public static String getInitials(String s){
        if (s == null)
            return "";

        StringBuilder initials = new StringBuilder();
        for (String a: s.split(" ")){
            if (!a.isEmpty())
                initials.append(a.charAt(0));
            if (initials.length()>=2)
                break;
        }

        return initials.toString().toUpperCase();
    }

    public static String adjustString(String original, float totalWidth, float[] widths, float widthLimit, Paint paint){

        if (totalWidth > widthLimit) {
            int pos = 0;
            String dots = "...";
            float dotsLenght = paint.measureText(dots);
            float adjustedTotalLength = 0;

            while (adjustedTotalLength + widths[pos] < (widthLimit - dotsLenght)) {
                adjustedTotalLength += widths[pos];
                pos++;
            }

            return original.substring(0, pos).concat(dots);
        }

        return original;
    }
}
