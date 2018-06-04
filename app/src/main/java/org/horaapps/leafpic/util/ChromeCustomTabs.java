package org.horaapps.leafpic.util;

import android.content.ComponentName;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;

import org.horaapps.liz.ThemeHelper;


public class ChromeCustomTabs {

    private CustomTabsServiceConnection serviceConnection;
    private CustomTabsIntent mCustomTabsIntent;

    @ColorInt private int toolbarColor;
    private Context context;

    public ChromeCustomTabs(@NonNull Context context) {
        this.context = context;
        toolbarColor = ThemeHelper.getInstance(context).getPrimaryColor();
        initService();
    }

    private void initService() {

        serviceConnection = new CustomTabsServiceConnection() {
            @Override
            public void onCustomTabsServiceConnected(ComponentName componentName, CustomTabsClient customTabsClient) {
                customTabsClient.warmup(0L);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };


        CustomTabsClient.bindCustomTabsService(context, ApplicationUtils.getPackageName(), serviceConnection);

        mCustomTabsIntent = new CustomTabsIntent.Builder()
                .setShowTitle(true)
                .setToolbarColor(toolbarColor)
                .build();
    }

    public void launchUrl(String Url) {
        mCustomTabsIntent.launchUrl(context, Uri.parse(Url));
    }


    public void destroy() {
        context.unbindService(serviceConnection);
    }


    public static void launchUrl(@NonNull Context context, @NonNull String url) {
        CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder().build();
        customTabsIntent.launchUrl(context, Uri.parse(url));
    }
}
