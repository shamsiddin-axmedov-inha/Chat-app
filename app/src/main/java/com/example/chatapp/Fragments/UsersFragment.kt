package com.example.chatapp.Fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.example.chatapp.Adapter.ChatAdapter
import com.example.chatapp.CreateGroupActivity
import com.example.chatapp.GroupParticipantAddActivity
import com.example.chatapp.MessageActivity
import com.example.chatapp.R
import com.example.chatapp.databinding.FragmentUsersBinding
import com.example.chatapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UsersFragment : Fragment() {
    private var _binding: FragmentUsersBinding? = null
    private val binding get() = _binding!!
    lateinit var chatAdapter: ChatAdapter
    lateinit var userList: ArrayList<User>
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUsersBinding.inflate(inflater, container, false)
        binding.rv.setHasFixedSize(true)
        firebaseAuth = FirebaseAuth.getInstance()
        userList = ArrayList()

        getAllUsers()


        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
        menu.findItem(R.id.action_create_group).isVisible = false

        super.onCreateOptionsMenu(menu, inflater)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.action_logout) {
            firebaseAuth.signOut()
        }
        return super.onOptionsItemSelected(item)
    }


    private fun getAllUsers() {
        val fUser = FirebaseAuth.getInstance().currentUser
        val reference = FirebaseDatabase.getInstance().getReference("Users")

        reference.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                for (child in snapshot.children){
                    val modelUser = child.getValue(User::class.java)
                    if (!modelUser?.uid.equals(fUser?.uid)){
                        userList.add(modelUser!!)
                    }

                }

                chatAdapter = ChatAdapter(userList, object : ChatAdapter.OnItemClickListener{
                    override fun onItemClick(user: User) {
                        val intent = Intent(context, MessageActivity::class.java)
                        intent.putExtra("hisUid", user.uid)
                        context?.startActivity(intent)
                    }
                })
                binding.rv.adapter = chatAdapter
            }
            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
}