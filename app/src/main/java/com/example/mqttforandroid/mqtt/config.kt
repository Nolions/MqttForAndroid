package com.example.mqttforandroid.mqtt

data class Config(
    val host: String,
    val username: String? = null,
    val password: String? = null,
    val clientId: String,
    val option: Option
) {
    data class Option(
        val cleanSession: Boolean = true,
        val automaticReconnect: Boolean = true,
        val timeOut: Int = 20,
        val keepAliveInterval: Int = 10,
    )
}
