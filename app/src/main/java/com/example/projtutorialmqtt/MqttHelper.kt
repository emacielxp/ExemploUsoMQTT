package com.example.projtutorialmqtt

import android.content.Context
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*

class MqttHelper(val context: Context) {

    private var clientId: String = generateClientId()
    private val mqttAndroidClient: MqttAndroidClient

    companion object {
        val CLIENT_ID_PREFIX = "prefixCliente"
        val SERVER_URI_MQTT = "wss://urlmqtt.com:1883"
        val SERVER_USERNAME = "usuariomqtt"
        val SERVER_PASSWORD = "senhamqtt"
        val QOS_MQTT = 2
        val TAG = "TesteMQTT"
    }

    init {
        mqttAndroidClient = MqttAndroidClient(context, SERVER_URI_MQTT, clientId)
        connectToServer()
    }

    private fun generateClientId() = "${CLIENT_ID_PREFIX}_${System.nanoTime()}"

    private fun connectToServer() {
        val connectOptions = MqttConnectOptions()
        connectOptions.userName = SERVER_USERNAME
        connectOptions.password = SERVER_PASSWORD.toCharArray()
        connectOptions.isAutomaticReconnect = true
        // Para que não fique recebendo todas as mensagens do passado sempre que connectar
        connectOptions.isCleanSession = true

        mqttAndroidClient.connect(connectOptions, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {

                Log.i(TAG, "Conectado em $SERVER_URI_MQTT")
                // Definindo configurações ao desconectar (Se não quiser definí-las, não use as próximas linhas):
                val disconnectedBufferOptions = DisconnectedBufferOptions()
                disconnectedBufferOptions.apply {
                    isBufferEnabled = true
                    bufferSize = 100
                    isPersistBuffer = false
                    isDeleteOldestMessages = false
                    mqttAndroidClient.setBufferOpts(this)
                }
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                Log.e(TAG, "Falha ao conectar em $SERVER_URI_MQTT: ${exception.toString()}")
            }
        })

        mqttAndroidClient.setCallback(object : MqttCallbackExtended {

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                Log.i(TAG, "Mensagem recebida do tópico '$topic': $message")
                (context as MainActivity).messages_list_title_text.text = topic
                val s = context.messages_list_text.text.toString()
                context.messages_list_text.text = "$s${message.toString()}\n"
            }

            override fun connectionLost(cause: Throwable?) {
                Log.i(TAG, "Conexão perdida com o servidor.")
            }

            override fun connectComplete(reconnect: Boolean, serverURI: String?) {}
            override fun deliveryComplete(token: IMqttDeliveryToken?) {}
        })
    }

    fun subscribeTopic(topic: String) {
        mqttAndroidClient.subscribe(topic, QOS_MQTT, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                Log.i(TAG, "Inscrito no tópico $topic")
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                Log.i(TAG, "Falha ao se inscrever o tópico $topic: ${exception.toString()}")
            }
        })
    }

    fun publisnOnTopic(topic: String, content: String) {
        val message = MqttMessage(content.toByteArray())
        message.isRetained = true
        message.qos = QOS_MQTT
        mqttAndroidClient.publish(topic, message, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                Log.i("TesteMQTT", "Mensagem publicada no tópico $topic: $content")
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                Log.i("TesteMQTT", "Falha ao publicar mensagem no tópico $topic: ${exception.toString()}")
            }
        })
    }

    fun disconnectFromServer() {
        if (mqttAndroidClient.isConnected) {
            mqttAndroidClient.unregisterResources()
            mqttAndroidClient.close()
        }
    }
}