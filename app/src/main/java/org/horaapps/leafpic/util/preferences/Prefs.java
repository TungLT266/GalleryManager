package org.horaapps.leafpic.util.preferences;

import android.content.Context;
import android.support.annotation.NonNull;

import org.horaapps.leafpic.CardViewStyle;
import org.horaapps.leafpic.data.sort.SortingMode;
import org.horaapps.leafpic.data.sort.SortingOrder;


public class Prefs {

    private static SharedPrefs sharedPrefs;


    public static void init(@NonNull Context context) {
        if (sharedPrefs != null) {
            throw new RuntimeException("Prefs has already been instantiated");
        }
        sharedPrefs = new SharedPrefs(context);
    }


    public static int getFolderColumnsPortrait() {
        return getPrefs().get(Keys.FOLDER_COLUMNS_PORTRAIT, Defaults.FOLDER_COLUMNS_PORTRAIT);
    }


    public static int getFolderColumnsLandscape() {
        return getPrefs().get(Keys.FOLDER_COLUMNS_LANDSCAPE, Defaults.FOLDER_COLUMNS_LANDSCAPE);
    }


    public static int getMediaColumnsPortrait() {
        return getPrefs().get(Keys.MEDIA_COLUMNS_PORTRAIT, Defaults.MEDIA_COLUMNS_PORTRAIT);
    }


    public static int getMediaColumnsLandscape() {
        return getPrefs().get(Keys.MEDIA_COLUMNS_LANDSCAPE, Defaults.MEDIA_COLUMNS_LANDSCAPE);
    }


    @NonNull
    public static SortingMode getAlbumSortingMode() {
        return SortingMode.fromValue(
                getPrefs().get(Keys.ALBUM_SORTING_MODE, Defaults.ALBUM_SORTING_MODE));
    }


    @NonNull
    public static SortingOrder getAlbumSortingOrder() {
        return SortingOrder.fromValue(
                getPrefs().get(Keys.ALBUM_SORTING_ORDER, Defaults.ALBUM_SORTING_ORDER));
    }


    public static boolean showVideos() {
        return getPrefs().get(Keys.SHOW_VIDEOS, Defaults.SHOW_VIDEOS);
    }


    public static boolean showMediaCount() {
        return getPrefs().get(Keys.SHOW_MEDIA_COUNT, Defaults.SHOW_MEDIA_COUNT);
    }


    public static boolean showAlbumPath() {
        return getPrefs().get(Keys.SHOW_ALBUM_PATH, Defaults.SHOW_ALBUM_PATH);
    }


    public static boolean showEasterEgg() {
        return getPrefs().get(Keys.SHOW_EASTER_EGG, Defaults.SHOW_EASTER_EGG);
    }


    public static boolean animationsEnabled() {
        return !getPrefs().get(Keys.ANIMATIONS_DISABLED, Defaults.ANIMATIONS_DISABLED);
    }


    public static boolean timelineEnabled() {
        return getPrefs().get(Keys.TIMELINE_ENABLED, Defaults.TIMELINE_ENABLED);
    }


    @NonNull
    public static CardViewStyle getCardStyle() {
        return CardViewStyle.fromValue(
                getPrefs().get(Keys.CARD_STYLE, Defaults.CARD_STYLE));
    }

    public static int getLastVersionCode() {
        return getPrefs().get(Keys.LAST_VERSION_CODE, Defaults.LAST_VERSION_CODE);
    }


    public static void setFolderColumnsPortrait(int value) {
        getPrefs().put(Keys.FOLDER_COLUMNS_PORTRAIT, value);
    }


    public static void setFolderColumnsLandscape(int value) {
        getPrefs().put(Keys.FOLDER_COLUMNS_LANDSCAPE, value);
    }


    public static void setMediaColumnsPortrait(int value) {
        getPrefs().put(Keys.MEDIA_COLUMNS_PORTRAIT, value);
    }


    public static void setMediaColumnsLandscape(int value) {
        getPrefs().put(Keys.MEDIA_COLUMNS_LANDSCAPE, value);
    }


    public static void setAlbumSortingMode(@NonNull SortingMode sortingMode) {
        getPrefs().put(Keys.ALBUM_SORTING_MODE, sortingMode.getValue());
    }


    public static void setAlbumSortingOrder(@NonNull SortingOrder sortingOrder) {
        getPrefs().put(Keys.ALBUM_SORTING_ORDER, sortingOrder.getValue());
    }


    public static void setShowVideos(boolean value) {
        getPrefs().put(Keys.SHOW_VIDEOS, value);
    }


    public static void setShowMediaCount(boolean value) {
        getPrefs().put(Keys.SHOW_MEDIA_COUNT, value);
    }


    public static void setShowAlbumPath(boolean value) {
        getPrefs().put(Keys.SHOW_ALBUM_PATH, value);
    }


    public static void setCardStyle(@NonNull CardViewStyle cardStyle) {
        getPrefs().put(Keys.CARD_STYLE, cardStyle.getValue());
    }


    public static void setLastVersionCode(int value) {
        getPrefs().put(Keys.LAST_VERSION_CODE, value);
    }


    public static void setShowEasterEgg(boolean value) {
        getPrefs().put(Keys.SHOW_EASTER_EGG, value);
    }



    @Deprecated
    public static void setToggleValue(@NonNull String key, boolean value) {
        getPrefs().put(key, value);
    }

    @Deprecated
    public static boolean getToggleValue(@NonNull String key, boolean defaultValue) {
        return getPrefs().get(key, defaultValue);
    }



    @NonNull
    private static SharedPrefs getPrefs() {
        if (sharedPrefs == null) {
            throw new RuntimeException("Prefs has not been instantiated. Call init() with context");
        }
        return sharedPrefs;
    }
}
