package com.example.diploma.admin.tasks

import android.Manifest
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.example.diploma.APP_ACTIVITY
import com.example.diploma.R
import com.example.diploma.data.Task
import com.example.diploma.database.*
import com.example.diploma.scanner.*
import com.google.firebase.database.DatabaseReference

fun deleteTask(task: Task) {
    AlertDialog.Builder(APP_ACTIVITY)
        .setTitle("Delete")
        .setIcon(R.drawable.ic_warning)
        .setMessage("Удалить задачу ${task.title}?")
        .setPositiveButton("Да"){
                dialog,_->
            databaseTask.child(task.title).removeValue().addOnSuccessListener {
                Toast.makeText(APP_ACTIVITY,"Задача была успешно удалена", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        .setNegativeButton("Нет"){ dialog,_-> dialog.dismiss() }
        .create()
        .show()
}

fun editTask(task: Task) {
    readFromDB(task.title)

    val inflter = LayoutInflater.from(APP_ACTIVITY)
    val myView = inflter.inflate(R.layout.add_order_dialog_item, null)

    myView.findViewById<TextView>(R.id.nameAddOrderWindow).text = "Редактирование"
    val title = myView.findViewById<TextView>(R.id.titleOrder)
    val description = myView.findViewById<TextView>(R.id.orderDescription)
    val worker : Button = myView.findViewById(R.id.addWorkerOrder)
    val machine : Button = myView.findViewById(R.id.addMachineOrder)
    val equipment : Button = myView.findViewById(R.id.addEquipmentOrder)
    val statusTask = task.status

    title.text = task.title
    title.isEnabled = false
    description.text = task.description


    worker.setOnClickListener { addWorker(numberOfSelectWorker) }
    machine.setOnClickListener { addEquipMach("Станки", listOfMachines, checkedMachines, databaseMachines) } //addMachines() }
    equipment.setOnClickListener { addEquipMach("Оборудование", listOfEquipments, checkedEquipments, databaseEquipment)} //addEquipments() }

    AlertDialog.Builder(APP_ACTIVITY)
        .setView(myView)
        .setPositiveButton(R.string.edit){ dialog, _->
            val machines = getSelectedElements(listOfMachines, checkedMachines)
            val equipments = getSelectedElements(listOfEquipments, checkedEquipments)
            val newOrder = Task(title=task.title, description=description.text.toString(), selectedWorker=selectWorker, machines=machines, equipments=equipments, status=statusTask)

            databaseTask.child(task.title).setValue(newOrder).addOnSuccessListener { Toast.makeText(APP_ACTIVITY, "Задача \"${task.title}\" была успешно отредактированна!", Toast.LENGTH_SHORT).show() }
            dialog.dismiss()
        }
        .setNegativeButton(R.string.cancel){ dialog, _-> dialog.dismiss() }
        .create()
        .show()
}

fun getSelectedElements(listOf: MutableList<String>, checked: MutableList<Boolean>) : MutableList<String>{
    val elements = mutableListOf<String>()
    for (i in 0 until checked.size)
        if (checked[i])
            elements.add(listOf[i])
    return elements
}

fun getCorrectWorkerFromBD(task: String){
    databaseTask.child(task).child(CHILD_TASK_SELECTED_WORKER).get().addOnSuccessListener {
        selectWorker = it.value.toString()
        for (i in listOfWorkers.indices){
            if (selectWorker == listOfWorkers[i])
                numberOfSelectWorker = i
        }
    }
}

fun getCorrectMachineFromBD(task: String){
    databaseTask.child(task).get().addOnSuccessListener {
        it.child(CHILD_TASK_MACHINES).children.forEach { itChild ->
            for (i in listOfMachines.indices) {
                if (itChild.value.toString() == listOfMachines[i]) {
                    checkedMachines[i] = true
                }
            }
        }
    }
}

fun getCorrectEquipmFromBD(task: String){
    databaseTask.child(task).get().addOnSuccessListener {
        it.child(CHILD_TASK_EQUIPMENT).children.forEach { itChild ->
            for (i in listOfEquipments.indices){
                if (itChild.value.toString() == listOfEquipments[i]){
                    checkedEquipments[i] = true
                }
            }
        }
    }
}

fun addWorker(selected: Int = 0) {
    val addDialog = AlertDialog.Builder(APP_ACTIVITY)
    addDialog.setTitle("Выберите сотрудника")
    val cloneListOfWorkers : Array<String> = listOfWorkers.toList().toTypedArray()

    addDialog.setSingleChoiceItems(cloneListOfWorkers, selected) { dialog, i ->
        selectWorker = listOfWorkers[i]
    }

    addDialog.setPositiveButton("Ok"){ dialog,_-> dialog.dismiss() }
    addDialog.create()
    addDialog.show()
}

fun addEquipMach(title: String, listOfEquipMach: MutableList<String>, checkedEquipMach: MutableList<Boolean>, databaseRef: DatabaseReference){
    val cloneListOfEquipments : Array<String> = listOfEquipMach.toList().toTypedArray()
    val cloneCheckedEquipments : BooleanArray = checkedEquipMach.toBooleanArray()

    AlertDialog.Builder(APP_ACTIVITY)
        .setTitle("Выберите $title")
        .setNeutralButton("Сканировать") { dialog, _ ->
            scanQRCode(title, listOfEquipMach, checkedEquipMach, databaseRef)
            dialog.dismiss()
        }
        .setMultiChoiceItems(cloneListOfEquipments, cloneCheckedEquipments) {dialog, which, isCheked ->
            checkedEquipMach[which] = isCheked
        }
        .setPositiveButton("Ok") {dialog, _ -> dialog.dismiss()}
        .create()
        .show()
}

fun scanQRCode(title: String, listOfEquipMach: MutableList<String>, checkedEquipMach: MutableList<Boolean>, databaseRef: DatabaseReference){
    val inflter = LayoutInflater.from(APP_ACTIVITY)
    val myViewQRCode = inflter.inflate(R.layout.sacnner_view, null)

    surfaceQRCode = myViewQRCode.findViewById(R.id.cameraSVQRCode)
    textResultQRCode = myViewQRCode.findViewById(R.id.tvScanQRResult)

    if (title == "оборудование")
        getExistingElement(databaseEquipment, CHILD_ELEMENT_TITLE)
    else getExistingElement(databaseMachines, CHILD_MACHINE_TITLE)

    if (ContextCompat.checkSelfPermission(APP_ACTIVITY, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
        askForCameraPermission()
    } else {
        if (myViewQRCode.parent != null) (myViewQRCode.parent as ViewGroup).removeView(myViewQRCode)

        AlertDialog.Builder(APP_ACTIVITY)
            .setTitle("Добавление")
            .setView(myViewQRCode)
            .setPositiveButton("Добавить"){ dialog,_->
                cameraSource.stop()
                saveNewElement(databaseRef, title)
//                addEquipMach(title, listOfEquipMach, checkedEquipMach, databaseRef)
                dialog.dismiss()
            }
            .setNegativeButton("Отмена"){ dialog,_ ->
                cameraSource.stop()
//                addEquipMach(title, listOfEquipMach, checkedEquipMach, databaseRef)
                dialog.dismiss() }
            .create()
            .show()

        setupControls()
    }
}

fun readFromDB(task: String = "") {
    databaseUsernames.get().addOnSuccessListener {
        listOfWorkers.clear()
        listOfWorkers.add("Для всех")
        it.children.forEach { index ->
            listOfWorkers.add(index.value.toString())
        }
        if (task != ""){
            getCorrectWorkerFromBD(task)
        }
    }

    databaseMachines.get().addOnSuccessListener {
        listOfMachines.clear()
        checkedMachines.clear()
        it.children.forEach { index ->
            listOfMachines.add(index.child(CHILD_MACHINE_TITLE).value.toString())
            checkedMachines.add(false)
        }
        if (task != ""){
            getCorrectMachineFromBD(task)
        }
    }

    databaseEquipment.get().addOnSuccessListener {
        listOfEquipments.clear()
        checkedEquipments.clear()
        it.children.forEach { index ->
            listOfEquipments.add(index.child(CHILD_ELEMENT_TITLE).value.toString())
            checkedEquipments.add(false)
        }
        if (task != ""){
            getCorrectEquipmFromBD(task)
        }
    }
}


//fun addEquipments() {
//    val addDialog = AlertDialog.Builder(APP_ACTIVITY)
//    addDialog.setTitle("Выберите станки")
//    val cloneListOfEquipments : Array<String> = listOfEquipments.toList().toTypedArray()
//    val cloneCheckedEquipments : BooleanArray = checkedEquipments.toBooleanArray()
//
//    addDialog.setMultiChoiceItems(cloneListOfEquipments, cloneCheckedEquipments) { dialog, which, isChecked ->
//        checkedEquipments[which] = isChecked
//    }
//    addDialog.setPositiveButton("Ok"){ dialog,_-> dialog.dismiss() }
//    addDialog.create()
//    addDialog.show()
//}



//fun addMachines() {
//    val addDialog = AlertDialog.Builder(APP_ACTIVITY)
//    addDialog.setTitle("Выберите оборудование")
//
//    val cloneListOfMachines : Array<String> = listOfMachines.toList().toTypedArray()
//    val cloneCheckedMachines : BooleanArray = checkedMachines.toBooleanArray()
//
//    addDialog.setMultiChoiceItems(cloneListOfMachines, cloneCheckedMachines) { dialog, which, isChecked ->
//        checkedMachines[which] = isChecked
//    }
//    addDialog.setPositiveButton("Ok"){ dialog,_-> dialog.dismiss() }
//    addDialog.create()
//    addDialog.show()
//}