package id.anantyan.tfliteaudio.utils

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Created by Arya Rezza Anantya on 20/03/2024.
 */
fun Context.messageListDialog(
    title: String,
    items: Array<String>,
    onItemClickListener: (itemName: String, which: Int) -> Unit
) {
    MaterialAlertDialogBuilder(this)
        .setTitle(title)
        .setItems(items) { _, which ->
            onItemClickListener.invoke(items[which], which)
        }
        .setCancelable(true)
        .show()
}