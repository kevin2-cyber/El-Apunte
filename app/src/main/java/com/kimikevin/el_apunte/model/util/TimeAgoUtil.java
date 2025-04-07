package com.kimikevin.el_apunte.model.util;


import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;

import java.util.Locale;

public class TimeAgoUtil {

    public static String getTimeAgo(long timeInMillis) {
        long now = System.currentTimeMillis();
        long diff = now - timeInMillis;

        Calendar noteCal = Calendar.getInstance();
        noteCal.setTimeInMillis(timeInMillis);

        Calendar nowCal = Calendar.getInstance();

        // "Just now"
        if (diff < 60 * 1000) {
            return "just now";
        }

        // "x minutes ago"
        if (diff < 60 * 60 * 1000) {
            long minutes = diff / (60 * 1000);
            return minutes + " minutes ago";
        }

        // "hh:mm a" (same day)
        if (isSameDay(noteCal, nowCal)) {
            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
            return sdf.format(noteCal.getTime());
        }

        // "Yesterday"
        nowCal.add(Calendar.DATE, -1);
        if (isSameDay(noteCal, nowCal)) {
            return "yesterday";
        }

        // Within the same week: return day of week
        Calendar weekAgo = Calendar.getInstance();
        weekAgo.add(Calendar.DATE, -7);
        if (noteCal.after(weekAgo)) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE", Locale.getDefault()); // Monday, etc.
            return sdf.format(noteCal.getTime());
        }

        // Older: return "MMM d, yyyy"
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        return sdf.format(noteCal.getTime());
    }

    private static boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
}
