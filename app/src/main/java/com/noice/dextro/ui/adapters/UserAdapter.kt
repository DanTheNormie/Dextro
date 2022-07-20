package com.noice.dextro.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.noice.dextro.R
import com.noice.dextro.data.model.UserItem
import com.noice.dextro.databinding.UserItemLayoutBinding

import com.noice.dextro.ui.viewholders.UserViewHolder
const val NORMAL_VIEW_TYPE = 1
const val INVALID_VIEW_TYPE = 2


class UserAdapter(val onClick:(username:String,photoUrl:String,uid:String)->Unit):PagingDataAdapter<UserItem, RecyclerView.ViewHolder>(Companion) {
    private val auth = FirebaseAuth.getInstance()
    private val tag = "UserAdapter"
    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)!!
        return if(auth.uid == item.uid){
            Log.i(tag,"Encountered same user id:${item.uid}")
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
            getItem(position)?.let { holder.bind(it){username, photoUrl, uid ->
                onClick.invoke(username, photoUrl, uid)
            } }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return if(viewType == NORMAL_VIEW_TYPE){
            UserViewHolder(UserItemLayoutBinding.inflate(layoutInflater,parent,false))
        }else{
            //passing an empty viewholder with a empty layout
            object :RecyclerView.ViewHolder(layoutInflater.inflate(R.layout.emptylayout,parent,false )) {}
        }
    }
}