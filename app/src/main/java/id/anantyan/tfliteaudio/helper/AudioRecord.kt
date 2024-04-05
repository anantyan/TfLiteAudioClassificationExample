package id.anantyan.tfliteaudio.helper

import java.io.File

/**
 * Created by Arya Rezza Anantya on 18/03/2024.
 */
interface AudioRecord {
    fun startRecord(outputFile: String)
    fun stopRecord()
}