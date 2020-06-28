package com.example.emaillistservice

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.*

//the question did not require the implementation of the activity, but I did so in order to test and
//illustrate how it functions
class EmailListClient : AppCompatActivity() {
    private lateinit var mMessenger: Messenger
    private var remoteMessenger: Messenger? = null
    private var bound = false

    internal inner class IncomingHandler(
        context: Context,
        private val applicationContext: Context = context.applicationContext
    ) : Handler() {
        override fun handleMessage(msg: Message) {
            @Suppress("UNCHECKED_CAST")
            val list = msg.data.getSerializable("cleanedList") as LinkedList<Int>
            var displayText = ""
            for (i in list)
                displayText += i.toString() + "\n"
            val textViewReply = findViewById<TextView>(R.id.textViewReply)
            textViewReply.text = displayText
        }
    }

    private fun packIntoIntegerList(str: String) : LinkedList<Int> {
        Log.d("EmailListClient", "Packing data $str")
        val strList = str.split(" ")
        val integerList = LinkedList<Int>()
        for (s in strList)
            integerList += s.toInt()
        return integerList
    }

    private fun sendData(list: LinkedList<Int>) {
        if (!bound) return
        val msg = Message.obtain()
        msg.replyTo = mMessenger
        val bundle = Bundle()
        bundle.putSerializable("emailList",list)
        msg.data = bundle
        remoteMessenger?.send(msg)
        Log.d("EmailListClient","Data sent to service")
    }

    private val myConn = object: ServiceConnection {
        override fun onServiceConnected(className: ComponentName?, service: IBinder?) {
            remoteMessenger = Messenger(service)
            mMessenger = Messenger(IncomingHandler(this@EmailListClient))
            bound = true
            Log.d("EmailListClient", "Bound to service")
        }

        override fun onServiceDisconnected(className: ComponentName?) {
            remoteMessenger = null
            bound = false
            Log.d("EmailListClient", "Unbound from service")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("EmailListClient","Created activity")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_list_client2)
        val buttonSend = findViewById<Button>(R.id.buttonSend)
        buttonSend.setOnClickListener{
            Log.d("EmailListClient", "Button Clicked")
            val editTextIntegersList = findViewById<EditText>(R.id.editTextIntegersList)
            val list = packIntoIntegerList(editTextIntegersList.text.toString())
            sendData(list)
        }
    }

    override fun onStart() {
        super.onStart()
        Intent(this, EmailListService::class.java).also { intent ->
            bindService(intent, myConn, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        if (bound) {
            unbindService(myConn)
            bound = false
        }
    }
}