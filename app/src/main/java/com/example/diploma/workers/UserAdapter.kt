package com.example.diploma.workers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.diploma.R
import com.example.diploma.data.User

class UserAdapter(private val userList : ArrayList<User>) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {
    private lateinit var mListener: onItemClickListener

    interface onItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnClickListener(listener: onItemClickListener){
        mListener = listener
    }

    inner class UserViewHolder(itemView: View, listener: onItemClickListener) : RecyclerView.ViewHolder(itemView){
        val worker : TextView = itemView.findViewById(R.id.nameWorker)

        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.worker_item, parent, false)
        return UserViewHolder(itemView, mListener)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val currentItem = userList[position]

        holder.worker.text = currentItem.username
    }

    override fun getItemCount(): Int {
        return userList.size
    }
}