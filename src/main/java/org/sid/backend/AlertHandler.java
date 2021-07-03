package org.sid.backend;

import org.json.JSONObject;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;

public class AlertHandler extends Thread {

    private final SQLConnector conn;
    private final int zone_id;
    private final char type;
    private final double value;
    private final String sensor;
    private final Timestamp time;

    public AlertHandler(SQLConnector conn, JSONObject jsonObject) {
        this.conn = conn;
        this.zone_id = Integer.parseInt(String.valueOf(jsonObject.get("Zona").toString().charAt(1)));
        this.type = jsonObject.get("Sensor").toString().charAt(0);
        this.value = Double.parseDouble(jsonObject.get("Medicao").toString());
        this.sensor = jsonObject.getString("Sensor");
        this.time = getTimeFromString(jsonObject.get("Data").toString());
    }

    @Override
    public void run() {
        goThroughCulturas();
    }

    private Timestamp getTimeFromString(String date_time) {
        String date = date_time.split("T")[0];
        String time = date_time.split("T")[1].replace("Z", "");
        String timestamp_string = new String (date + " " + time);
        return Timestamp.valueOf(timestamp_string);
    }

    private void goThroughCulturas() {
        ArrayList<Parameters> parametersArrayList = new ArrayList<>();
        try {
            ResultSet rs = conn.getCulturasByZoneID(zone_id);
            while (rs.next()) {
                int id_cultura = rs.getInt("id_cultura");
                parametersArrayList.add(getParameters(id_cultura));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        checkIfAlertIsNeeded(parametersArrayList);
    }

    private Parameters getParameters(int id_cultura) throws SQLException {
        ResultSet rs = conn.getParametersByCulturaIDAndType(id_cultura, type);
        if (rs.next()) {
            return new Parameters(rs);
        }
        return null;
    }

    private void checkIfAlertIsNeeded(ArrayList<Parameters> parametersArrayList) {
        for (Parameters parameter : parametersArrayList) {
            if (parameter != null) {
                parameter.handleAlert(conn, this);
            }
        }
    }

    public double getValue() {
        return value;
    }

    public String getSensor() {
        return sensor;
    }

    public Timestamp getTime() {
        return time;
    }

    public int getZone_id() {
        return zone_id;
    }


}
