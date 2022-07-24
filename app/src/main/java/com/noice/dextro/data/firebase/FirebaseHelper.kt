package com.noice.dextro.data.firebase

import android.util.Log
import com.google.firebase.appcheck.internal.util.Logger.TAG
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class FirebaseHelper {

    val database = Firebase.database
    val myConnectionsRef = database.getReference("users/joe/connections")

    // Stores the timestamp of my last disconnect (the last time I was seen online)
    val lastOnlineRef = database.getReference("/users/joe/lastOnline")

    val connectedRef = database.getReference(".info/connected")

    fun onConnect(){
        connectedRef.addValueEventListener(object:ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue<Boolean>() ?: false
                if (connected) {
                    val con = myConnectionsRef.push()

                    // When this device disconnects, remove it
                    con.onDisconnect().removeValue()

                    // When I disconnect, update the last time I was seen online
                    lastOnlineRef.onDisconnect().setValue(ServerValue.TIMESTAMP)

                    // Add this device to my connections list
                    // this value could contain info about the device or a timestamp too
                    con.setValue(java.lang.Boolean.TRUE)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Listener was cancelled at .info/connected")
            }
        })
    }


}