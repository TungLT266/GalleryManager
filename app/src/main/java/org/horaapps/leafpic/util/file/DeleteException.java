package org.horaapps.leafpic.util.file;

import org.horaapps.leafpic.data.Media;

public class DeleteException extends Exception {

    Media media;

    public DeleteException(Media media) {
        super("Cannot delete Media");
        this.media = media;
    }

    public DeleteException() {
        this(null);
    }

    public Media getMedia() {
        return media;
    }
}