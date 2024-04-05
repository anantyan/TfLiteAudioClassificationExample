package id.anantyan.tfliteaudio.helper

import org.tensorflow.lite.support.label.Category

/**
 * Created by Arya Rezza Anantya on 18/03/2024.
 */
interface AudioClassification {
    fun onError(error: String)
    fun onResult(results: List<Category>, inferenceTime: Long)
}