package com.example.chatapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.chatapp.Adapter.ParticipantsAddAdapter
import com.example.chatapp.databinding.ActivityGroupParticipantAddBinding
import com.example.chatapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class GroupParticipantAddActivity : AppCompatActivity() {
    lateinit var binding: ActivityGroupParticipantAddBinding
    lateinit var firebaseAuth: FirebaseAuth
    private lateinit var groupId: String
    lateinit var myGroupRole: String
    lateinit var userList: ArrayList<User>
    lateinit var participantsAddAdapter: ParticipantsAddAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupParticipantAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Add Participants"
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        firebaseAuth = FirebaseAuth.getInstance()
        groupId = intent.getStringExtra("groupId").toString()
        loadGroupInfo()
    }

    private fun getAllUsers() {
        userList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(ds in snapshot.children){
                    val user = ds.getValue(User::class.java)

                    if (!firebaseAuth.uid.equals(user?.uid)){
                        userList.add(user!!)
                    }
                }
                participantsAddAdapter = ParticipantsAddAdapter(this@GroupParticipantAddActivity, userList, groupId, myGroupRole)
                binding.userRv.adapter = participantsAddAdapter
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    private fun loadGroupInfo() {
        val ref1 = FirebaseDatabase.getInstance().getReference("Groups")

        val ref = FirebaseDatabase.getInstance().getReference("Groups")
        ref.orderByChild("groupId").equalTo(groupId)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children){
                        val groupId = ds.child("groupId").value
                        val groupTitle = ds.child("groupTitle").value
                        val groupDescription = ds.child("groupDescription").value
                        val createdBy = ds.child("createdBy").value
                        val timeStamp = ds.child("timeStamp").value

                        ref1.child(groupId.toString()).child("Participants").child(firebaseAuth.uid.toString())
                            .addValueEventListener(object: ValueEventListener{
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()){
                                        myGroupRole = snapshot.child("role").value.toString()
                                        actionBar?.title = groupTitle.toString() + "("+ myGroupRole + ")"

                                        getAllUsers()

                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                }

                            })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private fun checkOnlineStatus(status: String) {
        val dbRef = FirebaseDatabase.getInstance().getReference("Users").child(firebaseAuth.currentUser?.uid.toString())
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