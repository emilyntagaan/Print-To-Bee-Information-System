package com.println.test;

import com.println.config.DBConnection;
import java.sql.Connection;

public class TestDB {
    public static void main(String[] args) {

        System.out.println("Testing database connection...");

        // Try connecting using DBConnection class
        Connection conn = DBConnection.getConnection();

        if (conn != null) {
            System.out.println("SUCCESS: Connected to the database!");
        } else {
            System.out.println("FAILED: Could not connect to the database!");
        }

        // Close connection after test
        DBConnection.closeConnection();
    }
}