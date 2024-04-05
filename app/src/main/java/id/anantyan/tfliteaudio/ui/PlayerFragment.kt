package id.anantyan.tfliteaudio.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import id.anantyan.tfliteaudio.R
import id.anantyan.tfliteaudio.databinding.FragmentPlayerBinding
import id.anantyan.tfliteaudio.helper.AudioInterpreterHelper
import id.anantyan.tfliteaudio.helper.AudioPlayerHelper
import id.anantyan.tfliteaudio.helper.AudioRecordHelper
import id.anantyan.tfliteaudio.utils.FileUtils
import id.anantyan.tfliteaudio.utils.ViewModelFactory
import id.anantyan.tfliteaudio.utils.messageListDialog
import id.anantyan.tfliteaudio.utils.viewModel

class PlayerFragment : Fragment(), ViewModelFactory<PlayerViewModel> {

    private var _binding: FragmentPlayerBinding? = null
    private val binding: FragmentPlayerBinding get() = _binding!!
    private val viewModel: PlayerViewModel by lazy { viewModel(this, this@PlayerFragment) }
    private val pickDocument: ActivityResultLauncher<Array<String>> = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            val file = FileUtils.getFileFromUri(requireContext(), it)
            viewModel.onPlayPlayer(file.absolutePath)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindObserver()
        bindView()
    }

    private fun bindView() {
        binding.bottomSheetLayout.apply {
            btnRecordStop.setOnClickListener {
                if (viewModel.audioOnRecording.value == true) {
                    viewModel.onStopRecording()
                } else {
                    requireActivity().getExternalFilesDir(null)?.let {
                        val output = it.absolutePath + "/audio.wav"
                        viewModel.onStartRecording(output)
                    }
                }
            }
            btnPlayingStop.setOnClickListener {
                if (viewModel.audioOnPlaying.value == true) {
                    viewModel.onStopPlayer()
                } else {
                    val items = arrayOf("Recording", "Document")
                    val mimeType = arrayOf("audio/x-wav")
                    requireContext().messageListDialog(
                        "Pilih untuk klasifikasi Audio",
                        items
                    ) { title, _ ->
                        when (title) {
                            items[0] -> { viewModel.onPlayPlayer() }
                            items[1] -> { pickDocument.launch(mimeType) }
                        }
                    }
                }
            }
        }
    }

    private fun bindObserver() {
        viewModel.audioResultClassification.observe(viewLifecycleOwner) { isResult ->
            binding.labelTextView.text = isResult
        }

        viewModel.audioOnRecording.observe(viewLifecycleOwner) { isRecord ->
            if (isRecord == true) {
                binding.bottomSheetLayout.btnRecordStop.apply {
                    text = "Stop"
                    setIconResource(R.drawable.ic_radio_button_checked_24px)
                }
            } else {
                binding.bottomSheetLayout.btnRecordStop.apply {
                    text = "Record"
                    setIconResource(R.drawable.ic_radio_button_unchecked_24px)
                }
            }
        }

        viewModel.audioOnPlaying.observe(viewLifecycleOwner) { isPlaying ->
            if (isPlaying == true) {
                binding.bottomSheetLayout.btnPlayingStop.apply {
                    text = "Stop"
                }

            } else {
                binding.bottomSheetLayout.btnPlayingStop.apply {
                    text = "Classify"
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.onStopRecording()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun create(): PlayerViewModel {
        val record = AudioRecordHelper()
        val player = AudioPlayerHelper()
        val interpreter = AudioInterpreterHelper(requireContext())
        return PlayerViewModel(record, player, interpreter)
    }

    override val viewModelClass: Class<PlayerViewModel>
        get() = PlayerViewModel::class.java
}