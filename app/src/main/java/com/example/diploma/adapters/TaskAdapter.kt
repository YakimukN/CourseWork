package com.example.diploma.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.diploma.APP_ACTIVITY
import com.example.diploma.R
import com.example.diploma.admin.tasks.deleteTask
import com.example.diploma.admin.tasks.editTask
import com.example.diploma.data.Task
import com.example.diploma.database.userIsAdmin
import com.example.diploma.worker.profile.takeTask

class TaskAdapter(private val orderList : ArrayList<Task>) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>()  {

    private lateinit var mListener: onItemClickListener

    interface onItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnClickListener(listener: onItemClickListener){
        mListener = listener
    }

    inner class TaskViewHolder(itemView: View, listener: onItemClickListener) : RecyclerView.ViewHolder(itemView){
        val title : TextView = itemView.findViewById(R.id.titleTaskItemRec)
        val worker : TextView = itemView.findViewById(R.id.workerTaskItemRec)
        val status : TextView = itemView.findViewById(R.id.statusTaskItemRec)
        val menuOrder : ImageView = itemView.findViewById(R.id.menuTaskItemRec)

        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
            menuOrder.setOnClickListener { popupMenu(it) }
        }
        @SuppressLint("DiscouragedPrivateApi")
        private fun popupMenu(v: View){
            val position = orderList[adapterPosition]
            val popupMenus = PopupMenu(APP_ACTIVITY, v)
            if (userIsAdmin)
                popupMenus.inflate(R.menu.show_menu_task_admin)
            else popupMenus.inflate(R.menu.show_menu_task_worker)
            popupMenus.setOnMenuItemClickListener {

                when(it.itemId){
                    R.id.editOrder -> {
                        editTask(position)
                        true
                    }
                    R.id.deleteOrder -> {
                        deleteTask(position)
                        true
                    }
                    R.id.takeTask -> {
                        takeTask(position)
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.task_admin_item, parent, false)
        return TaskViewHolder(itemView, mListener)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val currentItem = orderList[position]
        holder.title.text = currentItem.title
        holder.status.text = currentItem.status
        holder.worker.text = currentItem.selectedWorker
    }

    override fun getItemCount(): Int { return orderList.size }
}