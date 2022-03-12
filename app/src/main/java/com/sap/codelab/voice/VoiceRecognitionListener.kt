package com.sap.codelab.voice

interface VoiceRecognitionListener {

    fun onVoiceRecognitionResult(result: VoiceCommands)

    fun onVoiceRecognitionPreResult(recognitionResult: String)

    fun onVoiceServiceError()

    fun finishListening()
}