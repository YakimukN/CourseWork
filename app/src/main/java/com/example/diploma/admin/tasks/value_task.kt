package com.example.diploma.admin.tasks

val listOfWorkers = mutableListOf<String>()

val listOfMachines = mutableListOf<String>()
val checkedMachines = mutableListOf<Boolean>()

val listOfEquipments = mutableListOf<String>()
val checkedEquipments = mutableListOf<Boolean>()

var selectWorker = ""
var numberOfSelectWorker = -1

const val STATUS_FREE = "свободен"
const val STATUS_COMPLETED = "выполнен"
const val STATUS_IN_PROGRESS = "в процессе..."