package com.noice.dextro.ui.viewholders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.noice.dextro.R
import com.noice.dextro.data.model.InboxItem
import com.noice.dextro.databinding.UserItemLayoutBinding
import com.noice.dextro.utils.formatAsListItem

class InboxViewHolder(private val bind:UserItemLayoutBinding):RecyclerView.ViewHolder(bind.root) {

    fun bind(inboxItem: InboxItem,onClick:(username:String, photoUrl:String, uid:String)->Unit){

        bind.titleTv.text = inboxItem.name
        bind.subTitleTv.text = inboxItem.recent_msg
        bind.timeTv.text = inboxItem.recent_msg_time.formatAsListItem(bind.root.context)
        if(inboxItem.unread_msg_count>0){
            bind.countTv.visibility = View.VISIBLE
            bind.countTv.text = inboxItem.unread_msg_count.toString()
        }else{
            bind.countTv.visibility = View.GONE
        }
        Glide.with(bind.root.context)
            .load(inboxItem.thumbnail_url)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .centerCrop()
            .placeholder(R.drawable.ic_baseline_person_24)
            .into(bind.userIv)

        bind.root.setOnClickListener {
            onClick.invoke(inboxItem.name,inboxItem.thumbnail_url,inboxItem.uid)
        }
    }
}