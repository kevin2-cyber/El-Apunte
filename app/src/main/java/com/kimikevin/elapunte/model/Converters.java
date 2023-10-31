package com.kimikevin.elapunte.model;

import androidx.room.TypeConverter;
import java.time.LocalDateTime;

public class Converters {
    @TypeConverter
    public LocalDateTime fromTimeStamp(String value) {
        return LocalDateTime.parse(value);
    }
//    fun fromTimeStamp(value: String?): LocalDateTime? {
//        return value?.let {
//            LocalDateTime.parse(it)
//        }
//    }

    @TypeConverter
    public String dateToTimeStamp(LocalDateTime date) {
        return date.toString();
    }
//    fun dateToTimeStamp(date: LocalDateTime?): String? {
//        return date?.toString()
//    }
}