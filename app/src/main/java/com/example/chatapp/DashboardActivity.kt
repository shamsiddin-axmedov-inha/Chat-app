package com.example.chatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.example.chatapp.Fragments.GroupChatFragment
import com.example.chatapp.Fragments.UsersFragment
import com.example.chatapp.databinding.ActivityDashboardBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class DashboardActivity : AppCompatActivity() {
    lateinit var binding: ActivityDashboardBinding
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var myUid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarLogin)
        supportActionBar?.title = "Profile"
        firebaseAuth = FirebaseAuth.getInstance()
        myUid = firebaseAuth.currentUser?.uid.toString()

        //group fragment transaction
        supportActionBar?.title = "Users"
        val fragment1 = UsersFragment()
        val ft1 = supportFragmentManager.beginTransaction()
        ft1.replace(R.id.content, fragment1, "")
        ft1.commit()

        binding.navigation.setOnNavigationItemSelectedListener(object :
            BottomNavigationView.OnNavigationItemSelectedListener {
            override fun onNavigationItemSelected(item: MenuItem): Boolean {

                when(item.itemId){
                    R.id.nav_profile -> {
                        //group fragment transaction
                        supportActionBar?.title = "Users"
                        val fragment1 = UsersFragment()
                        val ft1 = supportFragmentManager.beginTransaction()
                        ft1.replace(R.id.content, fragment1, "")
                        ft1.commit()
                        return true
                    }

                    R.id.nav_group -> {
                        supportActionBar?.title = "Group Chat"
                        val fragment2 = GroupChatFragment()
                        val ft2 = supportFragmentManager.beginTransaction()
                        ft2.replace(R.id.content, fragment2, "")
                        ft2.commit()
                        return true
                    }
                }
                return false
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
        checkUserStatus()
        checkOnlineStatus("online")
        super.onStart()
    }
    
    override fun onResume() {
        checkOnlineStatus("online")
        super.onResume()
    }

    override fun onPause() {
        val simpleDateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm")
        val time = simpleDateFormat.format(Date())
        checkOnlineStatus(time)
        super.onPause()
    }

    override fun onDestroy() {
        val simpleDateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm")
        val time = simpleDateFormat.format(Date())
        checkOnlineStatus(time)
        super.onDestroy()
    }
    private fun checkUserStatus() {
        val user = firebaseAuth.currentUser
        if (user == null) {
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (item.itemId == R.id.action_logout) {
            firebaseAuth.signOut()
            checkUserStatus()
        }else if(item.itemId == R.id.action_create_group){
            startActivity(Intent(this, CreateGroupActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

}