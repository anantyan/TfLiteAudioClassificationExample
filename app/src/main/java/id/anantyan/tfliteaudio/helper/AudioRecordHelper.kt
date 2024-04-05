package id.anantyan.tfliteaudio.helper

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import com.github.squti.androidwaverecorder.RecorderState
import com.github.squti.androidwaverecorder.WaveRecorder
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by Arya Rezza Anantya on 18/03/2024.
 */
class AudioRecordHelper : AudioRecord {
    private var waveRecorder: WaveRecorder? = null

    override fun startRecord(outputFile: String) {
        waveRecorder = WaveRecorder(outputFile).apply {
            startRecording()
        }
    }

    override fun stopRecord() {
        waveRecorder?.stopRecording()
    }
}