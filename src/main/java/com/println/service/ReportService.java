package com.println.service;

import com.println.config.DBConnection;
import java.sql.Connection;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.view.JasperViewer;
import java.util.HashMap;

public class ReportService {

    public static void printReceipt(int orderId) {
        try {
            // Correct classpath location of your JRXML
            String jrxmlPath = "/receipt.jrxml";

            // Load and compile the JRXML from resources
            JasperReport report = JasperCompileManager.compileReport(
                ReportService.class.getResourceAsStream(jrxmlPath)
            );

            // Parameters to pass to Jasper
            HashMap<String, Object> params = new HashMap<>();
            params.put("ORDER_ID", orderId);

            // DB connection
            Connection conn = DBConnection.getConnection();

            // Fill report with data
            JasperPrint print = JasperFillManager.fillReport(report, params, conn);

            // Display viewer
            JasperViewer.viewReport(print, false);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}