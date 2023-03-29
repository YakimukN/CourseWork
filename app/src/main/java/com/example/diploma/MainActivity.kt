package com.example.diploma

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import com.example.diploma.admin.profile.ProfileAdminFragment
import com.example.diploma.admin.tasks.TaskAdminFragment
import com.example.diploma.databinding.ActivityMainBinding
import com.example.diploma.authentication.LoginActivity
import com.example.diploma.database.*
import com.example.diploma.equipment.EquipmentFragment
import com.example.diploma.help.HelpFragment
import com.example.diploma.machine.MachineFragment
import com.example.diploma.worker.profile.ProfileFragment
import com.example.diploma.worker.tasks.TaskFragment
import com.example.diploma.workers.WorkerListFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle

    private var taskFragment = TaskFragment()
    private var profileFragment = ProfileFragment()
    private var machineFragment = MachineFragment()
    private var workersFragment = WorkerListFragment()
    private var equipmentFragment = EquipmentFragment()
    private var taskAdminFragment = TaskAdminFragment()
    private var profileAdminFragment = ProfileAdminFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        APP_ACTIVITY = this
        APP_CONTEXT = applicationContext
        APP_FRAGMENT_MANAGER = supportFragmentManager
        initFirebase()
        checkUser()

        drawLayout = binding.drawLayout
        toggle = ActionBarDrawerToggle(this, drawLayout, R.string.open, R.string.close)
        drawLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.navView.setNavigationItemSelectedListener {
            it.isChecked = true
            when (it.itemId){
                R.id.nav_workers -> replaceFragment(workersFragment, it.title.toString())
                R.id.nav_machines -> replaceFragment(machineFragment, it.title.toString())
                R.id.nav_equipments -> replaceFragment(equipmentFragment, it.title.toString())
                R.id.nav_tasks -> {
                    if (userIsAdmin)
                        replaceFragment(taskAdminFragment, it.title.toString())
                    else
                        replaceFragment(taskFragment, it.title.toString())
                }
                R.id.nav_help -> replaceFragment(HelpFragment(), it.title.toString())
                R.id.nav_logout -> {
                    firebaseAuth.signOut()
                    checkUser()
                }
            }
            true
        }
        binding.navView.getHeaderView(0).findViewById<TextView>(R.id.navViewIsAdmin).setOnClickListener {
            if(userIsAdmin)
                replaceFragment(profileAdminFragment, "Профиль")
            else replaceFragment(profileFragment, "Профиль")
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)){ return true }
        return super.onOptionsItemSelected(item)
    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser

        if (firebaseUser == null){
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            databaseUsers.child(firebaseUID).get().addOnSuccessListener {
                val name = binding.navView.getHeaderView(0).findViewById<TextView>(R.id.username)
                val isAdminTitle = binding.navView.getHeaderView(0).findViewById<TextView>(R.id.navViewIsAdmin)
                name.text = it.child(CHILD_USER_USERNAME).value.toString() //"new UserName"
                usernameData = name.text.toString()
//                Log.i("tag", it.child(CHILD_USER_ROLE).value.toString())
                if (it.child(CHILD_USER_ROLE).value.toString() == "admin") {
                    userIsAdmin = true
                    isAdminTitle.text = "Admin"
                    replaceFragment(taskAdminFragment, "Задачи")
                } else {
                    userIsAdmin = false
                    replaceFragment(taskFragment, "Задачи")
                }
            }
        }
    }
}