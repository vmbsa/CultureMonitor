package org.sid.backend;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import org.ini4j.Ini;
import org.sid.graphicControllers.SecondaryController;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ThreadLocalRandom;

public class IniReader {

    public static Ini loadConfigFile() throws IOException {
        Ini reader = new Ini();
        reader.load(new FileReader("config.ini"));
        return reader;
    }

    private static String[] getSQLFields() throws IOException {
        Ini reader = IniReader.loadConfigFile();
        String sql_host = reader.get("SQL Connection", "host", String.class);
        String sql_port = reader.get("SQL Connection", "port", String.class);
        String sql_database_name = reader.get("SQL Connection", "database_name", String.class);
        return new String[] {sql_host, sql_port, sql_database_name};
    }

    public static Connection getSQLConnection(String username, String password) throws ClassNotFoundException, IOException, SQLException {
        String[] sql_fields = IniReader.getSQLFields();
        Class.forName("com.mysql.cj.jdbc.Driver");
        String url = "jdbc:mysql://" + sql_fields[0] + ":" + sql_fields[1] + "/" + sql_fields[2];
        return DriverManager.getConnection(url, username, password);
    }

    private static String[] getMongoCloudFields() throws IOException {
        Ini reader = IniReader.loadConfigFile();
        String host = reader.get("Cloud Mongo", "host", String.class);
        String port = reader.get("Cloud Mongo", "port", String.class);
        String database_name = reader.get("Cloud Mongo", "database_name", String.class);
        String collections_names = reader.get("Cloud Mongo", "collection_name", String.class);
        String has_password = reader.get("Cloud Mongo", "has_password", String.class);
        String username = reader.get("Cloud Mongo", "username", String.class);
        String password = reader.get("Cloud Mongo", "password", String.class);
        return new String[] {host, port, database_name, collections_names, has_password, username, password};
    }

    public static void connectToMongos(SQLConnector conn, int periodicity) {
        try {
            start_servers();

            MQTTFetcher mqttToSQL = new MQTTFetcher(conn);
            mqttToSQL.start();

            MongoDatabase local_database = getLocalDatabase();

            String[] configFields = IniReader.getMongoCloudFields();

            boolean has_password = configFields[4].equals("true");
            String uri;
            if (has_password) {
                uri = "mongodb://" + configFields[5] + ":" + configFields[6] + "@" + configFields[0] + ":" + configFields[1] + "/?authSource=admin&authMechanism=SCRAM-SHA-1";
            } else {
                uri = "mongodb://" + configFields[0] + ":" + configFields[1];
            }
            MongoClientURI clientURI = new MongoClientURI(uri);
            MongoClient client = new MongoClient(clientURI);

            MongoDatabase cloud_database = client.getDatabase(configFields[2]);

            for (int i = 0; i < configFields[3].split(",").length; i++) {
                System.out.println(cloud_database.getName());
                System.out.println(local_database.getName());
                MongoCollection<Document> cloud_collection = cloud_database.getCollection(configFields[3].split(",")[i]);
                MongoCollection<Document> local_collection = local_database.getCollection(configFields[3].split(",")[i]);
                MongoFetcher fetcher = new MongoFetcher(cloud_collection, local_collection, 1);
                fetcher.start();
                MongoFilters filters = new MongoFilters(local_collection, local_database.getCollection("final"), periodicity);
                filters.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static MongoDatabase getLocalDatabase() throws IOException {
        Ini reader = IniReader.loadConfigFile();
        String ip1 = reader.get("Local Mongo", "ip1", String.class);
        String port1 = reader.get("Local Mongo", "port2", String.class);
        String ip2 = reader.get("Local Mongo", "ip2", String.class);
        String port2 = reader.get("Local Mongo", "port2", String.class);
        String ip3 = reader.get("Local Mongo", "ip3", String.class);
        String port3 = reader.get("Local Mongo", "port3", String.class);
        String database_name = reader.get("Local Mongo", "database_name", String.class);
        String uri = "mongodb://" + ip1 + ":" + port1 + "," + ip2 + ":" + port2 + "," + ip3 + ":" + port3;
        MongoClientURI clientURI = new MongoClientURI(uri);
        MongoClient client = new MongoClient(clientURI);
        MongoDatabase database = client.getDatabase(database_name);
        return database;
    }

    private static void start_servers() throws IOException {
        ProcessBuilder processBuilder_mongo = new ProcessBuilder("bat_files/activate_mongo_local.bat");
        processBuilder_mongo.start();
        ProcessBuilder processBuilder_replica1 = new ProcessBuilder("bat_files/activate_mongo_replica1.bat");
        processBuilder_replica1.start();
        ProcessBuilder processBuilder_replica2 = new ProcessBuilder("bat_files/activate_mongo_replica2.bat");
        processBuilder_replica2.start();
        ProcessBuilder processBuilder_replica3 = new ProcessBuilder("bat_files/activate_mongo_replica3.bat");
        processBuilder_replica3.start();
    }

    public static MqttClient getMqttConnection() throws IOException {
        Ini reader = IniReader.loadConfigFile();
        String url = reader.get("MQTT Broker", "mqtt_server", String.class);
        String topic = reader.get("MQTT Broker", "mqtt_topic", String.class);
        int randomID = ThreadLocalRandom.current().nextInt(0, 10000 + 1);
        try {
            MqttClient mqttClient = new MqttClient(url, "MongoToCloud" + topic + randomID, new MqttDefaultFilePersistence("/mqtt_cache"));
            mqttClient.connect();
            mqttClient.subscribe(topic);
            return mqttClient;
        } catch (MqttException var2) {
            System.err.println("Connection To Mqtt server failed");
        }
        return null;
    }

    public static String getMqttTopic() throws IOException {
        Ini reader = IniReader.loadConfigFile();
        return reader.get("MQTT Broker", "mqtt_topic", String.class);
    }

}
