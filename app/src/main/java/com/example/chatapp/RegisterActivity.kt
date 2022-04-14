package com.example.chatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.chatapp.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.*
import kotlin.collections.HashMap
import android.app.ProgressDialog as ProgressDialog

class RegisterActivity : AppCompatActivity() {
    lateinit var binding: ActivityRegisterBinding
    lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarRegister)
        supportActionBar!!.title = "Register"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        binding.toolbarRegister.setNavigationOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        mAuth = FirebaseAuth.getInstance()

        binding.btnRegister.setOnClickListener {
            registerUser()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }


    private fun registerUser() {

        val username = binding.usernameRegister.text.toString().trim()
        val email = binding.emailRegister.text.toString().trim()
        val password = binding.passwordRegister.text.toString().trim()

        when {
            username == "" -> {
                Toast.makeText(this, "Please, write username", Toast.LENGTH_SHORT).show()
            }
            email == "" -> {
                Toast.makeText(this, "Please, write email", Toast.LENGTH_SHORT).show()
            }
            password == "" -> {
                Toast.makeText(this, "Please, write password", Toast.LENGTH_SHORT).show()
            }
            else -> {
                mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = mAuth.currentUser
                            val username: String = username
                            val email: String = user?.email.toString()
                            val uid: String = user?.uid.toString()
                            val hashMap = HashMap<Any, String>()
                            hashMap["email"] = email
                            hashMap["uid"] = uid
                            hashMap["username"] = username
                            hashMap["onlineStatus"] = "online"
                            hashMap["typingTo"] = "noOne"

                            val database = FirebaseDatabase.getInstance()
                            val reference = database.getReference("Users")
                            reference.child(uid).setValue(hashMap)

                            if (user != null) {
                                Toast.makeText(
                                    this,
                                    "Registered...\n" + user.email,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            val intent = Intent(this, DashboardActivity::class.java)
                            startActivity(intent)

                        } else {
                            Toast.makeText(
                                this,
                                "Error message: " + task.exception!!.message.toString(),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }
    }
}
