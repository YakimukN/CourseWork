package com.example.diploma.data

data class Task(
    var time: String = "",
    var title: String = "",
    var status: String = "",
    var description: String = "",
    var selectedWorker: String = "",
    var machines: MutableList<String> = mutableListOf(),
    var equipments: MutableList<String> = mutableListOf(),
    var titleUID: String = "" // название с сотрудникам, нода для базы
)
