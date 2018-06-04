package org.horaapps.leafpic.timeline;

import android.support.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public enum GroupingMode {

    DAY {
        @Override
        public boolean isInGroup(@NonNull Calendar left, @NonNull Calendar right) {
            return WEEK.isInGroup(left, right) && isDayOfMonthSame(left, right);
        }

        @NonNull
        @Override
        public String getGroupHeader(@NonNull Calendar calendar) {
            return getFormattedDate(HEADER_PATTERN_DAY, calendar);
        }
    },


    WEEK {
        @Override
        public boolean isInGroup(@NonNull Calendar left, @NonNull Calendar right) {
            return MONTH.isInGroup(left, right) && isWeekOfMonthSame(left, right);
        }

        @NonNull
        @Override
        public String getGroupHeader(@NonNull Calendar calendar) {
            return "Week " + getFormattedDate(HEADER_PATTERN_WEEK, calendar);
        }
    },


    MONTH {
        @Override
        public boolean isInGroup(@NonNull Calendar left, @NonNull Calendar right) {
            return YEAR.isInGroup(left, right) && isMonthOfYearSame(left, right);
        }

        @NonNull
        @Override
        public String getGroupHeader(@NonNull Calendar calendar) {
            return getFormattedDate(HEADER_PATTERN_MONTH, calendar);
        }
    },


    YEAR {
        @Override
        public boolean isInGroup(@NonNull Calendar left, @NonNull Calendar right) {
            return isYearSame(left, right);
        }

        @NonNull
        @Override
        public String getGroupHeader(@NonNull Calendar calendar) {
            return getFormattedDate(HEADER_PATTERN_YEAR, calendar);
        }
    };


    private static final String HEADER_PATTERN_DAY = "E, d MMM yyyy";
    private static final String HEADER_PATTERN_WEEK = "W, MMM yyyy";
    private static final String HEADER_PATTERN_MONTH = "MMM yyyy";
    private static final String HEADER_PATTERN_YEAR = "yyyy";


    public abstract boolean isInGroup(@NonNull Calendar left, @NonNull Calendar right);


    @NonNull
    public abstract String getGroupHeader(@NonNull Calendar calendar);

    public boolean isDayOfMonthSame(@NonNull Calendar left, @NonNull Calendar right) {
        return left.get(Calendar.DAY_OF_MONTH) == right.get(Calendar.DAY_OF_MONTH);
    }

    public boolean isWeekOfMonthSame(@NonNull Calendar left, @NonNull Calendar right) {
        return left.get(Calendar.WEEK_OF_MONTH) == right.get(Calendar.WEEK_OF_MONTH);
    }

    public boolean isMonthOfYearSame(@NonNull Calendar left, @NonNull Calendar right) {
        return left.get(Calendar.MONTH) == right.get(Calendar.MONTH);
    }

    public boolean isYearSame(@NonNull Calendar left, @NonNull Calendar right) {
        return left.get(Calendar.YEAR) == right.get(Calendar.YEAR);
    }

    @NonNull
    public String getFormattedDate(@NonNull String formatter, @NonNull Calendar calendar) {
        return new SimpleDateFormat(formatter, Locale.ENGLISH).format(calendar.getTime());
    }
}
