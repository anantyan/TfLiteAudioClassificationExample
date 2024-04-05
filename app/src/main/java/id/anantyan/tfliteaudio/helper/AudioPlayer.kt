package id.anantyan.tfliteaudio.helper

import java.io.File

/**
 * Created by Arya Rezza Anantya on 18/03/2024.
 */
interface AudioPlayer {
    fun playSound(file: String)
    fun stopSound()
    fun stopListener(listener: () -> Unit)
}