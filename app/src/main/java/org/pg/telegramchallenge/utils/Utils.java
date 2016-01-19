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
}
