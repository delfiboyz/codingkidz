package com.codingkidz.app.platform

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

enum class Sfx { TAP, MOVE, TURN, PICK, PAINT, BUMP, SUCCESS, FAIL, STAR, CORRECT, WRONG, UNLOCK }

/**
 * Efek suara yang disintesis saat aplikasi pertama dibuka (tanpa file audio di APK),
 * lalu diputar lewat SoundPool supaya latensinya rendah.
 */
class SoundFx(private val context: Context) {
    private val pool = SoundPool.Builder()
        .setMaxStreams(6)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()
    private val ids = java.util.concurrent.ConcurrentHashMap<Sfx, Int>()
    var enabled = true

    init {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            val dir = File(context.cacheDir, "sfx-v1").apply { mkdirs() }
            Sfx.entries.forEach { sfx ->
                val file = File(dir, "${sfx.name.lowercase()}.wav")
                if (!file.exists()) writeWav(file, synth(sfx))
                ids[sfx] = pool.load(file.absolutePath, 1)
            }
        }
    }

    fun play(sfx: Sfx, volume: Float = 0.8f) {
        if (!enabled) return
        val id = ids[sfx] ?: return
        pool.play(id, volume, volume, 1, 0, 1f)
    }

    private data class Note(val freq: Double, val durMs: Int, val endFreq: Double = freq, val decay: Double = 6.0)

    private fun synth(sfx: Sfx): ShortArray = when (sfx) {
        Sfx.TAP -> render(Note(880.0, 45, 990.0, 10.0))
        Sfx.MOVE -> render(Note(520.0, 80, 680.0, 7.0))
        Sfx.TURN -> render(Note(440.0, 60, 520.0, 8.0))
        Sfx.PICK -> render(Note(988.0, 70), Note(1319.0, 110))
        Sfx.PAINT -> render(Note(620.0, 90, 930.0, 5.0))
        Sfx.BUMP -> render(Note(160.0, 160, 90.0, 4.0), harmonics = 4)
        Sfx.SUCCESS -> render(Note(523.0, 100), Note(659.0, 100), Note(784.0, 100), Note(1047.0, 260, decay = 3.0))
        Sfx.FAIL -> render(Note(392.0, 140), Note(330.0, 140), Note(262.0, 260, decay = 3.0))
        Sfx.STAR -> render(Note(1568.0, 160, decay = 5.0))
        Sfx.CORRECT -> render(Note(784.0, 90), Note(1175.0, 180, decay = 4.0))
        Sfx.WRONG -> render(Note(220.0, 220, 196.0, 3.0), harmonics = 3)
        Sfx.UNLOCK -> render(Note(659.0, 90), Note(880.0, 90), Note(1319.0, 90), Note(1760.0, 240, decay = 3.0))
    }

    private fun render(vararg notes: Note, harmonics: Int = 2): ShortArray {
        val samples = notes.sumOf { it.durMs * SAMPLE_RATE / 1000 }
        val out = ShortArray(samples)
        var offset = 0
        for (note in notes) {
            val n = note.durMs * SAMPLE_RATE / 1000
            var phase = 0.0
            for (i in 0 until n) {
                val t = i.toDouble() / n
                val freq = note.freq + (note.endFreq - note.freq) * t
                phase += 2 * PI * freq / SAMPLE_RATE
                var v = 0.0
                for (h in 1..harmonics) v += sin(phase * h) / (h * h)
                val attack = (i / (SAMPLE_RATE * 0.004)).coerceAtMost(1.0)
                val env = attack * exp(-note.decay * t)
                out[offset + i] = (v * env * 0.55 * Short.MAX_VALUE).toInt().coerceIn(-32767, 32767).toShort()
            }
            offset += n
        }
        return out
    }

    private fun writeWav(file: File, pcm: ShortArray) {
        val dataSize = pcm.size * 2
        val buf = ByteBuffer.allocate(44 + dataSize).order(ByteOrder.LITTLE_ENDIAN)
        buf.put("RIFF".toByteArray()).putInt(36 + dataSize).put("WAVE".toByteArray())
        buf.put("fmt ".toByteArray()).putInt(16).putShort(1).putShort(1)
            .putInt(SAMPLE_RATE).putInt(SAMPLE_RATE * 2).putShort(2).putShort(16)
        buf.put("data".toByteArray()).putInt(dataSize)
        pcm.forEach { buf.putShort(it) }
        FileOutputStream(file).use { it.write(buf.array()) }
    }

    private companion object {
        const val SAMPLE_RATE = 22050
    }
}
