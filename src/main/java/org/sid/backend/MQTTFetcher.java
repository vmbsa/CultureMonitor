package org.sid.backend;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class MQTTFetcher extends Thread implements MqttCallback {

    private MqttClient client;
    private SQLConnector sqlConnection;

    public MQTTFetcher(SQLConnector conn) {
        sqlConnection = conn;
    }

    @Override
    public void run() {
        try {
            client = IniReader.getMqttConnection();
            assert client != null;
            client.setCallback(this);
        } catch (IOException e) {
            e.printStackTrace();
            client = null;
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        sqlConnection.addValue(new JSONObject(new String(message.getPayload())));
    }

    @Override
    public void connectionLost(Throwable cause) { }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) { }
}
