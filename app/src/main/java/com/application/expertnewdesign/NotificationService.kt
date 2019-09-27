package com.application.expertnewdesign

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.widget.Toast



class NotificationService(val path: String) : Service(){
    override fun onBind(p0: Intent?): IBinder? {
        return null//To change body of created functions use File | Settings | File Templates.
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Toast.makeText(
            this, "Служба запущена",
            Toast.LENGTH_SHORT
        ).show()
        return super.onStartCommand(intent, flags, startId)
    }
}