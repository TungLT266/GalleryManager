package org.horaapps.leafpic.timeline.data;

import android.support.annotation.IntDef;

public interface TimelineItem {

    int TYPE_HEADER = 101;
    int TYPE_MEDIA = 102;

    @IntDef({TYPE_HEADER, TYPE_MEDIA})
    @interface TimelineItemType {}

    @TimelineItemType
    int getTimelineType();
}
