package com.guttracker.app.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a")
private val DAY_FORMATTER = DateTimeFormatter.ofPattern("EEEE, MMMM d")

fun isoToMillis(iso: String?): Long? = iso?.let { Instant.parse(it).toEpochMilli() }

fun millisToIso(millis: Long): String = Instant.ofEpochMilli(millis).toString()

fun todayDateString(): String = LocalDate.now().toString() // ISO "yyyy-MM-dd"

fun millisToTimeOfDay(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(TIME_FORMATTER)

fun dateStringToDisplay(date: String): String =
    LocalDate.parse(date).format(DAY_FORMATTER)
