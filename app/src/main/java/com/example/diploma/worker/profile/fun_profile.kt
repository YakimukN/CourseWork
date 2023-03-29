package com.example.diploma.worker.profile

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.example.diploma.APP_ACTIVITY
import com.example.diploma.R
import com.example.diploma.admin.tasks.*
import com.example.diploma.data.Machine
import com.example.diploma.data.Task
import com.example.diploma.database.*
import com.example.diploma.replaceFragment
import com.example.diploma.scanner.*
import com.google.firebase.database.DatabaseReference
import java.util.*

private var lastUserElement = ""

private val selectedEquipments = mutableListOf<String>()
private val selectedMachines = mutableListOf<String>()
var countSelectedEquipments = 0
var countSelectedMachines = 0

fun timeStringFromLong(ms: Long): String {
    val seconds = (ms / 1000) % 60
    val minutes = (ms / (1000 * 60) % 60)
    val hours = (ms / (1000 * 60 * 60) % 24)
    return makeTimeString(hours, minutes, seconds)
}

fun makeTimeString(hours: Long, minutes: Long, seconds: Long): String {
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

fun onViewStartWork(dataHelper: DataHelper, timeTV: TextView, startStopButton: Button){
    if(dataHelper.timerCountingWork()) {
        startTimerWork(dataHelper, startStopButton)
    }
    else {
        stopTimerWork(dataHelper, startStopButton)
        if(dataHelper.startTimeWork() != null && dataHelper.stopTimeWork() != null) {
            val time = Date().time - calcRestartTimeWork(dataHelper).time
            timeTV.text = timeStringFromLong(time)
        }
    }
}

fun calcRestartTimeWork(dataHelper: DataHelper): Date {
    val diff = dataHelper.startTimeWork()!!.time - dataHelper.stopTimeWork()!!.time
    return Date(System.currentTimeMillis() + diff)
}

fun startTimerWork(dataHelper: DataHelper, startStopButton: Button) {
    dataHelper.setTimerCountingWork(true)
//    startStopButton.text = APP_CONTEXT.getString(R.string.stopWork)
}

fun stopTimerWork(dataHelper: DataHelper, startStopButton: Button) {
    dataHelper.setTimerCountingWork(false)
//    startStopButton.text = APP_CONTEXT.getString(R.string.startWork)
}

fun resetActionWork(dataHelper: DataHelper, timeTV: TextView, startStopButton: Button) {
    dataHelper.setStopTimeWork(null)
    dataHelper.setStartTimeWork(null)
    stopTimerWork(dataHelper, startStopButton)
    timeTV.text = timeStringFromLong(0)
}

fun startStopActionWork(dataHelper: DataHelper, startStopButton: Button) {
    if(dataHelper.timerCountingWork()) {
        dataHelper.setStopTimeWork(Date())
        stopTimerWork(dataHelper, startStopButton)
    }
    else {
        if(dataHelper.stopTimeWork() != null) {
            dataHelper.setStartTimeWork(calcRestartTimeWork(dataHelper))
            dataHelper.setStopTimeWork(null)
        }
        else {
            dataHelper.setStartTimeWork(Date())
        }
        startTimerWork(dataHelper, startStopButton)

    }
}

fun takeTask(task: Task){
    databaseTask.child(task.title).child(CHILD_TASK_STATUS).get().addOnSuccessListener {
        if (STATUS_FREE == it.value.toString()) {
            databaseUsers.child(firebaseUID).get().addOnSuccessListener {
                Log.i("tag", "sss = ${it.child(CHILD_USER_CURRENT_TASK).value}")
                if (it.child(CHILD_USER_CURRENT_TASK).value == STATUS_FREE || it.child(CHILD_USER_CURRENT_TASK).value.toString() == task.title) {
                    selectedTask = task.title
                    replaceFragment(ProfileFragment(), "Профиль")
                } else {
                    Toast.makeText(APP_ACTIVITY, "Необходимо закончить текущую задачу.", Toast.LENGTH_SHORT).show()
                }
            }
        } else Toast.makeText(APP_ACTIVITY, "Эта задача уже выполняется или была выполнена", Toast.LENGTH_SHORT).show()
    }
}

fun workerStartDoTask(task: String){

    val listOfSelectedEquipments = mutableListOf<String>()
    val listOfSelectedMachines = mutableListOf<String>()

    databaseTask.child(task).get().addOnSuccessListener {
        it.child(CHILD_TASK_EQUIPMENT).children.forEach{ child ->
            listOfSelectedEquipments.add(child.value.toString())
        }
        it.child(CHILD_TASK_MACHINES).children.forEach { child ->
            listOfSelectedMachines.add(child.value.toString())
        }

//        if (listOfSelectedEquipments.size == countSelectedEquipments){
//            if (listOfSelectedMachines.size == countSelectedMachines){

                databaseUsers.child(firebaseUID).child(CHILD_USER_CURRENT_TASK).setValue(task)
                databaseUsers.child(firebaseUID).child(CHILD_USER_ALL_TASKS).child(task).setValue(task)
                databaseTask.child(selectedTask).child(CHILD_TASK_STATUS).setValue(STATUS_IN_PROGRESS)

                getUsernameCurrentUser()
                addUsedToElement(databaseEquipment, listOfSelectedEquipments)
                addUsedToElement(databaseMachines, listOfSelectedMachines)

//            } else Toast.makeText(APP_ACTIVITY, "Не все станки были выбраны", Toast.LENGTH_SHORT).show()
//        } else Toast.makeText(APP_ACTIVITY, "Не все оборудование было выбрано", Toast.LENGTH_SHORT).show()
    }
}

private fun addUsedToElement(dbRef: DatabaseReference, listOf: MutableList<String>){
    dbRef.get().addOnSuccessListener {
        it.children.forEach { child ->
            if (child.child(CHILD_ELEMENT_TITLE).value.toString() in listOf){
                val usedTimes = child.child(CHILD_ELEMENT_USED_TIMES).value.toString().toInt() + 1
                val element = Machine(title=child.child(CHILD_ELEMENT_TITLE).value.toString(), lastUser=lastUserElement, usedTimes=usedTimes)
                dbRef.child(child.child(CHILD_ELEMENT_TITLE).value.toString()).setValue(element)
            }
        }
    }
}

private fun getUsernameCurrentUser(){
    databaseUsers.child(firebaseUID).child(CHILD_USER_USERNAME).get().addOnSuccessListener{
        lastUserElement = it.value.toString()
    }
}

fun workerFinishedDoTask(time: String){
    databaseUsers.child(firebaseUID).get().addOnSuccessListener { user ->

        val currentTask = user.child(CHILD_USER_CURRENT_TASK).value.toString()
        val hoursWorked = user.child(CHILD_USER_HOURS_WORKED).value.toString()
        val totalTasks = user.child(CHILD_USER_TOTAL_TASKS).value.toString().toInt() + 1

        val totalTimeWorked = plusTimes(hoursWorked, time)
        val averageWorkingTime = countAverageTime(totalTimeWorked) /////////////////

        databaseTask.child(currentTask).child(CHILD_TASK_STATUS).setValue(STATUS_COMPLETED)
        databaseTask.child(currentTask).child(CHILD_TASK_TIME).setValue(time)
        databaseUsers.child(firebaseUID).child(CHILD_USER_CURRENT_TASK).setValue(STATUS_FREE)
        databaseUsers.child(firebaseUID).child(CHILD_USER_TOTAL_TASKS).setValue(totalTasks)
        databaseUsers.child(firebaseUID).child(CHILD_USER_HOURS_WORKED).setValue(totalTimeWorked)
        databaseUsers.child(firebaseUID).child(CHILD_USER_AVERAGE_WORKING_TIME).setValue(averageWorkingTime)
    }
}

fun checkUsedElements(selectedEquipments: MutableList<String>, selectedMachines: MutableList<String>, param: String){
    if (textFromQRCode in selectedEquipments)
        countSelectedEquipments++
    else if (param == "equipment") Toast.makeText(APP_ACTIVITY, "Неверное оборудование", Toast.LENGTH_SHORT).show()

    if (textFromQRCode in selectedMachines)
        countSelectedMachines++
    else if (param == "machine") Toast.makeText(APP_ACTIVITY, "Неверный станок", Toast.LENGTH_SHORT).show()
}

fun scanCheckElements(selectEquip: MutableList<String>, selectMac: MutableList<String>, param: String){
    val inflter = LayoutInflater.from(APP_ACTIVITY)
    val myViewQRCode = inflter.inflate(R.layout.sacnner_view, null)

    surfaceQRCode = myViewQRCode.findViewById(R.id.cameraSVQRCode)
    textResultQRCode = myViewQRCode.findViewById(R.id.tvScanQRResult)


    if (ContextCompat.checkSelfPermission(APP_ACTIVITY, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
        askForCameraPermission()
    } else {
        if (myViewQRCode.parent != null) (myViewQRCode.parent as ViewGroup).removeView(myViewQRCode)

        AlertDialog.Builder(APP_ACTIVITY)
            .setTitle("Сканирование")
            .setView(myViewQRCode)
            .setPositiveButton("Выбрать"){ dialog,_->
                checkUsedElements(selectEquip, selectMac, param)
                cameraSource.stop()
                dialog.dismiss()
            }
            .setNegativeButton("Отмена"){ dialog,_ ->
                cameraSource.stop()

                dialog.dismiss() }
            .create()
            .show()

        setupControls()
    }
}