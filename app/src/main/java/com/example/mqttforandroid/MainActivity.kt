package com.example.mqttforandroid

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.mqttforandroid.databinding.ActivityMainBinding
import com.example.mqttforandroid.mqtt.Config
import com.example.mqttforandroid.mqtt.MqttService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private val msgFlow: MutableStateFlow<MqttMessage?> = MutableStateFlow(null)
    private val statusFlow = MutableStateFlow(false)
    private var mqttService: MqttService? = null

    private lateinit var viewBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        initMQtt()
    }

    private fun initMQtt() {
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

        mqttService!!.init(config, statusFlow, msgFlow).connect()

        CoroutineScope(Dispatchers.IO).launch {
            statusFlow.collect {
                Log.d("MqttForAndroid", "${javaClass.simpleName}::onCreate(), mqtt connect status:$it")
                if (it) {
                    mqttService!!.subscribe("testMQ")
                }
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            msgFlow.collect {
                Log.d("MqttForAndroid", "${javaClass.simpleName}::onCreate(), mqtt msg:$it")
                withContext(Dispatchers.Main) {
                    it?.let { msg ->
                        val jsonMsg = JSONObject(String(msg.payload))
                        viewBinding.textLabel.text = jsonMsg.getString("msg")
                    }
                }
            }
        }
    }
}
