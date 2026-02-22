package com.println.ui.admin;


import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;


public class StyledTable {


    public static JTable create(String[] columns, Object[][] rows, AdminDashboard theme) {


        JTable table = new JTable(rows, columns);
        table.setFillsViewportHeight(true);
        table.setGridColor(theme.borderColor);
        table.setBackground(theme.cardBg);
        table.setForeground(theme.textColor);
        table.setRowHeight(28);


        JTableHeader header = table.getTableHeader();
        header.setBackground(theme.borderColor);    // Black header background
        header.setForeground(theme.accentYellow);   // Yellow text
        header.setFont(new Font("Arial", Font.BOLD, 14));


        return table;
    }
}