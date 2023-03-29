package com.example.diploma.admin.tasks

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diploma.APP_ACTIVITY
import com.example.diploma.R
import com.example.diploma.adapters.TaskAdapter
import com.example.diploma.data.Task
import com.example.diploma.database.databaseEquipment
import com.example.diploma.database.databaseMachines
import com.example.diploma.database.databaseTask
import com.example.diploma.database.userIsAdmin
import com.example.diploma.databinding.FragmentTaskAdminBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.util.*
import kotlin.collections.ArrayList

class TaskAdminFragment : Fragment() {

    private lateinit var binding: FragmentTaskAdminBinding

    private lateinit var taskAdminRecyclerview: RecyclerView
    private lateinit var taskAdminArrayList: ArrayList<Task>
    private lateinit var tempTaskAdminArrayList: ArrayList<Task>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTaskAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        binding.addBtnTask.isVisible = userIsAdmin

        taskAdminRecyclerview = APP_ACTIVITY.findViewById(R.id.taskAdminList)
        taskAdminRecyclerview.layoutManager = LinearLayoutManager(APP_ACTIVITY)
        taskAdminRecyclerview.setHasFixedSize(true)

        getOrderData()
        taskAdminArrayList = arrayListOf()
        tempTaskAdminArrayList = arrayListOf()

        binding.addBtnTask.setOnClickListener {
            readFromDB()
            addTaskInDB()
        }
    }

    private fun getOrderData() {
        databaseTask.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                taskAdminArrayList.clear()
                tempTaskAdminArrayList.clear()

                if (snapshot.exists()){
                    for (orderSnapshot in snapshot.children){
                        val order = orderSnapshot.getValue(Task::class.java)
                        taskAdminArrayList.add(order!!)
                    }

                    tempTaskAdminArrayList.addAll(taskAdminArrayList)

                    val adapter = TaskAdapter(tempTaskAdminArrayList) // OrderAdapter(orderArrayList)
                    taskAdminRecyclerview.adapter = adapter

                    adapter.setOnClickListener(object : TaskAdapter.onItemClickListener{
                        override fun onItemClick(position: Int) {
                            showInfoAboutOrder(tempTaskAdminArrayList[position]) // showInfoAboutOrder(orderArrayList[position])
                        }
                    })
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu, menu)
        val item = menu.findItem(R.id.search_action)
        val searchView = item?.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean { return true }

            @SuppressLint("NotifyDataSetChanged")
            override fun onQueryTextChange(newText: String?): Boolean {
                tempTaskAdminArrayList.clear()
                val searchText = newText!!.lowercase(Locale.getDefault())
                if (searchText.isNotEmpty()){
                    taskAdminArrayList.forEach {
                        if (it.title.lowercase(Locale.getDefault()).contains(searchText)){
                            tempTaskAdminArrayList.add(it)
                        }
                    }
                    taskAdminRecyclerview.adapter?.notifyDataSetChanged()

                } else {
                    tempTaskAdminArrayList.clear()
                    tempTaskAdminArrayList.addAll(taskAdminArrayList)
                    taskAdminRecyclerview.adapter?.notifyDataSetChanged()
                }
                return false
            }
        })
        super.onCreateOptionsMenu(menu, inflater)
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

    @SuppressLint("InflateParams")
    private fun addTaskInDB() {
        val inflter = LayoutInflater.from(APP_ACTIVITY)
        val myview = inflter.inflate(R.layout.add_order_dialog_item, null)
        val time = "00:00:00"

        selectWorker = ""

        val titleOrderTV : TextView = myview.findViewById(R.id.titleOrder)
        val descriptionOrderTV : TextView = myview.findViewById(R.id.orderDescription)

        val addDialog = AlertDialog.Builder(APP_ACTIVITY)
        addDialog.setView(myview)
            .setPositiveButton("Ok"){ dialog,_->

                if (selectWorker == "")
                    selectWorker = "Для всех"
                var title : String = titleOrderTV.text.toString()
                val descr : String = descriptionOrderTV.text.toString()
                val machines = getSelectedElements(listOfMachines, checkedMachines)
                val equipments = getSelectedElements(listOfEquipments, checkedEquipments)
                val order = Task(title=title, description=descr, selectedWorker=selectWorker, status=STATUS_FREE, time=time,machines=machines, equipments=equipments)

                if (title.isNotEmpty()){
//                    title += "_$selectWorker"
                    databaseTask.child(title).setValue(order).addOnSuccessListener {
                        Toast.makeText(APP_ACTIVITY, "Добавлено", Toast.LENGTH_SHORT).show()
                    }
                } else Toast.makeText(APP_ACTIVITY, "Ошибка! Название не должно быть пустым", Toast.LENGTH_SHORT).show()

                dialog.dismiss()
            }
            .setNegativeButton("Отмена"){ dialog,_-> dialog.dismiss() }
            .create()
            .show()

        myview.findViewById<Button>(R.id.addWorkerOrder).setOnClickListener { addWorker() }
        myview.findViewById<Button>(R.id.addMachineOrder).setOnClickListener {
            addEquipMach("станки", listOfMachines, checkedMachines, databaseMachines)
//            addMachines()
        }
        myview.findViewById<Button>(R.id.addEquipmentOrder).setOnClickListener {
            addEquipMach("оборудование", listOfEquipments, checkedEquipments, databaseEquipment)
//            addEquipments()
        }
    }
}
