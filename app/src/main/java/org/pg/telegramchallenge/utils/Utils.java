package org.pg.telegramchallenge.utils;

import android.content.Context;
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
}
