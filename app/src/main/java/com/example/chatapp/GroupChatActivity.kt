package com.example.chatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.chatapp.Adapter.GroupChatAdapter
import com.example.chatapp.databinding.ActivityGroupChatBinding
import com.example.chatapp.models.GroupChatModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class GroupChatActivity : AppCompatActivity() {
    lateinit var binding: ActivityGroupChatBinding
    lateinit var groupId: String
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var groupChatList: ArrayList<GroupChatModel>
    lateinit var groupChatAdapter: GroupChatAdapter

    lateinit var myGroupRole: String
    lateinit var myUid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        groupId = intent.getStringExtra("groupId").toString()
        firebaseAuth = FirebaseAuth.getInstance()
        myUid = firebaseAuth.currentUser?.uid.toString()

        loadGroupInfo()
        loadGroupMessages()
        loadMyGroupRole()


        binding.sendBtn.setOnClickListener {
            val message = binding.messageEt.text.toString().trim()

            if (!TextUtils.isEmpty(message)) {
                sendMessage(message)
            }
        }

        binding.addParticipantsBtn.setOnClickListener {
            val intent = Intent(this, GroupParticipantAddActivity::class.java)
            intent.putExtra("groupId", groupId)
            startActivity(intent)
        }
    }

    private fun loadMyGroupRole() {
        val ref = FirebaseDatabase.getInstance().getReference("Groups")
        ref.child(groupId).child("Participants").orderByChild("uid").equalTo(firebaseAuth.uid)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children){
                        myGroupRole = ds.child("role").value.toString()

                        if (myGroupRole == "creator"){
                            binding.addParticipantsBtn.visibility = View.VISIBLE
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun loadGroupMessages() {
        val ref = FirebaseDatabase.getInstance().getReference("Groups")
        ref.child(groupId).child("Message")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    groupChatList.clear()
                    for (ds in snapshot.children){
                        val model = ds.getValue(GroupChatModel::class.java)
                        groupChatList.add(model!!)
                    }

                    groupChatAdapter = GroupChatAdapter(this@GroupChatActivity, groupChatList)

                    binding.rv.adapter = groupChatAdapter
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }

    private fun sendMessage(message: String) {
        val timeStamp = System.currentTimeMillis().toString()

        val simpleDateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm")
        val time = simpleDateFormat.format(Date())

        val hashMap = HashMap<String, Any>()
        hashMap["sender"] = firebaseAuth.uid.toString()
        hashMap["message"] = message
        hashMap["timeStamp"] = time
        hashMap["type"] = "text"

        val ref = FirebaseDatabase.getInstance().getReference("Groups")
        ref.child(groupId).child("Message").child(timeStamp).setValue(hashMap)
            .addOnSuccessListener {
                binding.messageEt.text.clear()
            }.addOnFailureListener {
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadGroupInfo() {
        groupChatList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Groups")
        ref.orderByChild("groupId").equalTo(groupId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children) {
                        val groupTitle = ds.child("groupTitle").value
                        var groupDescription = ds.child("groupDescription").value
                        var timeStamp = ds.child("timeStamp").value
                        var createdBy = ds.child("createdBy").value

                        binding.groupTitleTv.text = groupTitle.toString()

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
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