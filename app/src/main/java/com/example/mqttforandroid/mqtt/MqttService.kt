package com.example.mqttforandroid.mqtt

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

class MqttService {
    private var client: IMqttAsyncClient? = null
    private lateinit var statusFlow: MutableStateFlow<Boolean>
    private lateinit var msgFlow: MutableStateFlow<MqttMessage?>

    /**
     * Initialization MqttAsyncClient and setting MqttConnectOptions config
     *
     * @param config
     * @param statusFlow
     * @param msgFlow
     *
     * @return MqttService
     */
    fun init(config: Config, statusFlow: MutableStateFlow<Boolean>, msgFlow: MutableStateFlow<MqttMessage?>): MqttService {
        this.statusFlow = statusFlow
        this.msgFlow = msgFlow

        client = MqttAsyncClient(
            config.host,
            config.clientId,
            MemoryPersistence(),
            TimerPingSender(),
            null
        )
        client?.setCallback(object : MqttCallback {
            override fun connectionLost(cause: Throwable?) {
                Log.d("MqttForAndroid", "${javaClass.simpleName}::initMqtt(), mqtt connect lost")
                CoroutineScope(Dispatchers.IO).launch {
                    this@MqttService.statusFlow.value = false
                }
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                Log.d(
                    "MqttForAndroid",
                    "${javaClass.simpleName}::init(), Receive Message, topic::$topic, msg: ${message.toString()}"
                )
                message?.let {
                    msgFlow.value = it
                }
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                Log.d("MqttForAndroid", "${javaClass.simpleName}::init(), deliveryComplete")
            }
        })

        options = MqttConnectOptions()
        options.isCleanSession = config.option.cleanSession
        options.connectionTimeout = config.option.timeOut
        options.keepAliveInterval = config.option.keepAliveInterval
        options.isAutomaticReconnect = config.option.automaticReconnect
        config.username?.let {
            options.userName = it
        }
        config.password?.let {
            options.password = it.toCharArray()
        }

        return this
    }

    fun connect() {
        try {
            val token = client?.connect(options)
            token?.waitForCompletion()
            Log.d("MqttForAndroid", "${javaClass.simpleName}::connect(), mqtt connect:success")
            CoroutineScope(Dispatchers.IO).launch {
                Log.d("Coroutine:", "================2=")
                statusFlow.value = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(
                "MqttService",
                "${javaClass.simpleName}::connect(), connect fail, error:${e.message}"
            )
        }
    }

    fun subscribe(topic: String, qos: Int = 1) {
        try {
            client?.subscribe(topic, qos, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(
                        "MqttForAndroid",
                        "${javaClass.simpleName}::subscribe(), mqtt subscribe:success, token:$asyncActionToken"
                    )
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.e(
                        "MqttForAndroid",
                        "${javaClass.simpleName}::subscribe(), mqtt subscribe:fail, error:${exception?.message}"
                    )
                }

            })
        } catch (e: Exception) {
            Log.e(
                "MqttForAndroid", "MainActivity::connect(), something wrong:${e.message},${e.cause}"
            )
            e.printStackTrace()
        }
    }

    companion object {
        @Volatile
        private var mqttService: MqttService? = null
        private lateinit var options: MqttConnectOptions
        fun init(): MqttService = mqttService ?: synchronized(this) {
            mqttService ?: MqttService()
        }
    }
}