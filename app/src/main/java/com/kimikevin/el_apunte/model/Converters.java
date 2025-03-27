package com.kimikevin.el_apunte.model;

import androidx.room.TypeConverter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class Converters {
    @TypeConverter
    public static LocalDateTime fromTimeStamp(Long value) {
        return value == null ? null : Instant.ofEpochMilli(value).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    @TypeConverter
    public static Long dateToTimeStamp(LocalDateTime date) {
        return date == null ? null : date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}