package com.gruwier.ktordemo

import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import java.net.UnknownHostException

suspend inline fun <reified T> safeCall(request: () -> HttpResponse): T = try {
    request.invoke().body<T>()
} catch (e: UnknownHostException) {
    throw UnknownHostException()
}
