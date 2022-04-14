package com.example.chatapp.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.chatapp.Adapter.GroupChatListAdapter
import com.example.chatapp.databinding.FragmentGroupChatBinding
import com.example.chatapp.models.GroupChatModelList
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class GroupChatFragment : Fragment() {
    private var _binding: FragmentGroupChatBinding? = null
    private val binding get() = _binding!!

    lateinit var firebaseAuth: FirebaseAuth
    lateinit var groupChatsList: ArrayList<GroupChatModelList>
    lateinit var groupChatListAdapter: GroupChatListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGroupChatBinding.inflate(inflater, container, false)
        binding.groupsRv.setHasFixedSize(true)
        firebaseAuth = FirebaseAuth.getInstance()

        loadGroupChatList()
        return binding.root
    }

    private fun loadGroupChatList() {
        groupChatsList = ArrayList()
        val reference = FirebaseDatabase.getInstance().getReference("Groups")
        reference.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                groupChatsList.clear()
                for (ds in snapshot.children){
                    if (ds.child("Participants").child(firebaseAuth.uid!!).exists()){
                         val model = ds.getValue(GroupChatModelList::class.java)
                        groupChatsList.add(model!!)
                    }
                }

                groupChatListAdapter = GroupChatListAdapter(activity, groupChatsList)
                binding.groupsRv.adapter = groupChatListAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(activity, "$error", Toast.LENGTH_SHORT).show()
            }

        })
    }
}