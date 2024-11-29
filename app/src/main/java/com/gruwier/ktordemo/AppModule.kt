package com.gruwier.ktordemo

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngine.Companion.invoke
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.accept
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import io.ktor.http.headersOf
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.util.Locale

private enum class Engine {
    Network,
    Mocked,
}

val appModule = module {

    factoryOf(::NetworkApi)
    factoryOf(::AppPreferencesDataStore)
    singleOf(::ChuckNorrisRepository)
    viewModelOf(::MainActivityViewModel)

    factory<HttpClient> {

        if (false) {
            get<HttpClient>(named(Engine.Mocked))
        } else {
            get<HttpClient>(qualifier = named(Engine.Network))
        }
    }

    factory<HttpClient>(named(Engine.Network)) { params ->
        val client = HttpClient(OkHttp) {
            defaultRequest {
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
            }

            install(Logging) {
                logger = Logger.ANDROID
                level = LogLevel.ALL
                sanitizeHeader { header -> header == HttpHeaders.Authorization }
            }

            // Add and refresh  to call token
            /*install(Auth) {
                bearer {
                    loadTokens { BearerTokens(getTokenUseCase(false), "") }
                    refreshTokens { BearerTokens(getTokenUseCase(true), "") }
                }
            }*/

            HttpResponseValidator {
                validateResponse { response ->
                    if (!response.status.isSuccess()) {
                        when (response.status.value) {
                            401 -> Log.e("", "Receive 401, let's disconnect user")
                            else -> Log.e("", "Receive another error")
                        }
                    }
                }
            }

            // Using kotlinx-serialization for json parsing
            install(ContentNegotiation) {
                json(
                    Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                    },
                )
            }
        }

        // Interceptor to add api key
        /*client.plugin(HttpSend).intercept { request ->
            request.headers {
                if (!this.contains(com.decathlon.geartrack.core.network.utils.HttpHeaders.X_API_KEY)) {
                    append(com.decathlon.geartrack.core.network.utils.HttpHeaders.X_API_KEY, apiDef.key)
                }
                if (!this.contains(com.decathlon.geartrack.core.network.utils.HttpHeaders.X_ENV)) {
                    append(com.decathlon.geartrack.core.network.utils.HttpHeaders.X_ENV, apiDef.environment.environmentHeader)
                }
            }
            execute(request)
        }*/
        client
    }

    factory<HttpClient>(named(Engine.Mocked)) {
        HttpClient(
            MockEngine { httpRequestData ->
                try {
                    androidContext()
                        .assets
                        .open(
                            "${httpRequestData.method.value.lowercase(Locale.ROOT)}_${
                                httpRequestData.url.encodedPathAndQuery.removePrefix(
                                    "/"
                                )
                            }"
                                .replace("/", ".") +
                                ".json",
                        )
                        .bufferedReader()
                        .use { it.readText() }
                } catch (_: Exception) {
                    null
                }?.let { jsonResponse ->
                    respond(
                        content = jsonResponse,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                } ?: when (httpRequestData.method) {
                    HttpMethod.Post, HttpMethod.Delete -> respondOk()
                    else -> respond(
                        content = "No stubs available for ${httpRequestData.url}. Please add one of them",
                        status = HttpStatusCode.NotFound,
                    )
                }
            },
        ) {
            defaultRequest {
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
            }
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.BODY
                sanitizeHeader { header -> header == HttpHeaders.Authorization }
            }

            install(ContentNegotiation) {
                json(
                    Json {
                        prettyPrint = true
                        isLenient = true
                    },
                )
            }
        }
    }
}