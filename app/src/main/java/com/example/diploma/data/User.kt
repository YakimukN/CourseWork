package com.example.diploma.data

import com.example.diploma.admin.tasks.STATUS_FREE

data class User (
    var uid: String = "",
    val role: String = "",
    var email: String = "",
    var username: String = "",
//    var failedOrder: Int = 0,
    var hoursWorked: String = "00:00:00",
    var currentTask: String = STATUS_FREE,
    var averageWorkingTime: String = "00:00:00",
    var totalTasks: Int = 0
)