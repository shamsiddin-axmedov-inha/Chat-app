package com.example.chatapp.Adapter

import android.opengl.Visibility
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.databinding.ChatLeftBinding
import com.example.chatapp.databinding.ChatRightBinding
import com.example.chatapp.models.ChatModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MessageAdapter(val chatList: List<ChatModel>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val MSG_TYPE_LEFT = 0
    val MSG_TYPE_RIGHT = 1
    var user: FirebaseUser? = null


    inner class VhRight(private val rightBinding: ChatRightBinding) :
        RecyclerView.ViewHolder(rightBinding.root) {
        fun onBind(chatModel: ChatModel, position: Int) {
            rightBinding.timeTv.text = chatModel.time
            rightBinding.messageTv.text = chatModel.message
            rightBinding.isSeenTv.text = chatModel.isSeen.toString()

            val reference = FirebaseDatabase.getInstance().getReference("Chats")
            reference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children) {
                        if (ds.child("receiver").value!! == chatModel.receiver && ds.child("sender").value!! == chatModel.sender ||
                            ds.child("receiver").value!! == chatModel.sender && ds.child("sender").value!! == chatModel.receiver
                        ){
                            if (ds.child("isSeen").value == "true"){
                                rightBinding.isSeenTv.text = "D"
                            }else{
                                rightBinding.isSeenTv.text = "B"
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })

//            if (position == chatList.size - 1) {
//                if (chatModel.isSeen == "true") {
//                    rightBinding.isSeenTv.text = "1"
//                } else {
//                    rightBinding.isSeenTv.text = "2"
//                }
//            } else {
//                rightBinding.isSeenTv.text = "3"
//            }
        }
    }

    inner class VhLeft(private val leftBinding: ChatLeftBinding) :
        RecyclerView.ViewHolder(leftBinding.root) {
        fun onBind(chatModel: ChatModel) {
            leftBinding.messageTv.text = chatModel.message
            leftBinding.timeTv.text = chatModel.time
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == MSG_TYPE_RIGHT) {
            VhRight(
                ChatRightBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        } else {
            VhLeft(ChatLeftBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == 1) {
            val fromVh = holder as VhRight
            fromVh.onBind(chatList[position], position)

        } else {
            val toVh = holder as VhLeft
            toVh.onBind(chatList[position])
        }
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun getItemViewType(position: Int): Int {
        user = FirebaseAuth.getInstance().currentUser

        return if (chatList[position].sender.equals(user?.uid)) {
            MSG_TYPE_RIGHT
        } else {
            MSG_TYPE_LEFT
        }
    }
}