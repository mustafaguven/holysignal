package com.mguven.holysignal.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Suppress("UNCHECKED_CAST")
@Singleton
class ViewModelFactory @Inject constructor(
    private val viewModels: Map<Class<out ViewModel>,
        @JvmSuppressWildcards Provider<ViewModel>>) : ViewModelProvider.Factory {

  override fun <T : ViewModel> create(viewModelClz: Class<T>): T {
    var viewModel: Provider<out ViewModel>? = viewModels[viewModelClz]
    if (viewModel == null) {
      for ((key, value) in viewModels) {
        if (viewModelClz.isAssignableFrom(key)) {
          viewModel = value
          break
        }
      }
    }
    requireNotNull(viewModel) { "None view model found such as $viewModelClz" }
    try {
      return viewModel.get() as T
    } catch (e: Exception) {
      throw RuntimeException(e)
    }
  }
}