package com.noice.dextro.ui.viewholders

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.noice.dextro.R
import com.noice.dextro.data.model.UserItem

import com.noice.dextro.databinding.UserItemLayoutBinding

class UserViewHolder(private val bind:UserItemLayoutBinding):RecyclerView.ViewHolder(bind.root) {

    fun bind(user: UserItem, onClick:(username:String, photoUrl:String, uid:String)->Unit){
        with(bind){
            countTv.isVisible = false
            timeTv.isVisible = false

            titleTv.text = user.name
            subTitleTv.text = user.status

            Glide.with(bind.root)
                .load(user.thumbnailUrl)
                .centerCrop()
                .placeholder(R.drawable.ic_baseline_person_24)
                .into(userIv)


            bind.root.setOnClickListener {
                onClick.invoke(user.name,user.thumbnailUrl,user.uid)
            }
        }
    }
}