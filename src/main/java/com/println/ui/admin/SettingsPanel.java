package com.println.ui.admin;


import com.println.config.DBConnection;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableModel;


public class SettingsPanel extends JPanel {


    // ---------- ADDED FIELDS (logic only, no UI change) ----------
    private int adminUserId = -1; // populated when loading admin info
    private JTable tblStaff;


    public SettingsPanel(AdminDashboard theme) {

        // ---------------------------------------------------
        // MODERN UI THEME
        // ---------------------------------------------------
        UIManager.put("ScrollPane.border", BorderFactory.createEmptyBorder());
        UIManager.put("ScrollPane.background", Color.WHITE);
        UIManager.put("Viewport.background", Color.WHITE);


        UIManager.put("TabbedPane.selected", Color.WHITE);
        UIManager.put("TabbedPane.contentAreaColor", Color.WHITE);
        UIManager.put("TabbedPane.borderHightlightColor", Color.WHITE);
        UIManager.put("TabbedPane.light", Color.WHITE);
        UIManager.put("TabbedPane.highlight", Color.WHITE);
        UIManager.put("TabbedPane.focus", Color.WHITE);
        UIManager.put("TabbedPane.shadow", Color.WHITE);
        UIManager.put("TabbedPane.darkShadow", Color.WHITE);


        UIManager.put("TextField.border",
                BorderFactory.createLineBorder(new Color(220, 220, 220)));
        UIManager.put("PasswordField.border",
                BorderFactory.createLineBorder(new Color(220, 220, 220)));


        // ---------------------------------------------------
        // PANEL BASE
        // ---------------------------------------------------
        setBackground(theme.darkBg);
        setLayout(new BorderLayout());


        // ================================
        // CREATE PANELS FIRST
        // ================================
        JPanel adminPanel = buildAdminSettings(theme);
        JPanel staffPanel = buildStaffManagement(theme);


        // ---------------------------
        // MODERN TABS
        // ---------------------------
        JTabbedPane tabs = new JTabbedPane();


        tabs.addChangeListener(e -> {
            for (int i = 0; i < tabs.getTabCount(); i++) {
                Component comp = tabs.getTabComponentAt(i);
                if (comp instanceof JLabel lbl) {
                    if (tabs.getSelectedIndex() == i) {
                        lbl.setOpaque(true);
                        lbl.setBackground(new Color(255, 230, 0, 80));
                        lbl.setForeground(Color.BLACK);
                    } else {
                        lbl.setOpaque(false);
                        lbl.setBackground(null);
                        lbl.setForeground(new Color(120, 120, 120));
                    }
                }
            }
        });


        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabs.setBackground(Color.WHITE);
        tabs.setForeground(Color.DARK_GRAY);
        tabs.setBorder(null);


        tabs.addTab("Admin Settings", wrapWithYellowContainer(adminPanel, theme));
        tabs.setTabComponentAt(0, createTabLabel("Admin Settings"));


        tabs.addTab("Staff Management", wrapWithYellowContainer(staffPanel, theme));
        tabs.setTabComponentAt(1, createTabLabel("Staff Management"));


        if (tabs.getTabCount() > 0) {
            tabs.setSelectedIndex(0);
            tabs.getChangeListeners()[0].stateChanged(new javax.swing.event.ChangeEvent(tabs));
        }


        add(tabs, BorderLayout.CENTER);
    }


    private Component createTabLabel(String title) {
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        return lbl;
    }


    // =====================================================================
    //  MODERN SCROLL BAR
    // =====================================================================
    private void applyModernScrollBar(JScrollPane scroll) {
        scroll.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(180, 180, 180);
                this.thumbDarkShadowColor = new Color(180, 180, 180);
                this.thumbHighlightColor = new Color(180, 180, 180);
                this.thumbLightShadowColor = new Color(180, 180, 180);
                this.trackColor = new Color(245, 245, 245);
                this.trackHighlightColor = new Color(245, 245, 245);
            }


            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createInvisibleButton();
            }


            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createInvisibleButton();
            }


            private JButton createInvisibleButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                button.setMinimumSize(new Dimension(0, 0));
                button.setMaximumSize(new Dimension(0, 0));
                return button;
            }


            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


                Color color;
                if (isDragging) {
                    color = new Color(140, 140, 140);
                } else if (isThumbRollover()) {
                    color = new Color(160, 160, 160);
                } else {
                    color = new Color(180, 180, 180);
                }


                g2.setColor(color);


                int padding = 3;
                g2.fillRoundRect(
                        thumbBounds.x + padding,
                        thumbBounds.y,
                        thumbBounds.width - (padding * 2),
                        thumbBounds.height,
                        8, 8
                );
                g2.dispose();
            }


            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(250, 250, 250));
                g2.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
                g2.dispose();
            }
        });


        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
    }


    // =====================================================================
    //  ADMIN SETTINGS PANEL
    // =====================================================================
    private JPanel buildAdminSettings(AdminDashboard theme) {


        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);


        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(30, 40, 30, 40));
        content.setBackground(Color.WHITE);


        JLabel title = new JLabel("Update Admin Credentials");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(20, 20, 20));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(title);


        JPanel line = new JPanel();
        line.setBackground(theme.accentYellow);
        line.setMaximumSize(new Dimension(140, 3));
        line.setPreferredSize(new Dimension(140, 3));
        line.setAlignmentX(Component.CENTER_ALIGNMENT);


        content.add(Box.createVerticalStrut(10));
        content.add(line);
        content.add(Box.createVerticalStrut(25));


        JLabel sec1 = buildSectionLabel("Login Credentials");
        sec1.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(sec1);


        JTextField tfUsername = createModernField();
        JPasswordField tfCurrentPass = createModernPassField();
        JPasswordField tfNewPass = createModernPassField();
        JPasswordField tfConfirmPass = createModernPassField();


        content.add(buildLabeledField("Username *", tfUsername));
        content.add(buildLabeledField("Current Password", tfCurrentPass));
        content.add(buildLabeledField("New Password", tfNewPass));
        content.add(buildLabeledField("Confirm New Password", tfConfirmPass));


        content.add(Box.createVerticalStrut(25));


        JLabel sec2 = buildSectionLabel("Personal Information");
        sec2.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(sec2);


        JTextField tfFname = createModernField();
        JTextField tfLname = createModernField();
        JTextField tfEmail = createModernField();
        JTextField tfContact = createModernField();
        applyStrictContactValidation(tfContact);
        JTextField tfAddress = createModernField();


        content.add(buildLabeledField("First Name *", tfFname));
        content.add(buildLabeledField("Last Name *", tfLname));
        content.add(buildLabeledField("Email *", tfEmail));
        content.add(buildLabeledField("Contact Number *", tfContact));
        content.add(buildLabeledField("Address (Street / Purok / Barangay / City / Province) *", tfAddress));


        content.add(Box.createVerticalStrut(25));


        JButton btnUpdate = createModernButton("Update", theme.accentYellow, Color.BLACK);


        btnUpdate.addActionListener(e -> {
            String username = tfUsername.getText().trim();
            String currentPass = new String(tfCurrentPass.getPassword());
            String newPass = new String(tfNewPass.getPassword());
            String confirmPass = new String(tfConfirmPass.getPassword());
            String fname = tfFname.getText().trim();
            String lname = tfLname.getText().trim();
            String email = tfEmail.getText().trim();
            String contact = tfContact.getText().trim();
            String address = tfAddress.getText().trim();


            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username is required.");
                return;
            }
            if (fname.isEmpty()) {
                JOptionPane.showMessageDialog(this, "First name is required.");
                return;
            }
            if (lname.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Last name is required.");
                return;
            }
            if (!email.isEmpty() && !isValidEmail(email)) {
                JOptionPane.showMessageDialog(this, "Invalid email format.");
                return;
            }


            try (Connection conn = DBConnection.getConnection()) {


                PreparedStatement psLoad = conn.prepareStatement("SELECT user_id, password FROM users WHERE role = 'Admin' LIMIT 1");
                ResultSet rs = psLoad.executeQuery();
                if (!rs.next()) {
                    JOptionPane.showMessageDialog(this, "Admin row not found in database.");
                    rs.close();
                    psLoad.close();
                    return;
                }


                int dbAdminId = rs.getInt("user_id");
                String dbPassword = rs.getString("password");
                rs.close();
                psLoad.close();


                if (!newPass.isEmpty()) {
                    if (currentPass.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Enter current password to change to a new password.");
                        return;
                    }
                    if (!currentPass.equals(dbPassword)) {
                        JOptionPane.showMessageDialog(this, "Current password is incorrect.");
                        return;
                    }
                    if (!newPass.equals(confirmPass)) {
                        JOptionPane.showMessageDialog(this, "New password and confirm password do not match.");
                        return;
                    }
                }


                PreparedStatement ps;
                if (!newPass.isEmpty()) {
                    ps = conn.prepareStatement(
                            "UPDATE users SET username=?, password=?, first_name=?, last_name=?, email=?, contact_no=?, address=? WHERE user_id=?"
                    );
                    ps.setString(1, username);
                    ps.setString(2, newPass);
                    ps.setString(3, fname);
                    ps.setString(4, lname);
                    ps.setString(5, email);
                    ps.setString(6, contact);
                    ps.setString(7, address);
                    ps.setInt(8, dbAdminId);
                } else {
                    ps = conn.prepareStatement(
                            "UPDATE users SET username=?, first_name=?, last_name=?, email=?, contact_no=?, address=? WHERE user_id=?"
                    );
                    ps.setString(1, username);
                    ps.setString(2, fname);
                    ps.setString(3, lname);
                    ps.setString(4, email);
                    ps.setString(5, contact);
                    ps.setString(6, address);
                    ps.setInt(7, dbAdminId);
                }


                int updated = ps.executeUpdate();
                ps.close();


                if (updated > 0) {
                    JOptionPane.showMessageDialog(this, "Admin account updated successfully.");
                    loadStaffTable();


                    loadAdminInfoIntoFields(tfUsername, tfCurrentPass, tfNewPass, tfConfirmPass,
                            tfFname, tfLname, tfEmail, tfContact, tfAddress);
                } else {
                    JOptionPane.showMessageDialog(this, "Update failed.");
                }


            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error updating admin: " + ex.getMessage());
            }
        });


        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnWrapper.setBackground(Color.WHITE);
        btnWrapper.add(btnUpdate);


        content.add(btnWrapper);


        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        applyModernScrollBar(scroll);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);


        panel.add(scroll, BorderLayout.CENTER);


        loadAdminInfoIntoFields(tfUsername, tfCurrentPass, tfNewPass, tfConfirmPass,
                tfFname, tfLname, tfEmail, tfContact, tfAddress);


        return panel;
    }


    // =====================================================================
    //  STAFF MANAGEMENT PANEL
    // =====================================================================
    private JPanel buildStaffManagement(AdminDashboard theme) {


        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);


        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(30, 40, 30, 40));
        content.setBackground(Color.WHITE);


        JPanel titleWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        titleWrapper.setBackground(Color.WHITE);
        titleWrapper.setAlignmentX(Component.CENTER_ALIGNMENT);


        JLabel title = new JLabel("Add New Staff");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(20, 20, 20));


        titleWrapper.add(title);
        content.add(titleWrapper);


        JPanel lineWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        lineWrapper.setBackground(Color.WHITE);
        lineWrapper.setAlignmentX(Component.CENTER_ALIGNMENT);


        JPanel line = new JPanel();
        line.setBackground(theme.accentYellow);
        line.setPreferredSize(new Dimension(130, 3));


        lineWrapper.add(line);


        content.add(Box.createVerticalStrut(10));
        content.add(lineWrapper);
        content.add(Box.createVerticalStrut(25));


        JTextField tfUsername = createModernField();
        JPasswordField tfPassword = createModernPassField();
        JTextField tfFname = createModernField();
        JTextField tfLname = createModernField();
        JTextField tfEmail = createModernField();
        JTextField tfContact = createModernField();
        applyStrictContactValidation(tfContact);
        JTextField tfAddress = createModernField();


        content.add(buildLabeledField("Username *", tfUsername));
        content.add(buildLabeledField("Password *", tfPassword));
        content.add(buildLabeledField("First Name *", tfFname));
        content.add(buildLabeledField("Last Name *", tfLname));
        content.add(buildLabeledField("Email *", tfEmail));
        content.add(buildLabeledField("Contact Number *", tfContact));
        content.add(buildLabeledField("Address (Street / Purok / Barangay / City / Province) *", tfAddress));


        content.add(Box.createVerticalStrut(25));


        JButton btnClear = createModernButton("Clear", new Color(240, 240, 240), new Color(60, 60, 60));
        JButton btnAdd = createModernButton("Add Staff", theme.accentYellow, Color.BLACK);


        btnAdd.addActionListener(e -> {
            String username = tfUsername.getText().trim();
            String password = new String(tfPassword.getPassword()).trim();
            String fname = tfFname.getText().trim();
            String lname = tfLname.getText().trim();
            String email = tfEmail.getText().trim();
            String contact = tfContact.getText().trim();
            String address = tfAddress.getText().trim();


            if (username.isEmpty() || password.isEmpty() || fname.isEmpty() || lname.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill required fields (username, password, first name, last name).");
                return;
            }


            if (!email.isEmpty() && !isValidEmail(email)) {
                JOptionPane.showMessageDialog(this, "Invalid email format.");
                return;
            }


            try (Connection conn = DBConnection.getConnection()) {


                PreparedStatement chk = conn.prepareStatement("SELECT user_id FROM users WHERE username = ?");
                chk.setString(1, username);
                ResultSet rschk = chk.executeQuery();
                if (rschk.next()) {
                    JOptionPane.showMessageDialog(this, "Username already exists. Choose another.");
                    rschk.close();
                    chk.close();
                    return;
                }
                rschk.close();
                chk.close();


                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO users (username, password, role, first_name, last_name, email, contact_no, address, status, date_created) " +
                                "VALUES (?, ?, 'Staff', ?, ?, ?, ?, ?, 'Active', NOW())"
                );
                ps.setString(1, username);
                ps.setString(2, password);
                ps.setString(3, fname);
                ps.setString(4, lname);
                ps.setString(5, email);
                ps.setString(6, contact);
                ps.setString(7, address);


                int inserted = ps.executeUpdate();
                ps.close();


                if (inserted > 0) {
                    JOptionPane.showMessageDialog(this, "Staff added successfully!");


                    loadStaffTable();


                    tfUsername.setText("");
                    tfPassword.setText("");
                    tfFname.setText("");
                    tfLname.setText("");
                    tfEmail.setText("");
                    tfContact.setText("");
                    tfAddress.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add staff.");
                }


            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error adding staff: " + ex.getMessage());
            }
        });


        btnClear.addActionListener(e -> {
            tfUsername.setText("");
            tfPassword.setText("");
            tfFname.setText("");
            tfLname.setText("");
            tfEmail.setText("");
            tfContact.setText("");
            tfAddress.setText("");
        });


        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnWrapper.setBackground(Color.WHITE);
        btnWrapper.add(btnClear);
        btnWrapper.add(btnAdd);


        content.add(btnWrapper);


        content.add(Box.createVerticalStrut(35));


        JLabel listTitle = new JLabel("Existing Staff Members");
        listTitle.setFont(new Font("Segoe UI", Font.BOLD, 19));
        listTitle.setForeground(new Color(20, 20, 20));
        content.add(listTitle);


        content.add(Box.createVerticalStrut(12));


        String[] columns = {"Username", "First Name", "Last Name", "Email", "Contact", "Action"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        loadStaffData(model);


        JTable table = new JTable(model) {
            public boolean isCellEditable(int row, int col) {
                return col == 5;
            }
        };


        tblStaff = table;


        // ---------------- ADDED: RED DELETE BUTTON ----------------
        table.getColumnModel().getColumn(5).setCellRenderer(new ActionButtonRenderer());
        table.getColumnModel().getColumn(5).setCellEditor(new ActionButtonEditor(new JCheckBox()));
        // -----------------------------------------------------------


        // ------------ ADDED: original mouse delete logic ------------
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());


                if (row >= 0 && col == 5) {
                    String username = model.getValueAt(row, 0).toString();


                    int confirm = JOptionPane.showConfirmDialog(
                            SettingsPanel.this,
                            "Delete staff '" + username + "'?",
                            "Confirm Delete",
                            JOptionPane.YES_NO_OPTION
                    );


                    if (confirm == JOptionPane.YES_OPTION) {


                        try (Connection conn = DBConnection.getConnection()) {


                            PreparedStatement ps = conn.prepareStatement(
                                    "DELETE FROM users WHERE username = ? AND role = 'Staff' LIMIT 1"
                            );
                            ps.setString(1, username);
                            int deleted = ps.executeUpdate();
                            ps.close();


                            if (deleted > 0) {
                                JOptionPane.showMessageDialog(SettingsPanel.this, "Staff deleted successfully!");


                                loadStaffTable();
                            } else {
                                JOptionPane.showMessageDialog(SettingsPanel.this, "Failed to delete staff.");
                            }


                        } catch (Exception ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(
                                    SettingsPanel.this,
                                    "Error deleting staff: " + ex.getMessage()
                            );
                        }
                    }
                }
            }
        });


        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(250, 250, 250));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
        table.setGridColor(new Color(240, 240, 240));
        table.setShowVerticalLines(false);


        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setPreferredSize(new Dimension(800, 280));
        tableScroll.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1));
        applyModernScrollBar(tableScroll);


        content.add(tableScroll);


        loadStaffIntoModel(model);


        JScrollPane mainScroll = new JScrollPane(content);
        mainScroll.setBorder(null);
        applyModernScrollBar(mainScroll);
        mainScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);


        panel.add(mainScroll, BorderLayout.CENTER);


        return panel;
    }


    // =====================================================================
    //  MODERN COMPONENTS
    // =====================================================================
    private JLabel buildSectionLabel(String text) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl.setForeground(new Color(70, 70, 70));
        lbl.setBorder(new EmptyBorder(12, 0, 8, 0));
        return lbl;
    }


    private JPanel buildLabeledField(String label, JComponent field) {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(Color.WHITE);


        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(new Color(50, 50, 50));


        field.setPreferredSize(new Dimension(400, 40));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(10, 14, 10, 14)));


        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        top.add(lbl, BorderLayout.NORTH);
        top.add(Box.createVerticalStrut(6), BorderLayout.CENTER);


        wrap.add(top, BorderLayout.NORTH);
        wrap.add(field, BorderLayout.CENTER);
        wrap.add(Box.createVerticalStrut(16), BorderLayout.SOUTH);


        return wrap;
    }


    private JTextField createModernField() {
        JTextField t = new JTextField();
        t.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        t.setBackground(new Color(252, 252, 252));
        return t;
    }


    private JPasswordField createModernPassField() {
        JPasswordField p = new JPasswordField();
        p.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        p.setBackground(new Color(252, 252, 252));
        return p;
    }


    private JButton createModernButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBorder(new EmptyBorder(11, 32, 11, 32));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));


        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(bg.darker());
            }


            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bg);
            }
        });


        return btn;
    }


    // =====================================================================
    //  CONTAINER WITH MODERN SHADOW EFFECT
    // =====================================================================
    private JPanel wrapWithYellowContainer(JPanel inner, AdminDashboard theme) {


        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(new Color(248, 248, 248));


        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);


        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(2, 2, 2, 2, theme.accentYellow),
                BorderFactory.createEmptyBorder(1, 1, 1, 1)
        ));


        JPanel padding = new JPanel(new BorderLayout());
        padding.setBackground(Color.WHITE);
        padding.add(Box.createVerticalStrut(15), BorderLayout.NORTH);
        padding.add(Box.createVerticalStrut(15), BorderLayout.SOUTH);
        padding.add(Box.createHorizontalStrut(15), BorderLayout.WEST);
        padding.add(Box.createHorizontalStrut(15), BorderLayout.EAST);
        padding.add(inner, BorderLayout.CENTER);


        card.add(padding, BorderLayout.CENTER);


        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }


    // =============================
    // ========== HELPERS ==========
    // =============================


    private void loadAdminInfoIntoFields(JTextField tfUsername,
                                         JPasswordField tfCurrentPass,
                                         JPasswordField tfNewPass,
                                         JPasswordField tfConfirmPass,
                                         JTextField tfFname,
                                         JTextField tfLname,
                                         JTextField tfEmail,
                                         JTextField tfContact,
                                         JTextField tfAddress) {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT user_id, username, first_name, last_name, email, contact_no, address, password FROM users WHERE role = 'Admin' LIMIT 1");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                adminUserId = rs.getInt("user_id");
                tfUsername.setText(rs.getString("username"));


                tfCurrentPass.setText("");
                tfNewPass.setText("");
                tfConfirmPass.setText("");


                tfFname.setText(rs.getString("first_name"));
                tfLname.setText(rs.getString("last_name"));
                tfEmail.setText(rs.getString("email"));
                tfContact.setText(rs.getString("contact_no"));
                tfAddress.setText(rs.getString("address"));
            }
            rs.close();
            ps.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private void loadStaffIntoModel(DefaultTableModel model) {
        model.setRowCount(0);
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT username, first_name, last_name, email, contact_no FROM users WHERE role = 'Staff' ORDER BY username ASC");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("username"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getString("contact_no"),
                        "Delete"
                });
            }
            rs.close();
            ps.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private void loadStaffData(DefaultTableModel model) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT username, first_name, last_name, email, contact_no FROM users WHERE role = 'Staff'"
             );
             ResultSet rs = stmt.executeQuery()) {


            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("username"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getString("contact_no"),
                        "Edit/Delete"
                });
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void loadStaffTable() {
        DefaultTableModel model = (DefaultTableModel) tblStaff.getModel();
        model.setRowCount(0);


        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT username, first_name, last_name, email, contact_no " +
                            "FROM users WHERE role = 'Staff' ORDER BY date_created DESC"
            );
            ResultSet rs = ps.executeQuery();


            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("username"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getString("contact_no"),
                        "Delete"
                });
            }


            rs.close();
            ps.close();


        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading staff: " + ex.getMessage());
        }
    }


    private void applyStrictContactValidation(JTextField field) {


        field.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();


                if (!Character.isDigit(c)) {
                    e.consume();
                    return;
                }


                if (field.getText().length() >= 11) {
                    e.consume();
                }
            }
        });


        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                String num = field.getText().trim();


                if (!num.matches("^09\\d{9}$")) {
                    JOptionPane.showMessageDialog(
                            SettingsPanel.this,
                            "Invalid contact number.\nFormat: 11 digits, must start with 09 (e.g., 09123456789)."
                    );
                    field.requestFocus();
                }
            }
        });
    }


    private boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) return false;
        return Pattern.compile("^[\\w.-]+@[\\w.-]+\\.[A-Za-z]{2,6}$").matcher(email).matches();
    }


    // ================================================================
    //  RED DELETE BUTTON RENDERER
    // ================================================================
    class ActionButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {


        public ActionButtonRenderer() {
            setOpaque(true);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setForeground(Color.WHITE);
            setBackground(new Color(255, 70, 70));
            setFocusPainted(false);
            setBorderPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(8, 14, 8, 14));
        }


        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {


            setText("Delete");
            return this;
        }
    }


    // ================================================================
    //  RED DELETE BUTTON EDITOR (CLICKABLE)
    // ================================================================
    class ActionButtonEditor extends DefaultCellEditor {


        private JButton button;
        private boolean clicked;


        public ActionButtonEditor(JCheckBox checkBox) {
            super(checkBox);


            button = new JButton("Delete");
            button.setOpaque(true);
            button.setFont(new Font("Segoe UI", Font.BOLD, 12));
            button.setForeground(Color.WHITE);
            button.setBackground(new Color(255, 70, 70));
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setBorder(new EmptyBorder(8, 14, 8, 14));
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));


            button.addActionListener(e -> fireEditingStopped());
        }


        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {


            clicked = true;
            return button;
        }


        @Override
        public Object getCellEditorValue() {
            return "Delete";
        }


        @Override
        public boolean stopCellEditing() {
            clicked = false;
            return super.stopCellEditing();
        }
    }
}