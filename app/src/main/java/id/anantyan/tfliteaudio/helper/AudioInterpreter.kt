package id.anantyan.tfliteaudio.helper

/**
 * Created by Arya Rezza Anantya on 19/03/2024.
 */
interface AudioInterpreter {
    fun onResult(listener: (String) -> Unit)
    fun onClassification(outputFile: String)
}