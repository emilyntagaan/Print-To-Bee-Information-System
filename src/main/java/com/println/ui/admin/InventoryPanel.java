 package com.println.ui.admin;

import com.println.config.DBConnection;
import com.println.dao.InventoryDAO;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.text.MaskFormatter;


public class InventoryPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private AdminDashboard theme;
    private InventoryDAO inventoryDAO = new InventoryDAO();

    private static final Color SOFT_YELLOW = Color.decode("#F7E9A9");

    //  Badge Renderer for STATUS
    private class StatusBadgeRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            JLabel lbl = new JLabel();
            lbl.setOpaque(false);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));

            if (value == null) return lbl;

            String status = value.toString().trim().toLowerCase();

            Color bg;
            Color fg;

            switch (status) {
                case "out of stock":
                    bg = new Color(255, 102, 102);  // red
                    fg = Color.WHITE;
                    break;

                case "available":
                    bg = new Color(102, 204, 102); // green
                    fg = Color.BLACK;
                    break;

                case "low":
                    bg = new Color(255, 204, 102); // yellow
                    fg = Color.BLACK;
                    break;

                default:
                    bg = table.getBackground();
                    fg = table.getForeground();
            }

            lbl.setForeground(fg);
            lbl.setText(value.toString());

            // Add badge panel (rounded)
            return new BadgePanel(lbl, bg);
        }
    }

    /**
     * Small rounded badge container for the status label.
     */
    private class BadgePanel extends JPanel {

        private Color bg;

        public BadgePanel(JLabel lbl, Color bg) {
            this.bg = bg;
            setOpaque(false);
            setLayout(new GridBagLayout());

            lbl.setBorder(new EmptyBorder(3, 12, 3, 12)); // padding inside badge
            add(lbl);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int arc = 20; // rounded pill shape
            g2.setColor(bg);
            g2.fillRoundRect(0, 8, getWidth(), getHeight() - 16, arc, arc);

            g2.dispose();
            super.paintComponent(g);
        }
    }


    public InventoryPanel(AdminDashboard theme) {
        this.theme = theme;

        setLayout(new BorderLayout());
        setBackground(theme.darkBg);

        // ===========================================================
        //                     NEW MODERN HEADER
        // ===========================================================
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(theme.darkBg);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(theme.darkBg);

        // ---------- SEARCH (Left) ----------
        JPanel searchGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchGroup.setBackground(theme.darkBg);

        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setForeground(theme.textColor);
        searchLbl.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        JTextField searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(380, 34));
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        searchField.setBorder(BorderFactory.createLineBorder(theme.borderColor));

        searchGroup.add(searchLbl);
        searchGroup.add(searchField);

        // ---------- BUTTONS (Right) ----------
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setBackground(theme.darkBg);
        buttons.setOpaque(true);
        buttons.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        JButton addBtn = createActionButton("Add Item", theme.accentGreen);
        JButton editBtn = createActionButton("Edit Item", theme.accentBlue);
        JButton deleteBtn = createActionButton("Delete Item", theme.accentRed);

        fixButton(addBtn, theme.accentGreen, Color.WHITE);
        fixButton(editBtn, theme.accentBlue, Color.WHITE);
        fixButton(deleteBtn, theme.accentRed, Color.WHITE);

        buttons.add(addBtn);
        buttons.add(editBtn);
        buttons.add(deleteBtn);

        topBar.add(searchGroup, BorderLayout.WEST);
        topBar.add(buttons, BorderLayout.EAST);

        header.add(topBar, BorderLayout.NORTH);
        add(header, BorderLayout.NORTH);

        // live search + enter key
        searchField.addActionListener(e -> searchInventory(searchField.getText()));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { searchInventory(searchField.getText()); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { searchInventory(searchField.getText()); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });

        // ===========================================================
        // TABLE
        // ===========================================================
        String[] cols = {
                "ID", "Item Name", "Description", "Category", "Unit",
                "Quantity", "Reorder Level", "Supplier", "Last Restock",
                "Cost/Unit", "Remarks", "Status"
        };

        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(model);
        styleTable(table);

        table.getColumnModel()
        .getColumn(11)   // STATUS column index
        .setCellRenderer(new StatusBadgeRenderer());


        JPanel centerWrap = new JPanel(new BorderLayout());
        centerWrap.setBackground(theme.darkBg);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(null);
        sp.getViewport().setBackground(theme.cardBg);
        sp.setBackground(theme.cardBg);

        centerWrap.add(sp, BorderLayout.CENTER);
        add(centerWrap, BorderLayout.CENTER);

        loadInventory();

        // BUTTON ACTIONS
        addBtn.addActionListener(e -> openAddDialog());
        editBtn.addActionListener(e -> openEditDialog());
        deleteBtn.addActionListener(e -> deleteItem());
    }

    private void fixButton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(true);
    }

    private JButton createActionButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        return btn;
    }

        private void styleTable(JTable table) {
        table.setRowHeight(35);
        table.setBackground(theme.cardBg);
        table.setForeground(theme.textColor);
        table.setFont(new Font("Arial", Font.PLAIN, 14));

        // MATCH PRODUCT PANEL SELECTION COLOR
        table.setSelectionBackground(new Color(186, 206, 229)); // light blue
        table.setSelectionForeground(Color.BLACK);

        JTableHeader header = table.getTableHeader();
        header.setBackground(theme.darkBg);
        header.setForeground(theme.mutedText);
        header.setFont(new Font("Arial", Font.BOLD, 14));
    }


    // ===========================================
    // LOAD FUNCTION
    // ===========================================
    private void loadInventory() {
        model.setRowCount(0);


        String sql = "SELECT * FROM inventory ORDER BY inventory_id DESC";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("inventory_id"),
                        rs.getString("item_name"),
                        rs.getString("description"),
                        rs.getString("category"),
                        rs.getString("unit"),
                        rs.getInt("quantity"),
                        rs.getInt("reorder_level"),
                        rs.getString("supplier_name"),
                        rs.getString("last_restock_date"),
                        rs.getDouble("cost_per_unit"),
                        rs.getString("remarks"),
                        rs.getString("status")
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage());
        }
    }

    // ===========================================
    // SEARCH FUNCTION
    // ===========================================
    private void searchInventory(String keyword) {
        model.setRowCount(0);

        if (keyword.trim().isEmpty()) {
            loadInventory();
            return;
        }

        String sql = "SELECT * FROM inventory WHERE "
                + "item_name LIKE ? OR "
                + "category LIKE ? OR "
                + "supplier_name LIKE ? OR "
                + "description LIKE ? "
                + "ORDER BY inventory_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);) {

            String kw = "%" + keyword + "%";
            ps.setString(1, kw);
            ps.setString(2, kw);
            ps.setString(3, kw);
            ps.setString(4, kw);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getInt("inventory_id"),
                            rs.getString("item_name"),
                            rs.getString("description"),
                            rs.getString("category"),
                            rs.getString("unit"),
                            rs.getInt("quantity"),
                            rs.getInt("reorder_level"),
                            rs.getString("supplier_name"),
                            rs.getString("last_restock_date"),
                            rs.getDouble("cost_per_unit"),
                            rs.getString("remarks"),
                            rs.getString("status")
                    });
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error searching: " + ex.getMessage());
        }
    }

    // ===== ADD ITEM, EDIT ITEM, DELETE (unchanged below) =====
    // (I kept all your original code untouched where possible)

    // --- REPLACED: openAddDialog uses AddInventoryDialog ---
    private void openAddDialog() {
        AddInventoryDialog dlg = new AddInventoryDialog(null);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);

        if (dlg.wasSuccessful()) {
            loadInventory();
        }
    }

    // --- REPLACED: openEditDialog uses AddInventoryDialog with existing data ---
    private void openEditDialog() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an item first.");
            return;
        }

        int modelRow = table.convertRowIndexToModel(row);

        // Create an object to hold existing data
        InventoryData existing = new InventoryData();
        existing.id = (int) model.getValueAt(modelRow, 0);
        existing.itemName = String.valueOf(model.getValueAt(modelRow, 1));
        existing.description = String.valueOf(model.getValueAt(modelRow, 2));
        existing.category = String.valueOf(model.getValueAt(modelRow, 3));
        existing.unit = String.valueOf(model.getValueAt(modelRow, 4));
        existing.quantity = String.valueOf(model.getValueAt(modelRow, 5));
        existing.reorderLevel = String.valueOf(model.getValueAt(modelRow, 6));
        existing.supplierName = String.valueOf(model.getValueAt(modelRow, 7));

        Object lr = model.getValueAt(modelRow, 8);
        existing.lastRestock = (lr == null || "null".equals(String.valueOf(lr))) ? "" : String.valueOf(lr);

        existing.costPerUnit = String.valueOf(model.getValueAt(modelRow, 9));
        existing.remarks = String.valueOf(model.getValueAt(modelRow, 10));

        AddInventoryDialog dlg = new AddInventoryDialog(existing);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);

        if (dlg.wasSuccessful()) {
            loadInventory();
        }
    }

    // legacy addItem/updateItem/deleteItem kept for compatibility (still used by other code paths if any)
    private void addItem(String name, String desc, String category, String unit,
                         String qty, String reorder, String supplier, String lastRestock, String cost, String remarks) {

        if (name.isEmpty() || unit.isEmpty() || qty.isEmpty() || reorder.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill required fields (Name, Unit, Quantity, Reorder).");
            return;
        }

        LocalDate parsedDate = null;
        if (lastRestock != null && !lastRestock.trim().isEmpty()) {
            if (!lastRestock.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
                JOptionPane.showMessageDialog(this, "Last Restock date must be in YYYY-MM-DD format.");
                return;
            }
            try {
                parsedDate = LocalDate.parse(lastRestock.trim());
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Last Restock date is not valid.");
                return;
            }
        }

        String sql = "INSERT INTO inventory (item_name, description, category, unit, quantity, reorder_level, supplier_name, last_restock_date, cost_per_unit, remarks) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);) {

            ps.setString(1, name);
            ps.setString(2, desc);
            ps.setString(3, category);
            ps.setString(4, unit);
            ps.setInt(5, Integer.parseInt(qty.trim()));
            ps.setInt(6, Integer.parseInt(reorder.trim()));
            ps.setString(7, supplier);

            if (parsedDate != null) {
                ps.setDate(8, java.sql.Date.valueOf(parsedDate));
            } else {
                ps.setNull(8, Types.DATE);
            }

            double c = 0.0;
            try { c = Double.parseDouble(cost.trim()); } catch (Exception ignored) {}
            ps.setDouble(9, c);

            ps.setString(10, remarks);

            ps.executeUpdate();
            
            // Get generated ID
            ResultSet rs = ps.getGeneratedKeys();
            int newId = -1;
            if (rs.next()) newId = rs.getInt(1);

            // Update status immediately
            if (newId != -1) {
                inventoryDAO.updateInventoryStatus(newId);
            }
            
            JOptionPane.showMessageDialog(this, "Item added successfully!");
            loadInventory();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error adding item: " + e.getMessage());
        }
    }

    private void updateItem(int id, String name, String desc, String category, String unit,
                            String qty, String reorder, String supplier, String lastRestock, String cost, String remarks) {

        LocalDate parsedDate = null;
        if (lastRestock != null && !lastRestock.trim().isEmpty()) {
            if (!lastRestock.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
                JOptionPane.showMessageDialog(this, "Last Restock date must be YYYY-MM-DD.");
                return;
            }
            try {
                parsedDate = LocalDate.parse(lastRestock.trim());
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Invalid date format.");
                return;
            }
        }

        String sql = "UPDATE inventory SET item_name=?, description=?, category=?, unit=?, quantity=?, reorder_level=?, supplier_name=?, last_restock_date=?, cost_per_unit=?, remarks=? WHERE inventory_id=?";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);) {

            ps.setString(1, name);
            ps.setString(2, desc);
            ps.setString(3, category);
            ps.setString(4, unit);
            ps.setInt(5, Integer.parseInt(qty.trim()));
            ps.setInt(6, Integer.parseInt(reorder.trim()));
            ps.setString(7, supplier);

            if (parsedDate != null) {
                ps.setDate(8, java.sql.Date.valueOf(parsedDate));
            } else {
                ps.setNull(8, Types.DATE);
            }

            double c = 0.0;
            try { c = Double.parseDouble(cost.trim()); } catch (Exception ignored) {}
            ps.setDouble(9, c);

            ps.setString(10, remarks);
            ps.setInt(11, id);

            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Item updated!");
            loadInventory();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error updating item: " + e.getMessage());
        }
    }

    private void deleteItem() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an item to delete.");
            return;
        }

        int modelRow = table.convertRowIndexToModel(row);
        int id = (int) model.getValueAt(modelRow, 0);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this item?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM inventory WHERE inventory_id=?";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);) {

                ps.setInt(1, id);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "Item deleted!");
                loadInventory();

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error deleting item: " + e.getMessage());
            }
        }
    }

    // ===========================================
    // New helper class to hold inventory data
    // ===========================================
    private static class InventoryData {
        int id;
        String itemName;
        String description;
        String category;
        String unit;
        String quantity;
        String reorderLevel;
        String supplierName;
        String lastRestock;
        String costPerUnit;
        String remarks;
    }

    /**
     * Custom dialog for Add/Edit Inventory Item (Style B — section-based, full-width fields)
     */
    private class AddInventoryDialog extends JDialog {
        private boolean successful = false;
        private Point dragOffset;

        // Form fields
        private final JTextField tfItemName = new JTextField();
        private final JTextField tfCategory = new JTextField();
        private final JTextArea taDescription = new JTextArea(3, 20);
        private final JTextField tfUnit = new JTextField();
        private final JTextField tfQuantity = new JTextField();
        private final JTextField tfReorderLevel = new JTextField();
        private final JTextField tfSupplierName = new JTextField();
        // private final JTextField tfLastRestock = new JTextField();
        private JFormattedTextField tfLastRestock;
        private final JTextField tfCostPerUnit = new JTextField();
        private final JTextArea taNotes = new JTextArea(3, 20);
        private JComboBox<String> cbCategory;
        private JComboBox<String> cbUnit;

        
        private final InventoryData existingData;

            private final String[] CATEGORY_OPTIONS = {
                "Ink", "Transfer Paper", "Fabric Paint / Coatings",
                "T-Shirts / Jerseys", "Caps / Hats", "Laces / ID Laces",
                "Mugs", "Keychains", "Tote Bags / Eco Bags",
                "Tarpaulin Sheets", "Vinyl", "Banner Frames",
                "Polybags", "Tape", "Labels", "Other"
            };

            private final String[] UNIT_OPTIONS = {
                "Piece", "Yard", "Roll", "Set", "Box"
            };

        private void enableDateValidation(JTextField field) {
            final Border normalBorder = field.getBorder();
                final Border redBorder = BorderFactory.createLineBorder(Color.RED, 2);

            field.setText("YYYY-MM-DD");
            field.setForeground(Color.GRAY);

            field.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusGained(FocusEvent e) {
                        if (field.getText().equals("YYYY-MM-DD")) {
                            field.setText("");
                            field.setForeground(Color.BLACK);
                        }
                    }

                    @Override
                    public void focusLost(FocusEvent e) {
                        validateDate();
                    }

                    private void validateDate() {
                        String t = field.getText().trim();

                        if (t.isEmpty()) {
                            field.setBorder(redBorder);
                            return;
                        }

                        if (!t.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
                            field.setBorder(redBorder);
                            return;
                        }

                        try {
                            LocalDate.parse(t);
                            field.setBorder(normalBorder); // valid
                        } catch (Exception ex) {
                            field.setBorder(redBorder);
                        }
                    }
                });

                // Also validate while typing
                field.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                    private void validateLive() {
                        String t = field.getText().trim();

                        if (!t.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
                            field.setBorder(redBorder);
                            return;
                        }
                        try {
                            LocalDate.parse(t);
                            field.setBorder(normalBorder);
                        } catch (Exception ex) {
                            field.setBorder(redBorder);
                        }
                    }

                    public void insertUpdate(javax.swing.event.DocumentEvent e) { validateLive(); }
                    public void removeUpdate(javax.swing.event.DocumentEvent e) { validateLive(); }
                    public void changedUpdate(javax.swing.event.DocumentEvent e) { validateLive(); }
                });
            }



        AddInventoryDialog(InventoryData existing) {
            super(SwingUtilities.getWindowAncestor(InventoryPanel.this), ModalityType.APPLICATION_MODAL);
            this.existingData = existing;
            setUndecorated(true);
            setPreferredSize(new Dimension(920, 780));
            initComponents();
            if (existing != null) populateFromExisting(existing);
        }

        private void initComponents() {
            // Root panel
            JPanel root = new JPanel(new BorderLayout());
            root.setBackground(Color.WHITE);
            root.setBorder(new LineBorder(new Color(200, 200, 200), 1));
            getContentPane().add(root);

            // Top Header Bar
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
                    Point location = AddInventoryDialog.this.getLocation();
                    location.x += e.getX() - dragOffset.x;
                    location.y += e.getY() - dragOffset.y;
                    AddInventoryDialog.this.setLocation(location);
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

            // Right: Title + Close button
            JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
            rightPanel.setOpaque(false);

            JLabel titleLabel = new JLabel(existingData == null ? "Add New Item" : "Edit Item");
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
            closeBtn.addActionListener(e -> dispose());

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

            // Item Information Section
            formPanel.add(createSectionHeader("Item Information"));
            formPanel.add(Box.createVerticalStrut(15));

            // Initialize Last Restock field with YYYY-MM-DD mask
            try {
                MaskFormatter dateMask = new MaskFormatter("####-##-##");
                dateMask.setPlaceholder("YYYY-MM-DD");
                tfLastRestock = new JFormattedTextField(dateMask);
                tfLastRestock.setColumns(10);
            } catch (Exception e) {
                tfLastRestock = new JFormattedTextField();
            }

            JPanel itemRow1 = new JPanel(new GridLayout(1, 2, 20, 0));
            itemRow1.setBackground(Color.WHITE);
            itemRow1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
            itemRow1.add(createFieldPanel("Item Name *", tfItemName, "Enter item name"));

            // CATEGORY COMBOBOX
            cbCategory = new JComboBox<>(CATEGORY_OPTIONS);
            cbCategory.setSelectedItem(tfCategory.getText().trim());
            tfCategory.setText(""); // clear placeholder
            tfCategory.putClientProperty("linkedComboBox", cbCategory); // link to field
            itemRow1.add(createFieldPanel("Category *", cbCategory, "Select category"));

            formPanel.add(itemRow1);
            formPanel.add(Box.createVerticalStrut(15));

            // DESCRIPTION SECTION
            JPanel descPanel = new JPanel(new BorderLayout(0, 8));
            descPanel.setBackground(Color.WHITE);
            descPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

            JLabel descLabel = new JLabel("Description");
            descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            descLabel.setForeground(new Color(70, 70, 70));

            taDescription.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(8, 10, 8, 10)
            ));
            taDescription.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            taDescription.setLineWrap(true);
            taDescription.setWrapStyleWord(true);
            JScrollPane descScroll = new JScrollPane(taDescription);
            descScroll.setPreferredSize(new Dimension(0, 70));

            descPanel.add(descLabel, BorderLayout.NORTH);
            descPanel.add(descScroll, BorderLayout.CENTER);
            formPanel.add(descPanel);

            formPanel.add(Box.createVerticalStrut(15));

            // Inventory Details Section
            formPanel.add(createSectionHeader("Inventory Details"));
            formPanel.add(Box.createVerticalStrut(15));

            JPanel inventoryRow = new JPanel(new GridLayout(1, 3, 15, 0));
            inventoryRow.setBackground(Color.WHITE);
            inventoryRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

            // UNIT COMBOBOX
            cbUnit = new JComboBox<>(UNIT_OPTIONS);
            cbUnit.setSelectedItem(tfUnit.getText().trim());
            tfUnit.setText(""); // clear placeholder
            tfUnit.putClientProperty("linkedComboBox", cbUnit);

            inventoryRow.add(createFieldPanel("Unit *", cbUnit, "Select unit"));
            inventoryRow.add(createFieldPanel("Quantity *", tfQuantity, "Enter quantity"));
            inventoryRow.add(createFieldPanel("Reorder Level *", tfReorderLevel, "Enter reorder level"));
            formPanel.add(inventoryRow);

            formPanel.add(Box.createVerticalStrut(15));

            // Supplier Information Section
            formPanel.add(createSectionHeader("Supplier Information"));
            formPanel.add(Box.createVerticalStrut(15));

            JPanel supplierRow = new JPanel(new GridLayout(1, 3, 15, 0));
            supplierRow.setBackground(Color.WHITE);
            supplierRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
            supplierRow.add(createFieldPanel("Supplier Name *", tfSupplierName, "Enter supplier name"));
            supplierRow.add(createFieldPanel("Last Restock *", tfLastRestock, "yyyy-mm-dd"));
            supplierRow.add(createFieldPanel("Cost Per Unit *", tfCostPerUnit, "Enter cost per unit"));
            formPanel.add(supplierRow);

            formPanel.add(Box.createVerticalStrut(15));

            // Additional Information Section
            formPanel.add(createSectionHeader("Additional Information"));
            formPanel.add(Box.createVerticalStrut(15));

            JPanel notesPanel = new JPanel(new BorderLayout(0, 8));
            notesPanel.setBackground(Color.WHITE);
            notesPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

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
            notesScroll.setPreferredSize(new Dimension(0, 70));

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

            JButton saveBtn = new JButton("💾 Save Item");
            saveBtn.setPreferredSize(new Dimension(200, 45));
            saveBtn.setBackground(new Color(247, 233, 169));
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

                if (existingData == null) {
                    // Add new item
                    addItemToDB();
                } else {
                    // Update existing item
                    updateItemInDB();
                }
            });

            clearBtn.addActionListener(e -> clearForm());

            // ESC to close
            getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

            // Style all text fields
            styleTextField(tfItemName);
            styleTextField(tfCategory);
            styleTextField(tfUnit);
            styleTextField(tfQuantity);
            styleTextField(tfReorderLevel);
            styleTextField(tfSupplierName);
            styleTextField(tfLastRestock);
            styleTextField(tfCostPerUnit);

            enableDateValidation(tfLastRestock);
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

        private boolean validateAllFields() {
            if (tfItemName.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Item Name is required.");
                tfItemName.requestFocus();
                return false;
            }

            if (cbCategory.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "Category is required.");
                cbCategory.requestFocus();
                return false;
            }

            if (cbUnit.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "Unit is required.");
                cbUnit.requestFocus();
                return false;
            }

            String qty = tfQuantity.getText().trim();
            if (qty.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Quantity is required.");
                tfQuantity.requestFocus();
                return false;
            }
            try {
                Integer.parseInt(qty);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Quantity must be a valid number.");
                tfQuantity.requestFocus();
                return false;
            }

            String reorder = tfReorderLevel.getText().trim();
            if (reorder.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Reorder Level is required.");
                tfReorderLevel.requestFocus();
                return false;
            }
            try {
                Integer.parseInt(reorder);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Reorder Level must be a valid number.");
                tfReorderLevel.requestFocus();
                return false;
            }

            if (tfSupplierName.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Supplier Name is required.");
                tfSupplierName.requestFocus();
                return false;
            }

            String lastRestock = tfLastRestock.getText().trim();
            if (!lastRestock.isEmpty() && !lastRestock.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
                JOptionPane.showMessageDialog(this, "Last Restock must be in yyyy-mm-dd format.");
                tfLastRestock.requestFocus();
                return false;
            }

            String cost = tfCostPerUnit.getText().trim();
            if (!cost.isEmpty()) {
                try {
                    Double.parseDouble(cost);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Cost Per Unit must be a valid number.");
                    tfCostPerUnit.requestFocus();
                    return false;
                }
            }

            return true;
        }

        private void addItemToDB() {
            String sql = "INSERT INTO inventory (item_name, description, category, unit, quantity, reorder_level, supplier_name, last_restock_date, cost_per_unit, remarks) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setString(1, tfItemName.getText().trim());
                ps.setString(2, taDescription.getText().trim());
                ps.setString(3, cbCategory.getSelectedItem().toString());
                ps.setString(4, cbUnit.getSelectedItem().toString());
                ps.setInt(5, Integer.parseInt(tfQuantity.getText().trim()));
                ps.setInt(6, Integer.parseInt(tfReorderLevel.getText().trim()));
                ps.setString(7, tfSupplierName.getText().trim());

                String lastRestock = tfLastRestock.getText().trim();
                if (!lastRestock.isEmpty()) {
                    ps.setDate(8, java.sql.Date.valueOf(LocalDate.parse(lastRestock)));
                } else {
                    ps.setNull(8, Types.DATE);
                }

                String cost = tfCostPerUnit.getText().trim();
                ps.setDouble(9, cost.isEmpty() ? 0.0 : Double.parseDouble(cost));
                ps.setString(10, taNotes.getText().trim());

                ps.executeUpdate();

                // GET NEW ID
                ResultSet rs = ps.getGeneratedKeys();
                int newId = -1;
                if (rs.next()) newId = rs.getInt(1);

                // UPDATE STATUS IMMEDIATELY
                if (newId != -1) {
                    inventoryDAO.updateInventoryStatus(newId);
                }

                JOptionPane.showMessageDialog(this, "Item added successfully!");
                successful = true;
                dispose();

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error adding item: " + e.getMessage());
                e.printStackTrace();
            }
        }


        private void updateItemInDB() {
            String sql = "UPDATE inventory SET item_name=?, description=?, category=?, unit=?, quantity=?, reorder_level=?, supplier_name=?, last_restock_date=?, cost_per_unit=?, remarks=? WHERE inventory_id=?";

            try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, tfItemName.getText().trim());
                ps.setString(2, taDescription.getText().trim());
                ps.setString(3, cbCategory.getSelectedItem().toString());
                ps.setString(4, cbUnit.getSelectedItem().toString());
                ps.setInt(5, Integer.parseInt(tfQuantity.getText().trim()));
                ps.setInt(6, Integer.parseInt(tfReorderLevel.getText().trim()));
                ps.setString(7, tfSupplierName.getText().trim());

                String lastRestock = tfLastRestock.getText().trim();
                if (!lastRestock.isEmpty()) {
                    ps.setDate(8, java.sql.Date.valueOf(LocalDate.parse(lastRestock)));
                } else {
                    ps.setNull(8, Types.DATE);
                }

                String cost = tfCostPerUnit.getText().trim();
                ps.setDouble(9, cost.isEmpty() ? 0.0 : Double.parseDouble(cost));
                ps.setString(10, taNotes.getText().trim());
                ps.setInt(11, existingData.id);

                ps.executeUpdate();

                // UPDATE STATUS AFTER UPDATE
                inventoryDAO.updateInventoryStatus(existingData.id);

                JOptionPane.showMessageDialog(this, "Item updated successfully!");
                successful = true;
                dispose();

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error updating item: " + e.getMessage());
                e.printStackTrace();
            }
        }


        private void clearForm() {
            tfItemName.setText("");
            tfCategory.setText("");
            taDescription.setText("");
            tfUnit.setText("");
            tfQuantity.setText("");
            tfReorderLevel.setText("");
            tfSupplierName.setText("");
            tfLastRestock.setText("");
            tfCostPerUnit.setText("");
            taNotes.setText("");
        }

        private void populateFromExisting(InventoryData data) {
            tfItemName.setText(data.itemName);
            taDescription.setText(data.description);

            // Correct: update selected items, NOT client properties
            cbCategory.setSelectedItem(data.category);
            cbUnit.setSelectedItem(data.unit);

            tfQuantity.setText(data.quantity);
            tfReorderLevel.setText(data.reorderLevel);
            tfSupplierName.setText(data.supplierName);
            tfLastRestock.setText(data.lastRestock);
            tfCostPerUnit.setText(data.costPerUnit);
            taNotes.setText(data.remarks);
        }

        public boolean wasSuccessful() {
            return successful;
        }
    }
}