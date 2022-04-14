package com.example.chatapp.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R
import com.example.chatapp.databinding.ItemUserBinding
import com.example.chatapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class ChatAdapter(private val list: List<User>, val onItemClickListener: OnItemClickListener) :
    RecyclerView.Adapter<ChatAdapter.Vh>() {

    inner class Vh(private val itemUserBinding: ItemUserBinding) :
        RecyclerView.ViewHolder(itemUserBinding.root) {
        fun onBind(user: User) {
            itemUserBinding.nameTv.text = user.username

            itemView.setOnClickListener {
                onItemClickListener.onItemClick(user)
            }

            val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString()
            val hisUid = user.uid

            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.orderByChild("uid").equalTo(user.uid.toString())
                .addValueEventListener(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (ds in snapshot.children){
                            val status = ds.child("onlineStatus").value.toString()
                            if (status == "online"){
                                itemUserBinding.status.visibility = View.VISIBLE
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })

            val reference = FirebaseDatabase.getInstance().getReference("Chats")
            reference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children) {
                        if (ds.child("receiver").value!! == myUid && ds.child("sender").value!! == hisUid ||
                            ds.child("receiver").value!! == hisUid && ds.child("sender").value!! == myUid
                        ){
                            val m = ds.child("message").value
                            itemUserBinding.emailTv.text = m.toString()

                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        return Vh(ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.onBind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    interface OnItemClickListener {
        fun onItemClick(user: User)
    }
}