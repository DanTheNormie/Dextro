package com.noice.dextro.ui.viewholders

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.noice.dextro.R
import com.noice.dextro.data.model.UserItem

import com.noice.dextro.databinding.UserItemLayoutBinding

class UserViewHolder(val bind:UserItemLayoutBinding):RecyclerView.ViewHolder(bind.root) {

    fun bind(userItem: UserItem,onClick:(uid:String,name:String,imgUrl:String)->Unit){
        with(bind){
            countTv.isVisible = false
            timeTv.isVisible = false

            titleTv.text = userItem.name
            titleTv.textSize = 20f
            if (userItem.status.isNotBlank()){
                bind.subTitleTv.apply {
                    visibility = View.VISIBLE
                    text = userItem.status
                }
            }else{
                bind.subTitleTv.visibility = View.GONE
            }

            Glide.with(bind.root)
                .load(userItem.thumbnailUrl)
                .centerCrop()
                .placeholder(R.drawable.ic_baseline_person_24)
                .into(userIv)

            root.setOnClickListener {
                onClick.invoke(userItem.uid,userItem.name,userItem.imageUrl)
            }
        }
    }
}