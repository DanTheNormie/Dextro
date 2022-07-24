package com.noice.dextro.ui.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.measurement.sdk.api.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.noice.dextro.data.model.InboxItem
import com.noice.dextro.data.model.Message
import com.noice.dextro.data.model.UserItem
import com.noice.dextro.data.model.*
import com.noice.dextro.databinding.ActivityChatBinding
import com.noice.dextro.ui.adapters.ChatAdapter
import com.noice.dextro.utils.KeyboardVisibilityUtil
import com.noice.dextro.utils.isSameDayAs
import com.vanniktech.emoji.EmojiPopup

const val UID = "UID"
const val NAME = "NAME"
const val IMG = "IMG"

class ChatActivity : AppCompatActivity() {

    private val bind: ActivityChatBinding by lazy {
        ActivityChatBinding.inflate(layoutInflater)
    }
    private val recipientUid: String by lazy {
        intent.getStringExtra(UID)!!
    }
    private val recipientName: String by lazy {
        intent.getStringExtra(NAME)!!
    }
    private val recipientImgUrl: String by lazy {
        intent.getStringExtra(IMG)!!
    }
    private val db by lazy {
        FirebaseDatabase.getInstance()
    }

    private lateinit var currentUser: UserItem

    private lateinit var messagesListener: ChildEventListener

    private lateinit var keyboardVisibilityHelper: KeyboardVisibilityUtil

    private val emoji_popup by lazy {
        EmojiPopup(bind.root, bind.msgEt)
    }

    private val mCurrentUid: String by lazy {
        FirebaseAuth.getInstance().uid!!
    }
    private val messages = mutableListOf<ChatItem>()

    private lateinit var chatAdapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(bind.root)

        initViewsAndValues()
    }


    private fun initViewsAndValues() {
        keyboardVisibilityHelper = KeyboardVisibilityUtil(bind.root) {
            bind.msgRv.scrollToPosition(messages.size - 1)
        }

        FirebaseFirestore.getInstance().collection("users").document(mCurrentUid).get()
            .addOnSuccessListener {
                Log.i("lolo", "onCreate: $it")
                currentUser = it.toObject(UserItem::class.java)!!
            }
        bind.swipeRefreshLayout.isEnabled = false

        getCurrentUserData()

        setupChatsRecyclerView()

        setupToolbar()

        setupOnClickListeners()

    }

    private fun getCurrentUserData() {
        FirebaseFirestore.getInstance().collection("users").document(mCurrentUid).get()
            .addOnSuccessListener {
                Log.i("firebase", "uid : $mCurrentUid  \n $it")
                currentUser = it.toObject(UserItem::class.java)!!
            }
    }
    private fun setupChatsRecyclerView() {
        chatAdapter = ChatAdapter(messages, mCurrentUid)
        bind.msgRv.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = chatAdapter
        }

        listenToMessages()
    }

    private fun listenToMessages() {
        messagesListener = getChatNodeReference(recipientUid)
            .orderByKey()
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val msgItem = snapshot.getValue(Message::class.java)
                    msgItem?.let { addMessage(it) }
                }
                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun addMessage(msgItem: Message) {
        val chatItemBefore = messages.lastOrNull()

        if (chatItemBefore == null || !chatItemBefore.sentAt.isSameDayAs(msgItem.sentAt)) {
            messages.add(DateSectionHeader(msgItem.sentAt, this))
        }
        messages.add(msgItem)

        chatAdapter.notifyItemInserted(messages.size - 1)
        bind.msgRv.scrollToPosition(messages.size - 1)
    }



    private fun setupToolbar() {
        bind.recipientNameTv.text = recipientName
        Glide.with(this)
            .load(recipientImgUrl)
            .placeholder(com.noice.dextro.R.drawable.ic_baseline_person_24)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .into(bind.recipientImgIv)
        bind.materialToolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupOnClickListeners() {
        bind.sendMsgBtn.setOnClickListener {
            bind.msgEt.text?.let {
                if (it.isNotEmpty()) {
                    sendMessage(it.toString())
                }else{
                    Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
        }

        bind.emojiBtn.setOnClickListener {
            emoji_popup.toggle()
        }
    }



    private fun sendMessage(msg: String) {
        val id = getChatNodeReference(recipientUid).push().key

        checkNotNull(id) { "Cannot be Null" }

        val msg_data = Message(msg, mCurrentUid, id)
        getChatNodeReference(recipientUid).child(id).setValue(msg_data)
            .addOnSuccessListener {
                Log.i("", "sendMessage: Message sent successfully")
                bind.msgEt.setText("")
                updateInboxNode(msg_data)

            }.addOnFailureListener {
                Log.i("", "sendMessage: Failed to deliver Message. Exception : ${it.localizedMessage}")
            }


    }


    private fun updateInboxNode(msgData: Message) {
        val inboxData = InboxItem(
            name = recipientName,
            thumbnail_url = recipientImgUrl,
            uid = recipientUid,
            recent_msg = msgData.msg,
            unread_msg_count = 0
        )
        //updating inbox for sender
        getInboxNodeReference(mCurrentUid, recipientUid).setValue(inboxData)

        //updating inbox for recipient (unread_msg_count++)
        getInboxNodeReference(recipientUid, mCurrentUid).get()
            .addOnSuccessListener {
                val inboxItem = it.getValue(InboxItem::class.java)

                inboxData.apply {
                    uid = msgData.uid
                    name = currentUser.name
                    thumbnail_url = currentUser.imageUrl
                    unread_msg_count = 1
                }
                    inboxItem?.let { item ->
                        inboxData.unread_msg_count = item.unread_msg_count + 1
                    }
                    getInboxNodeReference(recipientUid, mCurrentUid).setValue(inboxData)

            }

    }

    private fun getChatNodeReference(recipient_uid: String) =
        db.reference.child("chats/${getId(recipient_uid)}")

    private fun getInboxNodeReference(toUser: String, fromUser: String) =
        db.reference.child("inbox/$toUser/$fromUser")

    private fun getId(friendId: String): String {
        return if (friendId > mCurrentUid) {
            mCurrentUid + friendId
        } else {
            friendId + mCurrentUid
        }
    }

    override fun onResume() {
        super.onResume()
        bind.root.viewTreeObserver
            .addOnGlobalLayoutListener(keyboardVisibilityHelper.visibilityListener)
    }


    override fun onPause() {
        super.onPause()
        updateUnreadMsgCount()
        bind.root.viewTreeObserver
            .removeOnGlobalLayoutListener(keyboardVisibilityHelper.visibilityListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        getChatNodeReference(recipientUid).removeEventListener(messagesListener)
    }

    private fun updateUnreadMsgCount() {
        if(messages.size>1){
            val msg = (messages.lastOrNull() as Message)
            val inboxData = InboxItem(
                name = recipientName,
                thumbnail_url = recipientImgUrl,
                uid = recipientUid,
                recent_msg = msg.msg,
                recent_msg_time = msg.sentAt,
                unread_msg_count = 0
            )
            getInboxNodeReference(mCurrentUid, recipientUid).setValue(inboxData)
        }
    }
}


