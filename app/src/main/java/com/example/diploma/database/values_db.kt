package com.example.diploma.database

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference

lateinit var databaseTask: DatabaseReference
lateinit var databaseUsers: DatabaseReference
lateinit var databaseMachines: DatabaseReference
lateinit var databaseEquipment: DatabaseReference
lateinit var databaseUsernames: DatabaseReference

lateinit var firebaseUID: String
lateinit var firebaseAuth: FirebaseAuth

lateinit var usernameData: String

// является ли сотрудник админом
var userIsAdmin = false