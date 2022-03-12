package com.sap.codelab.service.voice

/**
 * Supported commands for recognizer
 */
enum class VoiceCommands(val value: String) {
    UNKNOWN(""),
    CREATE_MEMO("create memo"),
    SHOW_ALL("show all"),
    SHOW_OPEN("show open")
}