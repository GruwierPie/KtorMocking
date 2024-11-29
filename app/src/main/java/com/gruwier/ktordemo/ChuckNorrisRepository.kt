package com.gruwier.ktordemo

class ChuckNorrisRepository(
    private val networkApi: NetworkApi,
    private val dataStore: AppPreferencesDataStore
) {

    suspend fun fetchJoke(): String = networkApi.getRandomJoke().value

    suspend fun getEnvironment(): Environment = dataStore.getEnvironment()

    suspend fun setEnvironment() {
        val environment = when(getEnvironment()){
            Environment.PROD -> Environment.MOCKED
            Environment.MOCKED -> Environment.PROD
        }
        dataStore.updateEnvironment(environment)
    }
}