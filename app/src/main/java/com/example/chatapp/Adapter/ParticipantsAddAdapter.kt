package com.example.chatapp.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.databinding.ItemParticipantAddBinding
import com.example.chatapp.models.User
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.lang.Exception

class ParticipantsAddAdapter(
    val context: Context,
    private val participantsList: ArrayList<User>,
    val groupId: String,
    val myGroupRule: String
) : RecyclerView.Adapter<ParticipantsAddAdapter.Vh>() {

    inner class Vh(private val itemParticipantAddBinding: ItemParticipantAddBinding) :
        RecyclerView.ViewHolder(itemParticipantAddBinding.root) {
        fun onBind(user: User) {
            itemParticipantAddBinding.nameTv.text = user.username
            itemParticipantAddBinding.emailTv.text = user.username

            val databaseReference = FirebaseDatabase.getInstance().getReference("Groups")
            databaseReference.child(groupId).child("Participants").child(user.uid.toString())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val hisRole = snapshot.child("role").value
                            itemParticipantAddBinding.statusTv.text = hisRole.toString()
                        } else {
                            itemParticipantAddBinding.statusTv.text = ""
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }

                })

            itemParticipantAddBinding.root.setOnClickListener {
                val ref = FirebaseDatabase.getInstance().getReference("Groups")
                ref.child(groupId).child("Participants").child(user.uid.toString())
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val timestamp = System.currentTimeMillis()
                            val hashmap = HashMap<String, String>()
                            hashmap["uid"] = user.uid.toString()
                            hashmap["role"] = "participant"
                            hashmap["timeStamp"] = timestamp.toString()
                            val ref1 = FirebaseDatabase.getInstance().getReference("Groups")
                            ref1.child(groupId).child("Participants").child(user.uid.toString())
                                .setValue(hashmap)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Added Successfully.", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { p0 ->
                                    Toast.makeText(
                                        context,
                                        p0.toString(),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }

                    })
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        return Vh(
            ItemParticipantAddBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.onBind(participantsList[position])
    }

    override fun getItemCount(): Int = participantsList.size
}