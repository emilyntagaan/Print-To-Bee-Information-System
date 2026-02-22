package com.println.ui.admin;

import com.println.dao.CustomerDAO;
import com.println.model.Customer;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;

/**
 * CustomersPanel - updated to show a custom Add/Edit Customer dialog
 * that visually replicates the provided screenshot and enforces real-time
 * phone validation (exactly 11 digits).
 */
public class CustomersPanel extends JPanel {

    private final AdminDashboard theme;
    private final CustomerDAO customerDAO = new CustomerDAO();

    // Updated columns per request
    private String[] cols = {
        "Customer ID",
        "Name",
        "Gender",
        "Contact #",
        "Email Address",
        "Address",
        "Customer Type",
        "Date Registered",
        "Notes"
    };

    private JTable table;
    private JButton addBtn, editBtn, deleteBtn;
    private JTextField searchField;
    private TableRowSorter<?> rowSorter;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE; // YYYY-MM-DD
    private static final Color SOFT_YELLOW = Color.decode("#F7E9A9");

    public CustomersPanel(AdminDashboard theme) {
        this.theme = theme;

        setBackground(theme.darkBg);
        setLayout(new BorderLayout());

        // ===========================================================
        // MODERN HEADER (MATCHES PRODUCTS / ORDERS / INVENTORY)
        // ===========================================================
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(theme.darkBg);
        header.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(theme.darkBg);

        // ---------- SEARCH GROUP ----------
        JPanel searchGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchGroup.setBackground(theme.darkBg);

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setForeground(theme.textColor);
        searchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(380, 34));
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        searchField.setBorder(BorderFactory.createLineBorder(theme.borderColor));

        searchGroup.add(searchLabel);
        searchGroup.add(searchField);

        // ---------- BUTTON GROUP ----------
        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnGroup.setBackground(theme.darkBg);

        addBtn = createActionButton("+ Add Customer", theme.accentYellow);
        editBtn = createActionButton("Edit", theme.accentBlue);
        deleteBtn = createActionButton("Delete", theme.accentRed);

        btnGroup.add(addBtn);
        btnGroup.add(editBtn);
        btnGroup.add(deleteBtn);

        topBar.add(searchGroup, BorderLayout.WEST);
        topBar.add(btnGroup, BorderLayout.EAST);

        header.add(topBar, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        // ===========================================================
        // TABLE
        // ===========================================================
        table = StyledTable.create(cols, new Object[0][], theme);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // ===========================================================
        // SEARCH FILTERING
        // ===========================================================
        rowSorter = new TableRowSorter<>(table.getModel());
        table.setRowSorter(rowSorter);

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }

            private void filter() {
                String text = searchField.getText().trim();
                if (text.isEmpty()) rowSorter.setRowFilter(null);
                else rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            }
        });

        wireActions();
        reloadTable();
    }

    private JButton createActionButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.BLACK);
        b.setFont(new Font("Arial", Font.BOLD, 12));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setOpaque(true);
        b.setBorderPainted(false);
        return b;
    }

    private void wireActions() {
        addBtn.addActionListener(e -> {
            int createdBy = resolveCurrentUserId(); // still available if needed
            Customer created = showCustomerFormAndCreate(createdBy);
            if (created != null) reloadTable();
        });

        editBtn.addActionListener(e -> {
            int sel = table.getSelectedRow();
            if (sel < 0) {
                JOptionPane.showMessageDialog(this, "Select a customer to edit.");
                return;
            }

            int modelRow = table.convertRowIndexToModel(sel);
            int id = (int) table.getModel().getValueAt(modelRow, 0);
            Customer c = customerDAO.getCustomerById(id);
            if (c == null) return;

            Customer updated = showCustomerFormAndEdit(c);
            if (updated != null) reloadTable();
        });

        deleteBtn.addActionListener(e -> {
            int sel = table.getSelectedRow();
            if (sel < 0) {
                JOptionPane.showMessageDialog(this, "Select a customer to delete.");
                return;
            }

            int modelRow = table.convertRowIndexToModel(sel);
            int id = (int) table.getModel().getValueAt(modelRow, 0);

            int ok = JOptionPane.showConfirmDialog(this,
                    "Delete customer ID " + id + "?",
                    "Confirm", JOptionPane.YES_NO_OPTION);

            if (ok == JOptionPane.YES_OPTION) {
                if (customerDAO.deleteCustomer(id)) reloadTable();
            }
        });
    }

    private int resolveCurrentUserId() {
        try {
            String[] tryMethods = {"getCurrentUserId", "getLoggedInUserId", "getCurrentUser", "getLoggedInUser"};
            for (String mName : tryMethods) {
                try {
                    Method m = theme.getClass().getMethod(mName);
                    Object res = m.invoke(theme);
                    if (res == null) continue;

                    if (res instanceof Number) return ((Number) res).intValue();
                } catch (NoSuchMethodException ignored) {}
            }
        } catch (Exception ignored) {}

        return 1;
    }

    // ===========================================================
    // CUSTOMER FORM LOGIC (UPDATED per design)
    // Uses a custom Add/Edit dialog to match screenshot exactly
    // ===========================================================
    private Customer showCustomerFormAndCreate(int createdBy) {
        AddCustomerDialog dlg = new AddCustomerDialog(null);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
        Customer result = dlg.getResult();
        if (result != null) {
            // Ensure created_by and defaults are set prior to DAO call
            result.setCreatedBy(1);
            if (result.getStatus() == null) result.setStatus("Active");
            result.setDateRegistered(LocalDateTime.now());
            result.setDataUpdated(LocalDateTime.now());

            if (customerDAO.addCustomer(result)) {
                return result;
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add customer (DB).");
            }
        }
        return null;
    }

    private Customer showCustomerFormAndEdit(Customer existing) {
        AddCustomerDialog dlg = new AddCustomerDialog(existing);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
        Customer result = dlg.getResult();
        if (result != null) {
            // update timestamps
            result.setDataUpdated(LocalDateTime.now());
            if (customerDAO.updateCustomer(result)) {
                return result;
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update customer (DB).");
            }
        }
        return null;
    }

    /**
     * Custom JDialog that recreates the look from the screenshot.
     * It supports both Add (existing == null) and Edit (existing != null).
     */
    /**
 * Custom JDialog that recreates the exact look from the screenshot.
 * Replace the existing AddCustomerDialog inner class with this updated version.
 */
/**
 * Custom JDialog that recreates the exact look from the screenshot.
 * Replace the existing AddCustomerDialog inner class with this updated version.
 */
/**
 * Custom JDialog that recreates the exact look from the screenshot.
 * Replace the existing AddCustomerDialog inner class with this updated version.
 */
private class AddCustomerDialog extends JDialog {
    private Customer result = null;

    // Form components
    private final JTextField tfName = new JTextField();
    private final JComboBox<String> cbGender = new JComboBox<>(new String[]{"Select gender", "Male", "Female", "Other"});
    private final JTextField tfContact = new JTextField();
    private final JTextField tfEmail = new JTextField();

    // Address parts
    private final JTextField tfStreet = new JTextField();
    private final JTextField tfBarangay = new JTextField();
    private final JTextField tfCity = new JTextField();
    private final JTextField tfProvince = new JTextField();
    private final JTextField tfZip = new JTextField();

    private final JComboBox<String> cbType = new JComboBox<>(new String[]{"Select type", "Individual", "Business", "Corporate", "VIP"});
    private final JTextArea taNotes = new JTextArea(4, 20);

    private final Customer existingCustomer;

    private Point dragOffset;
    private JLabel lblContactHint = new JLabel(" ");

    AddCustomerDialog(Customer existing) {
        super(SwingUtilities.getWindowAncestor(CustomersPanel.this), ModalityType.APPLICATION_MODAL);
        this.existingCustomer = existing;
        setUndecorated(true);
        setPreferredSize(new Dimension(750, 820));
        initComponents();
        if (existing != null) populateFromExisting(existing);
    }

    private void initComponents() {
        // Root panel
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.setBorder(new LineBorder(new Color(200, 200, 200), 1));
        getContentPane().add(root);

        // Top Header Bar (Dark blue/black)
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setBackground(new Color(30, 41, 59));
        topHeader.setPreferredSize(new Dimension(0, 60));
        topHeader.setBorder(new EmptyBorder(10, 20, 10, 20));

        // Make header draggable
        topHeader.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                dragOffset = e.getPoint();
            }
        });
        topHeader.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                Point location = AddCustomerDialog.this.getLocation();
                location.x += e.getX() - dragOffset.x;
                location.y += e.getY() - dragOffset.y;
                AddCustomerDialog.this.setLocation(location);
            }
        });

        // Left: Logo + App Name
        JPanel logoArea = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        logoArea.setOpaque(false);
        
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/resources/images/printtobee.png"));
            Image img = icon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
            JLabel logo = new JLabel(new ImageIcon(img));
            logoArea.add(logo);
        } catch (Exception ex) {
            JLabel fallback = new JLabel("🐝");
            fallback.setFont(new Font("Segoe UI", Font.PLAIN, 32));
            logoArea.add(fallback);
        }
        
        JLabel appName = new JLabel("PRINT TO BEE");
        appName.setForeground(Color.WHITE);
        appName.setFont(new Font("Segoe UI", Font.BOLD, 16));
        logoArea.add(appName);

        // Right: "Add Customer" text + Close button
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Add Customer");
        titleLabel.setForeground(SOFT_YELLOW);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        
        JButton closeBtn = new JButton("✕");
        closeBtn.setPreferredSize(new Dimension(35, 35));
        closeBtn.setBackground(new Color(50, 61, 79));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFont(new Font("Segoe UI Symbol", Font.BOLD, 18));
        closeBtn.setFocusPainted(false);
        closeBtn.setBorder(null);
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                closeBtn.setBackground(new Color(220, 53, 69));
            }
            public void mouseExited(MouseEvent e) {
                closeBtn.setBackground(new Color(50, 61, 79));
            }
        });
        closeBtn.addActionListener(e -> {
            result = null;
            dispose();
        });
        
        rightPanel.add(titleLabel);
        rightPanel.add(closeBtn);

        topHeader.add(logoArea, BorderLayout.WEST);
        topHeader.add(rightPanel, BorderLayout.EAST);

        root.add(topHeader, BorderLayout.NORTH);

        // Main Form Area
        JPanel formContainer = new JPanel(new BorderLayout());
        formContainer.setBackground(Color.WHITE);
        formContainer.setBorder(new EmptyBorder(20, 30, 20, 30));

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);

        // Personal Information Section
        formPanel.add(createSectionHeader("Personal Information"));
        formPanel.add(Box.createVerticalStrut(15));
        
        JPanel personalRow = new JPanel(new GridLayout(1, 2, 20, 0));
        personalRow.setBackground(Color.WHITE);
        personalRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        personalRow.add(createFieldPanel("Full Name *", tfName, "Enter full name"));
        personalRow.add(createFieldPanel("Gender *", cbGender, null));
        formPanel.add(personalRow);
        
        formPanel.add(Box.createVerticalStrut(15));

        // Contact Information Section
        formPanel.add(createSectionHeader("Contact Information"));
        formPanel.add(Box.createVerticalStrut(15));
        
        JPanel contactRow = new JPanel(new GridLayout(1, 2, 20, 0));
        contactRow.setBackground(Color.WHITE);
        contactRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        
        // Contact number with validation hint
        JPanel contactWrapper = new JPanel(new BorderLayout(0, 8));
        contactWrapper.setBackground(Color.WHITE);
        
        JLabel contactLabel = new JLabel("Contact Number *");
        contactLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        contactLabel.setForeground(new Color(70, 70, 70));
        
        JPanel contactFieldWrapper = new JPanel(new BorderLayout());
        contactFieldWrapper.setBackground(Color.WHITE);
        contactFieldWrapper.add(tfContact, BorderLayout.CENTER);
        
        lblContactHint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblContactHint.setForeground(new Color(100, 100, 100));
        
        contactWrapper.add(contactLabel, BorderLayout.NORTH);
        contactWrapper.add(contactFieldWrapper, BorderLayout.CENTER);
        contactWrapper.add(lblContactHint, BorderLayout.SOUTH);
        
        contactRow.add(contactWrapper);
        contactRow.add(createFieldPanel("Email Address *", tfEmail, "email@example.com"));
        formPanel.add(contactRow);
        
        formPanel.add(Box.createVerticalStrut(15));

        // Address Details Section
        formPanel.add(createSectionHeader("Address Details"));
        formPanel.add(Box.createVerticalStrut(15));
        
        JPanel addressRow1 = new JPanel(new GridLayout(1, 2, 20, 0));
        addressRow1.setBackground(Color.WHITE);
        addressRow1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        addressRow1.add(createFieldPanel("Street Address *", tfStreet, "House No., Street Name"));
        addressRow1.add(createFieldPanel("Barangay *", tfBarangay, "Enter barangay"));
        formPanel.add(addressRow1);
        
        formPanel.add(Box.createVerticalStrut(15));
        
        JPanel addressRow2 = new JPanel(new GridLayout(1, 3, 15, 0));
        addressRow2.setBackground(Color.WHITE);
        addressRow2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        addressRow2.add(createFieldPanel("City *", tfCity, "Enter city"));
        addressRow2.add(createFieldPanel("Province *", tfProvince, "Enter province"));
        addressRow2.add(createFieldPanel("Zip Code *", tfZip, "Enter zip code"));
        formPanel.add(addressRow2);
        
        formPanel.add(Box.createVerticalStrut(15));

        // Customer Type Section
        formPanel.add(createSectionHeader("Customer Type"));
        formPanel.add(Box.createVerticalStrut(15));
        
        JPanel typeRow = new JPanel(new BorderLayout());
        typeRow.setBackground(Color.WHITE);
        typeRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        JPanel typeField = createFieldPanel("Customer Type *", cbType, null);
        typeField.setMaximumSize(new Dimension(500, 70)); // Increased from 350 to 500
        typeRow.add(typeField, BorderLayout.WEST);
        formPanel.add(typeRow);
        
        formPanel.add(Box.createVerticalStrut(15));

        // Additional Information Section
        formPanel.add(createSectionHeader("Additional Information"));
        formPanel.add(Box.createVerticalStrut(15));
        
        JPanel notesPanel = new JPanel(new BorderLayout(0, 8));
        notesPanel.setBackground(Color.WHITE);
        notesPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        
        JLabel notesLabel = new JLabel("Notes");
        notesLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        notesLabel.setForeground(new Color(70, 70, 70));
        
        taNotes.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(8, 10, 8, 10)
        ));
        taNotes.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        taNotes.setLineWrap(true);
        taNotes.setWrapStyleWord(true);
        JScrollPane notesScroll = new JScrollPane(taNotes);
        notesScroll.setPreferredSize(new Dimension(0, 90));
        
        notesPanel.add(notesLabel, BorderLayout.NORTH);
        notesPanel.add(notesScroll, BorderLayout.CENTER);
        formPanel.add(notesPanel);

        // Wrap in scroll pane
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        formContainer.add(scrollPane, BorderLayout.CENTER);

        root.add(formContainer, BorderLayout.CENTER);

        // Bottom Button Bar
        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonBar.setBackground(Color.WHITE);
        buttonBar.setBorder(new EmptyBorder(5, 20, 15, 20));

        JButton saveBtn = new JButton("💾 Save Customer");
        saveBtn.setPreferredSize(new Dimension(200, 45));
        saveBtn.setBackground(new Color(247, 233, 169)); // Yellow
        saveBtn.setForeground(Color.BLACK);
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveBtn.setFocusPainted(false);
        saveBtn.setBorder(new LineBorder(new Color(247, 233, 169), 1));
        saveBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JButton clearBtn = new JButton("✕ Clear Form");
        clearBtn.setPreferredSize(new Dimension(150, 45));
        clearBtn.setBackground(Color.WHITE);
        clearBtn.setForeground(Color.BLACK);
        clearBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        clearBtn.setFocusPainted(false);
        clearBtn.setBorder(new LineBorder(new Color(200, 200, 200), 1));
        clearBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        buttonBar.add(saveBtn);
        buttonBar.add(clearBtn);

        root.add(buttonBar, BorderLayout.SOUTH);

        // Action Listeners
        saveBtn.addActionListener(e -> {
            if (!validateAllFields()) return;

            Customer c = existingCustomer != null ? existingCustomer : new Customer();
            c.setName(tfName.getText().trim());
            
            String gender = (String) cbGender.getSelectedItem();
            if (!"Select gender".equals(gender)) {
                c.setGender(gender);
            }
            
            c.setContactNo(tfContact.getText().trim());
            c.setEmail(tfEmail.getText().trim());
            
            String street = tfStreet.getText().trim();
            String barangay = tfBarangay.getText().trim();
            String city = tfCity.getText().trim();
            String province = tfProvince.getText().trim();
            String zip = tfZip.getText().trim();
            c.setAddress(String.format("%s, %s, %s, %s, %s", street, barangay, city, province, zip));
            c.setCity(city);
            
            String type = (String) cbType.getSelectedItem();
            if (!"Select type".equals(type)) {
                c.setCustomerType(type);
            }
            
            c.setNotes(taNotes.getText().trim());
            c.setStatus("Active");
            c.setCreatedBy(1);

            result = c;
            dispose();
        });

        clearBtn.addActionListener(e -> clearForm());

        // ESC to close
        getRootPane().registerKeyboardAction(e -> {
            result = null;
            dispose();
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        // Style all text fields and comboboxes
        styleTextField(tfName);
        styleTextField(tfContact);
        styleTextField(tfEmail);
        styleTextField(tfStreet);
        styleTextField(tfBarangay);
        styleTextField(tfCity);
        styleTextField(tfProvince);
        styleTextField(tfZip);
        styleComboBox(cbGender);
        styleComboBox(cbType);
        
        // Add real-time validation for contact number
        tfContact.getDocument().addDocumentListener(new DocumentListener() {
            private void validatePhone() {
                String text = tfContact.getText().trim();
                String digits = text.replaceAll("\\D", "");
                
                if (text.isEmpty()) {
                    setContactNormal();
                    lblContactHint.setText(" ");
                    return;
                }
                
                // Check if contains non-digit characters
                if (!text.matches("\\d*")) {
                    setContactInvalid();
                    lblContactHint.setText("Only digits allowed");
                    return;
                }
                
                if (digits.length() < 11) {
                    setContactInvalid();
                    lblContactHint.setText((11 - digits.length()) + " digit(s) missing");
                } else if (digits.length() > 11) {
                    setContactInvalid();
                    lblContactHint.setText((digits.length() - 11) + " digit(s) over");
                } else {
                    setContactValid();
                    lblContactHint.setText("✓ Valid (11 digits)");
                }
            }
            
            @Override public void insertUpdate(DocumentEvent e) { validatePhone(); }
            @Override public void removeUpdate(DocumentEvent e) { validatePhone(); }
            @Override public void changedUpdate(DocumentEvent e) { validatePhone(); }
        });
    }
    
    private void setContactNormal() {
        tfContact.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(5, 10, 5, 10)
        ));
        lblContactHint.setForeground(new Color(100, 100, 100));
    }
    
    private void setContactValid() {
        tfContact.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(34, 197, 94), 2),
            new EmptyBorder(5, 10, 5, 10)
        ));
        lblContactHint.setForeground(new Color(34, 197, 94));
    }
    
    private void setContactInvalid() {
        tfContact.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(239, 68, 68), 2),
            new EmptyBorder(5, 10, 5, 10)
        ));
        lblContactHint.setForeground(new Color(239, 68, 68));
    }

    private JPanel createSectionHeader(String title) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(SOFT_YELLOW);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        header.setPreferredSize(new Dimension(0, 40));
        header.setBorder(new EmptyBorder(10, 15, 10, 15));

        // Left accent bar
        JPanel accent = new JPanel();
        accent.setBackground(new Color(218, 165, 32));
        accent.setPreferredSize(new Dimension(4, 20));

        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(Color.BLACK);
        label.setBorder(new EmptyBorder(0, 10, 0, 0));

        header.add(accent, BorderLayout.WEST);
        header.add(label, BorderLayout.CENTER);

        return header;
    }

    private JPanel createFieldPanel(String labelText, JComponent field, String placeholder) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(Color.WHITE);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(new Color(70, 70, 70));

        if (field instanceof JTextField && placeholder != null) {
            JTextField tf = (JTextField) field;
            tf.putClientProperty("JTextField.placeholderText", placeholder);
        }

        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);

        return panel;
    }

    private void styleTextField(JTextField field) {
        field.setPreferredSize(new Dimension(0, 38));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(5, 10, 5, 10)
        ));
    }

    private void styleComboBox(JComboBox<String> combo) {
        combo.setPreferredSize(new Dimension(0, 38));
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setBackground(Color.WHITE);
        combo.setBorder(new LineBorder(new Color(220, 220, 220), 1));
    }

    private boolean validateAllFields() {
        String name = tfName.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Full Name is required.");
            return false;
        }
        
        String gender = (String) cbGender.getSelectedItem();
        if (gender == null || gender.equals("Select gender")) {
            JOptionPane.showMessageDialog(this, "Gender is required.");
            return false;
        }
        
        String contact = tfContact.getText().trim();
        String digits = contact.replaceAll("\\D", "");
        if (digits.length() != 11) {
            JOptionPane.showMessageDialog(this, "Contact Number must be exactly 11 digits.");
            return false;
        }
        
        if (tfStreet.getText().trim().isEmpty() || tfBarangay.getText().trim().isEmpty()
                || tfCity.getText().trim().isEmpty() || tfProvince.getText().trim().isEmpty()
                || tfZip.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "All address fields are required.");
            return false;
        }
        
        String type = (String) cbType.getSelectedItem();
        if (type == null || type.equals("Select type")) {
            JOptionPane.showMessageDialog(this, "Customer Type is required.");
            return false;
        }
        
        return true;
    }

    private void clearForm() {
        tfName.setText("");
        cbGender.setSelectedIndex(0);
        tfContact.setText("");
        tfEmail.setText("");
        tfStreet.setText("");
        tfBarangay.setText("");
        tfCity.setText("");
        tfProvince.setText("");
        tfZip.setText("");
        cbType.setSelectedIndex(0);
        taNotes.setText("");
        lblContactHint.setText(" ");
        setContactNormal();
    }

    private void populateFromExisting(Customer existing) {
        tfName.setText(existing.getName());
        if (existing.getGender() != null) {
            cbGender.setSelectedItem(existing.getGender());
        }
        tfContact.setText(existing.getContactNo());
        tfEmail.setText(existing.getEmail());
        
        String addr = existing.getAddress();
        if (addr != null && addr.contains(",")) {
            String[] parts = addr.split("\\s*,\\s*");
            if (parts.length >= 1) tfStreet.setText(parts[0]);
            if (parts.length >= 2) tfBarangay.setText(parts[1]);
            if (parts.length >= 3) tfCity.setText(parts[2]);
            if (parts.length >= 4) tfProvince.setText(parts[3]);
            if (parts.length >= 5) tfZip.setText(parts[4]);
        } else {
            tfStreet.setText(existing.getAddress());
            tfCity.setText(existing.getCity());
        }
        
        if (existing.getCustomerType() != null) {
            cbType.setSelectedItem(existing.getCustomerType());
        }
        
        taNotes.setText(existing.getNotes());
    }

    public Customer getResult() {
        return result;
    }
}
    
    // ===========================================================
    // Small helper components for the custom UI
    // ===========================================================

    /**
     * RoundedPanel paints a rounded rectangle background (card look).
     */
    private static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color bg;

        RoundedPanel(int radius, Color bg) {
            this.radius = radius;
            this.bg = bg;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            // subtle shadow-like border
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, w, h, radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * SectionHeader renders the full-width soft yellow header similar to the screenshot.
     */
    private static class SectionHeader extends JPanel {
        private final String title;
        SectionHeader(String title) {
            this.title = title;
            setPreferredSize(new Dimension(0, 40));
            setOpaque(false);
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(6, 12, 6, 12));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            // background pale yellow rounded rectangle
            g2.setColor(SOFT_YELLOW);
            g2.fillRoundRect(0, 0, w, h - 6, 12, 12);

            // small left accent (like a pill)
            g2.setColor(SOFT_YELLOW.darker());
            g2.fillRoundRect(8, 6, 12, h - 18, 10, 10);

            // title text
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
            FontMetrics fm = g2.getFontMetrics();
            int textY = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(title, 36, textY);
            g2.dispose();
        }
    }

    // ===========================================================
    // RELOAD TABLE (HEADER MATCHED)
    // ===========================================================
    public void reloadTable() {
        List<Customer> list = customerDAO.getAllCustomers();
        List<Object[]> rows = new ArrayList<>();

        for (Customer c : list) {
            String dateRegistered = "";
            if (c.getDateRegistered() != null) {
                dateRegistered = c.getDateRegistered().format(DATE_FMT);
            }
            rows.add(new Object[]{
                c.getCustomerId(),
                c.getName(),
                c.getGender(),
                c.getContactNo(),
                c.getEmail(),
                c.getAddress(),
                c.getCustomerType(),
                dateRegistered,
                c.getNotes()
            });
        }

        Object[][] data = rows.toArray(new Object[0][]);

        removeAll();
        setLayout(new BorderLayout());

        // ---------- HEADER (REBUILD MATCHING VERSION) ----------
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(theme.darkBg);
        header.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(theme.darkBg);

        // Search group
        JPanel searchGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchGroup.setBackground(theme.darkBg);

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setForeground(theme.textColor);
        searchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(380, 34));
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        searchField.setBorder(BorderFactory.createLineBorder(theme.borderColor));

        searchGroup.add(searchLabel);
        searchGroup.add(searchField);

        // Buttons
        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnGroup.setBackground(theme.darkBg);

        addBtn = createActionButton("+ Add Customer", theme.accentYellow);
        editBtn = createActionButton("Edit", theme.accentBlue);
        deleteBtn = createActionButton("Delete", theme.accentRed);

        btnGroup.add(addBtn);
        btnGroup.add(editBtn);
        btnGroup.add(deleteBtn);

        topBar.add(searchGroup, BorderLayout.WEST);
        topBar.add(btnGroup, BorderLayout.EAST);

        header.add(topBar, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        // ---------- TABLE ----------
        table = StyledTable.create(cols, data, theme);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // ---------- FILTER ----------
        rowSorter = new TableRowSorter<>(table.getModel());
        table.setRowSorter(rowSorter);

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }

            private void filter() {
                String text = searchField.getText().trim();
                if (text.isEmpty()) rowSorter.setRowFilter(null);
                else rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            }
        });

        wireActions();
        revalidate();
        repaint();
    }
}