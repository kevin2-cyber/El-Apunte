package com.kimikevin.elapunte.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public final class Utils {

    public static final String PREF_KEY = "pref";
    public static final String BOTTOM_SHEET_TAG = "ModalBottomSheet";
    public static final String THEME_KEY = "theme_key";

    private static final DateTimeFormatter TIME_OF_DAY = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DAY_OF_WEEK = DateTimeFormatter.ofPattern("EEEE");
    private static final DateTimeFormatter SHORT_DATE  = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//    private static final DateTimeFormatter BACKEND     = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH-mm");

    private Utils() {}

    /**
     * WhatsApp-style chat list timestamp.
     *   today      -> "HH:mm"
     *   yesterday  -> "Yesterday"
     *   last week  -> day of week (e.g. "Monday")
     *   older      -> "dd/MM/yyyy"
     */
    public static String formatChatTimestamp(long timestamp) {
        if (timestamp <= 0L) return "";

        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime when = Instant.ofEpochMilli(timestamp).atZone(zone);
        LocalDate messageDate = when.toLocalDate();
        LocalDate today = LocalDate.now(zone);

        if (messageDate.equals(today)) {
            return TIME_OF_DAY.format(when);
        }
        if (messageDate.equals(today.minusDays(1))) {
            return "Yesterday";
        }
        if (messageDate.isAfter(today.minusDays(7))) {
            return DAY_OF_WEEK.format(messageDate);
        }
        return SHORT_DATE.format(messageDate);
    }

//    public static String getBackendDate(long timestamp) {
//        return BACKEND.format(Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()));
//    }
}
