package com.println.config;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import javax.swing.JOptionPane;

public class DBConnection {
    private static Connection conn;

    public static Connection getConnection() {
        try {
            if (conn == null || conn.isClosed()) {
                
                Properties props = new Properties();

                InputStream input = DBConnection.class.getResourceAsStream("dbconfig.properties");

                if (input == null) {
                    throw new FileNotFoundException("Configuration file not found: dbconfig.properties");
                }

                props.load(input);

                String host = props.getProperty("db.host");
                String port = props.getProperty("db.port");
                String dbname = props.getProperty("db.name");
                String user = props.getProperty("db.user");
                String pass = props.getProperty("db.password");

                String url = "jdbc:mysql://" + host + ":" + port + "/" + dbname;

                conn = DriverManager.getConnection(url, user, pass);
            }

            return conn;

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "ERROR: Unable to connect to database.\n" + e.getMessage());
            return null;
        }
    }

    public static void closeConnection() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                conn = null;
                System.out.println("Database connection closed.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
