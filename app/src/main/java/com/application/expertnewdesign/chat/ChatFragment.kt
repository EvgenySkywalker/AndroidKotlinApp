package com.application.expertnewdesign.chat

import android.content.Context
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.application.expertnewdesign.MainActivity
import com.application.expertnewdesign.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import kotlinx.android.synthetic.main.chat_fragment.*
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.message.view.*
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener


class ChatFragment: Fragment(){

    private var mAuth: FirebaseAuth? = null
    private var adapter: CustomAdapter? = null
    private var messageList = arrayListOf<ChatMessage>()
    private val reference: DatabaseReference = FirebaseDatabase.getInstance().getReference("messages")
    var username: String = "Аноним"

    private val messageEvent = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            messageList.clear()
            for (message in dataSnapshot.children) {
                val chatMessage = message.getValue(ChatMessage::class.java)
                messageList.add(chatMessage!!)
            }
            adapter!!.notifyDataSetChanged()
            list_of_messages.scrollToPosition(messageList.size - 1)
        }

        override fun onCancelled(databaseError: DatabaseError) {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.chat_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = activity as MainActivity
        activity.chatFragment = this

        setChat()
    }

    private fun setChat(){
        adapter = CustomAdapter(context!!, messageList)
        list_of_messages.layoutManager = LinearLayoutManager(context!!)
        list_of_messages.adapter = adapter

        fab.setOnClickListener {
            // Read the input field and push a new instance
            // of ChatMessage to the Firebase database
            reference
                .push()
                .setValue(ChatMessage(input.text.toString(), username))

            // Clear the input
            input.setText("")
            list_of_messages.scrollToPosition(messageList.size - 1)
        }

        reference.addValueEventListener(messageEvent)
    }

    override fun onDestroy() {
        reference.removeEventListener(messageEvent)
        super.onDestroy()
    }
}

class CustomAdapter(context: Context, private val items: MutableList<ChatMessage>) :
    RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    private val inflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.message, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.messageUser
        holder.subtitle.text = DateFormat.format("dd-MM-yyyy (HH:mm:ss)", item.messageTime)
        holder.body.text = item.messageText
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {

        val title: TextView = view.message_user
        val subtitle: TextView = view.message_time
        val body: TextView = view.message_text
    }
}