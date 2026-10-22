package com.codingkidz.app.platform

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/** Membacakan instruksi untuk anak yang belum lancar membaca (Text-to-Speech bahasa Indonesia). */
class Speaker(context: Context) {
    private var ready = false
    private var pending: String? = null
    private val tts: TextToSpeech = TextToSpeech(context.applicationContext) { status ->
        if (status == TextToSpeech.SUCCESS) {
            val id = Locale.forLanguageTag("id-ID")
            val result = tts.setLanguage(id)
            ready = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
            if (!ready) ready = tts.setLanguage(Locale.getDefault()) >= TextToSpeech.LANG_AVAILABLE
            tts.setSpeechRate(0.95f)
            tts.setPitch(1.1f)
            pending?.let { speak(it) }
            pending = null
        }
    }

    fun speak(text: String) {
        if (!ready) {
            pending = text
            return
        }
        tts.speak(text.replace("→", ","), TextToSpeech.QUEUE_FLUSH, null, "codingkidz")
    }

    fun stop() {
        pending = null
        if (ready) tts.stop()
    }
}
