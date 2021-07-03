package org.sid.backend;

import org.bson.Document;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.IOException;

public class MQTTMigration {

    private MqttClient mqttClient;
    private String mqtt_topic;

    public MQTTMigration() {
        try {
            mqttClient = IniReader.getMqttConnection();
            mqtt_topic = IniReader.getMqttTopic();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void send(Document doc) {
        MqttMessage message = new MqttMessage();
        message.setPayload(doc.toJson().getBytes());
        try {
            mqttClient.publish(mqtt_topic, message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }




}
