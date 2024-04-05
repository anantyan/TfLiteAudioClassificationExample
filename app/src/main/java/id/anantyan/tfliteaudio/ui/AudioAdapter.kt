package id.anantyan.tfliteaudio.ui
import android.content.res.ColorStateList
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import id.anantyan.tfliteaudio.R
import id.anantyan.tfliteaudio.databinding.ItemAudioBinding
import org.tensorflow.lite.support.label.Category

/**
* Created by Arya Rezza Anantya on 14/03/2024.
*/
class AudioAdapter : ListAdapter<Category, AudioAdapter.CategoriesViewHolder>(CategoriesComparator)  {

    private object CategoriesComparator : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem.index == newItem.index
        }

        override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriesViewHolder {
        return CategoriesViewHolder(
            ItemAudioBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CategoriesViewHolder, position: Int) {
        val category = getItem(position)
        holder.bind(category.label, category.score, category.index)
    }

    class CategoriesViewHolder(private val binding: ItemAudioBinding) : RecyclerView.ViewHolder(binding.root){
        private var primaryColor: IntArray = binding.root.resources.getIntArray((R.array.colors_progress_primary))
        private var backgroundColor: IntArray = binding.root.resources.getIntArray((R.array.colors_progress_background))
        fun bind(label: String, score: Float, index: Int){
            val value: Int = (score * 100).toInt()
            binding.apply {
                labelTextView.text = label
                progressBar.progressBackgroundTintList = ColorStateList.valueOf(backgroundColor[index % backgroundColor.size])
                progressBar.progressTintList = ColorStateList.valueOf(primaryColor[index % primaryColor.size])
                progressBar.setProgress(value, true)
            }
        }
    }

    companion object {
        private val noiseMap = mapOf(
            "Silence" to "Non Noise"
        )

        fun getNoiseLabel(word: String?): String {
            return noiseMap[word] ?: "Noise"
        }
    }
}