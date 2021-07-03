package org.sid.backend;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.text.SimpleDateFormat;
import java.util.*;

public class MongoFilters extends MongoConnection {

    private final ArrayList<Double> data = new ArrayList<>();
    private final MQTTMigration migration;


    public MongoFilters(MongoCollection<Document> tmp_collection, MongoCollection<Document> final_collection, int periodicity) {
        super(tmp_collection, final_collection, periodicity);
        migration = new MQTTMigration();
    }

    private boolean isAnomalous(Document doc) {
        SensorsSQLConnection sensorsConn = new SensorsSQLConnection();
        double[] limits = sensorsConn.getLimits(doc.get("Sensor").toString().charAt(0), Integer.parseInt(String.valueOf(doc.get("Sensor").toString().charAt(1))));
        double value = Double.parseDouble(doc.get("Medicao").toString());
        return (value < limits[0]) || (value > limits[1]);
    }

    @Override
    protected boolean isValid(Document doc) {
        addToList(doc);
        return /*!isOutlier(doc) && */!isAnomalous(doc);
    }

    @Override
    protected void handleData(Document doc) {
        migration.send(doc);
        super.getSearchedCollection().deleteOne(doc);
    }

    private void addToList(Document doc) {
        data.add(Double.parseDouble(doc.get("Medicao").toString()));
    }

    public boolean isOutlier(Document doc) {
        if (data.size() < 2)
            return false;

        List<Double> data1, data2;

        if (data.size() % 2 == 0) {
            data1 = data.subList(0, data.size() / 2);
            data2 = data.subList(data.size() / 2, data.size());
        } else {
            data1 = data.subList(0, data.size() / 2);
            data2 = data.subList(data.size() / 2 + 1, data.size());
        }

        double q1 = getMedian(data1);
        double q3 = getMedian(data2);
        double iqr = q3 - q1;
        double lowerFence = q1 - 1.5 * iqr;
        double upperFence = q3 + 1.5 * iqr;

        for (Double datum : data) {
            if (datum < lowerFence || datum > upperFence) {
                data.remove(datum);
                if (datum.equals(Double.parseDouble(doc.get("Medicao").toString())))
                    return true;
            }
        }
        return false;
    }

    private double getMedian(List<Double> data) {
        if (data.size() % 2 == 0) return (data.get(data.size() / 2) + data.get(data.size() / 2 - 1)) / 2;
        else return data.get(data.size() / 2);
    }

}
