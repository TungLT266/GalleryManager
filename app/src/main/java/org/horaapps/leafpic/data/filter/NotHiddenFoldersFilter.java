package org.horaapps.leafpic.data.filter;

import java.io.File;
import java.io.FileFilter;

public class NotHiddenFoldersFilter implements FileFilter {
    @Override
    public boolean accept(File file) {
        return file.isDirectory() && !file.isHidden() && !new File(file, ".nomedia").exists();
    }
}