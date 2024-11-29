package com.gruwier.ktordemo

class ChuckNorrisRepository(
    private val networkApi: NetworkApi,
) {

    suspend fun fetchJoke(): String = networkApi.getRandomJoke().value
}