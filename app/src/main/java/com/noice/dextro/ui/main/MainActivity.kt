package com.noice.dextro.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.noice.dextro.R
import com.noice.dextro.databinding.ActivityMainBinding
import com.noice.dextro.ui.adapters.ChatViewPagerAdapter
import com.noice.dextro.ui.auth.LoginActivity
import com.noice.dextro.ui.viewmodels.MainViewModel


class MainActivity : AppCompatActivity() {
    lateinit var bind: ActivityMainBinding
    lateinit var sharedViewModel:MainViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = DataBindingUtil.setContentView(this,R.layout.activity_main)
        sharedViewModel = ViewModelProvider(this)[MainViewModel::class.java]

        initViews()




    }

    private fun initViews() {
        // setting up actionbar
        setUptoolbar()

        //setting up viewpager
        setUpViewPager()

    }

    private fun setUptoolbar() {
        setSupportActionBar(bind.toolbar)
        // check (1) onCreateOptionsMenu() and (2) onOptionsItemSelected()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        //temporary, remove when adding real options with search()
        menu.add("Logout")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.title){
            "Logout" -> {
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this,LoginActivity::class.java))
            }
        }
        return true
    }

    private fun setUpViewPager() {
        bind.viewPager.adapter = ChatViewPagerAdapter(this)
        TabLayoutMediator(bind.tabs,bind.viewPager,false,true) { tab: TabLayout.Tab, pos: Int ->
            when (pos) {
                0 -> tab.text = "Inbox"
                1 -> tab.text = "People"
            }
        }.attach()
    }
}