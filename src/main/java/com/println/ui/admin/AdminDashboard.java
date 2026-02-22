package com.println.ui.admin;

import com.println.config.DBConnection;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.*;

public class AdminDashboard extends JFrame {

    private JPanel contentPanel;
    private CardLayout cardLayout;

    public final Color darkBg = new Color(255, 255, 255);
    public final Color cardBg = new Color(255, 248, 225);
    public final Color borderColor = new Color(0, 0, 0);
    public final Color textColor = new Color(0, 0, 0);
    public final Color mutedText = new Color(117, 117, 117);
    public final Color accentYellow = new Color(255, 193, 7);
    public final Color accentGreen = new Color(76, 175, 80);
    public final Color accentRed = new Color(244, 67, 54);
    public final Color accentOrange = new Color(255, 152, 0);
    public final Color accentBlue = new Color(33, 150, 243);
    public final Color sidebarBg = new Color(255, 243, 224);

    private final Font FONT_NAV = new Font("Segoe UI", Font.BOLD, 24);
    private final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 28);
    private final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 26);
    private final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 20);
    private final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 16);

    JLabel dynamicTitle;

    // 🔥 NEW: track selected menu button
    private JButton selectedButton = null;

    public AdminDashboard() {

        UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 18));
        UIManager.put("Button.font", FONT_BUTTON);

        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true);
        setResizable(false);
        setTitle("Admin Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(darkBg);

        JPanel topHeader = createTopHeader();
        mainPanel.add(topHeader, BorderLayout.NORTH);

        JPanel sidebar = createSidebar();
        mainPanel.add(sidebar, BorderLayout.WEST);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(Color.WHITE);

        // REGISTER PANELS
        contentPanel.add(new DashboardPanel(this), "DASHBOARD");
        contentPanel.add(new CustomersPanel(this), "CUSTOMERS");
        contentPanel.add(new ProductsPanel(this), "PRODUCTS");
        contentPanel.add(new InventoryPanel(this), "INVENTORY");
        contentPanel.add(new OrdersPanel(this), "ORDERS");
        contentPanel.add(new ReportsPanel(this), "REPORTS");
        contentPanel.add(new SettingsPanel(this), "SETTINGS");

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setBackground(Color.WHITE);
        centerWrapper.setBorder(BorderFactory.createEmptyBorder(24, 28, 28, 28));

        dynamicTitle = new JLabel("DASHBOARD");
        dynamicTitle.setFont(FONT_TITLE);
        dynamicTitle.setForeground(textColor);
        dynamicTitle.setBorder(BorderFactory.createEmptyBorder(8, 6, 20, 6));
        centerWrapper.add(dynamicTitle, BorderLayout.NORTH);

        centerWrapper.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(centerWrapper, BorderLayout.CENTER);

        add(mainPanel);
    }

    private JPanel createTopHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(accentYellow);
        header.setPreferredSize(new Dimension(0, 72));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 12));
        left.setOpaque(false);

        JLabel title = new JLabel("PRINT TO BEE");
        title.setFont(FONT_HEADER);
        title.setForeground(Color.BLACK);

        left.add(title);
        header.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 18));
        right.setOpaque(false);

        JLabel welcome = new JLabel("Welcome, " + getLoggedInName());
        welcome.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        welcome.setForeground(Color.BLACK);
        right.add(welcome);

        JButton logout = new JButton("Log Out");
        logout.setBackground(Color.BLACK);
        logout.setForeground(Color.WHITE);
        logout.addActionListener(e -> {
            dispose();
            new com.println.ui.LoginUI().showUI();
        });

        right.add(logout);
        header.add(right, BorderLayout.EAST);

        return header;
    }

    private JPanel createSidebar() {

        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(sidebarBg);
        sidebar.setPreferredSize(new Dimension(260, 0));

        String[] menu = {"DASHBOARD", "CUSTOMERS", "PRODUCTS", "INVENTORY", "ORDERS", "REPORTS", "SETTINGS"};
        String[] icons = {"📊", "👥", "📦", "📋", "🧾", "📈", "⚙️"};

        for (int i = 0; i < menu.length; i++) {
            JButton btn = createMenuButton(menu[i], icons[i]);
            sidebar.add(btn);
            sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
        }

        return sidebar;
    }

    // ⭐ NEW method for menu selection highlight
    private void setSelectedMenu(JButton btn) {
        if (selectedButton != null) {
            selectedButton.setBackground(sidebarBg);
        }
        selectedButton = btn;
        selectedButton.setBackground(new Color(255, 214, 120)); // highlighted yellow
    }

    private JButton createMenuButton(String name, String icon) {

    // 🎉 Use emoji-capable font
    Font emojiFont;
    try {
        emojiFont = new Font("Segoe UI Emoji", Font.PLAIN, 22);
    } catch (Exception e) {
        emojiFont = new Font("Segoe UI Symbol", Font.PLAIN, 22);
    }

    JButton btn = new JButton(icon + "  " + name);
    btn.setFont(emojiFont);
    btn.setForeground(textColor);
    btn.setBackground(sidebarBg);
    btn.setFocusPainted(false);
    btn.setBorderPainted(false);

    // Keep clean alignment
    btn.setHorizontalAlignment(SwingConstants.LEFT);
    btn.setIconTextGap(20);
    btn.setMargin(new Insets(0, 20, 0, 0));
    btn.setMaximumSize(new Dimension(260, 68));

    btn.addActionListener(e -> {
        dynamicTitle.setText(name);
        cardLayout.show(contentPanel, name);
        setSelectedMenu(btn);
    });

    btn.addMouseListener(new MouseAdapter() {
        @Override public void mouseEntered(MouseEvent e) {
            if (btn != selectedButton)
                btn.setBackground(new Color(255, 232, 180));
        }
        @Override public void mouseExited(MouseEvent e) {
            if (btn != selectedButton)
                btn.setBackground(sidebarBg);
        }
    });

    return btn;
}


    public void showView(String name) {
        dynamicTitle.setText(name);
        cardLayout.show(contentPanel, name);
    }

    public static void main(String[] args) {

        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new AdminDashboard().setVisible(true));
    }

    public String getLoggedInName() {
    try (Connection conn = DBConnection.getConnection()) {
        PreparedStatement ps = conn.prepareStatement(
                "SELECT first_name, last_name FROM users WHERE role='Admin' LIMIT 1"
        );
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getString("first_name") + " " + rs.getString("last_name");
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return "Administrator";
}

}