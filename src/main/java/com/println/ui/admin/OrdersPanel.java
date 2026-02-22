package com.println.ui.admin;


import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;


public class OrdersPanel extends JPanel {


    public OrdersPanel(AdminDashboard theme) {


        setBackground(theme.darkBg);
        setLayout(new BorderLayout());


        // ===========================================================
        // HEADER - MATCHED TO PRODUCTS/INVENTORY PANEL
        // ===========================================================
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(theme.darkBg);
        header.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));


        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(theme.darkBg);


        // -------------------------
        // SEARCH GROUP (LEFT)
        // -------------------------
        JPanel searchGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchGroup.setBackground(theme.darkBg);


        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setForeground(theme.textColor);
        searchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));


        JTextField searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(380, 34));
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        searchField.setBorder(BorderFactory.createLineBorder(theme.borderColor));


        searchGroup.add(searchLabel);
        searchGroup.add(searchField);


        // -------------------------
        // BUTTON GROUP (RIGHT)
        // -------------------------
        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnGroup.setBackground(theme.darkBg);


        JButton editBtn = createAction(theme, "Edit", theme.accentBlue);
        JButton deleteBtn = createAction(theme, "Delete", theme.accentRed);


        btnGroup.add(editBtn);
        btnGroup.add(deleteBtn);


        topBar.add(searchGroup, BorderLayout.WEST);
        topBar.add(btnGroup, BorderLayout.EAST);


        header.add(topBar, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);


        // ===========================================================
        // TABLE
        // ===========================================================
        String[] cols = {
            "Order ID", "Customer", "Order Date", "Due Date",
            "Status", "Payment", "Amount", "Quantity"
        };


        Object[][] data = {
            {"1001", "Juan Dela Cruz", "2025-01-10", "2025-01-15", "Pending", "Unpaid", "₱850.00", "12"},
            {"1002", "Maria Santos", "2025-01-11", "2025-01-14", "Completed", "Paid", "₱1,250.00", "25"}
        };


        JTable table = StyledTable.create(cols, data, theme);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);


        add(scroll, BorderLayout.CENTER);


        // ===========================================================
        // SEARCH FUNCTION (MATCHED)
        // ===========================================================
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(table.getModel());
        table.setRowSorter(sorter);


        searchField.getDocument().addDocumentListener(new DocumentListener() {


            @Override
            public void insertUpdate(DocumentEvent e) { filter(); }


            @Override
            public void removeUpdate(DocumentEvent e) { filter(); }


            @Override
            public void changedUpdate(DocumentEvent e) { filter(); }


            private void filter() {
                String text = searchField.getText().trim();


                if (text.isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });
    }


    private JButton createAction(AdminDashboard theme, String label, Color color) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setForeground(Color.BLACK);
        btn.setBackground(color);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        return btn;
    }
}