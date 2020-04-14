package com.example.projtutorialmqtt

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var mqttHelper: MqttHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mqttHelper = MqttHelper(this)

        subscribe_button.setOnClickListener {
            val topic = topic_edit_text.text.toString()
            if (!topic.isBlank()) {
                mqttHelper.subscribeTopic(topic)
            }

            messages_list_title_text.text = "Mensagens do tópico $topic"
        }

        publish_button.setOnClickListener {
            val topic = topic_edit_text.text.toString()
            val messageContent = message_edit_text.text.toString()
            if (!topic.isBlank() && !messageContent.isBlank()) {
                mqttHelper.publisnOnTopic(topic, messageContent)
            }
        }

        messages_list_text.text = ""
        messages_list_title_text.text = ""
    }

    override fun onDestroy() {
        mqttHelper.disconnectFromServer()
        super.onDestroy()
    }
}
