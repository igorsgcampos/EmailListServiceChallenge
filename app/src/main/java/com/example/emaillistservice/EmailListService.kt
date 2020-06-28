package com.example.emaillistservice

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.*
import android.widget.Toast
import java.util.*

class EmailListService : Service() {
    private lateinit var mMessenger: Messenger

    private fun listCleanup(list: LinkedList<Int>) {
        val itr = list.listIterator()
        val set = mutableSetOf<Int>()
        while (itr.hasNext()) {
            val e = itr.next()
            if (set.contains(e))
                itr.remove()
            else
                set.add(e)
        }
    }

    private fun commonNode(listA: LinkedList<Int>, listB: LinkedList<Int>): Int{
        if (listA.size == 0 || listB.size == 0) return -1
        val itrA = listA.listIterator(listA.size)
        val itrB = listB.listIterator(listB.size)
        var temp = -1
        while (itrA.hasPrevious() && itrB.hasPrevious()) {
            val a = itrA.previous()
            val b = itrB.previous()
            if (a != b)
                return temp
            else
                temp = a
        }
        return temp
    }

    internal inner class IncomingHandler(
        context: Context,
        private val applicationContext: Context = context.applicationContext
    ) : Handler() {
        override fun handleMessage(msg: Message) {
            @Suppress("UNCHECKED_CAST")
            val list = msg.data.getSerializable("emailList") as LinkedList<Int>
            listCleanup(list)
            val cleanedList = Bundle()
            cleanedList.putSerializable("cleanedList", list);
            val reply = Message.obtain()
            reply.data = cleanedList
            msg.replyTo?.send(reply)
            //super.handleMessage(msg)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        Toast.makeText(applicationContext, "bound to Email List Service", Toast.LENGTH_SHORT).show()
        mMessenger = Messenger(IncomingHandler(this))
        return mMessenger.binder
    }

}