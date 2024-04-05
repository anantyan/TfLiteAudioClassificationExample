package id.anantyan.tfliteaudio.helper

import android.content.Context
import android.media.AudioRecord
import android.os.SystemClock
import android.util.Log
import org.tensorflow.lite.support.audio.TensorAudio
import org.tensorflow.lite.task.audio.classifier.AudioClassifier
import org.tensorflow.lite.task.audio.classifier.Classifications
import org.tensorflow.lite.task.core.BaseOptions
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Created by Arya Rezza Anantya on 13/03/2024.
 */
class AudioClassificationHelper(
    private val context: Context,
    private val listener: AudioClassification,
    var currentModel: String = YAMNET_MODEL,
    private var classificationThreshold: Float = DISPLAY_THRESHOLD,
    private var overlap: Float = DEFAULT_OVERLAP_VALUE,
    private var numOfResults: Int = DEFAULT_NUM_OF_RESULTS,
    private var currentDelegate: Int = 0,
    private var numThreads: Int = 4
) {
    private lateinit var classifier: AudioClassifier
    private lateinit var tensorAudio: TensorAudio
    private lateinit var recorder: AudioRecord
    private lateinit var executor: ScheduledThreadPoolExecutor

    private val classifyRunnable = Runnable { classifyAudio() }

    init { bindInit() }

    fun bindInit() {
        val baseOptionsBuilder: BaseOptions.Builder = BaseOptions.builder().setNumThreads(numThreads)
        when (currentDelegate) {
            DELEGATE_CPU -> {}
            DELEGATE_NNAPI -> baseOptionsBuilder.useNnapi()
        }

        val options: AudioClassifier.AudioClassifierOptions = AudioClassifier.AudioClassifierOptions.builder()
            .setScoreThreshold(classificationThreshold)
            .setMaxResults(numOfResults)
            .setBaseOptions(baseOptionsBuilder.build())
            .build()

        try {
            classifier = AudioClassifier.createFromFileAndOptions(context, currentModel, options)
            tensorAudio = classifier.createInputTensorAudio()
            recorder = classifier.createAudioRecord()
            bindStart()
        } catch (e: IllegalStateException) {
            listener.onError("Pengklasifikasi Audio gagal melakukan inisialisasi. Lihat log kesalahan untuk rinciannya $e")
            Log.e("AudioClassification", "TFLite gagal memuat dengan kesalahan: " + e.message)
        }
    }

    fun bindStart() {
        if (recorder.recordingState == AudioRecord.RECORDSTATE_RECORDING) return

        recorder.startRecording()
        executor = ScheduledThreadPoolExecutor(1)

        val lengthInMilliSeconds: Float = ((classifier.requiredInputBufferSize * 1.0f) / classifier.requiredTensorAudioFormat.sampleRate) * 1000
        val interval: Long = (lengthInMilliSeconds * (1 - overlap)).toLong()

        executor.scheduleAtFixedRate(
            classifyRunnable,
            0,
            interval,
            TimeUnit.MILLISECONDS
        )
    }

    fun bindStop() {
        recorder.stop()
        executor.shutdownNow()
    }

    private fun classifyAudio() {
        tensorAudio.load(recorder)
        var inferenceTime: Long = SystemClock.uptimeMillis()
        val output: List<Classifications> = classifier.classify(tensorAudio)
        inferenceTime = SystemClock.uptimeMillis() - inferenceTime
        output.forEach { it: Classifications ->
            listener.onResult(it.categories, inferenceTime)
        }
    }

    companion object {
        const val DELEGATE_CPU = 0
        const val DELEGATE_NNAPI = 1
        const val DISPLAY_THRESHOLD = 0.1f
        const val DEFAULT_NUM_OF_RESULTS = 1
        const val DEFAULT_OVERLAP_VALUE = 0.5f
        /**
         * Cukup ganti jenis model Tensor Flow Lite disini
         * */
        const val YAMNET_MODEL = "yamnet_model.tflite"
        const val SPEECH_COMMAND_MODEL = "speech_model.tflite"
        const val NOISE = "noise_model.tflite"
        const val BIRD_MODEL = "birds_model.tflite"
    }
}