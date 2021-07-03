package org.sid.backend;

import org.json.JSONObject;

import java.io.IOException;
import java.sql.*;
import java.time.Instant;

public class SQLConnector {

    private final String sql_username;
    private final String sql_password;
    private Connection conn;

    public SQLConnector(String username, String password) throws IOException {
        this.sql_username = username;
        this.sql_password = password;
    }

    private void verifyUser() {
        if (!sql_username.equals("java_admin")) {
            throw new IllegalArgumentException();
        }
    }

    public void connectToSQL() throws ClassNotFoundException, SQLException, IOException {
        verifyUser();
        conn = IniReader.getSQLConnection(sql_username, sql_password);
    }

    public void addValue(JSONObject jsonObject) {
        AlertHandler alertHandler = new AlertHandler(this, jsonObject);
        alertHandler.start();
        try {
            int id_zone = jsonObject.get("Zona").toString().charAt(1);
            String sensor = jsonObject.get("Sensor").toString();
            String date_time = jsonObject.get("Data").toString();
            double value = Double.parseDouble(jsonObject.get("Medicao").toString());
            String date = date_time.split("T")[0];
            String time = date_time.split("T")[1].replace("Z", "");
            String timestamp_string = new String (date + " " + time);
            Timestamp timestamp = Timestamp.valueOf(timestamp_string);

            String query = "INSERT INTO medicao (id_zona,sensor,hora,leitura) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1,id_zone);
            pstmt.setString(2,sensor);
            pstmt.setTimestamp(3, timestamp);
            pstmt.setDouble(4,value);

            pstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ResultSet getCulturasByZoneID(int zone_id) throws SQLException {
        String query = "SELECT * FROM cultura WHERE id_zona = " + zone_id;
        Statement stmt = conn.createStatement();
        return stmt.executeQuery(query);
    }

    public ResultSet getParametersByCulturaIDAndType(int cultura_id, char type) throws SQLException {
        String query = "SELECT * FROM parametroscultura WHERE id_cultura = " + cultura_id + " AND Tipo = '" + type + "'";
        Statement stmt = conn.createStatement();
        return stmt.executeQuery(query);
    }

    public ResultSet getLastAlertByID(int cultura_id, String sensor, int code) throws SQLException {
        String query = "SELECT hora FROM alerta WHERE id_cultura = " + cultura_id + " AND sensor = '" + sensor + "' AND tipo_alerta = " + code + " ORDER BY hora DESC";
        Statement stmt;
        stmt = conn.createStatement();
        return stmt.executeQuery(query);
    }

    public void sendAlert(int id_zona, String sensor, Timestamp time, double value, int code, String cultura_name, String message, int id_cultura, int id_user) {
        try {
            String query = "INSERT INTO alerta (id_zona,sensor,hora,leitura,tipo_alerta,cultura,mensagem,id_cultura,id_utilizador,hora_escrita) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1,id_zona);
            pstmt.setString(2,sensor);
            pstmt.setTimestamp(3, time);
            pstmt.setDouble(4,value);
            pstmt.setInt(5,code);
            pstmt.setString(6, cultura_name);
            pstmt.setString(7, message);
            pstmt.setInt(8, id_cultura);
            pstmt.setInt(9, id_user);
            pstmt.setTimestamp(10, Timestamp.from(Instant.now()));

            pstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setCulturaState(int id_cultura, int code) {
        int state = 0;
        if (code == 0 || code == 2) state = 1;
        else if (code == 1 || code == 3) state = 2;
        String query = "UPDATE cultura SET estado = " + state + " WHERE id_cultura = " + id_cultura;
        Statement stmt;
        try {
            stmt = conn.createStatement();
            stmt.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getCulturaNameByID(int cultura_id) {
        String query = "SELECT nome_cultura FROM cultura WHERE id_cultura = " + cultura_id;
        Statement stmt;
        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) return rs.getString("nome_cultura");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    public int getUserByCulturaID(int cultura_id) {
        String query = "SELECT id_utilizador FROM cultura WHERE id_cultura = " + cultura_id;
        Statement stmt;
        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) return rs.getInt("id_utilizador");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return 0;
    }

}
