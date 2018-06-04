package org.horaapps.leafpic.settings;

import org.horaapps.liz.ThemedActivity;

class ThemedSetting {

    private ThemedActivity activity;

    ThemedSetting(ThemedActivity activity) {
        this.activity = activity;
    }

    public ThemedSetting() {
    }

    public ThemedActivity getActivity() {
        return activity;
    }

}
