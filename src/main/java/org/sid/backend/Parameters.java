package org.sid.backend;

import java.sql.*;
import java.time.Instant;

public class Parameters {

    private final int cultura_id;
    private final double max;
    private final double min;
    private final double max_tolerance;
    private final double min_tolerance;
    private final int orange_frequency;
    private final int red_frequency;

    private double value;
    private String sensor;
    private Timestamp time;
    private String cultura_name;
    private int id_user;
    private int zone_id;

    public Parameters(ResultSet data) throws SQLException {
        cultura_id = Integer.parseInt(data.getString("id_cultura"));
        char type = data.getString("Tipo").charAt(0);
        max = Double.parseDouble(data.getString("Maximo"));
        min = Double.parseDouble(data.getString("Minimo"));
        max_tolerance = Double.parseDouble(data.getString("Tolerancia_Maxima"));
        min_tolerance = Double.parseDouble(data.getString("Tolerancia_Minima"));
        orange_frequency = (int) Double.parseDouble(data.getString("Frequencia_Laranja"));
        red_frequency = (int) Double.parseDouble(data.getString("Frequencia_Vermelha"));
    }

    private boolean isInMinOrangeZone(double value) {
        double[] minOrangeZone = new double[]{((min_tolerance - min) / 2) + 0.1 + min, min_tolerance};
        return (value > minOrangeZone[0] && value < minOrangeZone[1]);
    }

    private boolean isInMinRedZone(double value) {
        double[] minRedZone = new double[]{min, min + ((min_tolerance - min) / 2)};
        return (value < minRedZone[1]);
    }

    private boolean isInMaxOrangeZone(double value) {
        double[] maxOrangeZone = new double[]{max_tolerance, max_tolerance + (max - max_tolerance) / 2};
        return (value > maxOrangeZone[0] && value < maxOrangeZone[1]);
    }

    private boolean isInMaxRedZone(double value) {
        double[] maxRedZone = new double[]{max_tolerance + 0.1 + (max - max_tolerance) / 2, max_tolerance};
        return (value > maxRedZone[0]);
    }

    private void setAlertData(SQLConnector conn, AlertHandler alertHandler) {
        zone_id = alertHandler.getZone_id();
        value = alertHandler.getValue();
        sensor = alertHandler.getSensor();
        time = alertHandler.getTime();
        cultura_name = conn.getCulturaNameByID(cultura_id);
        id_user = conn.getUserByCulturaID(cultura_id);
    }

    private String getTypeString() {
        String type = String.valueOf(sensor.charAt(0));
        if (type.equals("T")) {
            return "Temperatura";
        } else if (type.equals("H")) {
            return "Humidade";
        } else {
            return "Luminusidade";
        }
    }

    public void handleAlert(SQLConnector conn, AlertHandler alertHandler) {
        setAlertData(conn, alertHandler);
        if (isInMinOrangeZone(value)) {
            handleMinOrangeAlert(conn);
        }
        else if (isInMinRedZone(value)) {
            handleMinRedAlert(conn);
        }
        else if (isInMaxOrangeZone(value)) {
            handleMaxOrangeAlert(conn);
        }
        else if (isInMaxRedZone(value)) {
            handleMaxRedAlert(conn);
        }
        else{
            conn.setCulturaState(cultura_id, -1);
        }
    }

    private void handleMinOrangeAlert(SQLConnector conn) {
        if (isActiveAlert(conn, orange_frequency, 0) && isActiveAlert(conn, red_frequency, 1)) {
            conn.sendAlert(zone_id, sensor, time, value, 0, cultura_name, "Alerta Laranja: A " + getTypeString() +  " está em baixo", cultura_id, id_user);
            conn.setCulturaState(cultura_id, 0);
            System.out.println(0 + ", escreveu");
        }
    }

    private void handleMinRedAlert(SQLConnector conn) {
        if (isActiveAlert(conn, red_frequency, 1)) {
            conn.sendAlert(zone_id, sensor, time, value, 1, cultura_name, "Alerta Vermelho: A " + getTypeString() + " está em baixo", cultura_id, id_user);
            conn.setCulturaState(cultura_id, 1);
            System.out.println(1 + ", escreveu");
        }
    }

    private void handleMaxOrangeAlert(SQLConnector conn) {
        if (isActiveAlert(conn, orange_frequency, 2) && isActiveAlert(conn, red_frequency, 3)) {
            conn.sendAlert(zone_id, sensor, time, value, 2, cultura_name, "Alerta Laranja: A " + getTypeString() + " está em cima", cultura_id, id_user);
            conn.setCulturaState(cultura_id, 2);
            System.out.println(2 + ", escreveu");
        }
    }

    private void handleMaxRedAlert(SQLConnector conn) {
        if (isActiveAlert(conn, red_frequency, 3)) {
            conn.sendAlert(zone_id, sensor, time, value, 3, cultura_name, "Alerta Vermelho: A " + getTypeString() + " está em cima", cultura_id, id_user);
            conn.setCulturaState(cultura_id, 3);
            System.out.println(3 + ", escreveu");
        }
    }

    private boolean isActiveAlert(SQLConnector conn, int frequency, int code) {
        String lastAlert = getLastAlertByID(conn, code);
        if (lastAlert == null) {
            return true;
        }
        long limit = Timestamp.valueOf(lastAlert).toInstant().toEpochMilli() + frequency * 60 * 1000L;
        long current_time = getCurrentTime();
        return current_time >= limit;
    }

    private Long getCurrentTime() {
       return Instant.now().toEpochMilli();
    }

    private String getLastAlertByID(SQLConnector conn, int code) {
        try {
            ResultSet rs = conn.getLastAlertByID(cultura_id, sensor, code);
            if (rs.next()) {
                return rs.getString("hora");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


}
