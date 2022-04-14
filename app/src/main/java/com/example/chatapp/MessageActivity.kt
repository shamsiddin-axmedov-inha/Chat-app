package com.example.chatapp

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.Adapter.MessageAdapter
import com.example.chatapp.databinding.ActivityMessageBinding
import com.example.chatapp.models.ChatModel
import com.google.android.gms.common.util.Strings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MessageActivity : AppCompatActivity() {
    lateinit var binding: ActivityMessageBinding
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var reference: DatabaseReference
    lateinit var hisUid: String
    lateinit var myUid: String
    lateinit var seenListener: ValueEventListener
    lateinit var userRefForSeen: DatabaseReference

    lateinit var chatList: ArrayList<ChatModel>
    lateinit var messageAdapter: MessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        hisUid = intent.getStringExtra("hisUid").toString()

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        reference = firebaseDatabase.getReference("Users")


        val userQuery = reference.orderByChild("uid").equalTo(hisUid)

        userQuery.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    val name = "" + child.child("username").value
                    val onlineStatus = "" + child.child("onlineStatus").value
                    val typingStatus = "" + child.child("typingTo").value

                    binding.nameTv.text = name

                    if (typingStatus == myUid) {
                        binding.userStatusTv.text = "typing..."
                    } else {
                        if (onlineStatus == "online") {
                            binding.userStatusTv.text = onlineStatus
                        }
                        else {
                            binding.userStatusTv.text = "Last seen at $onlineStatus"
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

        binding.sendBtn.setOnClickListener {
            val message = binding.messageEd.text.toString().trim()
            if (!TextUtils.isEmpty(message)) {
                sendMessage(message)
            }
        }

        binding.rv.setHasFixedSize(true)
        binding.messageEd.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0.toString().trim().isEmpty()) {
                    checkTypingStatus("noOne")
                } else {
                    checkTypingStatus(hisUid)
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })

        checkUserStatus()
        readMessages()
        seenMessage()

    }

    private fun seenMessage() {
        userRefForSeen = FirebaseDatabase.getInstance().getReference("Chats")
        seenListener = userRefForSeen.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (ds in snapshot.children) {
                    val chat = ds.getValue(ChatModel::class.java)
                    if (chat?.receiver.equals(myUid) && chat?.sender.equals(hisUid)) {
                        val hasSeen = HashMap<String, Any>()
                        hasSeen["isSeen"] = "true"
                        ds.ref.updateChildren(hasSeen)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    override fun onStart() {
        checkUserStatus()
        checkOnlineStatus("online")
        super.onStart()
    }

    override fun onPause() {
        super.onPause()
        userRefForSeen.removeEventListener(seenListener)
        checkTypingStatus("noOne")

//        val simpleDateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm")
//        val time = simpleDateFormat.format(Date())
//        checkOnlineStatus(time)
    }

    override fun onResume() {
        checkOnlineStatus("online")
        super.onResume()
    }


    private fun sendMessage(message: String) {
        val databaseReference = FirebaseDatabase.getInstance().reference
        val simpleDateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm")
        val time = simpleDateFormat.format(Date())

        val hashMap = HashMap<String, Any>()
        hashMap["sender"] = myUid
        hashMap["receiver"] = hisUid
        hashMap["message"] = message
        hashMap["time"] = time
        hashMap["isSeen"] = "false"
        databaseReference.child("Chats").push().setValue(hashMap)

        binding.messageEd.text.clear()
    }

    private fun readMessages() {
        chatList = ArrayList()
        val dbRef = FirebaseDatabase.getInstance().getReference("Chats")
        dbRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                chatList.clear()
                for (ds in snapshot.children) {
                    val chat: ChatModel? = ds.getValue(ChatModel::class.java)
                    if ( chat?.receiver.equals(myUid) && chat?.sender.equals(hisUid) ||
                        chat?.receiver.equals(hisUid) && chat?.sender.equals(myUid)
                    ) {
                        chatList.add(chat!!)
                    }
                    messageAdapter = MessageAdapter(chatList)
                    messageAdapter.notifyDataSetChanged()
                    binding.rv.adapter = messageAdapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun checkUserStatus() {
        val user = firebaseAuth.currentUser
        if (user != null) {
            myUid = user.uid
        } else {
            startActivity(Intent(this, WelcomeActivity::class.java))
        }
    }

    private fun checkOnlineStatus(status: String) {
        val dbRef = FirebaseDatabase.getInstance().getReference("Users").child(myUid)
        val hashMap = HashMap<String, Any>()
        hashMap["onlineStatus"] = status

        dbRef.updateChildren(hashMap)
    }

    private fun checkTypingStatus(typing: String) {
        val dbRef = FirebaseDatabase.getInstance().getReference("Users").child(myUid)
        val hashMap = HashMap<String, Any>()
        hashMap["typingTo"] = typing
        dbRef.updateChildren(hashMap)
    }
}