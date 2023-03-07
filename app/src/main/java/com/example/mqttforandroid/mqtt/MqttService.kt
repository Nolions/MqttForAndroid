package com.example.mqttforandroid.mqtt

import android.util.Log
import kotlinx.coroutines.channels.Channel
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

class MqttService {
    private var client: IMqttAsyncClient? = null
    private var channel = Channel<Boolean>()

    /**
     * Initialization MqttAsyncClient and setting MqttConnectOptions config
     *
     * @param config
     * @param channel
     *
     * @return MqttService
     */
    fun init(config: Config, channel: Channel<Boolean>): MqttService {
        this.channel = channel
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
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                Log.d(
                    "MqttForAndroid",
                    "${javaClass.simpleName}::init(), Receive Message, topic::$topic, msg: ${message.toString()}"
                )
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

    suspend fun connect() {
        try {
            val token = client?.connect(options)
            token?.waitForCompletion()
            Log.d("MqttForAndroid", "${javaClass.simpleName}::connect(), mqtt connect:success1")

            channel.send(true)
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

//    private lateinit var options: MqttConnectOptions

    companion object {
        @Volatile
        private var mqttService: MqttService? = null
        private lateinit var options: MqttConnectOptions
        fun init(): MqttService = mqttService ?: synchronized(this) {
            mqttService ?: MqttService()
        }
    }

//    public fun init(context: Context, config: Config) {
//        client = MqttAndroidClient(context, config.host, config.clientId)
//        client?.setCallback(object : MqttCallback {
//            override fun connectionLost(cause: Throwable?) {
//                Log.d("MqttForAndroid", "MainActivity::initMqtt(), mqtt connect lost")
//            }
//
//            override fun messageArrived(topic: String?, message: MqttMessage?) {
//                Log.d(
//                    "MqttForAndroid",
//                    "MainActivity::initMqtt(), Receive Message, topic::$topic, msg: ${message.toString()}"
//                )
//            }
//
//            override fun deliveryComplete(token: IMqttDeliveryToken?) {
//                Log.d("MqttForAndroid", "MainActivity::initMqtt(), deliveryComplete")
//            }
//        })
//
//        options = MqttConnectOptions()
//        options.isCleanSession = config.option.cleanSession
//        options.connectionTimeout = config.option.timeOut
//        options.keepAliveInterval = config.option.keepAliveInterval
//        options.isAutomaticReconnect = config.option.automaticReconnect
//        config.username?.let {
//            options.userName = it
//        }
//        config.password?.let {
//            options.password = it.toCharArray()
//        }
//    }
//
//    public fun connect() {
//        try {
//
//        } catch (e: Exception) {
//            e.printStackTrace()
//            Log.e(
//                "MqttForAndroid", "Mqtt::connect(), connect fail, error:${e.message}"
//            )
//        }
//    }
//
//    public fun subscribe() {
//
//    }
//
//    public fun publisher() {
//
//    }
}