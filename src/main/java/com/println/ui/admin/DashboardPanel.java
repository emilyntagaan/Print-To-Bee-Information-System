package com.println.ui.admin;

import javax.swing.*;
import java.awt.*;
import com.println.config.DBConnection;


public class DashboardPanel extends JPanel {

    public DashboardPanel(AdminDashboard theme) {

        setBackground(theme.darkBg);
        setLayout(new BorderLayout());

        JLabel title = new JLabel("📊 Dashboard Overview");
        title.setForeground(theme.textColor);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(title, BorderLayout.NORTH);

        JPanel stats = new JPanel(new GridLayout(2, 3, 20, 20));
        stats.setBackground(theme.darkBg);
        stats.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        stats.add(createCard("Total Customers", "1,250", theme.accentBlue, theme));
        stats.add(createCard("Total Products", "320", theme.accentYellow, theme));
        stats.add(createCard("Low Stock Items", "14", theme.accentRed, theme));
        stats.add(createCard("Monthly Revenue", "₱ 420k", theme.accentGreen, theme));
        stats.add(createCard("Pending Orders", "37", theme.accentOrange, theme));
        stats.add(createCard("Returns", "5", theme.accentRed, theme));

        add(stats, BorderLayout.CENTER);
    }

    private JPanel createCard(String label, String value, Color accent, AdminDashboard theme) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(theme.cardBg);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(theme.borderColor),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel lbl = new JLabel(label);
        lbl.setForeground(theme.mutedText);
        lbl.setFont(new Font("Arial", Font.PLAIN, 14));

        JLabel val = new JLabel(value);
        val.setForeground(accent);
        val.setFont(new Font("Arial", Font.BOLD, 26));

        card.add(lbl);
        card.add(Box.createVerticalStrut(10));
        card.add(val);

        return card;
    }
}
