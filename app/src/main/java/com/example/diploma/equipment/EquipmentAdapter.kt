package com.example.diploma.equipment

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.diploma.APP_ACTIVITY
import com.example.diploma.R
import com.example.diploma.data.Equipment
import com.example.diploma.database.databaseEquipment
import com.example.diploma.database.userIsAdmin
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter

class EquipmentAdapter(private val equipmentList: ArrayList<Equipment>) : RecyclerView.Adapter<EquipmentAdapter.EquipmentViewHolder>() {
    private lateinit var mListener: onItemClickListener

    interface onItemClickListener{ fun onItemClick(position: Int) }

    fun setOnClickListener(listener: onItemClickListener){ mListener = listener }

    inner class EquipmentViewHolder(itemView: View, listener: onItemClickListener) : RecyclerView.ViewHolder(itemView){
        val equipment : TextView = itemView.findViewById(R.id.titleEquipment)
        val menuEquip : ImageView = itemView.findViewById(R.id.menuEquipmentItemRec)

        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
            menuEquip.setOnClickListener { popupMenu(it) }
        }

        @SuppressLint("DiscouragedPrivateApi")
        private fun popupMenu(v: View) {
            val position = equipmentList[adapterPosition]
            val popupMenus = PopupMenu(APP_ACTIVITY, v)
            if (userIsAdmin) {
                popupMenus.inflate(R.menu.show_menu_popup)

                popupMenus.setOnMenuItemClickListener {

                    when (it.itemId) {
                        R.id.deleteMenuPopup -> {
                            deleteEquipment(position)
                            true
                        }
                        R.id.generationQRMenuPopup -> {
                            createQRCode(position.title)
                            true
                        }
                        else -> true
                    }
                }
                popupMenus.show()
                val popup = PopupMenu::class.java.getDeclaredField("mPopup")
                popup.isAccessible = true
                val menu = popup.get(popupMenus)
                menu.javaClass.getDeclaredMethod("setForceShowIcon", Boolean::class.java).invoke(menu, true)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EquipmentViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.equipment_item, parent, false)
        return EquipmentViewHolder(itemView, mListener)
    }

    override fun onBindViewHolder(holder: EquipmentViewHolder, position: Int) {
        val currentItem = equipmentList[position]
        holder.equipment.text = currentItem.title
    }

    override fun getItemCount(): Int { return equipmentList.size }
}