package com.example.diploma.scanner

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.SparseArray
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.util.isNotEmpty
import com.example.diploma.APP_ACTIVITY
import com.example.diploma.R
import com.example.diploma.data.Equipment
import com.example.diploma.data.Machine
import com.example.diploma.database.*
import com.example.diploma.databinding.FragmentScannerBinding
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector

class ScannerFragment : Fragment() {

    private lateinit var binding: FragmentScannerBinding

    private lateinit var machineList: ArrayList<String>
    private lateinit var equipmentList: ArrayList<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentScannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        if (fromWhatFragment == ROOT_EQUIPMENT) getEquipmentList()
//        else getMachineList()
//        if (ContextCompat.checkSelfPermission(APP_ACTIVITY, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
//            askForCameraPermission()
//        } else {
//            setupControls()
//        }
//
//        binding.previous.setOnClickListener {
//            if (fromWhatFragment == ROOT_EQUIPMENT)
//                replaceFragment(EquipmentFragment(), "Оборудование")
//            else replaceFragment(MachineFragment(), "Станки")
//        }
//
//        binding.scanBtn.setOnClickListener {
//            cameraSource.stop()
//
//            if (fromWhatFragment == ROOT_EQUIPMENT){
//                if (textFromQRCode != "")
//                    saveEquipmentDataInBD()
//                replaceFragment(EquipmentFragment(), "Оборудование")
//            }
//            else {
//                if (textFromQRCode != "")
//                    saveMachineDataInBD()
//                replaceFragment(MachineFragment(), "Станки")
//            }
//        }
    }

    private fun getMachineList() {
        machineList = arrayListOf()
        databaseMachines.get().addOnSuccessListener {
            machineList.clear()
            it.children.forEach { index ->
                 machineList.add(index.child(CHILD_MACHINE_TITLE).value.toString())
            }
        }
    }

    private fun getEquipmentList() {
        equipmentList = arrayListOf()
        databaseEquipment.get().addOnSuccessListener {
            equipmentList.clear()
            it.children.forEach { index ->
                equipmentList.add(index.child(CHILD_ELEMENT_TITLE).value.toString())
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun saveEquipmentDataInBD() {
        val equip = Equipment(title=textFromQRCode)

        if (textFromQRCode in equipmentList){
            AlertDialog.Builder(APP_ACTIVITY)
                .setTitle("Предупреждение")
                .setIcon(R.drawable.ic_warning)
                .setMessage("Это оборудование уже существует. Заменить его?")
                .setPositiveButton("Да"){ dialog,_ ->
                    databaseEquipment.child(equip.title).setValue(equip).addOnSuccessListener {
                        Toast.makeText(APP_ACTIVITY, "Оборудование было успешно заменено", Toast.LENGTH_SHORT).show()
                    }
                    dialog.dismiss() }
                .setNegativeButton("Нет"){ dialog,_ -> dialog.dismiss() }
                .create()
                .show()
        } else
            AlertDialog.Builder(APP_ACTIVITY)
            .setTitle("Добавление оборудования")
            .setMessage("Добавить Оборудование $textFromQRCode?")
            .setPositiveButton("Да"){ dialog,_->
                databaseEquipment.child(equip.title).setValue(equip).addOnSuccessListener {
                    Toast.makeText(APP_ACTIVITY, "Оборудование было успешно добавлено", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss() }
            .setNegativeButton("Нет"){ dialog,_ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun saveMachineDataInBD(){
        val machine = Machine(title=textFromQRCode)

        if (textFromQRCode in machineList)
            AlertDialog.Builder(APP_ACTIVITY)
                .setTitle("Предупреждение")
                .setIcon(R.drawable.ic_warning)
                .setMessage("Этот Станок уже существует. Заменить его?")
                .setPositiveButton("Да"){ dialog,_ ->
                    databaseMachines.child(machine.title).setValue(machine).addOnSuccessListener {
                        Toast.makeText(APP_ACTIVITY, "Станок был успешно добавлено", Toast.LENGTH_SHORT).show()
                    }
                    dialog.dismiss() }
                .setNegativeButton("Нет"){ dialog,_ -> dialog.dismiss() }
                .create()
                .show()
        else
            AlertDialog.Builder(APP_ACTIVITY)
            .setTitle("Добавление Станков")
            .setMessage("Добавить Станок $textFromQRCode?")
            .setPositiveButton("Да"){ dialog,_->
                databaseMachines.child(machine.title).setValue(machine).addOnSuccessListener {
                    Toast.makeText(APP_ACTIVITY, "Станок был успешно добавлено", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss() }
            .setNegativeButton("Нет"){ dialog,_ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun setupControls() {
        detector = BarcodeDetector.Builder(APP_ACTIVITY).build()
        cameraSource = CameraSource.Builder(APP_ACTIVITY, detector).setAutoFocusEnabled(true).build()

        binding.cameraSurfaceView.holder.addCallback(surgaceCallBack)
        detector.setProcessor(processor)
    }

    private fun askForCameraPermission() {
        ActivityCompat.requestPermissions(APP_ACTIVITY, arrayOf(Manifest.permission.CAMERA), requestCodeCameraPermission)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestCodeCameraPermission && grantResults.isNotEmpty()){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                setupControls()
            } else {
                Toast.makeText(APP_ACTIVITY, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val surgaceCallBack = object : SurfaceHolder.Callback{
        @SuppressLint("MissingPermission")
        override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
            try {
                cameraSource.start(surfaceHolder)
            } catch (exeption: Exception){
                Toast.makeText(APP_ACTIVITY, "Что-то пошло не так...", Toast.LENGTH_SHORT).show()
            }
        }

        override fun surfaceDestroyed(p0: SurfaceHolder) { cameraSource.stop() }
        override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {}
    }

    private val processor = object : Detector.Processor<Barcode> {
        override fun release() {}

        override fun receiveDetections(detections: Detector.Detections<Barcode>?) {
            if (detections != null && detections.detectedItems.isNotEmpty()){
                val qrCodes: SparseArray<Barcode> = detections.detectedItems
                val code = qrCodes.valueAt(0)
                binding.textScanResult.text = code.displayValue
                textFromQRCode = code.displayValue
            } else { binding.textScanResult.text = "" }
        }
    }
}