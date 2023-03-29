package com.example.diploma.scanner

import android.Manifest
import android.annotation.SuppressLint
import android.util.SparseArray
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.util.isNotEmpty
import com.example.diploma.APP_ACTIVITY
import com.example.diploma.R
import com.example.diploma.admin.tasks.checkedEquipments
import com.example.diploma.admin.tasks.checkedMachines
import com.example.diploma.admin.tasks.listOfEquipments
import com.example.diploma.admin.tasks.listOfMachines
import com.example.diploma.data.Equipment
import com.example.diploma.data.Machine
import com.example.diploma.database.databaseEquipment
import com.example.diploma.database.databaseMachines
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.firebase.database.DatabaseReference

@SuppressLint("StaticFieldLeak")
lateinit var cameraSource: CameraSource
var requestCodeCameraPermission = 1001
lateinit var detector: BarcodeDetector

var textFromQRCode = ""
var fromWhatFragment: String = ""

@SuppressLint("StaticFieldLeak")
lateinit var surfaceQRCode : SurfaceView //= myView.findViewById(R.id.cameraSVQRCode)
@SuppressLint("StaticFieldLeak")
lateinit var textResultQRCode : TextView //= myView.findViewById(R.id.tvScanQRResult)

private lateinit var machineListQRCode: ArrayList<String>
private lateinit var equipmentListQRCode: ArrayList<String>

private lateinit var existingElementList: ArrayList<String>

fun setupControls() {
    detector = BarcodeDetector.Builder(APP_ACTIVITY).build()
    cameraSource = CameraSource.Builder(APP_ACTIVITY, detector).setAutoFocusEnabled(true).build()

    surfaceQRCode.holder.addCallback(surgaceCallBack)
    detector.setProcessor(processor)
}

fun askForCameraPermission() {
    ActivityCompat.requestPermissions(APP_ACTIVITY, arrayOf(Manifest.permission.CAMERA), requestCodeCameraPermission)
}

val surgaceCallBack = object : SurfaceHolder.Callback{
    @SuppressLint("MissingPermission")
    override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
        try {
            cameraSource.start(surfaceHolder)
        } catch (exeption: Exception){
            Toast.makeText(APP_ACTIVITY, "Something went wrong", Toast.LENGTH_SHORT).show()
        }
    }

    override fun surfaceDestroyed(p0: SurfaceHolder) { cameraSource.stop() }
    override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {}
}

val processor = object : Detector.Processor<Barcode> {
    override fun release() {}

    override fun receiveDetections(detections: Detector.Detections<Barcode>?) {
        if (detections != null && detections.detectedItems.isNotEmpty()){
            val qrCodes: SparseArray<Barcode> = detections.detectedItems
            val code = qrCodes.valueAt(0)
            textResultQRCode.text = code.displayValue
            textFromQRCode = code.displayValue
        } else { textResultQRCode.text = "" }
    }
}

fun getExistingElement(database: DatabaseReference, child_title: String){
    existingElementList = arrayListOf()
    database.get().addOnSuccessListener {
        existingElementList.clear()
        it.children.forEach { index ->
            existingElementList.add(index.child(child_title).value.toString())
        }
    }
}

fun saveNewElement(database: DatabaseReference, title: String){
    val element = Machine(title=textFromQRCode)

    if (textFromQRCode in existingElementList)
        AlertDialog.Builder(APP_ACTIVITY)
            .setTitle("Предупреждение")
            .setIcon(R.drawable.ic_warning)
            .setMessage("Данный элемент уже существует. Заменить его?")
            .setPositiveButton("Да"){ dialog,_ ->
                database.child(element.title).setValue(element).addOnSuccessListener {
                    Toast.makeText(APP_ACTIVITY, "Елемент был успешно заменен", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss() }
            .setNegativeButton("Нет"){ dialog,_ -> dialog.dismiss() }
            .create()
            .show()
    else
        AlertDialog.Builder(APP_ACTIVITY)
            .setTitle("Добавить $title")
            .setMessage("Добавить $title $textFromQRCode?")
            .setPositiveButton("Да"){ dialog,_->
                if (title == "оборудование") {
                    checkedEquipments.add(false)
                    listOfEquipments.add(element.title)
                }
                else {
                    checkedMachines.add(false)
                    listOfMachines.add(element.title)
                }
                database.child(element.title).setValue(element).addOnSuccessListener {
                    Toast.makeText(APP_ACTIVITY, "Елемент был успешно добавлен", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss() }
            .setNegativeButton("Нет"){ dialog,_ -> dialog.dismiss() }
            .create()
            .show()
}
//
//@SuppressLint("MissingPermission")
//fun saveEquipmentDataInBD() {
//    val equip = Equipment(title=textFromQRCode)
//
//    if (textFromQRCode in equipmentListQRCode){
//        AlertDialog.Builder(APP_ACTIVITY)
//            .setTitle("Предупреждение")
//            .setIcon(R.drawable.ic_warning)
//            .setMessage("Это оборудование уже существует. Заменить его?")
//            .setPositiveButton("Да"){ dialog,_ ->
//                databaseEquipment.child(equip.title).setValue(equip).addOnSuccessListener {
//                    Toast.makeText(APP_ACTIVITY, "Оборудование было успешно заменено", Toast.LENGTH_SHORT).show()
//                }
//                dialog.dismiss() }
//            .setNegativeButton("Нет"){ dialog,_ -> dialog.dismiss() }
//            .create()
//            .show()
//    } else
//        AlertDialog.Builder(APP_ACTIVITY)
//            .setTitle("Добавление оборудования")
//            .setMessage("Добавить Оборудование $textFromQRCode?")
//            .setPositiveButton("Да"){ dialog,_->
//                databaseEquipment.child(equip.title).setValue(equip).addOnSuccessListener {
//                    Toast.makeText(APP_ACTIVITY, "Оборудование было успешно добавлено", Toast.LENGTH_SHORT).show()
//                }
//                dialog.dismiss() }
//            .setNegativeButton("Нет"){ dialog,_ -> dialog.dismiss() }
//            .create()
//            .show()
//}
//
//fun saveMachineDataInBD(){
//    val machine = Machine(title=textFromQRCode)
//
//    if (textFromQRCode in machineListQRCode)
//        AlertDialog.Builder(APP_ACTIVITY)
//            .setTitle("Предупреждение")
//            .setIcon(R.drawable.ic_warning)
//            .setMessage("Этот Станок уже существует. Заменить его?")
//            .setPositiveButton("Да"){ dialog,_ ->
//                databaseMachines.child(machine.title).setValue(machine).addOnSuccessListener {
//                    Toast.makeText(APP_ACTIVITY, "Станок был успешно добавлено", Toast.LENGTH_SHORT).show()
//                }
//                dialog.dismiss() }
//            .setNegativeButton("Нет"){ dialog,_ -> dialog.dismiss() }
//            .create()
//            .show()
//    else
//        AlertDialog.Builder(APP_ACTIVITY)
//            .setTitle("Добавление Станков")
//            .setMessage("Добавить Станок $textFromQRCode?")
//            .setPositiveButton("Да"){ dialog,_->
//                databaseMachines.child(machine.title).setValue(machine).addOnSuccessListener {
//                    Toast.makeText(APP_ACTIVITY, "Станок был успешно добавлено", Toast.LENGTH_SHORT).show()
//                }
//                dialog.dismiss() }
//            .setNegativeButton("Нет"){ dialog,_ -> dialog.dismiss() }
//            .create()
//            .show()
//}


//private fun getMachineList() {
//    machineListQRCode = arrayListOf()
//    databaseMachines.get().addOnSuccessListener {
//        machineListQRCode.clear()
//        it.children.forEach { index ->
//            machineListQRCode.add(index.child(CHILD_MACHINE_TITLE).value.toString())
//        }
//    }
//}
//
//private fun getEquipmentList() {
//    equipmentListQRCode = arrayListOf()
//    databaseEquipment.get().addOnSuccessListener {
//        equipmentListQRCode.clear()
//        it.children.forEach { index ->
//            equipmentListQRCode.add(index.child(CHILD_EQUIPMENT_TITLE).value.toString())
//        }
//    }
//}