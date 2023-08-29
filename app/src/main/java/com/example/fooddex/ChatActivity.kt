package com.example.fooddex

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.example.fooddex.databinding.ActivityChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var rvMessages: RecyclerView
    private lateinit var chatId: String
    private lateinit var dbReference: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var messageList = mutableListOf<Message>()
    private lateinit var adapter: MessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbReference = Firebase.database.reference
        auth = Firebase.auth

        binding.topAppBar.setNavigationOnClickListener {
            finish()
        }

        rvMessages = binding.rvMessages
        adapter = MessageAdapter(messageList, this)

        rvMessages.adapter = adapter

        //(rvMessages.layoutManager as LinearLayoutManager).stackFromEnd = true

        chatId = intent.getStringExtra("chatId")!!

        val itemDecorator = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        itemDecorator.setDrawable(ContextCompat.getDrawable(this, R.drawable.rv_spacing_8dp)!!)
        rvMessages.addItemDecoration(itemDecorator)

        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text.toString().trim()
            if (!text.isNullOrEmpty()){
                sendMessage(auth.currentUser!!.uid, text, System.currentTimeMillis())
                binding.etMessage.text.clear()
            }
        }

        retrieveMessages()

    }

    private fun retrieveMessages(){
        val userRef = dbReference.child("users").child(auth.currentUser?.uid!!)

        userRef.child("familyId").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val familyId = snapshot.getValue(String::class.java)

                if (familyId != null) {

                    val chatRef = dbReference.child("chat").child(familyId).child(chatId)

                    chatRef.addChildEventListener(object : ChildEventListener{
                        override fun onChildAdded(
                            snapshot: DataSnapshot,
                            previousChildName: String?
                        ) {
                            val message = snapshot.getValue(Message::class.java)
                            message?.let {

                                val userNameRef = dbReference.child("users").child(message.senderId).child("name")

                                userNameRef.addListenerForSingleValueEvent(object : ValueEventListener{
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        Log.d("CHAT", "$snapshot")
                                        message.senderName = snapshot.getValue(String::class.java)!!

                                        messageList.add(it)
                                        updateRecyclerView()
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                    }
                                })
                            }
                        }

                        override fun onChildChanged(
                            snapshot: DataSnapshot,
                            previousChildName: String?
                        ) {
                            val message = snapshot.getValue(Message::class.java)
                            message?.let {

                                val userNameRef = dbReference.child("users").child(message.id).child("name")

                                userNameRef.addListenerForSingleValueEvent(object : ValueEventListener{
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        message.senderName = snapshot.getValue(String::class.java)!!

                                        val index = messageList.indexOfFirst { m -> m.id == it.id }
                                        if (index >= 0) {
                                            messageList[index] = it
                                            updateRecyclerView()
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                    }
                                })
                            }
                        }

                        override fun onChildRemoved(snapshot: DataSnapshot) {
                            val product = snapshot.getValue(Product::class.java)
                            product?.let {
                                messageList.removeAll { m -> m.id == it.id }
                                updateRecyclerView()
                            }
                        }

                        override fun onChildMoved(
                            snapshot: DataSnapshot,
                            previousChildName: String?
                        ) {

                        }

                        override fun onCancelled(error: DatabaseError) {

                        }

                    })
                } else {
                    // Handle the case when "familyId" doesn't exist in the database for the user
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun sendMessage(userId: String, text: String, timestamp:Long){
        val userRef = dbReference.child("users").child(auth.currentUser?.uid!!)

        userRef.child("familyId").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val familyId = snapshot.getValue(String::class.java)

                if (familyId != null) {

                    val chatRef = dbReference.child("chat").child(familyId).child(chatId).push()

                    val message = Message(chatRef.key!!, userId, text, timestamp)
                    chatRef.setValue(message)
                } else {
                    // Handle the case when "familyId" doesn't exist in the database for the user
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun updateRecyclerView(){
        messageList.sortBy { it.timestamp }
        Log.d("CHAT", "${messageList.size} messages found in chat $chatId")
        adapter.notifyDataSetChanged()
        binding.rvMessages.smoothScrollToPosition(messageList.size - 1)
    }
}