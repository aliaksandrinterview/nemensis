package com.sap.codelab.view.voice

import android.Manifest
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sap.codelab.service.voice.VoiceCommands
import com.sap.codelab.service.voice.VoiceService

/**
 * The abstract activity for voice recognition. For start recognition call startVoiceService() function.
 **/
class VoiceRecognizer(private val builder: Builder) : VoiceRecognitionListener {

    /**
     * Voice recognition service
     */
    private var voiceService: VoiceService? = null

    /**
     * Current state of voice recognition service
     */
    var isRecognitionStarted = false
        private set

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
    fun start(): VoiceRecognizer {
        if (isVoicePermissionGranted()) {
            builder.activity.let {
                val intent = Intent(it, VoiceService::class.java)
                it.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            }
        } else {
            requestVoicePermission()
        }
        return this
    }

    /**
     * Function for stop voice recognition service
     **/
    fun stop() {
        if (isRecognitionStarted) {
            isRecognitionStarted = false
            stopListeningVoice()
            builder.let {
                it.activity.unbindService(serviceConnection)
                it.onStop?.invoke()
            }
        }
    }

    override fun onVoiceRecognitionResult(result: VoiceCommands) {
        builder.onVoiceRecognitionResult?.invoke(result)
    }

    override fun onVoiceRecognitionPreResult(recognitionResult: String) {
        builder.onVoiceRecognitionPreResult?.invoke(recognitionResult)
    }

    override fun onVoiceServiceError() {
        builder.onVoiceRecognitionError?.invoke()
    }

    override fun finishListening() {
        stop()
    }

    /**
     * Start listening voice after successful connected service.
     * @param service voice recognition service
     */
    private fun startListeningVoice(service: VoiceService) {
        isRecognitionStarted = true
        voiceService = service
        voiceService?.startVoiceRecognition(this)
        builder.onVoiceRecognitionStarted?.invoke()
    }

    /**
     * Stop listening voice after disconnect service.
     */
    private fun stopListeningVoice() {
        voiceService?.stopVoiceRecognition()
        voiceService = null
        isRecognitionStarted = false
    }

    /**
     * Function check record audio permission for access to listen voice
     */
    private fun isVoicePermissionGranted(): Boolean =
        ContextCompat.checkSelfPermission(
            builder.activity,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

    /**
     * Function that request record audio permission for access to listen voice
     */
    private fun requestVoicePermission() {
        ActivityCompat.requestPermissions(
            builder.activity,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            RECORD_AUDIO_REQUEST_CODE
        )
    }

    class Builder(public val activity: Activity) {

        var onVoiceRecognitionResult: ((result: VoiceCommands) -> Unit)? = null
            private set
        var onVoiceRecognitionStarted: (() -> Unit)? = null
            private set
        var onVoiceRecognitionPreResult: ((recognitionResult: String) -> Unit)? = null
            private set
        var onVoiceRecognitionError: (() -> Unit)? = null
            private set
        var onStop: (() -> Unit)? = null
            private set

        fun setOnVoiceRecognitionResult(onVoiceRecognitionResult: ((result: VoiceCommands) -> Unit)): Builder {
            this.onVoiceRecognitionResult = onVoiceRecognitionResult
            return this
        }

        fun setOnVoiceRecognitionPreResult(onVoiceRecognitionPreResult: ((recognitionResult: String) -> Unit)): Builder {
            this.onVoiceRecognitionPreResult = onVoiceRecognitionPreResult
            return this
        }

        fun setOnVoiceRecognitionStarted(onVoiceRecognitionStarted: (() -> Unit)): Builder {
            this.onVoiceRecognitionStarted = onVoiceRecognitionStarted
            return this
        }

        fun setOnVoiceRecognitionError(onVoiceRecognitionError: (() -> Unit)): Builder {
            this.onVoiceRecognitionError = onVoiceRecognitionError
            return this
        }

        fun setOnStop(onStop: (() -> Unit)): Builder {
            this.onStop = onStop
            return this
        }

        fun build(): VoiceRecognizer = VoiceRecognizer(this)
    }

    companion object {
        private const val RECORD_AUDIO_REQUEST_CODE = 342
    }
}