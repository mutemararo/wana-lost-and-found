package com.wanalnf.wana_lost_and_found.utils


import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Date

class TimeCalculations {

    fun calculateTimeBetweenDates(startDate: String): String{
        val endDate = timeStampToString(System.currentTimeMillis())
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm")


        val date2 = sdf.parse(endDate)
        val date1 = sdf.parse(timeStampToString(startDate.toLong()))

        var isNegative = false
        var difference = date2.time - date1.time

        if(difference < 0){
            difference = -(difference)
            isNegative = true
        }

        val minutes = difference / 60 / 1000
        val hours = difference / 60 / 1000 / 60
        val days = (difference / 60 / 1000 / 60) / 24
        val months = (difference / 60 / 1000 / 60) / 24 / (365/12)
        val years = difference / 60 / 1000/ 60 / 24 / 365

        return when{
            minutes < 240 -> "$minutes minutes ago"
            hours < 48 -> "$hours hours ago"
            days < 61 -> "$days days ago"
            months < 24 -> "$months months ago"
            else -> "$years years ago"
        }
    }

    private fun timeStampToString(timeStamp: Long): String{
        val stamp = Timestamp(timeStamp)
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm")
        val date = sdf.format((Date(stamp.time)))

        return date.toString()
    }

    fun cleanDate(_day: Int, _month: Int, _year : Int): String{
        var day = _day.toString()
        var month = _month.toString()

        if(_day < 10){
            day = "0$_day"
        }
        if(_month < 9){
            month = "0${_month + 1}"
        }

        return "$day/$month/$_year"
    }

    fun cleanTime(_hour: Int, _minutes: Int): String{
        var hour = _hour.toString()
        var minutes = _minutes.toString()

        if(_hour < 10){
            hour = "0$_hour"
        }
        if (_minutes < 10){
            minutes = "0$_minutes"
        }

        return "$hour:$minutes"
    }
}