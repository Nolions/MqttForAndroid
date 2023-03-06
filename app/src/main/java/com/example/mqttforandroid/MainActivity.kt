package com.example.mqttforandroid

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*

class MainActivity : AppCompatActivity() {
    private var client: MqttAndroidClient? = null
    private lateinit var options: MqttConnectOptions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initMqtt()
        connect()
    }

    private fun initMqtt() {
        try {
//            if (client == null) {
            // init client
            client =
                MqttAndroidClient(this, "tcp://broker.emqx.io:1883", "clientID${Math.random()}")

            client?.setCallback(object : MqttCallback {
                override fun connectionLost(cause: Throwable?) {
                    Log.d("MqttForAndroid", "MainActivity::initMqtt(), mqtt connect lost")
                }

                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    Log.d(
                        "MqttForAndroid",
                        "MainActivity::initMqtt(), Receive Message, topic::$topic, msg: ${message.toString()}"
                    )
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                    Log.d("MqttForAndroid", "MainActivity::initMqtt(), deliveryComplete")
                }
            })

//            connect()
//            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun connect() {
        try {
            options = MqttConnectOptions()
            options.isCleanSession = true // 是否清空session
            options.connectionTimeout = 20 // timeout(second)
            options.keepAliveInterval = 10 // 服務器會每隔10秒的時間向客戶端發送個消息判斷客戶端是否在線
            options.isAutomaticReconnect = true //是否自動重新連線

            client?.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d("MqttForAndroid", "MainActivity::connect(), mqtt connect:success")
                    subscribe("testMQ")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.e(
                        "MqttForAndroid",
                        "MainActivity::connect(), mqtt connect:fail, error:${exception}"
                    )
                }
            })

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(
                "MqttForAndroid", "MainActivity::connect(), connect fail, error:${e.message}"
            )
        }
    }

    private fun subscribe(topic: String, qos: Int = 1) {
        try {
            client?.subscribe(topic, qos, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(
                        "MqttForAndroid",
                        "MainActivity::subscribe(), mqtt subscribe:success, token:$asyncActionToken"
                    )
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.e(
                        "MqttForAndroid",
                        "MainActivity::subscribe(), mqtt subscribe:fail, error:${exception?.message}"
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
}
