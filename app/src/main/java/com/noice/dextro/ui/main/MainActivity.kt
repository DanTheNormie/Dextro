package com.noice.dextro.ui.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.noice.dextro.R
import com.noice.dextro.databinding.ActivityMainBinding
import com.noice.dextro.ui.adapters.ChatViewPagerAdapter
import com.noice.dextro.ui.adapters.UserAdapter

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
}