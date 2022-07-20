package com.noice.dextro.ui.main.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.noice.dextro.R
import com.noice.dextro.databinding.FragmentUsersBinding
import com.noice.dextro.ui.adapters.UserAdapter
import com.noice.dextro.ui.main.ChatActivity
import com.noice.dextro.ui.main.IMG
import com.noice.dextro.ui.main.NAME
import com.noice.dextro.ui.main.UID
import com.noice.dextro.ui.viewmodels.MainViewModel
import kotlinx.coroutines.launch


class UsersFragment : Fragment() {

    lateinit var userAdapter:UserAdapter
    val auth by lazy {
      FirebaseAuth.getInstance()
    }
    val firestoreDb by lazy {
        FirebaseFirestore.getInstance().collection("users")
            .orderBy("name", Query.Direction.DESCENDING)
    }
    lateinit var bind: FragmentUsersBinding

    private val sharedViewModel:MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bind = DataBindingUtil.inflate(layoutInflater,R.layout.fragment_users,container,false)
        userAdapter = UserAdapter { username, photoUrl, uid ->
            val intent = Intent(requireContext(),ChatActivity::class.java).apply {
                putExtra(UID,uid)
                putExtra(NAME,username)
                putExtra(IMG,photoUrl)
            }
            startActivity(intent)
        }

        bind.usersListRv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = userAdapter
        }

        lifecycleScope.launch {
            sharedViewModel.flow.collect {
                userAdapter.submitData(it)
            }
        }
        return bind.root
    }
}