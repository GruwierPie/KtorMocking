package com.gruwier.ktordemo

import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse

suspend inline fun <reified T> safeCall(request: () -> HttpResponse): T = request.invoke().body<T>()