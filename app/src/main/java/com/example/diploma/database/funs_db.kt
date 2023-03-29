package com.example.diploma.database

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

fun initFirebase(){
    firebaseAuth = FirebaseAuth.getInstance()
    firebaseUID = firebaseAuth.currentUser?.uid.toString()

    databaseUsers = FirebaseDatabase.getInstance().getReference(ROOT_USERS)
    databaseTask = FirebaseDatabase.getInstance().getReference(ROOT_TASK)
    databaseMachines = FirebaseDatabase.getInstance().getReference(ROOT_MACHINE)
    databaseUsernames = FirebaseDatabase.getInstance().getReference(ROOT_USERNAME)
    databaseEquipment = FirebaseDatabase.getInstance().getReference(ROOT_EQUIPMENT)
}