package com.example.mqttforandroid

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.mqttforandroid.mqtt.Config
import com.example.mqttforandroid.mqtt.MqttService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val channel = Channel<Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var mqttService: MqttService? = null
        mqttService = MqttService.init()
        val config = Config(
            host = "tcp://broker.emqx.io:1883",
            clientId = "clientID${Math.random()}",
            option = Config.Option(
                cleanSession = true,
                automaticReconnect = true,
                timeOut = 20,
                keepAliveInterval = 10,
            )
        )

        CoroutineScope(Dispatchers.IO).launch {
            mqttService.init(config, channel).connect()
            val result = channel.receive()
            Log.d("MqttForAndroid", "MainActivity::initMqtt(), channel:$result")
        }

        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                delay(100)
                val connStatus = channel.receive()
                if (connStatus) {
                    mqttService.subscribe("testMQ")
                }
            }
        }
    }
}
