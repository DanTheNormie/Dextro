package com.noice.dextro.ui.main.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.noice.dextro.data.model.InboxItem
import com.noice.dextro.databinding.FragmentInboxBinding
import com.noice.dextro.ui.adapters.InboxAdapter
import com.noice.dextro.ui.main.ChatActivity
import com.noice.dextro.ui.main.IMG
import com.noice.dextro.ui.main.NAME
import com.noice.dextro.ui.main.UID


class InboxFragment : Fragment() {

    private val mCurrentUid:String by lazy {
        FirebaseAuth.getInstance().uid!!
    }
    private val db by lazy {
        FirebaseDatabase.getInstance("https://dextro-11-default-rtdb.asia-southeast1.firebasedatabase.app")

    }

    private lateinit var bind:FragmentInboxBinding
    private lateinit var inboxAdapter:InboxAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bind = FragmentInboxBinding.inflate(layoutInflater,container,false)
        setupInboxRecyclerView()
        return bind.root
    }

    private fun setupInboxRecyclerView() {
        val options = FirebaseRecyclerOptions.Builder<InboxItem>()
            .setQuery(getInboxNodeReference(mCurrentUid), InboxItem::class.java)
            .setLifecycleOwner(viewLifecycleOwner)
            .build()

        inboxAdapter = InboxAdapter(options){name, imgUrl, uid ->
            val intent = Intent(requireContext(), ChatActivity::class.java).apply {
                putExtra(UID,uid)
                putExtra(NAME,name)
                putExtra(IMG,imgUrl)
            }
            startActivity(intent)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bind.inboxList.apply {
            setHasFixedSize(true)
            adapter = inboxAdapter
            layoutManager = LinearLayoutManager(requireContext())
            itemAnimator = null
        }

    }

    override fun onStart() {
        super.onStart()
        inboxAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        inboxAdapter.stopListening()
    }

    private fun getInboxNodeReference(uid:String) = db.reference.child("inbox/$uid")
}