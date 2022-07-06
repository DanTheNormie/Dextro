package com.noice.dextro.ui.main.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.noice.dextro.R
import com.noice.dextro.data.model.User
import com.noice.dextro.databinding.FragmentUsersBinding
import com.noice.dextro.ui.adapters.UserAdapter
import com.noice.dextro.ui.viewholders.UserViewHolder
import com.noice.dextro.ui.viewmodels.MainViewModels
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


class UsersFragment : Fragment() {

    private val userAdapter = UserAdapter()
    val auth by lazy {
      FirebaseAuth.getInstance()
    }
    val firestoreDb by lazy {
        FirebaseFirestore.getInstance().collection("users")
            .orderBy("name", Query.Direction.DESCENDING)
    }
    lateinit var bind: FragmentUsersBinding

    private val sharedViewModel:MainViewModels by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bind = DataBindingUtil.inflate(layoutInflater,R.layout.fragment_users,container,false)
        bind.usersListRv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = userAdapter
        }

        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            sharedViewModel.flow.collect{
                userAdapter.submitData(it)
            }
        }
    }
}