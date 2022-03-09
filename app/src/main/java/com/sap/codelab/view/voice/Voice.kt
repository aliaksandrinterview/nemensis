package com.sap.codelab.view.voice

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import com.sap.codelab.utils.voice.VoiceService

abstract class Voice : AppCompatActivity() {

    private var voiceService: VoiceService? = null

    private var isServiceConnected = false
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (service != null) {
                isServiceConnected = true
                onServiceBind((service as VoiceService.VoiceServiceBinder).getService())
            }
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            onServiceBind(null)
            isServiceConnected = false
        }
    }

    fun bindVoiceService() {
        val intent = Intent(this, VoiceService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun unbindVoiceService() {
        if (isServiceConnected) {
            isServiceConnected = false
            unbindService(serviceConnection)
        }
    }

    private fun onServiceBind(service: VoiceService?) {
        if (service == null) {
            voiceService?.stopVoiceRecognition()
            voiceService = null
        } else {
            voiceService = service
            voiceService?.startVoiceRecognition()
        }
    }

    override fun onDestroy() {
        unbindVoiceService()
        super.onDestroy()
    }
}