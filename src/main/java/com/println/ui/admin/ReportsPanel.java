package com.println.ui.admin;

import javax.swing.*;
import java.awt.*;
import com.println.config.DBConnection;


public class ReportsPanel extends JPanel {

    public ReportsPanel(AdminDashboard theme) {

        setBackground(theme.darkBg);
        setLayout(new BorderLayout());

        JLabel title = new JLabel("📈 Reports");
        title.setForeground(theme.textColor);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(title, BorderLayout.NORTH);

        JLabel placeholder = new JLabel("Reports & analytics will appear here.", SwingConstants.CENTER);
        placeholder.setForeground(theme.mutedText);
        placeholder.setFont(new Font("Arial", Font.PLAIN, 18));
        add(placeholder, BorderLayout.CENTER);
    }
}
