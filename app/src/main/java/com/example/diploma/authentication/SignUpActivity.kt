package com.example.diploma.authentication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import com.example.diploma.databinding.ActivitySignUpBinding
import com.example.diploma.MainActivity
import com.example.diploma.data.User
import com.example.diploma.database.databaseUsernames
import com.example.diploma.database.databaseUsers
import com.example.diploma.database.firebaseAuth
import com.example.diploma.database.initFirebase

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding

    private var email = ""
    private var password = ""
    private var username = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initFirebase()

        binding.signUpBtn.setOnClickListener {
            checkUsername()        //            validateData()
        }

        binding.noAccountTv.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun validateData(originalUsername: Int) {
        val repeatPassword = binding.passwordRepeatEt.text.toString().trim()

        if (originalUsername == 0) {
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.emailEt.error = "Неверный формат"
            } else if (TextUtils.isEmpty(password)) {
                binding.passwordEt.error = "Введите пароль"
            } else if (password.length < 6) {
                binding.passwordEt.error = "Пароль должен быть больше 6 символов"
            } else if (repeatPassword != password) {
                binding.passwordRepeatEt.error = "Пароли не совпадают"
            } else if (username.isEmpty()) {
                binding.usernameEt.error = "Введите фамилию и имя"
            } else {
                Log.i("tag", "создан новый аккаунт $username")
                firebaseSignUp(email)
            }
        } else binding.usernameEt.error = "Данный пользователь уже существует"
    }

    private fun firebaseSignUp(email: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                var uidUser = firebaseAuth.currentUser?.uid.toString()
                val user = User(uid=uidUser, email=email, username=username)
                databaseUsers.child(uidUser).setValue(user)
                databaseUsernames.child(username).setValue(username)

                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "SignUp failed due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private fun checkUsername(){
        databaseUsernames.get().addOnSuccessListener {
            var originalUsername = 0
            email = binding.emailEt.text.toString().trim()
            password = binding.passwordEt.text.toString().trim()
            username = binding.usernameEt.text.toString().trim()
            for (i in 0 until it.children.reversed().size){
//                Log.i("tag", "it = ${it.children.reversed()[i].toString()}")
                if (it.children.reversed()[i].value.toString() == username){
                    originalUsername += 1
                }
            }
            validateData(originalUsername)
        }.addOnFailureListener { e ->
            Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}