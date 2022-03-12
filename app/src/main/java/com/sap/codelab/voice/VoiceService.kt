package com.sap.codelab.voice

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.sap.codelab.BuildConfig
import java.util.*

/**
 * Service for recognizing voice commands
 */
class VoiceService : Service() {

    private val binder = VoiceServiceBinder()

    /**
     * Current state of recognition listening
     */
    private var isVoiceRecognitionStart = true

    /**
     * Interface for sending recognition results
     */
    private var voiceListener: VoiceRecognitionListener? = null

    /**
     * Intent for start speech recognition
     */
    private val speechRecognizerIntent: Intent by lazy {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, BuildConfig.APPLICATION_ID)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
    }

    /**
     * Object used for receiving notifications from the SpeechRecognizer when the recognition related events occur
     */
    private val speechResultListener = object : RecognitionListener {

        override fun onReadyForSpeech(params: Bundle?) {
            val texts = params?.getStringArrayList(
                SpeechRecognizer.RESULTS_RECOGNITION
            ) ?: emptyList<String>()
            Log.d("VoiceService", "onReadyForSpeech: ${texts.joinToString(" ")}")
        }

        override fun onRmsChanged(rmsdB: Float) {
            Log.d("VoiceService", "onRmsChanged")
        }

        override fun onBufferReceived(buffer: ByteArray?) {
            Log.d("VoiceService", "onBufferReceived")
        }

        override fun onPartialResults(partialResults: Bundle) {
            val texts = partialResults.getStringArrayList(
                SpeechRecognizer.RESULTS_RECOGNITION
            ) ?: emptyList<String>()
            Log.d("VoiceService", "onPartialResults: ${texts.joinToString(" ")}")
        }

        override fun onEvent(eventType: Int, params: Bundle?) {
            Log.d("VoiceService", "onEvent")
        }

        override fun onBeginningOfSpeech() {
            Log.d("VoiceService", "onBeginningOfSpeech")
        }

        override fun onEndOfSpeech() {
            Log.d("VoiceService", "onEndOfSpeech")
        }

        override fun onError(error: Int) {
            if (isVoiceRecognitionStart)
                if (error != SpeechRecognizer.ERROR_RECOGNIZER_BUSY) {
                    startVoiceRecognition(voiceListener)
                } else {
                    voiceListener?.onVoiceServiceError()
                }
        }

        override fun onResults(results: Bundle) {
            val texts = results.getStringArrayList(
                SpeechRecognizer.RESULTS_RECOGNITION
            ) ?: emptyList<String>()
            Log.d("VoiceService", "onResults: ${texts.joinToString(" ")}")
            onRecognizerResult(texts)
        }
    }

    /**
     * This service allows access to the speech recognizer
     */
    private lateinit var speechRecognizer: SpeechRecognizer

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(speechResultListener)
    }

    /**
     * Function for start listen commands
     * @param voiceListener listener for send recognition results
     */
    fun startVoiceRecognition(voiceListener: VoiceRecognitionListener?) {
        this.voiceListener = voiceListener
        speechRecognizer.startListening(speechRecognizerIntent)
        isVoiceRecognitionStart = true
    }

    /**
     * Function for stop listen commands
     */
    fun stopVoiceRecognition() {
        voiceListener = null
        speechRecognizer.cancel()
        isVoiceRecognitionStart = false
    }

    /**
     * Process recognition results
     * @param result list of recognitions
     */
    private fun onRecognizerResult(result: List<String>) {
        if (result.isNotEmpty()) {
            val recognitionResult = result.joinToString(separator = " ")
            voiceListener?.onVoiceRecognitionPreResult(recognitionResult)
            val command =
                VoiceCommands.values()
                    .firstOrNull { it.value == recognitionResult }
                    ?: VoiceCommands.UNKNOWN
            voiceListener?.onVoiceRecognitionResult(command)
        } else {
            voiceListener?.onVoiceRecognitionResult(VoiceCommands.UNKNOWN)
        }
        voiceListener?.finishListening()
    }

    inner class VoiceServiceBinder : Binder() {
        fun getService(): VoiceService = this@VoiceService
    }
}