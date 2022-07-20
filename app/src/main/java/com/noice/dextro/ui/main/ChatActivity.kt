package com.noice.dextro.ui.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.noice.dextro.data.model.InboxItem
import com.noice.dextro.data.model.Message
import com.noice.dextro.data.model.UserItem
import com.noice.dextro.databinding.ActivityChatBinding
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.google.GoogleEmojiProvider

const val UID = "uid"
const val NAME = "name"
const val IMG = "ph"
class ChatActivity : AppCompatActivity() {
   private val mCurrentUid:String by lazy {
       FirebaseAuth.getInstance().uid!!
   }
    private val db:FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance()
    }
    private val bind:ActivityChatBinding by lazy {
        ActivityChatBinding.inflate(layoutInflater)
    }
    private val recipient_uid: String by lazy {
        intent.getStringExtra(UID)!!
    }
    private val recipient_name: String by lazy {
        intent.getStringExtra(NAME)!!
    }
    private val recipient_img: String by lazy {
        intent.getStringExtra(IMG)!!
    }
    lateinit var currentUser: UserItem
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EmojiManager.install(GoogleEmojiProvider())
        setContentView(bind.root)

        initViewsAndValues()

    }

    private fun getChatNodeReference(friendId: String) =
        db.reference.child("chats/${getId(friendId)}")

    private fun getInboxNodeReference(toUser:String, fromUser:String) =
        db.reference.child("inbox/$toUser/$fromUser")

    private fun getId(friendId:String):String{
        return if (friendId > mCurrentUid){
            mCurrentUid + friendId
        }else{
            friendId + mCurrentUid
        }
    }

    private fun initViewsAndValues() {

        FirebaseFirestore.getInstance().collection("users").document(mCurrentUid).get()
            .addOnSuccessListener {
                Log.i("lolo", "onCreate: $it")
                currentUser = it.toObject(UserItem::class.java)!!
            }

        bind.recipientNameTv.text = recipient_name

        Glide.with(this).load(recipient_img).into(bind.recipientImgIv)

        bind.sendMsgBtn.setOnClickListener {
            bind.msgEt.text?.let {
                if (it.isNotEmpty()){
                    sendMessage(it.toString())
                    it.clear()
                }else{
                    Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun sendMessage(msg: String) {
        val id = getChatNodeReference(recipient_uid).push().key
        checkNotNull(id){"Cannot be Null"}

        val msg_data = Message(msg,mCurrentUid,id)
        getChatNodeReference(recipient_uid).child(id).setValue(msg_data).addOnSuccessListener {
            Log.i("", "sendMessage: Message sent successfully")

            updateInboxNode(msg_data)

        }.addOnFailureListener {
            Log.i("", "sendMessage: Failed to deliver Message. Exception : ${it.localizedMessage}")
        }


    }

    private fun markAsRead(){
        getInboxNodeReference(mCurrentUid,recipient_uid).child("unread_msg_count").setValue(0)
    }

    private fun updateInboxNode(msgData: Message) {
        val inboxData = InboxItem(
            name = recipient_name,
            thumbnail_url = recipient_img,
            uid = recipient_uid,
            recent_msg = msgData.msg,
            unread_msg_count = 0
        )
        //updating inbox for sender
        getInboxNodeReference(mCurrentUid,recipient_uid).setValue(inboxData)

        //updating inbox for recipient (unread_msg_count++)
        getInboxNodeReference(recipient_uid,mCurrentUid).get().addOnSuccessListener {
            val inboxItem = it.getValue(InboxItem::class.java)

            inboxData.apply {
                uid = msgData.uid
                name = currentUser.name
                thumbnail_url = currentUser.imageUrl
                unread_msg_count = 1
            }
            inboxItem?.let { item ->
                inboxData.unread_msg_count = item.unread_msg_count+1
            }
            getInboxNodeReference(recipient_uid,mCurrentUid).setValue(inboxData)
        }
    }
}