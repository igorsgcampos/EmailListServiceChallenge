package com.example.emaillistservice

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.*
import android.widget.Toast
import java.util.*

//Q6
//the e-mail list is represented as a LinkedList<Int> as there isn't a defined structure for e-mail
//as the objective of these questions is clear to be the manipulation of the LinkedList and Service
//implementation rather than the design of the e-mail class itself,
//I chose to use an int which would represent the id of the email, but in true it could be replaced
//by any class that implements Serializable (in order to be bundled for IPC) and a hash (to use Set)
//The service was implemented as a binder service rather than intent service as this is deprecated
//this should conform with current android development guidelines.
//to respect the singly linked list restriction I only used forward iteration in the code
class EmailListService : Service() {
    private lateinit var mMessenger: Messenger

    //Q5 function to cleanup the list, removing duplicates uses a set to check whether it is so
    //time complexity is O(size(list)) and memory complexity is O(size(unique(list))), where unique
    //represents the number of unique items in the list
    private fun listCleanup(list: LinkedList<Int>) {
        val itr = list.listIterator()
        val set = mutableSetOf<Int>()

        //iterate through the list, if item is not in set, add, otherwise, remove from list in place
        while (itr.hasNext()) {
            val e = itr.next()
            if (set.contains(e))
                itr.remove()
            else
                set.add(e)
        }
    }

    //Q7 returns the common node between two lists (assumes lists are positive integers only)
    //return -1 if there is no common node, this function is unused
    //cost is O(size(a + b)) in time, no additional space was used.
    private fun commonNode(listA: LinkedList<Int>, listB: LinkedList<Int>): Int {
        //if either list is empty there is no common node
        if (listA.size == 0 || listB.size == 0) return -1
        val itrA: ListIterator<Int>
        val itrB: ListIterator<Int>

        //set the iterator from both lists such that it takes the same number of iterations
        //for both to reach the end (skips the first n elements from the longer list, where
        //n = size(bigList) - size(smallList))
        when {
            listA.size > listB.size -> {
                itrA = listA.listIterator(listA.size - listB.size)
                itrB = listB.listIterator()
            }
            listA.size < listB.size -> {
                itrA = listA.listIterator()
                itrB = listB.listIterator(listB.size - listA.size)
            }
            else -> {
                itrA = listA.listIterator()
                itrB = listB.listIterator()
            }
        }
        var commonNode = -1
        while (itrA.hasNext() && itrB.hasNext()) {
            val a = itrA.next()
            val b = itrB.next()

            //get the common node only if there is a match until the end of the lists
            if (a == b) {
                if (commonNode == -1)
                    commonNode = a
            } else
                commonNode = -1
        }
        return commonNode
    }

    //gets list from bundle that comes as additional data field in the incoming message
    //cleans up the list and sends back to the client through the replyTo messenger
    internal inner class IncomingHandler(
        context: Context,
        private val applicationContext: Context = context.applicationContext
    ) : Handler() {
        override fun handleMessage(msg: Message) {
            //suppresses the cast, as it comes from a Bundle, which loses the type information
            @Suppress("UNCHECKED_CAST")
            val list = msg.data.getSerializable("emailList") as LinkedList<Int>
            listCleanup(list)
            val cleanedList = Bundle()
            cleanedList.putSerializable("cleanedList", list);
            val reply = Message.obtain()
            reply.data = cleanedList
            msg.replyTo?.send(reply)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        //displays a toast notification upon binding
        Toast.makeText(applicationContext, "bound to Email List Service", Toast.LENGTH_SHORT).show()
        mMessenger = Messenger(IncomingHandler(this))
        return mMessenger.binder
    }

}