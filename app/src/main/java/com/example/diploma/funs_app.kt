package com.example.diploma

import android.annotation.SuppressLint
import android.content.Context
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

lateinit var APP_ACTIVITY: MainActivity
@SuppressLint("StaticFieldLeak")
lateinit var APP_CONTEXT: Context
lateinit var APP_FRAGMENT_MANAGER: FragmentManager
lateinit var drawLayout: DrawerLayout

fun replaceFragment(fragment: Fragment, title: String) {
    val transaction = APP_FRAGMENT_MANAGER.beginTransaction()
    transaction.replace(R.id.fragmentContainer, fragment)
    transaction.commit()
    APP_ACTIVITY.title = title
    drawLayout.closeDrawers()
}