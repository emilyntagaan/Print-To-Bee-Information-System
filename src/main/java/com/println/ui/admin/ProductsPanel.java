package com.println.ui.admin;

import com.println.dao.InventoryDAO;
import com.println.dao.ProductDAO;
import com.println.model.Inventory;
import com.println.model.Product;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class ProductsPanel extends JPanel {

    private static final Color SOFT_YELLOW = Color.decode("#F7E9A9");

    private final ProductDAO productDAO = new ProductDAO();
    private JTable table;
    private DefaultTableModel model;
    private final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;
    private final DecimalFormat PRICE_FMT = new DecimalFormat("₱#,##0.00");

    public ProductsPanel(AdminDashboard theme) {

        setBackground(theme.darkBg);
        setLayout(new BorderLayout());

        // ===========================================================
        //                     NEW MODERN HEADER
        // ===========================================================
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(theme.darkBg);
        header.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(theme.darkBg);

        // ---------- SEARCH (Left) ----------
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

        // ---------- BUTTONS (Right) ----------
        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnGroup.setBackground(theme.darkBg);

        JButton addBtn = createAction(theme, "+ Add Product", theme.accentYellow);
        JButton editBtn = createAction(theme, "Edit", theme.accentBlue);
        JButton deleteBtn = createAction(theme, "Delete", theme.accentRed);

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
        String[] cols = {
            "ID",
            "Product Name",
            "Description",
            "Category",
            "Price",
            "Unit",
            "Material Used",
            "Quantity Used",
            "Status",
            "Date Added",
            "Print Time",
            "Size",
            "Notes"
        };

        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(model);
        table.setRowHeight(36);
        table.getTableHeader().setReorderingAllowed(false);

        // Basic styling to match other tables (you can refine with your StyledTable helper later)
        table.setFont(new Font("Arial", Font.PLAIN, 13));

        // MATCH PRODUCT PANEL SELECTION COLOR (updated)
        table.setSelectionBackground(new Color(186, 206, 229)); // light blue
        table.setSelectionForeground(Color.BLACK);

        JTableHeader headerComp = table.getTableHeader();
        headerComp.setBackground(theme.darkBg);
        headerComp.setForeground(theme.mutedText);
        headerComp.setFont(new Font("Arial", Font.BOLD, 13));


        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);

        // LOAD DATA
        reloadTable();

        // TABLE SEARCH FUNCTION (client-side filtering)
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(table.getModel());
        table.setRowSorter(sorter);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filter(); }
            @Override public void removeUpdate(DocumentEvent e) { filter(); }
            @Override public void changedUpdate(DocumentEvent e) { filter(); }

            private void filter() {
                String text = searchField.getText().trim();
                if (text.isEmpty()) sorter.setRowFilter(null);
                else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            }
        });

        // OPEN ADD PRODUCT FORM
        addBtn.addActionListener(e -> {
            AddProductForm form = new AddProductForm(theme);
            form.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);

            form.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    reloadTable();
                }
            });

            form.setVisible(true);
        });

        // ===========================
        // EDIT - wired to dialog
        // ===========================
        editBtn.addActionListener(e -> {
            int sel = table.getSelectedRow();
            if (sel < 0) {
                JOptionPane.showMessageDialog(this, "Select a product to edit.");
                return;
            }
            int modelRow = table.convertRowIndexToModel(sel);
            int id = (int) model.getValueAt(modelRow, 0);
            Product p = productDAO.getProductById(id);
            if (p == null) {
                JOptionPane.showMessageDialog(this, "Selected product not found.");
                return;
            }

            ProductEditDialog dlg = new ProductEditDialog(p);
            dlg.pack();
            dlg.setLocationRelativeTo(this);
            dlg.setVisible(true);

            Product updated = dlg.getResult();
            if (updated != null) {
                boolean ok = productDAO.updateProduct(updated);
                if (!ok) {
                    JOptionPane.showMessageDialog(this, "Failed to update product (DB).");
                } else {
                    reloadTable();
                }
            }
        });

        // DELETE
        deleteBtn.addActionListener(e -> {
            int sel = table.getSelectedRow();
            if (sel < 0) {
                JOptionPane.showMessageDialog(this, "Select a product to delete.");
                return;
            }
            int modelRow = table.convertRowIndexToModel(sel);
            int id = (int) model.getValueAt(modelRow, 0);

            int ok = JOptionPane.showConfirmDialog(this,
                    "Delete product ID " + id + "?",
                    "Confirm Deletion",
                    JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) {
                boolean deleted = productDAO.deleteProduct(id);
                if (deleted) {
                    JOptionPane.showMessageDialog(this, "Product deleted.");
                    reloadTable();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete product. Check logs.");
                }
            }
        });
    }

    private void reloadTable() {
        // clear
        model.setRowCount(0);

        try {
            List<Product> list = productDAO.getAllProducts();
            for (Product p : list) {
                Object[] row = new Object[] {
                    p.getProductId(),
                    p.getProductName(),
                    p.getDescription(),
                    p.getCategory(),
                    formatPrice(p.getPrice()),
                    p.getUnit(),
                    p.getMaterialUsed(),
                    p.getQuantityUsed(),
                    p.getStatus(),
                    p.getDateAdded() != null ? p.getDateAdded().format(DATE_FMT) : "",
                    p.getPrintTime(),
                    p.getSize(),
                    p.getNotes()
                };
                model.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading products: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String formatPrice(BigDecimal price) {
        if (price == null) return "₱0.00";
        try {
            return PRICE_FMT.format(price);
        } catch (Exception e) {
            return "₱" + price.toString();
        }
    }

    private JButton createAction(AdminDashboard t, String label, Color bg) {
        JButton b = new JButton(label);
        b.setFont(new Font("Arial", Font.BOLD, 12));
        b.setForeground(Color.BLACK);
        b.setBackground(bg);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        b.setOpaque(true);
        b.setBorderPainted(false);
        return b;
    }

    // =====================================================================
    //  ADD PRODUCT FORM – Matching the exact design from the image
    //  (unchanged from your version; left here for completeness)
    // =====================================================================
    class AddProductForm extends JDialog {

        private Point dragOffset;

        private void styleComboBoxWide(JComboBox<String> combo, int width) {
            combo.setPreferredSize(new Dimension(width, 38));
            combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            combo.setBackground(Color.WHITE);
            combo.setBorder(new LineBorder(new Color(220, 220, 220), 1));
        }

        // Materials selection
        private List<Inventory> inventoryList = new ArrayList<>();
        private List<Inventory> selectedMaterials = new ArrayList<>();
        private final JButton selectMaterialsBtn = new JButton("▼");

        // Form fields
        private final JTextField tfProductName = new JTextField();
        private final JComboBox<String> cbCategory = new JComboBox<>(new String[]{
            "Select category", "3D Printing", "Laser Cutting", "Printing", "Resin", "Other"
        });
        private final JTextArea taDescription = new JTextArea(4, 20);

        private final JTextField tfPrice = new JTextField();
        private final JComboBox<String> cbUnit = new JComboBox<>(new String[]{
            "Select unit", "pc", "set", "roll", "gram", "kg", "ml"
        });

        private final JTextField tfMaterialUsed = new JTextField();
        private final JTextField tfQuantityUsed = new JTextField();
        private final JTextField tfPrintTime = new JTextField();
        private final JComboBox<String> cbSize = new JComboBox<>(new String[]{
            "Select size", "XS", "S", "M", "L", "XL", "Not Applicable"
        });

        private final JTextArea taNotes = new JTextArea(3, 20);

        public AddProductForm(AdminDashboard theme) {
            super(SwingUtilities.getWindowAncestor(ProductsPanel.this), ModalityType.APPLICATION_MODAL);
            // Load inventory items for materials selector
            inventoryList = new InventoryDAO().getAllInventory();
            setUndecorated(true);
            setPreferredSize(new Dimension(800, 850));
            initComponents(theme);
        }

        private void initComponents(AdminDashboard theme) {
            // (identical to your form code — unchanged for now)
            JPanel root = new JPanel(new BorderLayout());
            root.setBackground(Color.WHITE);
            root.setBorder(new LineBorder(new Color(200, 200, 200), 1));
            getContentPane().add(root);

            JPanel topHeader = new JPanel(new BorderLayout());
            topHeader.setBackground(new Color(30, 41, 59));
            topHeader.setPreferredSize(new Dimension(0, 60));
            topHeader.setBorder(new EmptyBorder(10, 20, 10, 20));

            topHeader.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    dragOffset = e.getPoint();
                }
            });
            topHeader.addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseDragged(MouseEvent e) {
                    Point location = AddProductForm.this.getLocation();
                    location.x += e.getX() - dragOffset.x;
                    location.y += e.getY() - dragOffset.y;
                    AddProductForm.this.setLocation(location);
                }
            });

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

            JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
            rightPanel.setOpaque(false);

            JLabel titleLabel = new JLabel("Add Product");
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
                public void mouseEntered(MouseEvent e) { closeBtn.setBackground(new Color(220, 53, 69)); }
                public void mouseExited(MouseEvent e) { closeBtn.setBackground(new Color(50, 61, 79)); }
            });
            closeBtn.addActionListener(e -> dispose());

            rightPanel.add(titleLabel);
            rightPanel.add(closeBtn);

            topHeader.add(logoArea, BorderLayout.WEST);
            topHeader.add(rightPanel, BorderLayout.EAST);
            root.add(topHeader, BorderLayout.NORTH);

            JPanel formContainer = new JPanel(new BorderLayout());
            formContainer.setBackground(Color.WHITE);
            formContainer.setBorder(new EmptyBorder(20, 30, 20, 30));
            JPanel formPanel = new JPanel();
            formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
            formPanel.setBackground(Color.WHITE);

            formPanel.add(createSectionHeader("Product Information"));
            formPanel.add(Box.createVerticalStrut(15));
            JPanel productRow1 = new JPanel(new GridLayout(1, 2, 20, 0));
            productRow1.setBackground(Color.WHITE);
            productRow1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
            productRow1.add(createFieldPanel("Product Name *", tfProductName, "Enter product name"));
            productRow1.add(createFieldPanel("Category *", cbCategory, null));
            formPanel.add(productRow1);
            formPanel.add(Box.createVerticalStrut(15));

            JPanel descPanel = new JPanel(new BorderLayout(0, 8));
            descPanel.setBackground(Color.WHITE);
            descPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
            JLabel descLabel = new JLabel("Description *");
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

            formPanel.add(createSectionHeader("Pricing & Unit"));
            formPanel.add(Box.createVerticalStrut(15));
            JPanel pricingRow = new JPanel(new GridLayout(1, 2, 20, 0));
            pricingRow.setBackground(Color.WHITE);
            pricingRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
            pricingRow.add(createFieldPanel("Price *", tfPrice, "0.00"));
            pricingRow.add(createFieldPanel("Unit *", cbUnit, null));
            formPanel.add(pricingRow);
            formPanel.add(Box.createVerticalStrut(15));

            formPanel.add(createSectionHeader("Material & Production Details"));
            formPanel.add(Box.createVerticalStrut(15));
            JPanel materialRow1 = new JPanel(new GridLayout(1, 3, 15, 0));
            materialRow1.setBackground(Color.WHITE);
            materialRow1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
            materialRow1.add(createMaterialSelectorPanel());
            materialRow1.add(createFieldPanel("Quantity Used *", tfQuantityUsed, "e.g., 50g, 100ml"));
            materialRow1.add(createFieldPanel("Print Time *", tfPrintTime, "e.g., 2 hours, 45 mins"));
            formPanel.add(materialRow1);
            formPanel.add(Box.createVerticalStrut(15));

            JPanel sizeRow = new JPanel(new BorderLayout());
            sizeRow.setBackground(Color.WHITE);
            sizeRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
            JPanel sizeField = createFieldPanel("Size *", cbSize, null);
            sizeField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
            sizeRow.add(sizeField, BorderLayout.WEST);
            formPanel.add(sizeRow);
            formPanel.add(Box.createVerticalStrut(15));

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

            JScrollPane scrollPane = new JScrollPane(formPanel);
            scrollPane.setBorder(null);
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
            formContainer.add(scrollPane, BorderLayout.CENTER);
            root.add(formContainer, BorderLayout.CENTER);

            JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
            buttonBar.setBackground(Color.WHITE);
            buttonBar.setBorder(new EmptyBorder(5, 20, 15, 20));
            JButton saveBtn = new JButton("💾 Save Product");
            saveBtn.setPreferredSize(new Dimension(200, 45));
            saveBtn.setBackground(new Color(247, 233, 169));
            saveBtn.setForeground(Color.BLACK);
            saveBtn.setFont(new Font("Segoe UI Symbol", Font.BOLD, 14));
            saveBtn.setFocusPainted(false);
            saveBtn.setBorder(new LineBorder(new Color(247, 233, 169), 1));
            saveBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            JButton clearBtn = new JButton("✕ Clear Form");
            clearBtn.setPreferredSize(new Dimension(150, 45));
            clearBtn.setBackground(Color.WHITE);
            clearBtn.setForeground(Color.BLACK);
            clearBtn.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 13));
            clearBtn.setFocusPainted(false);
            clearBtn.setBorder(new LineBorder(new Color(200, 200, 200), 1));
            clearBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            buttonBar.add(saveBtn);
            buttonBar.add(clearBtn);
            root.add(buttonBar, BorderLayout.SOUTH);

            saveBtn.addActionListener(e -> {
                if (validateFields()) {
                    if (saveProductToDatabase()) {
                        JOptionPane.showMessageDialog(this, "Product saved successfully!");
                        dispose();
                    }
                }
            });

            clearBtn.addActionListener(e -> clearForm());
            getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

            styleTextField(tfProductName);
            styleTextField(tfPrice);
            styleTextField(tfMaterialUsed);
            styleTextField(tfQuantityUsed);
            styleTextField(tfPrintTime);
            styleComboBox(cbCategory);
            styleComboBox(cbUnit);
            styleComboBoxWide(cbSize, 200);  // ← adjust 300 to any width you want

            pack();
        }

        private JPanel createMaterialSelectorPanel() {
            JPanel panel = new JPanel(new BorderLayout(0, 8));
            panel.setBackground(Color.WHITE);

            JLabel label = new JLabel("Materials Used *");
            label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            label.setForeground(new Color(70, 70, 70));

            tfMaterialUsed.setEditable(false);
            tfMaterialUsed.setBackground(Color.WHITE);
            tfMaterialUsed.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(5, 10, 5, 10)
            ));

            selectMaterialsBtn.setPreferredSize(new Dimension(38, 38));
            selectMaterialsBtn.setFont(new Font("Segoe UI Symbol", Font.BOLD, 14));
            selectMaterialsBtn.setBackground(new Color(240, 240, 240));
            selectMaterialsBtn.setBorder(new LineBorder(new Color(200, 200, 200)));
            selectMaterialsBtn.setFocusPainted(false);
            selectMaterialsBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            selectMaterialsBtn.addActionListener(e -> openMaterialSelectorDialog());

            JPanel row = new JPanel(new BorderLayout(10, 0));
            row.setBackground(Color.WHITE);
            row.add(tfMaterialUsed, BorderLayout.CENTER);
            row.add(selectMaterialsBtn, BorderLayout.EAST);

            panel.add(label, BorderLayout.NORTH);
            panel.add(row, BorderLayout.CENTER);

            return panel;
        }

        private void openMaterialSelectorDialog() {
            JDialog dialog = new JDialog(this, "Select Materials", true);
            dialog.setSize(350, 400);
            dialog.setLocationRelativeTo(this);
            dialog.setLayout(new BorderLayout());

            JPanel listPanel = new JPanel();
            listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
            listPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

            List<JCheckBox> checkBoxes = new ArrayList<>();

            for (Inventory inv : inventoryList) {
                JCheckBox box = new JCheckBox(inv.getItemName());
                box.setSelected(selectedMaterials.contains(inv));
                checkBoxes.add(box);
                listPanel.add(box);
            }

            JScrollPane scroll = new JScrollPane(listPanel);
            dialog.add(scroll, BorderLayout.CENTER);

            JButton saveBtn = new JButton("Save Selection");
            saveBtn.setBackground(new Color(247, 233, 169));
            saveBtn.setBorder(new LineBorder(new Color(200, 200, 200)));
            saveBtn.setFocusPainted(false);

            saveBtn.addActionListener(e -> {
                selectedMaterials.clear();
                for (int i = 0; i < checkBoxes.size(); i++) {
                    if (checkBoxes.get(i).isSelected()) {
                        selectedMaterials.add(inventoryList.get(i));
                    }
                }

                // update text field
                tfMaterialUsed.setText(
                    selectedMaterials.stream()
                        .map(Inventory::getItemName)
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("")
                );

                dialog.dispose();
            });

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottom.add(saveBtn);
            dialog.add(bottom, BorderLayout.SOUTH);

            dialog.setVisible(true);
        }

        private JPanel createSectionHeader(String title) {
            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(SOFT_YELLOW);
            header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            header.setPreferredSize(new Dimension(0, 40));
            header.setBorder(new EmptyBorder(10, 15, 10, 15));
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

        private boolean saveProductToDatabase() {
            try {
                Product p = new Product();

                p.setProductName(tfProductName.getText().trim());
                p.setCategory((String) cbCategory.getSelectedItem());
                p.setDescription(taDescription.getText().trim());
                p.setPrice(new BigDecimal(tfPrice.getText().trim()));
                p.setUnit((String) cbUnit.getSelectedItem());
                String materialList = selectedMaterials.stream()
                    .map(Inventory::getItemName)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");

                p.setMaterialUsed(materialList);

                // quantity_used (must be INT)
                try {
                    p.setQuantityUsed(Integer.parseInt(tfQuantityUsed.getText().trim()));
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Quantity Used must be a whole number.");
                    tfQuantityUsed.requestFocus();
                    return false;
                }

                // reorder_level (your form does NOT have this — so default to 0 for now)
                p.setReorderLevel(0);

                // default status for now
                p.setStatus("Active");

                // added_by (you don't have this yet, so NULL)
                p.setAddedBy(null);

                p.setPrintTime(tfPrintTime.getText().trim());
                String selectedSize = (String) cbSize.getSelectedItem();
                p.setSize(selectedSize != null ? selectedSize : "Not Applicable");

                p.setNotes(taNotes.getText().trim());

                // inventory_id = null for now (since form has no field)
                p.setInventoryId(null);

                boolean ok = productDAO.addProduct(p);
                return ok;

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Failed to save product:\n" + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        private boolean validateFields() {
            if (tfProductName.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Product Name is required.");
                tfProductName.requestFocus();
                return false;
            }
            String category = (String) cbCategory.getSelectedItem();
            if (category == null || category.equals("Select category")) {
                JOptionPane.showMessageDialog(this, "Category is required.");
                cbCategory.requestFocus();
                return false;
            }
            if (taDescription.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Description is required.");
                taDescription.requestFocus();
                return false;
            }
            if (tfPrice.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Price is required.");
                tfPrice.requestFocus();
                return false;
            }
            try { Double.parseDouble(tfPrice.getText().trim()); }
            catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Price must be a valid number.");
                tfPrice.requestFocus();
                return false;
            }
            String unit = (String) cbUnit.getSelectedItem();
            if (unit == null || unit.equals("Select unit")) {
                JOptionPane.showMessageDialog(this, "Unit is required.");
                cbUnit.requestFocus();
                return false;
            }
            if (tfMaterialUsed.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Material Used is required.");
                tfMaterialUsed.requestFocus();
                return false;
            }
            if (tfQuantityUsed.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Quantity Used is required.");
                tfQuantityUsed.requestFocus();
                return false;
            }
            if (tfPrintTime.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Print Time is required.");
                tfPrintTime.requestFocus();
                return false;
            }
            return true;
        }

        private void clearForm() {
            tfProductName.setText("");
            cbCategory.setSelectedIndex(0);
            taDescription.setText("");
            tfPrice.setText("");
            cbUnit.setSelectedIndex(0);
            tfMaterialUsed.setText("");
            tfQuantityUsed.setText("");
            tfPrintTime.setText("");
            taNotes.setText("");
        }
    }

    // ===========================================================
    //  Product Edit Dialog (copied / adapted from CustomersPanel style)
    // ===========================================================
    private class ProductEditDialog extends JDialog {
        private Product result = null;
        private final Product existing;

        // Drag support
        private Point dragOffset;

        // Fields matching AddProductForm
        private final JTextField tfProductName = new JTextField();
        private final JComboBox<String> cbCategory = new JComboBox<>(new String[]{
            "Select category", "3D Printing", "Laser Cutting", "Printing", "Resin", "Other"
        });
        private final JTextArea taDescription = new JTextArea(4, 20);

        private final JTextField tfPrice = new JTextField();
        private final JComboBox<String> cbUnit = new JComboBox<>(new String[]{
            "Select unit", "pc", "set", "roll", "gram", "kg", "ml"
        });

        private final JTextField tfMaterialUsed = new JTextField();
        private final JTextField tfQuantityUsed = new JTextField();
        private final JTextField tfPrintTime = new JTextField();
        private final JComboBox<String> cbSize = new JComboBox<>(new String[]{
            "Select size", "XS", "S", "M", "L", "XL", "Not Applicable"
        });

        private final JTextArea taNotes = new JTextArea(3, 20);

        // For material selection
        private List<Inventory> inventoryList = new ArrayList<>();
        private List<Inventory> selectedMaterials = new ArrayList<>();
        private final JButton selectMaterialsBtn = new JButton("▼");

        ProductEditDialog(Product existing) {
            super(SwingUtilities.getWindowAncestor(ProductsPanel.this), ModalityType.APPLICATION_MODAL);
            this.existing = existing;
            setUndecorated(true);
            setPreferredSize(new Dimension(800, 700));
            initComponents();
            populateFromExisting(existing);
        }

        private void initComponents() {
            JPanel root = new JPanel(new BorderLayout());
            root.setBackground(Color.WHITE);
            root.setBorder(new LineBorder(new Color(200, 200, 200), 1));
            getContentPane().add(root);

            // Top header (draggable)
            JPanel topHeader = new JPanel(new BorderLayout());
            topHeader.setBackground(new Color(30, 41, 59));
            topHeader.setPreferredSize(new Dimension(0, 60));
            topHeader.setBorder(new EmptyBorder(10, 20, 10, 20));

            topHeader.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    dragOffset = e.getPoint();
                }
            });
            topHeader.addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseDragged(MouseEvent e) {
                    Point location = ProductEditDialog.this.getLocation();
                    location.x += e.getX() - dragOffset.x;
                    location.y += e.getY() - dragOffset.y;
                    ProductEditDialog.this.setLocation(location);
                }
            });

            // Left: logo area
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

            // Right: title + close
            JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
            rightPanel.setOpaque(false);
            JLabel titleLabel = new JLabel("Edit Product");
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
                public void mouseEntered(MouseEvent e) { closeBtn.setBackground(new Color(220, 53, 69)); }
                public void mouseExited(MouseEvent e) { closeBtn.setBackground(new Color(50, 61, 79)); }
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

            // Form content
            JPanel formContainer = new JPanel(new BorderLayout());
            formContainer.setBackground(Color.WHITE);
            formContainer.setBorder(new EmptyBorder(20, 30, 20, 30));
            JPanel formPanel = new JPanel();
            formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
            formPanel.setBackground(Color.WHITE);

            // Product Information
            formPanel.add(createSectionHeader("Product Information"));
            formPanel.add(Box.createVerticalStrut(15));
            JPanel productRow1 = new JPanel(new GridLayout(1, 2, 20, 0));
            productRow1.setBackground(Color.WHITE);
            productRow1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
            productRow1.add(createFieldPanel("Product Name *", tfProductName, "Enter product name"));
            productRow1.add(createFieldPanel("Category *", cbCategory, null));
            formPanel.add(productRow1);
            formPanel.add(Box.createVerticalStrut(15));

            // Description
            JPanel descPanel = new JPanel(new BorderLayout(0, 8));
            descPanel.setBackground(Color.WHITE);
            descPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
            JLabel descLabel = new JLabel("Description *");
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

            // Pricing & Unit
            formPanel.add(createSectionHeader("Pricing & Unit"));
            formPanel.add(Box.createVerticalStrut(15));
            JPanel pricingRow = new JPanel(new GridLayout(1, 2, 20, 0));
            pricingRow.setBackground(Color.WHITE);
            pricingRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
            pricingRow.add(createFieldPanel("Price *", tfPrice, "0.00"));
            pricingRow.add(createFieldPanel("Unit *", cbUnit, null));
            formPanel.add(pricingRow);
            formPanel.add(Box.createVerticalStrut(15));

            // Material & Production Details
            formPanel.add(createSectionHeader("Material & Production Details"));
            formPanel.add(Box.createVerticalStrut(15));
            JPanel materialRow1 = new JPanel(new GridLayout(1, 3, 15, 0));
            materialRow1.setBackground(Color.WHITE);
            materialRow1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
            materialRow1.add(createMaterialSelectorPanel());
            materialRow1.add(createFieldPanel("Quantity Used *", tfQuantityUsed, "e.g., 50g, 100ml"));
            materialRow1.add(createFieldPanel("Print Time *", tfPrintTime, "e.g., 2 hours, 45 mins"));
            formPanel.add(materialRow1);
            formPanel.add(Box.createVerticalStrut(15));

            // Size
            JPanel sizeRow = new JPanel(new BorderLayout());
            sizeRow.setBackground(Color.WHITE);
            sizeRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
            JPanel sizeField = createFieldPanel("Size *", cbSize, null);
            sizeField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
            sizeRow.add(sizeField, BorderLayout.WEST);
            formPanel.add(sizeRow);
            formPanel.add(Box.createVerticalStrut(15));

            // Additional Info
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

            JScrollPane scrollPane = new JScrollPane(formPanel);
            scrollPane.setBorder(null);
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
            formContainer.add(scrollPane, BorderLayout.CENTER);
            root.add(formContainer, BorderLayout.CENTER);

            // Bottom buttons
            JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
            buttonBar.setBackground(Color.WHITE);
            buttonBar.setBorder(new EmptyBorder(5, 20, 15, 20));
            JButton saveBtn = new JButton("💾 Save Changes");
            saveBtn.setPreferredSize(new Dimension(200, 45));
            saveBtn.setBackground(new Color(247, 233, 169));
            saveBtn.setForeground(Color.BLACK);
            saveBtn.setFont(new Font("Segoe UI Symbol", Font.BOLD, 14));
            saveBtn.setFocusPainted(false);
            saveBtn.setBorder(new LineBorder(new Color(247, 233, 169), 1));
            saveBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            JButton cancelBtn = new JButton("✕ Cancel");
            cancelBtn.setPreferredSize(new Dimension(150, 45));
            cancelBtn.setBackground(Color.WHITE);
            cancelBtn.setForeground(Color.BLACK);
            cancelBtn.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 13));
            cancelBtn.setFocusPainted(false);
            cancelBtn.setBorder(new LineBorder(new Color(200, 200, 200), 1));
            cancelBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            buttonBar.add(saveBtn);
            buttonBar.add(cancelBtn);
            root.add(buttonBar, BorderLayout.SOUTH);

            saveBtn.addActionListener(e -> {
                if (!validateAllFields()) return;

                // apply values to existing product
                try {
                    existing.setProductName(tfProductName.getText().trim());
                    existing.setCategory((String) cbCategory.getSelectedItem());
                    existing.setDescription(taDescription.getText().trim());
                    existing.setPrice(new BigDecimal(tfPrice.getText().trim()));
                    existing.setUnit((String) cbUnit.getSelectedItem());
                    existing.setMaterialUsed(tfMaterialUsed.getText().trim());

                    try {
                        existing.setQuantityUsed(Integer.parseInt(tfQuantityUsed.getText().trim()));
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Quantity Used must be a whole number.");
                        tfQuantityUsed.requestFocus();
                        return;
                    }

                    existing.setPrintTime(tfPrintTime.getText().trim());
                    String sizeSel = (String) cbSize.getSelectedItem();
                    existing.setSize(sizeSel != null ? sizeSel : existing.getSize());
                    existing.setNotes(taNotes.getText().trim());
                    // preserve other fields (status, dateAdded, etc.)
                    result = existing;
                    dispose();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage());
                }
            });

            cancelBtn.addActionListener(e -> {
                result = null;
                dispose();
            });

            // ESC to close
            getRootPane().registerKeyboardAction(e -> {
                result = null;
                dispose();
            }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

            // style fields
            styleTextField(tfProductName);
            styleTextField(tfPrice);
            styleTextField(tfMaterialUsed);
            styleTextField(tfQuantityUsed);
            styleTextField(tfPrintTime);
            styleComboBox(cbCategory);
            styleComboBox(cbUnit);
            styleComboBoxWide(cbSize, 200);

            // material selector loads inventory list
            inventoryList = new InventoryDAO().getAllInventory();
            selectMaterialsBtn.addActionListener(e -> openMaterialSelectorDialog());
        }

        private void populateFromExisting(Product p) {
            tfProductName.setText(p.getProductName());
            if (p.getCategory() != null) cbCategory.setSelectedItem(p.getCategory());
            taDescription.setText(p.getDescription());
            if (p.getPrice() != null) tfPrice.setText(p.getPrice().toPlainString());
            if (p.getUnit() != null) cbUnit.setSelectedItem(p.getUnit());
            tfMaterialUsed.setText(p.getMaterialUsed() != null ? p.getMaterialUsed() : "");
            tfQuantityUsed.setText(String.valueOf(p.getQuantityUsed()));
            tfPrintTime.setText(p.getPrintTime() != null ? p.getPrintTime() : "");
            if (p.getSize() != null) cbSize.setSelectedItem(p.getSize());
            taNotes.setText(p.getNotes() != null ? p.getNotes() : "");

            // try to pre-fill selectedMaterials if materialUsed matches existing inventory names
            selectedMaterials.clear();
            String mat = p.getMaterialUsed();
            if (mat != null && !mat.trim().isEmpty()) {
                String[] parts = mat.split("\\s*,\\s*");
                List<Inventory> allInv = new InventoryDAO().getAllInventory();
                for (String name : parts) {
                    for (Inventory inv : allInv) {
                        if (inv.getItemName().equalsIgnoreCase(name)) {
                            selectedMaterials.add(inv);
                            break;
                        }
                    }
                }
            }
        }

        private JPanel createSectionHeader(String title) {
            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(SOFT_YELLOW);
            header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            header.setPreferredSize(new Dimension(0, 40));
            header.setBorder(new EmptyBorder(10, 15, 10, 15));

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

        private JPanel createMaterialSelectorPanel() {
            JPanel panel = new JPanel(new BorderLayout(0, 8));
            panel.setBackground(Color.WHITE);

            JLabel label = new JLabel("Materials Used *");
            label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            label.setForeground(new Color(70, 70, 70));

            tfMaterialUsed.setEditable(false);
            tfMaterialUsed.setBackground(Color.WHITE);
            tfMaterialUsed.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(5, 10, 5, 10)
            ));

            selectMaterialsBtn.setPreferredSize(new Dimension(38, 38));
            selectMaterialsBtn.setFont(new Font("Segoe UI Symbol", Font.BOLD, 14));
            selectMaterialsBtn.setBackground(new Color(240, 240, 240));
            selectMaterialsBtn.setBorder(new LineBorder(new Color(200, 200, 200)));
            selectMaterialsBtn.setFocusPainted(false);
            selectMaterialsBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JPanel row = new JPanel(new BorderLayout(10, 0));
            row.setBackground(Color.WHITE);
            row.add(tfMaterialUsed, BorderLayout.CENTER);
            row.add(selectMaterialsBtn, BorderLayout.EAST);

            panel.add(label, BorderLayout.NORTH);
            panel.add(row, BorderLayout.CENTER);
            return panel;
        }

        private void openMaterialSelectorDialog() {
            JDialog dialog = new JDialog(this, "Select Materials", true);
            dialog.setSize(350, 400);
            dialog.setLocationRelativeTo(this);
            dialog.setLayout(new BorderLayout());

            JPanel listPanel = new JPanel();
            listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
            listPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

            List<JCheckBox> checkBoxes = new ArrayList<>();
            for (Inventory inv : inventoryList) {
                JCheckBox box = new JCheckBox(inv.getItemName());
                box.setSelected(selectedMaterials.contains(inv));
                checkBoxes.add(box);
                listPanel.add(box);
            }

            JScrollPane scroll = new JScrollPane(listPanel);
            dialog.add(scroll, BorderLayout.CENTER);

            JButton saveBtn = new JButton("Save Selection");
            saveBtn.setBackground(new Color(247, 233, 169));
            saveBtn.setBorder(new LineBorder(new Color(200, 200, 200)));
            saveBtn.setFocusPainted(false);

            saveBtn.addActionListener(e -> {
                selectedMaterials.clear();
                for (int i = 0; i < checkBoxes.size(); i++) {
                    if (checkBoxes.get(i).isSelected()) {
                        selectedMaterials.add(inventoryList.get(i));
                    }
                }

                // update text field
                tfMaterialUsed.setText(
                    selectedMaterials.stream()
                        .map(Inventory::getItemName)
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("")
                );

                dialog.dispose();
            });

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottom.add(saveBtn);
            dialog.add(bottom, BorderLayout.SOUTH);

            dialog.setVisible(true);
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

        private void styleComboBoxWide(JComboBox<String> combo, int width) {
            combo.setPreferredSize(new Dimension(width, 38));
            combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            combo.setBackground(Color.WHITE);
            combo.setBorder(new LineBorder(new Color(220, 220, 220), 1));
        }

        private boolean validateAllFields() {
            if (tfProductName.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Product Name is required.");
                tfProductName.requestFocus();
                return false;
            }
            String category = (String) cbCategory.getSelectedItem();
            if (category == null || category.equals("Select category")) {
                JOptionPane.showMessageDialog(this, "Category is required.");
                cbCategory.requestFocus();
                return false;
            }
            if (taDescription.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Description is required.");
                taDescription.requestFocus();
                return false;
            }
            if (tfPrice.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Price is required.");
                tfPrice.requestFocus();
                return false;
            }
            try { new BigDecimal(tfPrice.getText().trim()); }
            catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Price must be a valid number.");
                tfPrice.requestFocus();
                return false;
            }
            String unit = (String) cbUnit.getSelectedItem();
            if (unit == null || unit.equals("Select unit")) {
                JOptionPane.showMessageDialog(this, "Unit is required.");
                cbUnit.requestFocus();
                return false;
            }
            if (tfMaterialUsed.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Material Used is required.");
                tfMaterialUsed.requestFocus();
                return false;
            }
            if (tfQuantityUsed.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Quantity Used is required.");
                tfQuantityUsed.requestFocus();
                return false;
            }
            if (tfPrintTime.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Print Time is required.");
                tfPrintTime.requestFocus();
                return false;
            }
            return true;
        }

        public Product getResult() {
            return result;
        }
    }
}