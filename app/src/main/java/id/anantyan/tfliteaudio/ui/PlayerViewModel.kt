package id.anantyan.tfliteaudio.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import id.anantyan.tfliteaudio.helper.AudioInterpreter
import id.anantyan.tfliteaudio.helper.AudioPlayer
import id.anantyan.tfliteaudio.helper.AudioRecord

/**
 * Created by Arya Rezza Anantya on 18/03/2024.
 */
class PlayerViewModel(
    private val record: AudioRecord,
    private val player: AudioPlayer,
    private val audioInterpreter: AudioInterpreter
) : ViewModel() {
    private var _audioFile: MutableLiveData<String> = MutableLiveData(null)
    private var _audioOnRecording: MutableLiveData<Boolean> = MutableLiveData(false)
    private var _audioOnPlaying: MutableLiveData<Boolean> = MutableLiveData(false)
    private var _audioResultClassification: MutableLiveData<String> = MutableLiveData()

    val audioOnRecording: LiveData<Boolean> = _audioOnRecording
    val audioOnPlaying: LiveData<Boolean> = _audioOnPlaying
    val audioResultClassification: LiveData<String> = _audioResultClassification

    fun onStartRecording(file: String) {
        record.startRecord(file)
        _audioFile.value = file
        _audioOnRecording.value = true
    }

    fun onStopRecording() {
        record.stopRecord()
        _audioOnRecording.value = false
    }

    fun onPlayPlayer() {
        _audioOnPlaying.value = true
        _audioFile.value?.let { file ->
            player.playSound(file)
            audioInterpreter.onClassification(file)
        }
        player.stopListener {
            _audioOnPlaying.value = false
            player.stopSound()
        }
        audioInterpreter.onResult { result -> _audioResultClassification.value = result }
    }

    fun onPlayPlayer(file: String) {
        _audioOnPlaying.value = true
        player.playSound(file)
        audioInterpreter.onClassification(file)
        player.stopListener {
            _audioOnPlaying.value = false
            player.stopSound()
        }
        audioInterpreter.onResult { result -> _audioResultClassification.value = result }
    }

    fun onStopPlayer() {
        player.stopSound()
        _audioOnPlaying.value = false
    }
}