package com.example.chatapp

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.example.chatapp.databinding.ActivityCreateGroupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class CreateGroupActivity : AppCompatActivity() {
    lateinit var binding: ActivityCreateGroupBinding
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var myUid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateGroupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Create group"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        firebaseAuth = FirebaseAuth.getInstance()
        myUid = firebaseAuth.currentUser?.uid.toString()
        checkUser()

        binding.creatingGroupBtn.setOnClickListener {
            startCreatingGroup()
        }

    }

    private fun startCreatingGroup() {
//        val progressDialog = ProgressDialog(this)
//        progressDialog.setMessage("Creating group")
//        progressDialog.show()

        val groupTitle: String = binding.groupTitleEt.text.toString().trim()
        val groupDescription: String = binding.groupDescriptionEt.text.toString().trim()

        if (TextUtils.isEmpty(groupTitle)) {
            Toast.makeText(this, "Please enter group title", Toast.LENGTH_SHORT).show()
            return
        }

        val timeStamp = System.currentTimeMillis().toString()

        val hashMap = HashMap<String, String>()
        hashMap["groupId"] = "" + timeStamp
        hashMap["groupTitle"] = "" + groupTitle
        hashMap["groupDescription"] = "" + groupDescription
        hashMap["timeStamp"] = "" + timeStamp
        hashMap["createdBy"] = "" + firebaseAuth.uid.toString()

        val ref = FirebaseDatabase.getInstance().getReference("Groups")
        ref.child(timeStamp).setValue(hashMap)
            .addOnSuccessListener {
                val hashmap1 = HashMap<String, String>()
                hashmap1["uid"] = firebaseAuth.uid.toString()
                hashmap1["role"] = "creator"
                hashmap1["timeStamp"] = "" + timeStamp

                val ref1 = FirebaseDatabase.getInstance().getReference("Groups")
                ref1.child(timeStamp).child("Participants").child(firebaseAuth.uid.toString())
                    .setValue(hashmap1).addOnSuccessListener {
                        Toast.makeText(this, "Group created...", Toast.LENGTH_SHORT).show()
                        finish()
//                        progressDialog.dismiss()
                    }.addOnFailureListener {
//                        progressDialog.dismiss()
                        Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                    }
            }.addOnFailureListener {
//                progressDialog.dismiss()
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkUser() {
        val user = firebaseAuth.currentUser
        if (user != null) {
            supportActionBar?.subtitle = user.email
        }
    }

    override fun onNavigateUp(): Boolean {
        onBackPressed()
        return super.onNavigateUp()
    }

    private fun checkOnlineStatus(status: String) {
        val dbRef = FirebaseDatabase.getInstance().getReference("Users").child(myUid)
        val hashMap = HashMap<String, Any>()
        hashMap["onlineStatus"] = status

        dbRef.updateChildren(hashMap)
    }

    override fun onStart() {
        checkOnlineStatus("online")
        super.onStart()
    }

    override fun onResume() {
        checkOnlineStatus("online")
        super.onResume()
    }
}