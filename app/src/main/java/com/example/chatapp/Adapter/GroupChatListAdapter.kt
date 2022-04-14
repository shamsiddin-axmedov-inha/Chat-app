package com.example.chatapp.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.GroupChatActivity
import com.example.chatapp.databinding.ItemGroupChatsListBinding
import com.example.chatapp.models.GroupChatModelList
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class GroupChatListAdapter(val context: Context?, private val groupChatList: List<GroupChatModelList>): RecyclerView.Adapter<GroupChatListAdapter.Vh>() {

    inner class Vh(private val itemGroupChatsListBinding: ItemGroupChatsListBinding): RecyclerView.ViewHolder(itemGroupChatsListBinding.root){
        fun onBind(groupChatModelList: GroupChatModelList) {
            itemGroupChatsListBinding.groupTitleTv.text = groupChatModelList.groupTitle

            itemGroupChatsListBinding.root.setOnClickListener {
                val intent = Intent(context, GroupChatActivity::class.java)
                intent.putExtra("groupId", groupChatModelList.groupId)
                context?.startActivity(intent)
            }

            val reference = FirebaseDatabase.getInstance().getReference("Groups")
            reference.child(groupChatModelList.groupId!!).child("Message").limitToLast(1).addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children){
                        val message = ds.child("message").value
                        val timeStamp = ds.child("timeStamp").value
                        val sender = ds.child("sender").value.toString()

                        itemGroupChatsListBinding.messageTv.text = message.toString()
                        itemGroupChatsListBinding.timeTv.text = timeStamp.toString()

                        val ref = FirebaseDatabase.getInstance().getReference("Users")
                        ref.orderByChild("uid").equalTo(sender)
                            .addValueEventListener(object : ValueEventListener{
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    for (d in snapshot.children){
                                        val name = d.child("username").value
                                        itemGroupChatsListBinding.nameTv.text = name.toString()
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                }
                            })
                        itemGroupChatsListBinding.nameTv.text = sender
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        return Vh(ItemGroupChatsListBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.onBind(groupChatList[position])
    }

    override fun getItemCount(): Int = groupChatList.size

}