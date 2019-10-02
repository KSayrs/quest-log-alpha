/*
* This file contains miscellaneous type converters for already existing classes. Custom classes will
* have their type converters in the same file.
* */
package com.example.questlogalpha.data

import androidx.room.TypeConverter
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class ZonedDateTimeConverter {

    // OffsetDateTime should be what is stored in the database because ordering things by date can get scrambled if you
    // include all the intricacies of ZonedDateTime. See https://stackoverflow.com/a/30234992
    private val formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME

    @TypeConverter
    fun stringToZonedDateTime(value: String): ZonedDateTime = value.let {
        return formatter.parse(value, ZonedDateTime::from)
    }

    @TypeConverter
    fun zonedDateTimeToString(date: ZonedDateTime): String? {
        return date.format(formatter)
    }
}