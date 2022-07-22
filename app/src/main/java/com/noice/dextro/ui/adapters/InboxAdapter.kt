package com.noice.dextro.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerAdapter_LifecycleAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.noice.dextro.data.model.InboxItem
import com.noice.dextro.data.model.UserItem
import com.noice.dextro.databinding.UserItemLayoutBinding
import com.noice.dextro.ui.viewholders.InboxViewHolder

class InboxAdapter(
    options: FirebaseRecyclerOptions<InboxItem>,
    private val onClick:(username:String, photoUrl:String, uid:String)->Unit
    ):FirebaseRecyclerAdapter<InboxItem,InboxViewHolder>(options){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InboxViewHolder {
        val bind = UserItemLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return InboxViewHolder(bind)
    }

    override fun onBindViewHolder(holder: InboxViewHolder, position: Int, model: InboxItem) {
        holder.bind(getItem(position), onClick)
    }

}