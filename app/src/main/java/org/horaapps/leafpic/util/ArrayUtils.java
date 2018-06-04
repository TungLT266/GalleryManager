package org.horaapps.leafpic.util;

import android.support.annotation.NonNull;


public final class ArrayUtils {


    public static <T> int getIndex(@NonNull T[] array, @NonNull T element) {
        for (int pos = 0; pos < array.length; pos++) {
            if (array[pos].equals(element)) return pos;
        }
        return -1;
    }
}
