package com.sap.codelab.utils.voice

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

class VoiceService : Service() {

    private val binder = VoiceServiceBinder()
    private var isVoiceRecognitionStart = true
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
            if (error != SpeechRecognizer.ERROR_RECOGNIZER_BUSY) {
                startVoiceRecognition()
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
    private lateinit var speechRecognizer: SpeechRecognizer

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(speechResultListener)
    }

    private fun onRecognizerResult(result: List<String>) {
        if (result.isNotEmpty()) {

        }
    }

    fun startVoiceRecognition() {
        speechRecognizer.startListening(speechRecognizerIntent)
        isVoiceRecognitionStart = true
    }

    fun stopVoiceRecognition() {
        speechRecognizer.stopListening()
        isVoiceRecognitionStart = false
    }

    inner class VoiceServiceBinder : Binder() {

        fun getService(): VoiceService = this@VoiceService
    }
}