package com.sap.codelab.view.voice

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sap.codelab.R
import com.sap.codelab.utils.voice.VoiceService

/**
 * The abstract activity for voice recognition. For start recognition call startVoiceService() function.
 **/
abstract class Voice : AppCompatActivity(), VoiceRecognitionListener {

    /**
     * Voice recognition service
     */
    private var voiceService: VoiceService? = null

    /**
     * Current state of voice recognition service
     */
    private var isServiceConnected = false

    /**
     * Object for monitoring the state of an application voice recognition service
     */
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            service?.let { startListeningVoice((it as VoiceService.VoiceServiceBinder).getService()) }
        }

        override fun onServiceDisconnected(p0: ComponentName?) = stopListeningVoice()
    }

    /**
     * Function for start voice recognition service
     **/
    protected fun startVoiceService() {
        if (isVoicePermissionGranted()) {
            val intent = Intent(this, VoiceService::class.java)
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        } else {
            requestVoicePermission()
        }
    }

    /**
     * Function for stop voice recognition service
     **/
    protected fun stopVoiceService() {
        if (isServiceConnected) {
            isServiceConnected = false
            unbindService(serviceConnection)
        }
    }

    override fun onVoiceServiceError() {
        Toast.makeText(this, R.string.voice_service_error, Toast.LENGTH_LONG).show()
    }

    override fun finishListening() {
        stopVoiceService()
    }

    /**
     * Show message about unknown command to user
     **/
    protected fun unknownCommand() {
        Toast.makeText(this, R.string.unknown_command, Toast.LENGTH_LONG).show()
    }

    /**
     * Start listening voice after successful connected service.
     * @param service voice recognition service
     */
    private fun startListeningVoice(service: VoiceService) {
        isServiceConnected = true
        voiceService = service
        voiceService?.startVoiceRecognition(this)
        Toast.makeText(this, R.string.say_command, Toast.LENGTH_LONG).show()
    }

    /**
     * Stop listening voice after disconnect service.
     */
    private fun stopListeningVoice() {
        voiceService?.stopVoiceRecognition()
        voiceService = null
        isServiceConnected = false
    }

    /**
     * Function check record audio permission for access to listen voice
     */
    private fun isVoicePermissionGranted(): Boolean =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

    /**
     * Function that request record audio permission for access to listen voice
     */
    private fun requestVoicePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            RECORD_AUDIO_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == RECORD_AUDIO_REQUEST_CODE && grantResults.all { it == PackageManager.PERMISSION_GRANTED })
            startVoiceService()
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onDestroy() {
        stopVoiceService()
        super.onDestroy()
    }

    companion object {
        private const val RECORD_AUDIO_REQUEST_CODE = 342
    }
}