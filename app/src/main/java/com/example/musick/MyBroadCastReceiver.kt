package com.example.musick

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class MyBroadCastReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action: String = intent.getAction().toString()

        val intentService = Intent(context, PlayerService::class.java)
        intentService.setAction(action)
        context.startService(intentService)
    }
}