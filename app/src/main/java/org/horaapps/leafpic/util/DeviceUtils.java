package org.horaapps.leafpic.util;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.annotation.NonNull;


public class DeviceUtils {


    public static boolean isLandscape(@NonNull Resources resources) {
        return resources.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }


    public static boolean isPortrait(@NonNull Resources resources) {
        return resources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }
}
