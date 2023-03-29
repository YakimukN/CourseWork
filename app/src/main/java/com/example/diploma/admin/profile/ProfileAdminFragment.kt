package com.example.diploma.admin.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.diploma.database.CHILD_USER_USERNAME
import com.example.diploma.database.databaseUsers
import com.example.diploma.database.firebaseUID
import com.example.diploma.databinding.FragmentProfileAdminBinding

class ProfileAdminFragment : Fragment() {

    private lateinit var binding: FragmentProfileAdminBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        databaseUsers.child(firebaseUID).get().addOnSuccessListener {
            binding.usernameAdmin.text = it.child(CHILD_USER_USERNAME).value.toString()
        }
    }
}