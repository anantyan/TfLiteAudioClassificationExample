package id.anantyan.tfliteaudio.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner

/**
 * Created by Arya Rezza Anantya on 18/03/2024.
 */

fun <T : ViewModel> viewModel(owner: ViewModelStoreOwner, factory: ViewModelFactory<T>): T {
    val vmFactory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
        override fun <U : ViewModel> create(modelClass: Class<U>): U {
            return factory.create() as U
        }
    }
    return ViewModelProvider(owner, vmFactory)[factory.viewModelClass]
}

interface ViewModelFactory<T : ViewModel> {
    fun create(): T
    val viewModelClass: Class<T>
}