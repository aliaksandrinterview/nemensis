package com.sap.codelab.view.voice

import com.sap.codelab.service.voice.VoiceCommands

interface VoiceRecognitionListener {

    fun onVoiceRecognitionResult(result: VoiceCommands)

    fun onVoiceRecognitionPreResult(recognitionResult: String)

    fun onVoiceServiceError()

    fun finishListening()
}