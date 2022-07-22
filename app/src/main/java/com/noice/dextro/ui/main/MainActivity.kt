package com.noice.dextro.ui.main

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.databinding.DataBindingUtil
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.noice.dextro.R
import com.noice.dextro.databinding.ActivityMainBinding
import com.noice.dextro.ui.adapters.ChatViewPagerAdapter
import com.noice.dextro.ui.adapters.UserAdapter
import com.noice.dextro.ui.auth.LoginActivity
import com.noice.dextro.ui.auth.SignUpActivity

class MainActivity : AppCompatActivity() {
    lateinit var bind: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


            bind = DataBindingUtil.setContentView(this,R.layout.activity_main)

            setSupportActionBar(bind.toolbar)
            bind.viewPager.adapter = ChatViewPagerAdapter(this)

            TabLayoutMediator(bind.tabs,bind.viewPager,false,true) { tab: TabLayout.Tab, pos: Int ->
                when (pos) {
                    0 -> tab.text = "Inbox"
                    1 -> tab.text = "People"
                }
            }.attach()

    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_activity_options_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.title=="logout"){
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this,LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
        }
        return super.onOptionsItemSelected(item)
    }

}