package com.gruwier.ktordemo

import kotlinx.serialization.Serializable

@Serializable
data class JokeResponse(
    val icon_url : String,
    val id : String,
    val url : String,
    val value :  String
)