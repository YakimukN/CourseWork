package com.example.diploma.worker.tasks

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diploma.APP_ACTIVITY
import com.example.diploma.R
import com.example.diploma.adapters.TaskAdapter
import com.example.diploma.data.Task
import com.example.diploma.database.databaseTask
import com.example.diploma.database.usernameData
import com.example.diploma.databinding.FragmentTaskBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class TaskFragment : Fragment() {

    private lateinit var binding: FragmentTaskBinding

    private lateinit var taskRecyclerview: RecyclerView
    private lateinit var taskArrayList: ArrayList<Task>
    private lateinit var tempTaskArrayList: ArrayList<Task>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)

        taskRecyclerview = APP_ACTIVITY.findViewById(R.id.taskList)
        taskRecyclerview.layoutManager = LinearLayoutManager(APP_ACTIVITY)
        taskRecyclerview.setHasFixedSize(true)

        getOrderData()
        taskArrayList = arrayListOf()
        tempTaskArrayList = arrayListOf()
    }

    private fun getOrderData() {
        databaseTask.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                taskArrayList.clear()
                tempTaskArrayList.clear()

                if (snapshot.exists()){
                    for (orderSnapshot in snapshot.children){
                        val order = orderSnapshot.getValue(Task::class.java)
                        if (order?.selectedWorker.toString() == usernameData || order?.selectedWorker == "Для всех")
                            taskArrayList.add(order!!)
                    }

                    tempTaskArrayList.addAll(taskArrayList)

                    val adapter = TaskAdapter(tempTaskArrayList) // OrderAdapter(orderArrayList)
                    taskRecyclerview.adapter = adapter

                    adapter.setOnClickListener(object : TaskAdapter.onItemClickListener{
                        override fun onItemClick(position: Int) {
                            showInfoAboutOrder(tempTaskArrayList[position]) // showInfoAboutOrder(orderArrayList[position])
                        }
                    })
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    @SuppressLint("SetTextI18n")
    private fun showInfoAboutOrder(task: Task){
        val inflter = LayoutInflater.from(APP_ACTIVITY)
        val myView = inflter.inflate(R.layout.order_info_item, null)

        val equiOrder : TextView = myView.findViewById(R.id.orderInfoEquipments)
        val machiOrder : TextView = myView.findViewById(R.id.orderInfoMachines)
        val descrOrder : TextView = myView.findViewById(R.id.orderInfoDescription)
        val statusTask : TextView = myView.findViewById(R.id.orderInfoStatus)
        val timeTask : TextView = myView.findViewById(R.id.orderInfoTime)

        myView.findViewById<TextView>(R.id.orderInfoTitle).text = ("Информация о \"" + task.title + "\"")
        descrOrder.text = ("Описание: " + task.description)
        descrOrder.movementMethod = ScrollingMovementMethod()
        myView.findViewById<TextView>(R.id.orderInfoWorker).text = ("Сотрудник: " + task.selectedWorker)

        var orderEquip = ""
        for (i in task.equipments.indices){
            orderEquip += task.equipments[i] + "\n"
        }
        var orderMachi = ""
        for (i in task.machines.indices){
            orderMachi += task.machines[i] + "\n"
        }

        equiOrder.text = orderEquip
        equiOrder.movementMethod = ScrollingMovementMethod()
        machiOrder.text = orderMachi
        machiOrder.movementMethod = ScrollingMovementMethod()
        statusTask.text = task.status
        timeTask.text = task.time

        AlertDialog.Builder(APP_ACTIVITY)
            .setView(myView)
            .setPositiveButton("Ok"){ dial, _ -> dial.dismiss() }
            .create()
            .show()
    }
}