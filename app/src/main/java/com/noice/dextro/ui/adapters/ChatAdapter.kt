package com.noice.dextro.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.appcheck.internal.util.Logger.TAG
import com.noice.dextro.R
import com.noice.dextro.data.model.ChatItem
import com.noice.dextro.data.model.DateSectionHeader
import com.noice.dextro.data.model.Message
import com.noice.dextro.utils.formatAsHeader
import com.noice.dextro.utils.formatAsTime

class ChatAdapter(
    private val list: MutableList<ChatItem>,
    private val mCurrentId:String):RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val inflateLayout :(Int) -> View = {layout -> LayoutInflater.from(parent.context).inflate(layout,parent,false)}

        return when(viewType){
            INCOMING_MSG_TYPE->{MessageViewHolder(inflateLayout(R.layout.incoming_msg_item_layout))}
            OUTGOING_MSG_TYPE->{MessageViewHolder(inflateLayout(R.layout.outgoing_msg_item_layout))}
            DATE_SECTION_HEADER_TYPE->{DateSectionHeaderViewHolder(inflateLayout(R.layout.date_section_header_msg_item_layout))}

            else -> {
                Log.i(TAG, "onCreateViewHolder: creating empty view for Date")
                object : RecyclerView.ViewHolder(inflateLayout(R.layout.emptylayout)){}
            }
        }
    }


override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when(val chatItem = list[position]){
        is DateSectionHeader ->{
            holder.itemView.findViewById<TextView>(R.id.time_tv).text = chatItem.date
        }
        is Message ->{
            holder.itemView.findViewById<TextView>(R.id.content).text = chatItem.msg
            holder.itemView.findViewById<TextView>(R.id.time).text = chatItem.sentAt.formatAsTime()
        }
    }
}

override fun getItemCount(): Int {
    return list.size
}

override fun getItemViewType(position: Int): Int {
    return  when(val chatItem = list[position]) {
        is Message -> {
            if (chatItem.uid == mCurrentId){
                OUTGOING_MSG_TYPE
            }else{
                INCOMING_MSG_TYPE
            }
        }
        is DateSectionHeader -> DATE_SECTION_HEADER_TYPE

        else -> UNSUPPORTED_TYPE
    }
}

class MessageViewHolder(view: View):RecyclerView.ViewHolder(view)
class DateSectionHeaderViewHolder(view: View):RecyclerView.ViewHolder(view)

companion object{
    private const val UNSUPPORTED_TYPE = -1
    private const val INCOMING_MSG_TYPE = 0
    private const val OUTGOING_MSG_TYPE = 1
    private const val DATE_SECTION_HEADER_TYPE = 2

}
}

