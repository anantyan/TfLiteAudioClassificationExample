package id.anantyan.tfliteaudio.helper

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import android.util.Log
import androidx.core.net.toUri
import java.io.File

/**
 * Created by Arya Rezza Anantya on 18/03/2024.
 */
class AudioPlayerHelper : AudioPlayer {
    private var player: MediaPlayer? = null

    override fun playSound(file: String) {
        val mediaPlayer = MediaPlayer()
        try {
            mediaPlayer.setDataSource(file)
            mediaPlayer.prepare()
            mediaPlayer.start()
            player = mediaPlayer
        } catch (e: Exception) {
            Log.d("DEBUGGING", e.message.toString(), e)
        }
    }

    override fun stopSound() {
        player?.stop()
        player?.release()
        player = null
    }

    override fun stopListener(listener: () -> Unit) {
        player?.setOnCompletionListener { listener.invoke() }
    }
}