package com.noice.dextro.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.noice.dextro.R
import com.noice.dextro.data.model.UserItem
import com.noice.dextro.databinding.UserItemLayoutBinding
import com.noice.dextro.ui.viewholders.EmptyViewHolder

import com.noice.dextro.ui.viewholders.UserViewHolder
const val NORMAL_VIEW_TYPE = 1
const val INVALID_VIEW_TYPE = 2

class UserAdapter(private val onClick:(uid:String,name:String,imgUrl:String)->Unit):PagingDataAdapter<UserItem, RecyclerView.ViewHolder>(Companion) {
    private val auth = FirebaseAuth.getInstance()
    private val tag = "UserAdapter"
    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)!!
        Log.i(tag,"Current user id:${auth.uid}")
        return if(auth.uid == item.uid){
            Log.i(tag,"Encountered Invalid user id:${item.uid}")
            INVALID_VIEW_TYPE
        }else{
            Log.i(tag,"Encountered Normal user id:${item.uid}")
            NORMAL_VIEW_TYPE
        }

    }

    companion object : DiffUtil.ItemCallback<UserItem>(){
        override fun areItemsTheSame(oldItem: UserItem, newItem: UserItem): Boolean {
            return oldItem.uid == newItem.uid
        }

        override fun areContentsTheSame(oldItem: UserItem, newItem: UserItem): Boolean {
            return oldItem == newItem
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder is UserViewHolder){
            getItem(position)?.let { holder.bind(it, onClick) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return if(viewType == NORMAL_VIEW_TYPE){
            val bind = UserItemLayoutBinding.inflate(layoutInflater,parent,false)
            UserViewHolder(bind)
        }else{
            EmptyViewHolder(layoutInflater.inflate(R.layout.emptylayout, parent,false))
        }


    }
}