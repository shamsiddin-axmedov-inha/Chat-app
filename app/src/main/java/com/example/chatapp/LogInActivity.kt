package com.example.chatapp

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import com.example.chatapp.databinding.ActivityLogInBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.lang.Exception

class LogInActivity : AppCompatActivity() {
    lateinit var binding: ActivityLogInBinding
    lateinit var mAuth: FirebaseAuth
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var reference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarLogin)
        supportActionBar!!.title = "Login"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        binding.toolbarLogin.setNavigationOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        mAuth = FirebaseAuth.getInstance()
        binding.btnLogin.setOnClickListener {
            loginUser()
        }

        binding.forgotPasswordTv.setOnClickListener {
            showRecoverPasswordDialog()
        }
    }

    private fun showRecoverPasswordDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Recover password")

        val linearLayout = LinearLayout(this)
        val editEt = EditText(this)
        editEt.hint = "Email"
        editEt.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS

        editEt.minEms = 16

        linearLayout.addView(editEt)
        linearLayout.setPadding(10, 10, 10, 10)

        builder.setView(linearLayout)

        builder.setPositiveButton("Recover", object : DialogInterface.OnClickListener {
            override fun onClick(p0: DialogInterface?, p1: Int) {
                val email = editEt.text.toString().trim()
                beginRecovery(email)
            }
        })
        builder.setNegativeButton("Cancel", object : DialogInterface.OnClickListener {
            override fun onClick(p0: DialogInterface?, p1: Int) {
                p0?.dismiss()
            }
        })

        builder.create().show()
    }

    private fun beginRecovery(email: String) {

        mAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener(object : OnCompleteListener<Void>{
                override fun onComplete(p0: Task<Void>) {
                    if (p0.isSuccessful){
                        Toast.makeText(this@LogInActivity, "Email send", Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(this@LogInActivity, "Failed...", Toast.LENGTH_SHORT).show()
                    }
                }
            }).addOnFailureListener(object : OnFailureListener{
                override fun onFailure(p0: Exception) {
                    Toast.makeText(this@LogInActivity, p0.message, Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }


    private fun loginUser() {
        val email = binding.emailLogin.text.toString()
        val password = binding.passwordLogin.text.toString()

        when {
            email == "" -> {
                Toast.makeText(this, "Please, write email", Toast.LENGTH_SHORT).show()
            }
            password == "" -> {
                Toast.makeText(this, "Please, write password", Toast.LENGTH_SHORT).show()
            }
            else -> {
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = mAuth.currentUser

                        firebaseAuth = FirebaseAuth.getInstance()
                        firebaseDatabase = FirebaseDatabase.getInstance()
                        reference = firebaseDatabase.getReference("Users")


                        val userQuery = reference.orderByChild("uid").equalTo(user?.uid)

                        userQuery.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                for (child in snapshot.children) {
                                    val name = "" + child.child("username").value

                                    val email: String = user?.email.toString()
                                    val uid: String = user?.uid.toString()
                                    val hashMap = HashMap<Any, String>()
                                    hashMap["email"] = email
                                    hashMap["uid"] = uid
                                    hashMap["username"] = name
                                    hashMap["onlineStatus"] = "online"
                                    hashMap["typingTo"] = "noOne"


                                    val database = FirebaseDatabase.getInstance()
                                    val reference = database.getReference("Users")

                                    reference.child(uid).setValue(hashMap)

                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                            }

                        })

                        val intent = Intent(this, DashboardActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()

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