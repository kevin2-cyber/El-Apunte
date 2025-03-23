package com.kimikevin.el_apunte.model.util;

import android.text.format.DateUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class TimeAgoUtil {
    public static String getTimeAgo(LocalDateTime dateTime) {
        long timestamp = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return DateUtils.getRelativeTimeSpanString(
                timestamp,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
        ).toString();
    }
}
