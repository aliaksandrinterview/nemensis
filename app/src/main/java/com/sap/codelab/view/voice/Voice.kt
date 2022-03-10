package com.sap.codelab.view.voice

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sap.codelab.R
import com.sap.codelab.model.VoiceCommands
import com.sap.codelab.utils.voice.VoiceService

abstract class Voice : AppCompatActivity(), VoiceRecognitionListener {

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

    protected fun bindVoiceService() {
        if (isVoicePermissionGranted()) {
            val intent = Intent(this, VoiceService::class.java)
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        } else {
            requestVoicePermission()
        }
    }

    protected fun unbindVoiceService() {
        if (isServiceConnected) {
            isServiceConnected = false
            unbindService(serviceConnection)
        }
    }

    override fun onVoiceServiceError() {
        Toast.makeText(this, R.string.voice_service_error, Toast.LENGTH_LONG).show()
    }

    override fun finishListening() {
        unbindVoiceService()
    }

    protected fun unknownCommand() {
        Toast.makeText(this, R.string.unknown_command, Toast.LENGTH_LONG).show()
    }

    private fun onServiceBind(service: VoiceService?) {
        if (service == null) {
            voiceService?.stopVoiceRecognition()
            voiceService = null
        } else {
            voiceService = service
            voiceService?.startVoiceRecognition(this)
            Toast.makeText(this, R.string.say_command, Toast.LENGTH_LONG).show()
        }
    }

    private fun isVoicePermissionGranted(): Boolean =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

    private fun requestVoicePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            RECORD_AUDIO_REQUEST_CODE
        )
    }

    override fun onDestroy() {
        unbindVoiceService()
        super.onDestroy()
    }

    companion object {
        private const val RECORD_AUDIO_REQUEST_CODE = 342
    }
}