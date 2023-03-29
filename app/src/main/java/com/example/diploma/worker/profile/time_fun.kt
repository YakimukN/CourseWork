package com.example.diploma.worker.profile

fun getTimeFromString(time: String) : TimeW{
    val timeW = TimeW()

    if (time.length == 8){
        timeW.second = ("${time[6]}${time[7]}").toInt()
        timeW.minute = ("${time[3]}${time[4]}").toInt()
        timeW.hour = ("${time[0]}${time[1]}").toInt()
    } else {
        timeW.second = ("${time[7]}${time[8]}").toInt()
        timeW.minute = ("${time[4]}${time[5]}").toInt()
        timeW.hour = ("${time[0]}${time[1]}${time[2]}").toInt()
    }
    return timeW
}

fun getStringFromTimeW(timeW: TimeW) : String {
    var second = timeW.second.toString()
    var minute = timeW.minute .toString()
    var hour = timeW.hour.toString()
    if (second.length != 2) second = "0$second"
    if (minute.length != 2) minute = "0$minute"
    if (hour.length < 2) hour = "0$hour"

    return "$hour:$minute:$second"
}

fun plusTimes(timeFirst: String, timeSecond: String) : String{
    val firstEl = getTimeFromString(timeFirst)
    val secondEl = getTimeFromString(timeSecond)

    var second = firstEl.second + secondEl.second
    var minute = firstEl.minute + secondEl.minute
    var hour = firstEl.hour + secondEl.hour

    if (second >= 60){
        second -= 60
        minute++
    }
    if (minute >= 60){
        minute -= 60
        hour++
    }

    val timeResult = TimeW(second, minute, hour)

    return getStringFromTimeW(timeResult)
}

fun countAverageTime(getTime: String) : String{
    val time = getTimeFromString(getTime)

    var second = time.second
    var minute = time.minute
    val hour = time.hour / 2

    if (time.hour % 2 != 0) minute += 30
    if (time.minute % 2 != 0) second += 30

    second /= 2
    minute /= 2

    return getStringFromTimeW(TimeW(second, minute, hour))
}





















