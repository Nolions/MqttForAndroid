# MqttForAndroid 

Android上實作從Mqtt上接收與發送訊息

## Setting and config

### dependencies {

在app的build.gradle中新增以下依賴

```groovy
dependencies {
    .
    .
    .
    implementation 'org.eclipse.paho:org.eclipse.paho.android.service:1.1.1'
    implementation 'org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5'
    .
    .
    .
}

```

### Setting enableJetifier

因為step1中依賴的library不是使用AndroidX，所以需要啟用enableJetifier

在gradle.properties檔案中新增以下設定

```yaml
android.enableJetifier=true
```

### 註冊所需要的權限

AndroidManifest.xml中新增以下權限

| 權限 | 說明 |
| --- | ---- |
| android.permission.WAKE_LOCK | 允許喚醒 |
| android.permission.ACCESS_NETWORK_STATE | 網路通訊許可 |
| android.permission.INTERNE | 網路使用權限 |

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android" xmlns:tools="http://schemas.android.com/tools">
    <!--  休眠喚醒  -->
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <!-- 網路通訊許可權限 -->
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <!-- 網路使用權限 -->
    <uses-permission android:name="android.permission.INTERNET" />

    .
    .
    .

</manifest>
```

### 註冊MqttService

檔案AndroidManifest.xml中註冊MqttService

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android" xmlns:tools="http://schemas.android.com/tools">
    .
    .
    .
     <application ...>
        .
        .
        .

        <service android:name="org.eclipse.paho.android.service.MqttService" />
     </application>
    .
    .
    .
</manifest>

```

## use

### Init MqttClient

```kotlin
val client = MqttAndroidClient(this, "tcp://broker.emqx.io:1883", "clientID${Math.random()}")
```

or

```kotlin
val client = MqttAsyncClient(tcp://broker.emqx.io:1883, "clientID${Math.random()}", MemoryPersistence(), TimerPingSender(),null)
```

### 設定MQTT連線時設定參數，並進行連線

設定連線時需要參數，EX: username、password、timeout時間、心跳頻率、斷線後是否允許自動重連...設定

```kotlin
val options = MqttConnectOptions()
options.isCleanSession = true // 是否清空session
options.connectionTimeout = 20 // timeout(second)
options.keepAliveInterval = 10 // 服務器會每隔10秒的時間向客戶端發送個消息判斷客戶端是否在線
options.isAutomaticReconnect = true //是否自動重新連線

client.connect(options, null, object : IMqttActionListener {
    override fun onSuccess(asyncActionToken: IMqttToken?) {
        // TODO connection scuuess                
    }
    
    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
        // TODO connection fail
    }
})

```

### 訂閱Topic

與MQTT連線成功後就可以針對topic進行訂閱了

``` kotlin
client.subscribe(topic, qos, null, object : IMqttActionListener {
    override fun onSuccess(asyncActionToken: IMqttToken?) {
        // subscribe topic success
    }

    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
        // subscribe topic fail
    }

})

```

### 從MQTT接收訊息

接收訊息有兩種方法

第一種：使用setCallback

```
client.setCallback(object : MqttCallback {
    override fun connectionLost(cause: Throwable?) {
        // TODO connect lost
    }

    override fun messageArrived(topic: String?, message: MqttMessage?) {
        // TODO Receive Message
    }

    override fun deliveryComplete(token: IMqttDeliveryToken?) {
        // delivery Complete
    }
})
```

第二種：使用訂閱topic時用的`subscribe`函數中的`MqttMessageListener`

MqttMessageListener是一個lambdah參數

```
client.setCallback(object : MqttCallback {
    ...
}){topic, msg ->
    // TODO TODO Receive message and message of topic
}
```

PS. 如果MQTT的操作不是main thread上，則可以使用`callback function`或是`Coroutine搭配stateflow`
