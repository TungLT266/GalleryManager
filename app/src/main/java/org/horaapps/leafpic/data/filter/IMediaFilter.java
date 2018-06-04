package org.horaapps.leafpic.data.filter;

import org.horaapps.leafpic.data.Media;

public interface IMediaFilter {
    boolean accept(Media media);
}
