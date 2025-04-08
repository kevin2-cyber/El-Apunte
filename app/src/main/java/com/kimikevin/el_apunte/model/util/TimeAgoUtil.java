package com.kimikevin.el_apunte.model.util;

import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.util.Log;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TimeAgoUtil {

    public static String getTimeUsing24HourFormat(long timestamp) {
        Instant instant = Instant.ofEpochMilli(timestamp);
        LocalDate messageDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate today = LocalDate.now();

        if (messageDate.equals(today)) {
            return DateTimeFormatter.ofPattern("HH:mm").format(instant.atZone(ZoneId.systemDefault())); // 24-hour
        } else if (messageDate.equals(today.minusDays(1))) {
            return "Yesterday";
        } else if (messageDate.isAfter(today.minusDays(7))) {
            return DateTimeFormatter.ofPattern("EEEE").format(messageDate);
        } else {
            return DateTimeFormatter.ofPattern("MMM d, yyyy").format(messageDate);
        }
    }

    public static String getTimeUsing12HourFormat(long timestamp) {
        Instant instant = Instant.ofEpochMilli(timestamp);
        LocalDate messageDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate today = LocalDate.now();

        if (messageDate.equals(today)) {
            return DateTimeFormatter.ofPattern("h:mm a").format(instant.atZone(ZoneId.systemDefault()));
        } else if (messageDate.equals(today.minusDays(1))) {
            return "Yesterday";
        } else if (messageDate.isAfter(today.minusDays(7))) {
            return DateTimeFormatter.ofPattern("EEEE").format(messageDate);
        } else {
            return DateTimeFormatter.ofPattern("MMM d, yyyy").format(messageDate);
        }
    }
}
