package com.example.diploma.machine

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diploma.APP_ACTIVITY
import com.example.diploma.R
import com.example.diploma.admin.tasks.*
import com.example.diploma.data.Machine
import com.example.diploma.database.ROOT_MACHINE
import com.example.diploma.database.databaseEquipment
import com.example.diploma.database.databaseMachines
import com.example.diploma.database.userIsAdmin
import com.example.diploma.databinding.FragmentMachineBinding
import com.example.diploma.replaceFragment
import com.example.diploma.scanner.ScannerFragment
import com.example.diploma.scanner.fromWhatFragment
import com.example.diploma.scanner.textFromQRCode
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.util.*
import kotlin.collections.ArrayList

class MachineFragment : Fragment() {

    private lateinit var binding: FragmentMachineBinding

    private lateinit var machineRecyclerView: RecyclerView
    private lateinit var machineArrayList: ArrayList<Machine>
    private lateinit var tempMachineArrayList: ArrayList<Machine>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMachineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.addBtnMachine.isVisible = userIsAdmin
        setHasOptionsMenu(true)
        machineRecyclerView = APP_ACTIVITY.findViewById(R.id.machineList)
        machineRecyclerView.layoutManager = LinearLayoutManager(APP_ACTIVITY)
        machineRecyclerView.setHasFixedSize(true)

        textFromQRCode = ""

        getMachineData()
        machineArrayList = arrayListOf()
        tempMachineArrayList = arrayListOf()

        binding.addBtnMachine.setOnClickListener {
//            fromWhatFragment = ROOT_MACHINE
//            replaceFragment(ScannerFragment(), "Scanner")
            scanQRCode("станки", listOfMachines, checkedMachines, databaseMachines)
        }
    }

    private fun getMachineData() {
        databaseMachines.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                machineArrayList.clear()
                tempMachineArrayList.clear()

                if (snapshot.exists()){
                    for (machineSnapshot in snapshot.children){
                        val machine = machineSnapshot.getValue(Machine::class.java)
                        machineArrayList.add(machine!!)
                    }
                    tempMachineArrayList.addAll(machineArrayList)

                    val adapter = AdapterMachine(tempMachineArrayList)
                    machineRecyclerView.adapter = adapter

                    adapter.setOnClickListener(object : AdapterMachine.onItemClickListener{
                        override fun onItemClick(position: Int) {
                            showInfoAboutMachine(tempMachineArrayList[position])
                        }
                    })
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun showInfoAboutMachine(machine: Machine){
        val inflter = LayoutInflater.from(APP_ACTIVITY)
        val myView = inflter.inflate(R.layout.info_item_equipment, null)

        myView.findViewById<TextView>(R.id.titleInfo).text = machine.title
        myView.findViewById<TextView>(R.id.usedTimesInfo).text = machine.usedTimes.toString()
        myView.findViewById<TextView>(R.id.lastUsernameInfo).text = machine.lastUser

        AlertDialog.Builder(APP_ACTIVITY)
            .setView(myView)
            .setPositiveButton("Ok"){ dialog,_-> dialog.dismiss() }
            .create()
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(R.menu.search_menu, menu)
        val item = menu.findItem(R.id.search_action)
        val searchView = item?.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean { return true }

            @SuppressLint("NotifyDataSetChanged")
            override fun onQueryTextChange(newText: String?): Boolean {
                tempMachineArrayList.clear()
                val searchText = newText!!.lowercase(Locale.getDefault())
                if (searchText.isNotEmpty()){
                    machineArrayList.forEach {
                        if (it.title.lowercase(Locale.getDefault()).contains(searchText)){
                            tempMachineArrayList.add(it)
                        }
                    }
                    machineRecyclerView.adapter?.notifyDataSetChanged()
                } else {
                    tempMachineArrayList.clear()
                    tempMachineArrayList.addAll(machineArrayList)
                    machineRecyclerView.adapter?.notifyDataSetChanged()
                }
                return false
            }
        })
        super.onCreateOptionsMenu(menu, inflater)
    }
}