package com.gruwier.ktordemo

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivityViewModel(
    private val chuckNorrisRepository: ChuckNorrisRepository
) : ViewModel() {

    private val _joke = MutableStateFlow<String?>(null)
    val joke = _joke.asStateFlow()

    val environment = MutableStateFlow<Environment>(
        runBlocking { chuckNorrisRepository.getEnvironment() }
    )

    fun fetchJoke() {
        viewModelScope.launch {
            val joke = chuckNorrisRepository.fetchJoke()
            _joke.update { joke }
        }
    }

    fun switchEnvironment() {
        viewModelScope.launch{
            chuckNorrisRepository.setEnvironment()
            throw IllegalStateException()
        }
    }
}