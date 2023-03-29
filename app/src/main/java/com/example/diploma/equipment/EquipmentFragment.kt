package com.example.diploma.equipment

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
import com.example.diploma.admin.tasks.checkedEquipments
import com.example.diploma.admin.tasks.listOfEquipments
import com.example.diploma.admin.tasks.scanQRCode
import com.example.diploma.data.Equipment
import com.example.diploma.database.ROOT_EQUIPMENT
import com.example.diploma.database.databaseEquipment
import com.example.diploma.database.userIsAdmin
import com.example.diploma.databinding.FragmentEquipmentBinding
import com.example.diploma.replaceFragment
import com.example.diploma.scanner.ScannerFragment
import com.example.diploma.scanner.fromWhatFragment
import com.example.diploma.scanner.textFromQRCode
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.util.*
import kotlin.collections.ArrayList

class EquipmentFragment : Fragment() {

    private lateinit var binding: FragmentEquipmentBinding

    private lateinit var equipmentRecyclerview: RecyclerView
    private lateinit var equipmentArrayList: ArrayList<Equipment>
    private lateinit var tempEquipmentArrayList: ArrayList<Equipment>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEquipmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.addBtnEquip.isVisible = userIsAdmin

        setHasOptionsMenu(true)
        equipmentRecyclerview = APP_ACTIVITY.findViewById(R.id.equipmentsList)
        equipmentRecyclerview.layoutManager = LinearLayoutManager(APP_ACTIVITY)
        equipmentRecyclerview.setHasFixedSize(true)
        textFromQRCode = ""

        getUserData()
        equipmentArrayList = arrayListOf()
        tempEquipmentArrayList = arrayListOf()

        binding.addBtnEquip.setOnClickListener {
//            fromWhatFragment = ROOT_EQUIPMENT
//            replaceFragment(ScannerFragment(), "Scanner")
            scanQRCode("оборудование", listOfEquipments, checkedEquipments, databaseEquipment)
        }
    }

    private fun getUserData() {
        databaseEquipment.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                equipmentArrayList.clear()
                tempEquipmentArrayList.clear()

                if (snapshot.exists()){
                    for (equipSnapshot in snapshot.children){
                        val equip = equipSnapshot.getValue(Equipment::class.java)
                        equipmentArrayList.add(equip!!)
                    }
                    tempEquipmentArrayList.addAll(equipmentArrayList)

                    val adapter = EquipmentAdapter(tempEquipmentArrayList)
                    equipmentRecyclerview.adapter = adapter

                    adapter.setOnClickListener(object : EquipmentAdapter.onItemClickListener{
                        override fun onItemClick(position: Int) {
                            showInfoAboutEquipment(tempEquipmentArrayList[position])
                        }
                    })
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun showInfoAboutEquipment(equipment: Equipment) {
        val inflter = LayoutInflater.from(APP_ACTIVITY)
        val myView = inflter.inflate(R.layout.info_item_equipment, null)

        myView.findViewById<TextView>(R.id.titleInfo).text = equipment.title
        myView.findViewById<TextView>(R.id.usedTimesInfo).text = equipment.usedTimes.toString()
        myView.findViewById<TextView>(R.id.lastUsernameInfo).text = equipment.lastUser

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
                tempEquipmentArrayList.clear()
                val searchText = newText!!.lowercase(Locale.getDefault())
                if (searchText.isNotEmpty()){
                    equipmentArrayList.forEach {
                        if (it.title.lowercase(Locale.getDefault()).contains(searchText)){
                            tempEquipmentArrayList.add(it)
                        }
                    }
                    equipmentRecyclerview.adapter?.notifyDataSetChanged()
                } else {
                    tempEquipmentArrayList.clear()
                    tempEquipmentArrayList.addAll(equipmentArrayList)
                    equipmentRecyclerview.adapter?.notifyDataSetChanged()
                }
                return false
            }
        })
        super.onCreateOptionsMenu(menu, inflater)
    }
}