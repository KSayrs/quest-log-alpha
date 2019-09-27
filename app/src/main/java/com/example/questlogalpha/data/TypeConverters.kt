package com.example.questlogalpha.data

import androidx.room.TypeConverter
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class ZonedDateTimeConverter {

    // OffsetDateTime should be what is stored in the database because ordering things by date can get scrambled if you
    // include all the intricacies of ZonedDateTime. See https://stackoverflow.com/a/30234992
    private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    @TypeConverter
    fun stringToZonedDateTime(value: String): ZonedDateTime = value.let {
        val offsetDateTime = formatter.parse(value, OffsetDateTime::from)
        return offsetDateTime.toZonedDateTime()
    }

    @TypeConverter
    fun zonedDateTimeToString(date: ZonedDateTime): String? {
        val offsetDateTime = date.toOffsetDateTime()
        return offsetDateTime.format(formatter)
    }
}