package com.example.diploma.machine

import android.annotation.SuppressLint
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
import com.example.diploma.data.Machine
import com.example.diploma.database.databaseMachines
import com.example.diploma.database.userIsAdmin
import com.example.diploma.equipment.createQRCode
import com.example.diploma.equipment.deleteMachine

class AdapterMachine(private val machineList: ArrayList<Machine>) : RecyclerView.Adapter<AdapterMachine.EquipmentViewHolder>() {
    private lateinit var mListener: onItemClickListener

    interface onItemClickListener{ fun onItemClick(position: Int) }

    fun setOnClickListener(listener: onItemClickListener){ mListener = listener }

    inner class EquipmentViewHolder(itemView: View, listener: onItemClickListener) : RecyclerView.ViewHolder(itemView){
        val machine : TextView = itemView.findViewById(R.id.titleMachine)
        val menuMachine : ImageView = itemView.findViewById(R.id.menuMachineItemRec)

        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
            menuMachine.setOnClickListener { popupMenu(it) }
        }

        @SuppressLint("DiscouragedPrivateApi")
        private fun popupMenu(v: View) {
            val position = machineList[adapterPosition]
            val popupMenus = PopupMenu(APP_ACTIVITY, v)
            if (userIsAdmin) {
                popupMenus.inflate(R.menu.show_menu_popup)

                popupMenus.setOnMenuItemClickListener {

                    when (it.itemId) {
                        R.id.deleteMenuPopup -> {
                            deleteMachine(position)
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
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.machine_item, parent, false)
        return EquipmentViewHolder(itemView, mListener)
    }

    override fun onBindViewHolder(holder: EquipmentViewHolder, position: Int) {
        val currentItem = machineList[position]
        holder.machine.text = currentItem.title
    }

    override fun getItemCount(): Int { return machineList.size }
}