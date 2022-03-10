package com.sap.codelab.view.voice

import com.sap.codelab.model.VoiceCommands

interface VoiceRecognitionListener {

    fun onVoiceRecognitionResult(result: VoiceCommands)

    fun onVoiceServiceError()

    fun finishListening()
}