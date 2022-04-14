package com.example.chatapp.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.databinding.GroupChatLeftBinding
import com.example.chatapp.databinding.GroupChatRightBinding
import com.example.chatapp.models.GroupChatModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.collections.ArrayList

class GroupChatAdapter(val context: Context, private val modelGroupChatList: ArrayList<GroupChatModel>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val MSG_TYPE_LEFT = 0
    val MSG_TYPE_RIGHT = 1
    lateinit var firebaseAuth: FirebaseAuth
    var user: FirebaseUser? = null

    val ref = FirebaseDatabase.getInstance().getReference("Users")

    inner class VhRight(private val groupChatRightBinding: GroupChatRightBinding) :
        RecyclerView.ViewHolder(groupChatRightBinding.root) {
        fun onBind(groupChatModel: GroupChatModel, position: Int) {

            groupChatRightBinding.messageTv.text = groupChatModel.message
            groupChatRightBinding.timeTv.text = groupChatModel.timeStamp
        }
    }

    inner class VhLeft(private val groupChatLeftBinding: GroupChatLeftBinding) :
        RecyclerView.ViewHolder(groupChatLeftBinding.root) {
        fun onBind(groupChatModel: GroupChatModel) {
            groupChatLeftBinding.messageTv.text = groupChatModel.message
            groupChatLeftBinding.timeTv.text = groupChatModel.timeStamp

            ref.orderByChild("uid").equalTo(groupChatModel.sender)
                .addValueEventListener(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (ds in snapshot.children){
                            val name = ds.child("username").value
                            groupChatLeftBinding.nameTv.text = name.toString()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == MSG_TYPE_RIGHT) {
            VhRight(
                GroupChatRightBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        } else {
            VhLeft(GroupChatLeftBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == 1) {
            val fromVh = holder as VhRight
            fromVh.onBind(modelGroupChatList[position], position)

        } else {
            val toVh = holder as VhLeft
            toVh.onBind(modelGroupChatList[position])
        }
    }

    override fun getItemCount(): Int {
        return modelGroupChatList.size
    }

    override fun getItemViewType(position: Int): Int {
        user = FirebaseAuth.getInstance().currentUser

        return if (modelGroupChatList[position].sender.equals(user?.uid)) {
            MSG_TYPE_RIGHT
        } else {
            MSG_TYPE_LEFT
        }
    }
}