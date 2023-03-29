package com.example.diploma.worker.profile

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.diploma.APP_ACTIVITY
import com.example.diploma.APP_CONTEXT
import com.example.diploma.database.CHILD_TASK_EQUIPMENT
import com.example.diploma.database.CHILD_TASK_MACHINES
import com.example.diploma.database.databaseTask
import com.example.diploma.databinding.FragmentProfileBinding
import com.example.diploma.replaceFragment
import com.example.diploma.worker.tasks.TaskFragment
import java.util.*

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private val taskFragment = TaskFragment()

    private val timerWork = Timer()
    private lateinit var dataHelperWork: DataHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dataHelperWork = DataHelper(APP_CONTEXT)

        val listOfSelectedEquipments = mutableListOf<String>()
        val listOfSelectedMachines = mutableListOf<String>()

        databaseTask.child(selectedTask).get().addOnSuccessListener {
            it.child(CHILD_TASK_EQUIPMENT).children.forEach { child ->
                listOfSelectedEquipments.add(child.value.toString())
            }
            it.child(CHILD_TASK_MACHINES).children.forEach { child ->
                listOfSelectedMachines.add(child.value.toString())
            }
        }

        binding.startWorkButton.setOnClickListener{

            if (selectedTask != "") {

                if (listOfSelectedEquipments.size <= countSelectedEquipments && listOfSelectedEquipments.size != 0){
                    if (listOfSelectedMachines.size <= countSelectedMachines && listOfSelectedMachines.size != 0){

                        it.isClickable = false
                        binding.finishWorkButton.isClickable = true

                        workerStartDoTask(selectedTask)
                        startStopActionWork(dataHelperWork, binding.startWorkButton)
                        countSelectedEquipments = 0
                        countSelectedMachines = 0

                    } else Toast.makeText(APP_ACTIVITY, "Не все станки были выбраны", Toast.LENGTH_SHORT).show()
                } else Toast.makeText(APP_ACTIVITY, "Не все оборудование было выбрано", Toast.LENGTH_SHORT).show()
            } else {
                replaceFragment(taskFragment, "Задачи")
                Toast.makeText(APP_ACTIVITY, "Необходимо выбрать задачу", Toast.LENGTH_SHORT).show()
            }
        }

        binding.finishWorkButton.setOnClickListener{
            it.isClickable = false
            binding.startWorkButton.isClickable = true
            workerFinishedDoTask(binding.timeWorkTV.text.toString())
            selectedTask = ""
            resetActionWork(dataHelperWork, binding.timeWorkTV, binding.startWorkButton)
        }

        onViewStartWork(dataHelperWork, binding.timeWorkTV, binding.startWorkButton)

        timerWork.scheduleAtFixedRate(TimeTaskWork(), 0, 500)

        binding.workerScanEquipment.setOnClickListener {
            scanCheckElements(listOfSelectedEquipments, listOfSelectedMachines, "equipment")
        }
        binding.workerScanMachine.setOnClickListener {
            scanCheckElements(listOfSelectedEquipments, listOfSelectedMachines, "machine")
        }
    }

    private inner class TimeTaskWork: TimerTask() {
        override fun run() {
            if (dataHelperWork.timerCountingWork()) {
                val time = Date().time - dataHelperWork.startTimeWork()!!.time
                APP_ACTIVITY.runOnUiThread(java.lang.Runnable {
                    binding.timeWorkTV.text = timeStringFromLong(time)
                })
            }
        }
    }
}