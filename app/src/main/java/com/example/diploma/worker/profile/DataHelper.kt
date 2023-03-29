package com.example.diploma.worker.profile

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

class DataHelper(context: Context) {
    private var sharedPrefWork : SharedPreferences = context.getSharedPreferences(PREFERENCES_WORK, Context.MODE_PRIVATE)
    private var dateFormat = SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.getDefault())

    private var timerCountingWork = false
    private var startTimeWork: Date? = null
    private var stopTimeWork: Date? = null

    fun startTimeWork(): Date? = startTimeWork
    fun stopTimeWork(): Date? = stopTimeWork
    fun timerCountingWork(): Boolean = timerCountingWork

    init {
        timerCountingWork = sharedPrefWork.getBoolean(COUNTING_KEY_WORK, false)
        startTimeWork = initStartString(startTimeWork, sharedPrefWork, START_TIME_KEY_WORK)
        stopTimeWork = initStopString(stopTimeWork, sharedPrefWork, STOP_TIME_KEY_WORK)
    }

    private fun initStartString(date: Date?, sharedPreferences: SharedPreferences, keyString: String) : Date?{
        var startTime: Date? = date
        val startString = sharedPreferences.getString(keyString, null)
        if (startString != null)
            startTime = dateFormat.parse(startString)
        return startTime
    }

    private fun initStopString(date: Date?, sharedPreferences: SharedPreferences, keyString: String) : Date?{
        var stopTime: Date? = date
        val stopString = sharedPreferences.getString(keyString, null)
        if (stopString != null)
            stopTime = dateFormat.parse(stopString)
        return stopTime
    }

    fun setStartTimeWork(date: Date?) {
        startTimeWork = setStartTime(date, sharedPrefWork, START_TIME_KEY_WORK)
    }

    private fun setStartTime(date: Date?, sharedPreferences: SharedPreferences, keyString: String) : Date?{
        with(sharedPreferences.edit())
        {
            val stringDate = if (date == null) null else dateFormat.format(date)
            putString(keyString,stringDate)
            apply()
        }
        return date
    }

    fun setStopTimeWork(date: Date?) {
        stopTimeWork = setStopTime(date, sharedPrefWork, STOP_TIME_KEY_WORK)
    }

    private fun setStopTime(date: Date?, sharedPreferences: SharedPreferences, keyString: String) : Date?{
        with(sharedPreferences.edit())
        {
            val stringDate = if (date == null) null else dateFormat.format(date)
            putString(keyString,stringDate)
            apply()
        }
        return date
    }

    fun setTimerCountingWork(value: Boolean) {
        timerCountingWork = setTimeCounting(value, sharedPrefWork, COUNTING_KEY_WORK)
    }

    private fun setTimeCounting(value: Boolean, sharedPreferences: SharedPreferences, keyString: String) : Boolean{
        with(sharedPreferences.edit())
        {
            putBoolean(keyString, value)
            apply()
        }
        return value
    }

    companion object {
        const val PREFERENCES_WORK = "prefsWork"
        const val START_TIME_KEY_WORK = "startKeyWork"
        const val STOP_TIME_KEY_WORK = "stopKeyWork"
        const val COUNTING_KEY_WORK = "countingKeyWork"
    }
}