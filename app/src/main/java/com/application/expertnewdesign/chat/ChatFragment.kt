package com.application.expertnewdesign.chat

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.text.format.DateFormat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.application.expertnewdesign.BASE_URL
import com.application.expertnewdesign.MainActivity
import com.application.expertnewdesign.R
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import kotlinx.android.synthetic.main.chat_fragment.*
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.message.view.*
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import java.io.File

interface SendCheckAPI{
    @GET("isBannedInChatSelf")
    fun sendCheck(@Header("Authorization") token: String): Call<ResponseBody>
}

interface BanAPI{
    @POST("setChatBan")
    fun ban(@Header("Authorization") token: String, @Query("username") username: String, @Query("value") ban: Boolean = true): Call<ResponseBody>
}

class ChatFragment: Fragment(){

    var username: String = "Аноним"
    var rights: String? = null

    private var mAuth: FirebaseAuth? = null
    private var adapter: CustomAdapter? = null
    private var messageList = arrayListOf<ChatMessage>()
    private val reference: DatabaseReference = FirebaseDatabase.getInstance().getReference("messages")
    private var isNotScrolled = true
    private val messageEvent = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            messageList.clear()
            for (message in dataSnapshot.children) {
                val chatMessage = message.getValue(ChatMessage::class.java)
                chatMessage!!.key = message.key
                messageList.add(chatMessage)
            }
            adapter!!.notifyDataSetChanged()
            if(isNotScrolled){
                list_of_messages.scrollToPosition(messageList.size - 1)
            }
        }

        override fun onCancelled(databaseError: DatabaseError) {

        }
    }

    val sendCheckHandler = Handler()
    private val sendCheckRunnable = object: Runnable{
        override fun run() {
            checkPermission()
            sendCheckHandler.postDelayed(this, 10000)
        }
    }

    val rightsCheckHandler = Handler()
    private val rightsCheckRunnable = object: Runnable{
        override fun run() {
            if(rights == null) {
                rightsCheckHandler.postDelayed(this, 200)
            }else{
                if(rights == "moderator"){
                    input.isEnabled = true
                    input.clearFocus()
                    input.hint = "Введите сообщение..."
                    input_layout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_FILLED)
                    fab.show()
                    setChat(true)
                }else{
                    setChat()
                    sendCheckHandler.postDelayed(sendCheckRunnable, 50)
                }
            }
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


        input.isEnabled = false
        input.setText("")
        input.hint = "Подключение..."
        input_layout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE)
        fab.hide()
        localUser()
        rightsCheckHandler.postDelayed(rightsCheckRunnable, 50)
    }

    private fun setChat(control: Boolean = false){
        val token = activity!!.intent.getStringExtra("token")!!
        adapter = CustomAdapter(activity!!, username == "Администратор", token, messageList, reference, control)
        list_of_messages.layoutManager = LinearLayoutManager(context!!)
        list_of_messages.adapter = adapter
        list_of_messages.addOnScrollListener(object: RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                isNotScrolled = dy >= 0
                super.onScrolled(recyclerView, dx, dy)
            }
        })

        fab.setOnClickListener {
            if(rights != "moderator") {
                checkPermission(true)
            }else{
                sendMessage()
            }
        }

        reference.addValueEventListener(messageEvent)
    }

    fun localUser(){
        val file = File("${activity!!.filesDir.path}/user.data")
        if(file.exists()) {
            file.bufferedReader().use {
                val str = it.readLine()
                username = str.substringBefore(" ")
                rights = str.substringAfter(" ")
            }
        }
    }

    private fun checkPermission(isMessage: Boolean = false){

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .build()

        val sendAPI = retrofit.create(SendCheckAPI::class.java)
        val token = activity!!.intent.getStringExtra("token")!!
        sendAPI.sendCheck("Token $token").enqueue(object: Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                input.isEnabled = response.isSuccessful
                if(response.isSuccessful){
                    if(isMessage){
                        sendMessage()
                    }
                    input.hint = "Введите сообщение..."
                    input_layout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_FILLED)
                    fab.show()
                }else{
                    input.setText("")
                    input.hint = "Вы были забанены"
                    input_layout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE)
                    fab.hide()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                input.isEnabled = false
                input.setText("")
                input.hint = "Сервер не отвечает"
                input_layout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE)
                fab.hide()
            }
        })
    }

    private fun sendMessage(){
        val str = input.text.toString()
        if(!str.isBlank()) {
            reference
                .push()
                .setValue(ChatMessage(str, username))

            // Clear the input
            input.setText("")
        }
    }

    override fun onDestroy() {
        reference.removeEventListener(messageEvent)
        sendCheckHandler.removeCallbacks(sendCheckRunnable)
        rightsCheckHandler.removeCallbacks(rightsCheckRunnable)
        super.onDestroy()
    }
}

class CustomAdapter(val context: Context,
                    val admin: Boolean,
                    val token: String,
                    private val items: MutableList<ChatMessage>,
                    private val reference: DatabaseReference,
                    val control: Boolean = false) :
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
        holder.position = position
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun banUser(username: String){

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .build()

        val banAPI = retrofit.create(BanAPI::class.java)
        banAPI.ban("Token $token", username).enqueue(object: Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.isSuccessful){
                    items.forEach {
                        if(it.messageUser != null) {
                            if (it.messageUser!!.substringBefore("(") == username) {
                                reference.child(it.key!!).removeValue()
                            }
                        }
                    }
                }else{
                    Toast.makeText(
                        context,
                        "Нельзя забанить",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }
        })
    }

    fun BAN(username: String){
        val builder = AlertDialog.Builder(
            context)
        builder.setTitle("Бан")
            .setMessage("""-Вы хотите забанить пользователя?
                |-Все его сообщения удалятся
                |-Убрать бан можно в Панели управления
                |пользователями
            """.trimMargin())
            .setPositiveButton("Да") { dialog, _ ->
                dialog.cancel()
                banUser(username)
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.cancel()
            }
        val alert = builder.create()
        alert.show()
        val positiveButton = alert.getButton(AlertDialog.BUTTON_POSITIVE)
        val parent = positiveButton.parent as LinearLayout
        parent.gravity = Gravity.CENTER_HORIZONTAL
        parent.setPadding(0, 0, 0, 20)
        val leftSpacer = parent.getChildAt(1)
        leftSpacer.visibility = GONE
    }

    fun deleteMessage(position: Int){
        if(items[position].messageUser != "Администратор" || admin) {
            reference.child(items[position].key!!).removeValue()
        }else{
            Toast.makeText(
                context,
                "Недостаточно прав",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    inner class ViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {

        val title: TextView = view.message_user
        val subtitle: TextView = view.message_time
        val body: TextView = view.message_text
        var position: Int? = null
        private val banButton: ImageView = view.ban
        private val deleteButton: ImageView = view.delete

        init {
            if(control){
                view.control_buttons.visibility = VISIBLE
            }
            banButton.setOnClickListener(this)
            deleteButton.setOnClickListener(this)
        }

        override fun onClick(p0: View) {
            when(p0.id){
                banButton.id->{
                    if(title.text.toString() != "") {
                        BAN(title.text.toString().substringBefore("("))
                    }else{
                        Toast.makeText(
                            context,
                            "Неизвестный пользователь",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                deleteButton.id->{
                    if(position != null) {
                        deleteMessage(position!!)
                    }
                }
            }
        }
    }
}