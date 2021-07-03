package org.sid.backend;

import java.sql.*;

public class SensorsSQLConnection {

    private Connection conn;

    public SensorsSQLConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://" + "194.210.86.10" + ":" + "3306" + "/" + "sid2021";
            conn = DriverManager.getConnection(url, "aluno", "aluno");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public double[] getLimits(char type, int idZone) {
        String culturas_query = "SELECT * FROM sensor" + ";";
        Statement culturas_stmt = null;
        try {
            culturas_stmt = conn.createStatement();
            ResultSet rs_culturas = culturas_stmt.executeQuery(culturas_query);
            while (rs_culturas.next()) {
                char result_type = rs_culturas.getString("tipo").charAt(0);
                int result_idZone = Integer.parseInt(rs_culturas.getString("idzona"));
                if (result_idZone == idZone && result_type == type) {
                    double inferior = Double.parseDouble(rs_culturas.getString("limiteinferior"));
                    double superior = Double.parseDouble(rs_culturas.getString("limitesuperior"));
                    return new double[]{inferior, superior};
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
