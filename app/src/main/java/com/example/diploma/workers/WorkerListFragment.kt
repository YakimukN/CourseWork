package com.example.diploma.workers

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diploma.APP_ACTIVITY
import com.example.diploma.R
import com.example.diploma.data.User
import com.example.diploma.database.databaseUsers
import com.example.diploma.databinding.FragmentWorkerListBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.util.*

class WorkerListFragment : Fragment() {

    private lateinit var binding: FragmentWorkerListBinding

    private lateinit var userRecyclerview: RecyclerView
    private lateinit var userArrayList: ArrayList<User>
    private lateinit var tempUserArrayList: ArrayList<User>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWorkerListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        userRecyclerview = APP_ACTIVITY.findViewById(R.id.workersList)
        userRecyclerview.layoutManager = LinearLayoutManager(APP_ACTIVITY)
        userRecyclerview.setHasFixedSize(true)

        getUserData()
        userArrayList = arrayListOf()
        tempUserArrayList = arrayListOf()
    }

    private fun getUserData() {
        databaseUsers.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userArrayList.clear()
                tempUserArrayList.clear()

                if (snapshot.exists()){
                    for (userSnapshot in snapshot.children){
                        val user = userSnapshot.getValue(User::class.java)
                        userArrayList.add(user!!)
                    }

                    tempUserArrayList.addAll(userArrayList)

                    val adapter = UserAdapter(tempUserArrayList) // OrderAdapter(orderArrayList)
                    userRecyclerview.adapter = adapter

                    adapter.setOnClickListener(object : UserAdapter.onItemClickListener{
                        override fun onItemClick(position: Int) {
                            showInfoAboutUser(tempUserArrayList[position]) // showInfoAboutOrder(orderArrayList[position])
                        }
                    })
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun showInfoAboutUser(user: User){
        val inflter = LayoutInflater.from(APP_ACTIVITY)
        val myView = inflter.inflate(R.layout.user_info_item, null)

        myView.findViewById<TextView>(R.id.userInfoTitle).text = user.username
        myView.findViewById<TextView>(R.id.userHoursWork).text = user.hoursWorked
        myView.findViewById<TextView>(R.id.userCurrentOrder).text = user.currentTask
        myView.findViewById<TextView>(R.id.userAverageWorkingTime).text = user.averageWorkingTime

//        myView.findViewById<TextView>(R.id.userFailedOrders).text = user.failedOrder.toString()

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
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onQueryTextChange(newText: String?): Boolean {
                tempUserArrayList.clear()
                val searchText = newText!!.lowercase(Locale.getDefault())
                if (searchText.isNotEmpty()){
                    userArrayList.forEach {
                        if (it.username.lowercase(Locale.getDefault()).contains(searchText)){
                            tempUserArrayList.add(it)
                        }
                    }
                    userRecyclerview.adapter?.notifyDataSetChanged()
                } else {
                    tempUserArrayList.clear()
                    tempUserArrayList.addAll(userArrayList)
                    userRecyclerview.adapter?.notifyDataSetChanged()
                }
                return false
            }
        })
        super.onCreateOptionsMenu(menu, inflater)
    }
}