package org.horaapps.leafpic.data;

import android.database.Cursor;

import java.sql.SQLException;

public interface CursorHandler<T> {
    T handle(Cursor cu);
    static String [] getProjection() {
        return new String[0];
    }
}
