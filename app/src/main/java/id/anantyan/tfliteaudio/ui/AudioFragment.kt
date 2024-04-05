package id.anantyan.tfliteaudio.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import com.github.squti.androidwaverecorder.WaveRecorder
import id.anantyan.tfliteaudio.R
import id.anantyan.tfliteaudio.databinding.FragmentAudioBinding
import id.anantyan.tfliteaudio.helper.AudioClassification
import id.anantyan.tfliteaudio.helper.AudioClassificationHelper
import id.anantyan.tfliteaudio.utils.showNotification
import org.tensorflow.lite.support.label.Category

/**
 * Created by Arya Rezza Anantya on 14/03/2024.
 */
class AudioFragment : Fragment(), AudioClassification {

    private var _binding: FragmentAudioBinding? = null
    private val binding: FragmentAudioBinding get() = _binding!!
    private val helper: AudioClassificationHelper by lazy { AudioClassificationHelper(requireContext(), this) }
    private val adapter: AudioAdapter by lazy { AudioAdapter() }

    private val requestPermissionLauncher: ActivityResultLauncher<Array<String>> = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { helper.bindStart() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAudioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindView()
    }

    private fun bindView() {
        binding.recyclerView.apply {
            itemAnimator = DefaultItemAnimator()
            adapter = this@AudioFragment.adapter
        }
        binding.bottomSheetLayout.apply {
            modelSelector.setOnCheckedChangeListener { _, checkedId ->
                /**
                 * Setup click radio button
                 * Ketika user ingin mengganti jenis model yang diinginkan
                 * Serta developer bisa menambahkan sebuah model baru dari UI yang telah ditambah
                 * */
                when (checkedId) {
                    R.id.yamnet -> {
                        helper.bindStop()
                        helper.currentModel = AudioClassificationHelper.YAMNET_MODEL
                        helper.bindInit()
                    }

                    R.id.speech_command -> {
                        helper.bindStop()
                        helper.currentModel = AudioClassificationHelper.SPEECH_COMMAND_MODEL
                        helper.bindInit()
                    }

                    R.id.noise_model -> {
                        helper.bindStop()
                        helper.currentModel = AudioClassificationHelper.NOISE
                        helper.bindInit()
                    }

                    R.id.bird_model -> {
                        helper.bindStop()
                        helper.currentModel = AudioClassificationHelper.BIRD_MODEL
                        helper.bindInit()
                    }
                }
            }
        }
    }

    private fun shouldPermission() {
        when {
            shouldShowRequestPermissionRationale(android.Manifest.permission.RECORD_AUDIO) -> {
                /**
                 * Sebenarnya lebih baik menggunakan Custom Alert Dialog untuk memenuhi
                 * UX yang baik
                 * */
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", requireActivity().packageName, null)
                    startActivity(this)
                }
            }
            else -> requestPermissionLauncher.launch(PERMISSION_REQUIRED)
        }
    }

    override fun onResume() {
        super.onResume()
        if (!hasPermission(requireContext())) shouldPermission() else helper.bindStart()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        helper.bindStop()
        _binding = null
    }

    @SuppressLint("SetTextI18n")
    override fun onResult(results: List<Category>, inferenceTime: Long) {
        binding.bottomSheetLayout.inferenceTimeVal.text = "${inferenceTime}ms"
        adapter.submitList(results)
        results.forEach { category: Category ->
            requireContext().showNotification(
                category.label,
                "Probabilitas: ${category.score} - Duration: ${inferenceTime}ms"
            )
        }
    }

    override fun onError(error: String) {
        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        adapter.submitList(emptyList())
    }

    companion object {
        private val PERMISSION_REQUIRED: Array<String> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                android.Manifest.permission.RECORD_AUDIO,
                android.Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            arrayOf(
                android.Manifest.permission.RECORD_AUDIO
            )
        }

        fun hasPermission(context: Context): Boolean = PERMISSION_REQUIRED.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}