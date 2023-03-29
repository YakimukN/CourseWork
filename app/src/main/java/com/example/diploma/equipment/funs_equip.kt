package com.example.diploma.equipment

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.startActivity
import com.example.diploma.APP_ACTIVITY
import com.example.diploma.R
import com.example.diploma.data.Equipment
import com.example.diploma.data.Machine
import com.example.diploma.database.databaseEquipment
import com.example.diploma.database.databaseMachines
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import java.io.ByteArrayOutputStream

private lateinit var imageUri: Uri

private fun generateCode(data: String){
    val inflter = LayoutInflater.from(APP_ACTIVITY)
    val myView = inflter.inflate(R.layout.generatioq_qr_view, null)

    myView.findViewById<ImageView>(R.id.ivQRCode).setImageURI(imageUri)

    AlertDialog.Builder(APP_ACTIVITY)
        .setTitle("QR-код")
        .setView(myView)
        .setPositiveButton("Поделиться"){ dialog,_->
            shareQRCode(data)
            dialog.dismiss()
        }
        .setNegativeButton("Отмена"){ dialog,_-> dialog.dismiss() }
        .create()
        .show()
}

private fun shareQRCode(data: String){
    val intent = Intent(Intent.ACTION_SEND)
    intent.type = "image/png"
    intent.putExtra(Intent.EXTRA_SUBJECT, data) /// check this useful or not
    intent.putExtra(Intent.EXTRA_STREAM, imageUri)
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

    APP_ACTIVITY.startActivity(Intent.createChooser(intent, "Поделиться... "))
}

fun createQRCode(data: String){
    val element = data

    if (element.isEmpty()){
        Toast.makeText(APP_ACTIVITY, "Ошибка получения элемента", Toast.LENGTH_SHORT).show()
    } else {

        val writer = QRCodeWriter()
        try {
            val bitMatrix = writer.encode(element, BarcodeFormat.QR_CODE, 512, 512)
            val width = bitMatrix.width
            val height = bitMatrix.height

            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width){
                for (y in 0 until height){
                    bmp.setPixel(x, y, if(bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            imageUri = getImageUriFromBitmap(APP_ACTIVITY, bmp, data)
            generateCode(data)
        } catch (e: WriterException){
            e.printStackTrace()
        }
    }
}

private fun getImageUriFromBitmap(context: Context, bitmap: Bitmap, data: String): Uri{
    val bytes = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
    val path = MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, data, null)
    return Uri.parse(path.toString())
}

fun deleteEquipment(data: Equipment){
    val element = data.title

    AlertDialog.Builder(APP_ACTIVITY)
        .setTitle("Удаление")
        .setIcon(R.drawable.ic_warning)
        .setMessage("Удалить оборудование $element?")
        .setPositiveButton("Да"){
                dialog,_->
            databaseEquipment.child(element).removeValue().addOnSuccessListener {
                Toast.makeText(APP_ACTIVITY,"Оборудование было успешно удалено", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        .setNegativeButton("Нет"){ dialog,_-> dialog.dismiss() }
        .create()
        .show()
}

fun deleteMachine(data: Machine){
    val element = data.title

    AlertDialog.Builder(APP_ACTIVITY)
        .setTitle("Удаление")
        .setIcon(R.drawable.ic_warning)
        .setMessage("Удалить станок $element?")
        .setPositiveButton("Да"){
                dialog,_->
            databaseMachines.child(element).removeValue().addOnSuccessListener {
                Toast.makeText(APP_ACTIVITY,"Станок был успешно удален", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        .setNegativeButton("Нет"){ dialog,_-> dialog.dismiss() }
        .create()
        .show()
}
