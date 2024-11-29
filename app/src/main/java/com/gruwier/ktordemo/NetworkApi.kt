package com.gruwier.ktordemo

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.http.appendPathSegments

class NetworkApi(private val ktor: HttpClient) {

    suspend fun getRandomJoke(): JokeResponse = safeCall {
        ktor.get {
            url("https://api.chucknorris.io/jokes/random")
        }
    }

}