package org.horaapps.leafpic.fragments;

import android.view.View;

import javax.annotation.Nullable;

public interface EditModeListener {
    void changedEditMode(boolean editMode, int selected, int total, @Nullable View.OnClickListener listener, @Nullable String title);


    void onItemsSelected(int count, int total);
}
