package com.noice.dextro.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noice.dextro.R
import com.noice.dextro.data.model.ChatItem
import com.noice.dextro.data.model.DateSectionHeader
import com.noice.dextro.data.model.Message

class ChatAdapter(
    private val list: MutableList<ChatItem>,
    private val mCurrentUid: String
    ):RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflateLayout = { layout:Int ->
            LayoutInflater.from(parent.context).inflate(layout,parent,false)
        }

        return when(viewType){
            /*INCOMING_MSG_TYPE->{}
            OUTGOING_MSG_TYPE->{}
            DATE_SECTION_HEADER_TYPE->{}*/

            else -> {object : RecyclerView.ViewHolder(inflateLayout(R.layout.emptylayout)){}}
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }

    override fun getItemViewType(position: Int): Int {
        return when(val chatItem = list[position]){
            is Message -> {
                if(chatItem.uid == mCurrentUid){
                    OUTGOING_MSG_TYPE
                }else{
                    INCOMING_MSG_TYPE
                }
            }
            is DateSectionHeader -> DATE_SECTION_HEADER_TYPE
            else -> UNSUPPORTED_TYPE
        }
    }

    companion object{
        private const val UNSUPPORTED_TYPE = -1
        private const val INCOMING_MSG_TYPE = 0
        private const val OUTGOING_MSG_TYPE = 1
        private const val DATE_SECTION_HEADER_TYPE = 2
    }
}