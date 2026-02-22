package com.println.ui.Staff;


import com.println.dao.CustomerDAO;
import com.println.dao.InventoryDAO;
import com.println.dao.ProductDAO;
import com.println.model.Customer;
import com.println.model.Inventory;
import com.println.model.Order;
import com.println.model.OrderDetail;
import com.println.model.Product;
import com.println.service.OrderService;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.MaskFormatter;


public class OrderManagementUI extends JFrame {


    // LIGHT THEME COLORS (Palette 1)
    private static final Color NAVY = new Color(245, 245, 245);
    private static final Color YELLOW = new Color(244, 192, 78);
    private static final Color YELLOW_BORDER = new Color(224, 191, 60);
    private static final Color TEXT_DARK = new Color(26, 26, 26);
    private static final Color GREEN = new Color(120, 200, 65);


    private final CustomerDAO customerDAO = new CustomerDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private final OrderService orderService = new OrderService();


    private JComboBox<CustomerComboItem> cbCustomer;
    private JComboBox<ProductComboItem> cbProduct;
    private JTextField tfQty, tfUnitPrice, tfDiscount, tfRemarks;
    private JFormattedTextField tfTargetDate;
    private DefaultTableModel tableModel;
    private JLabel lblTotal;
    private JLabel lblDownPayment;

    private JButton btnAdd, btnRemove, btnClear, btnPlace, btnSummary;
    private JButton btnBack;

    private JComboBox<String> cbPaymentStatus;


    private final List<OrderDetail> orderDetails = new ArrayList<>();
    private final Map<Integer, Product> productMap = new HashMap<>();
    private final int DEFAULT_USER_ID = 1;

    private final JFrame owner;
    private List<Customer> allCustomerList = new ArrayList<>();

    // CLASS FIELD (accessible everywhere)
    private JComboBox<String> cbPaymentMethod;


    public OrderManagementUI(JFrame owner) {
        this.owner = owner;
        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setAlwaysOnTop(true);
        initialize();
    }


    public OrderManagementUI() {
        this(null);
    }

    private List<String> parseMaterials(String materialUsed) {
        List<String> list = new ArrayList<>();
        if (materialUsed == null || materialUsed.isBlank()) return list;

        for (String m : materialUsed.split(",")) {
            String trimmed = m.trim();
            if (!trimmed.isEmpty()) list.add(trimmed);
        }

        return list;
    }

    private String getInventoryStatusByName(String itemName) {
        List<Inventory> invList = inventoryDAO.getAllInventory();

        for (Inventory inv : invList) {
            if (inv.getItemName().equalsIgnoreCase(itemName)) {
                return inv.getStatus(); // "Available", "Low", "Out of Stock"
            }
        }

        return null; // not found
    }

    private void initialize() {
        setTitle("Order Management System - PrintLo");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1280, 820);
        setLocationRelativeTo(null);


        getContentPane().setLayout(new BorderLayout(8, 8));
        getContentPane().setBackground(NAVY);


        // ---------- Header (top yellow bar outside card) ----------
        JPanel header = new JPanel(new BorderLayout());
        header.setPreferredSize(new Dimension(0, 62));
        header.setBackground(YELLOW);


        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        leftHeader.setBackground(YELLOW);


    // Logo (try resource, else emoji)
    try {
        ImageIcon icon = new ImageIcon(getClass().getResource("/resources/images/printtobee.png"));
        Image img = icon.getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH);

        JLabel logoLabel = new JLabel(new ImageIcon(img));
        logoLabel.setBorder(new EmptyBorder(0, 0, 0, 6));
        leftHeader.add(logoLabel);

    } catch (Exception ex) {

        JLabel placeholder = new JLabel("🐝");
        placeholder.setFont(new Font("Segoe UI", Font.BOLD, 24));
        placeholder.setForeground(TEXT_DARK);
        placeholder.setBorder(new EmptyBorder(0, 0, 0, 6));
        leftHeader.add(placeholder);
    }

        // Title box inside the yellow header (left side)
        JPanel titleBox = new JPanel();
        titleBox.setBackground(YELLOW);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Print to Bee");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(TEXT_DARK);
        titleBox.add(title);
        leftHeader.add(titleBox);


        header.add(leftHeader, BorderLayout.WEST);


        // Top-right back button
        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        headerRight.setBackground(YELLOW);
        btnBack = new JButton("← Back");
        btnBack.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnBack.setBackground(YELLOW);
        btnBack.setForeground(TEXT_DARK);
        btnBack.setBorder(new CompoundBorder(new LineBorder(YELLOW_BORDER, 2, true), new EmptyBorder(6,10,6,10)));
        btnBack.setFocusPainted(false);
        btnBack.addActionListener(e -> {
            dispose();
            if (owner != null) {
                owner.toFront();
                owner.requestFocus();
            }
        });
        headerRight.add(btnBack);
        header.add(headerRight, BorderLayout.EAST);


        // ---------- Top card (rounded) ----------
        int cardRadius = 12;
        RoundedPanel card = new RoundedPanel(cardRadius);
        card.setLayout(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setOpaque(false);


        JPanel cardOuter = new JPanel(new BorderLayout());
        cardOuter.setBackground(NAVY);
        cardOuter.setBorder(new EmptyBorder(6, 12, 6, 12));
        cardOuter.add(card, BorderLayout.CENTER);


        int headerHeight = 46;
        TopRoundedPanel cardHeader = new TopRoundedPanel(YELLOW, cardRadius, headerHeight);
        cardHeader.setLayout(new BorderLayout());
        cardHeader.setPreferredSize(new Dimension(0, headerHeight));
        cardHeader.setBorder(new EmptyBorder(8, 14, 8, 14));
        JLabel cardHeaderTitle = new JLabel("Create New Order");
        cardHeaderTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        cardHeaderTitle.setForeground(TEXT_DARK);
        cardHeader.add(cardHeaderTitle, BorderLayout.WEST);
        card.add(cardHeader, BorderLayout.NORTH);


        // Card content area (form)
        JPanel cardContent = new JPanel(new GridBagLayout());
        cardContent.setOpaque(false);
        cardContent.setBorder(new EmptyBorder(12, 12, 12, 12));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 8, 6, 8);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;


        // Row 1 labels: Customer and Product
        JLabel lblCustomer = smallLabel("Customer");
        c.gridx = 0; c.gridy = 0; c.gridwidth = 1; c.weightx = 0.5;
        cardContent.add(lblCustomer, c);


        JLabel lblProduct = smallLabel("Product");
        c.gridx = 2; c.gridy = 0; c.gridwidth = 1; c.weightx = 0.5;
        cardContent.add(lblProduct, c);


        // Row 2 inputs: Customer and Product comboboxes
        cbCustomer = new JComboBox<>();
        cbCustomer.setEditable(true);          // allow typing
        setupAutoComplete(cbCustomer);         // enable search


        styleControl(cbCustomer);
        cbCustomer.setBackground(Color.WHITE);
        cbCustomer.setForeground(TEXT_DARK);
        cbCustomer.setPreferredSize(new Dimension(0, 30));
        c.gridx = 0; c.gridy = 1; c.gridwidth = 2; c.weightx = 0.5;
        cardContent.add(cbCustomer, c);


        JTextField customerEditor = (JTextField) cbCustomer.getEditor().getEditorComponent();

// Make placeholder disappear when typing or focusing 
customerEditor.addFocusListener(new FocusAdapter() {
    @Override
    public void focusGained(FocusEvent e) {
        if (customerEditor.getText().equals("-- Select Customer --")) {
            customerEditor.setText("");
        }
    }

    @Override
    public void focusLost(FocusEvent e) {
        if (customerEditor.getText().trim().isEmpty()) {
            customerEditor.setText("-- Select Customer --");
        }
    }
});

// Also clear placeholder on key press
customerEditor.addKeyListener(new KeyAdapter() {
    @Override
    public void keyPressed(KeyEvent e) {
        if (customerEditor.getText().equals("-- Select Customer --")) {
            customerEditor.setText("");
        }
    }
});




final boolean[] firstClick = { true };


customerEditor.addFocusListener(new FocusAdapter() {
    @Override
    public void focusGained(FocusEvent e) {


        CustomerComboItem sel = (CustomerComboItem) cbCustomer.getSelectedItem();


        // Only clear WHEN:
        //   - It is the first time focusing
        //   - The placeholder is selected
        if (firstClick[0] && sel != null && sel.customerId == null) {
            customerEditor.setText("");   // clear placeholder
            firstClick[0] = false;
        }


        customerEditor.selectAll();  // highlight selected text (normal behavior)
    }
});




        cbProduct = new JComboBox<>();
        styleControl(cbProduct);
        cbProduct.setBackground(Color.WHITE);
        cbProduct.setForeground(TEXT_DARK);
        cbProduct.setPreferredSize(new Dimension(0, 30));
        c.gridx = 2; c.gridy = 1; c.gridwidth = 2; c.weightx = 0.5;
        cardContent.add(cbProduct, c);


        // Horizontal row: Quantity | Unit Price | Discount % | Remarks
        JPanel rowInputs = new JPanel(new GridBagLayout());
        rowInputs.setOpaque(false);
        GridBagConstraints r = new GridBagConstraints();
        r.insets = new Insets(6, 6, 6, 6);
        r.anchor = GridBagConstraints.WEST;
        r.fill = GridBagConstraints.HORIZONTAL;


        // Quantity
        r.gridx = 0; r.gridy = 0; r.weightx = 0;
        rowInputs.add(smallLabel("Quantity"), r);
        tfQty = new JTextField();
        tfQty.setPreferredSize(new Dimension(80, 28));
        r.gridx = 1; r.gridy = 0;
        rowInputs.add(tfQty, r);


        // spacer
        r.gridx = 2; r.gridy = 0;
        rowInputs.add(Box.createHorizontalStrut(8), r);


        // Unit Price
        r.gridx = 3; r.gridy = 0;
        rowInputs.add(smallLabel("Unit Price (₱)"), r);
        tfUnitPrice = new JTextField("0");
        tfUnitPrice.setPreferredSize(new Dimension(100, 28));
        r.gridx = 4; r.gridy = 0;
        rowInputs.add(tfUnitPrice, r);


        // spacer
        r.gridx = 5; r.gridy = 0;
        rowInputs.add(Box.createHorizontalStrut(12), r);


        // Discount %
        r.gridx = 6; r.gridy = 0;
        rowInputs.add(smallLabel("Applied Discount %"), r);
        tfDiscount = new JTextField("0");
        tfDiscount.setPreferredSize(new Dimension(90, 28));
        r.gridx = 7; r.gridy = 0;
        rowInputs.add(tfDiscount, r);


        // spacer
        r.gridx = 8; r.gridy = 0;
        rowInputs.add(Box.createHorizontalStrut(12), r);


        // Remarks
        r.gridx = 9; r.gridy = 0;
        rowInputs.add(smallLabel("Order Remarks"), r);
        tfRemarks = new JTextField();
        tfRemarks.setPreferredSize(new Dimension(400, 28));
        r.gridx = 10; r.gridy = 0; r.weightx = 1.0;
        r.fill = GridBagConstraints.HORIZONTAL;
        rowInputs.add(tfRemarks, r);


        c.gridx = 0; c.gridy = 2; c.gridwidth = 4; c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        cardContent.add(rowInputs, c);


        card.add(cardContent, BorderLayout.CENTER);


        // Card footer with + Add Item button
        JPanel cardFooter = new JPanel(new BorderLayout());
        cardFooter.setOpaque(false);
        cardFooter.setBorder(new EmptyBorder(6, 12, 10, 12));
        btnAdd = new JButton("+ Add Item");
        btnAdd.setBackground(YELLOW);
        btnAdd.setForeground(TEXT_DARK);
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAdd.setFocusPainted(false);
        btnFooterRightWrap(cardFooter).add(btnAdd);
        card.add(cardFooter, BorderLayout.SOUTH);


        // Stack header + cardOuter vertically
        JPanel northContainer = new JPanel();
        northContainer.setLayout(new BoxLayout(northContainer, BoxLayout.Y_AXIS));
        northContainer.setBackground(NAVY);
        northContainer.add(header);
        northContainer.add(cardOuter);
        getContentPane().add(northContainer, BorderLayout.NORTH);


        // ---------- Center: Order Items table ----------
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(NAVY);
        centerPanel.setBorder(new EmptyBorder(8, 8, 8, 8));


        JPanel itemsCard = new JPanel(new BorderLayout());
        itemsCard.setBackground(NAVY);
        itemsCard.setBorder(new CompoundBorder(
                new LineBorder(YELLOW_BORDER, 3, true),
                new EmptyBorder(8,8,8,8)
        ));


        JLabel lblOrderItems = new JLabel("  Order Items");
        lblOrderItems.setOpaque(true);
        lblOrderItems.setBackground(YELLOW);
        lblOrderItems.setForeground(TEXT_DARK);
        lblOrderItems.setFont(new Font("Segoe UI", Font.BOLD, 18));
        itemsCard.add(lblOrderItems, BorderLayout.NORTH);


        String[] cols = {"#", "Product", "Qty", "Unit Price", "Subtotal"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };


        JTable table = new JTable(tableModel);


        table.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        table.setRowHeight(34);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(230, 230, 230));
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setBackground(Color.WHITE);
        table.setForeground(TEXT_DARK);


        JTableHeader headerTbl = table.getTableHeader();
        headerTbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        headerTbl.setOpaque(true);
        headerTbl.setBackground(new Color(255, 242, 204));
        headerTbl.setForeground(TEXT_DARK);
        headerTbl.setPreferredSize(new Dimension(headerTbl.getWidth(), 38));
        headerTbl.setBorder(BorderFactory.createEmptyBorder());


        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {


                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);


                if (isSelected) {
                    c.setBackground(new Color(255, 230, 150));
                    c.setForeground(Color.BLACK);
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 248, 248));
                    c.setForeground(TEXT_DARK);
                }


                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                return c;
            }
        });


        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(Color.WHITE);
        itemsCard.add(sp, BorderLayout.CENTER);


        // ---------- Footer inside itemsCard with Remove/Clear and Target Date (unchanged) ----------
        JPanel footerRowPanel = new JPanel(new BorderLayout());
        footerRowPanel.setBackground(NAVY);


        JPanel leftBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        leftBtnPanel.setOpaque(false);


        btnRemove = new JButton("Remove Selected");
        btnRemove.setBackground(new Color(200,50,60));
        btnRemove.setForeground(Color.BLACK);
        btnRemove.setFont(new Font("Segoe UI", Font.BOLD, 14));


        btnClear = new JButton("🗑 Clear All Items");
        btnClear.setBackground(new Color(224, 58, 62));
        btnClear.setForeground(Color.BLACK);
        btnClear.setFont(new Font("Segoe UI", Font.BOLD, 14));


        leftBtnPanel.add(btnRemove);
        leftBtnPanel.add(btnClear);


        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        rightPanel.setOpaque(false);


        JLabel lblTargetDate = new JLabel("Target Date:");
        lblTargetDate.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTargetDate.setForeground(TEXT_DARK);


        MaskFormatter mf = null;
        try {
            mf = new MaskFormatter("##/##/####");
            mf.setPlaceholder("  /  /    ");
            mf.setValidCharacters("0123456789");
        } catch (ParseException ex) {
            ex.printStackTrace();
        }


        tfTargetDate = new PlaceholderFormattedField(mf, "MM/DD/YYYY");
        tfTargetDate.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
        tfTargetDate.setColumns(10);
        tfTargetDate.setToolTipText("Format: MM/DD/YYYY");
        tfTargetDate.setBorder(BorderFactory.createLineBorder(Color.RED, 2));


        // validation
        tfTargetDate.getDocument().addDocumentListener(new DocumentListener() {
            private void validateField() {
                String input = tfTargetDate.getText().trim();
                boolean valid = input.matches("^(0[1-9]|1[0-2])/(0[1-9]|[12][0-9]|3[01])/([0-9]{4})$");
                tfTargetDate.setBorder(BorderFactory.createLineBorder(valid ? Color.GREEN : Color.RED, 2));
            }
            @Override public void insertUpdate(DocumentEvent e) { validateField(); }
            @Override public void removeUpdate(DocumentEvent e) { validateField(); }
            @Override public void changedUpdate(DocumentEvent e) { validateField(); }
        });


        rightPanel.add(lblTargetDate);
        rightPanel.add(tfTargetDate);


        footerRowPanel.add(leftBtnPanel, BorderLayout.WEST);
        footerRowPanel.add(rightPanel, BorderLayout.EAST);


        itemsCard.add(footerRowPanel, BorderLayout.SOUTH);


        centerPanel.add(itemsCard, BorderLayout.CENTER);
        getContentPane().add(centerPanel, BorderLayout.CENTER);


        // ---------- Bottom: Total + Down Payment + Order Summary + Place Order ----------
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(NAVY);
        bottom.setPreferredSize(new Dimension(0, 72));
        bottom.setBorder(new CompoundBorder(
                new MatteBorder(3,3,3,3, YELLOW_BORDER),
                new EmptyBorder(8,12,8,12)
        ));


        // TOTAL (bold)
        lblTotal = new JLabel("Total Amount: ₱0.00");
        lblTotal.setForeground(TEXT_DARK);
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 20));


        // DOWN PAYMENT (auto 50%)
        lblDownPayment = new JLabel("Down Payment: ₱0.00");
        lblDownPayment.setForeground(TEXT_DARK);
        lblDownPayment.setFont(new Font("Segoe UI", Font.PLAIN, 18));


        // Left panel containing labels and payment status
        JPanel leftBottomPanel = new JPanel();
        leftBottomPanel.setLayout(new BoxLayout(leftBottomPanel, BoxLayout.Y_AXIS));
        leftBottomPanel.setOpaque(false);


        // Total Amount
        lblTotal.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftBottomPanel.add(lblTotal);


        // Down Payment
        lblDownPayment.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftBottomPanel.add(lblDownPayment);


        // Small vertical space
        leftBottomPanel.add(Box.createVerticalStrut(6));


        bottom.add(leftBottomPanel, BorderLayout.WEST);


        // Right side: Payment Status + buttons
        JPanel rightBottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        rightBottomPanel.setOpaque(false);


        // Payment Status label
        JLabel lblPaymentStatus = new JLabel("Payment Status:");
        lblPaymentStatus.setForeground(TEXT_DARK);
        lblPaymentStatus.setFont(new Font("Segoe UI", Font.PLAIN, 16));


        // Modern Payment Status dropdown
cbPaymentStatus = new JComboBox<>(new String[]{
        "Down Payment Paid",
        "Fully Paid"
});
cbPaymentStatus.setFont(new Font("Segoe UI", Font.PLAIN, 14));
cbPaymentStatus.setBackground(new Color(250, 250, 250));
cbPaymentStatus.setForeground(TEXT_DARK);
cbPaymentStatus.setBorder(BorderFactory.createLineBorder(YELLOW_BORDER, 1));
cbPaymentStatus.setFocusable(false);

// ------------------ Payment Method (Same Design as Payment Status) ------------------
JLabel lblPaymentMethod = new JLabel("Payment Method:");
lblPaymentMethod.setForeground(TEXT_DARK);
lblPaymentMethod.setFont(new Font("Segoe UI", Font.PLAIN, 16));

// Payment Method ComboBox
cbPaymentMethod = new JComboBox<>(new String[]{"Gcash", "Cash"});

cbPaymentMethod.setFont(new Font("Segoe UI", Font.PLAIN, 14));
cbPaymentMethod.setBackground(new Color(250, 250, 250));
cbPaymentMethod.setForeground(TEXT_DARK);
cbPaymentMethod.setBorder(BorderFactory.createLineBorder(YELLOW_BORDER, 1));
cbPaymentMethod.setFocusable(false);

// Same custom arrow
cbPaymentMethod.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
    @Override
    protected JButton createArrowButton() {
        JButton arrow = new JButton("▼");
        arrow.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 12));
        arrow.setBorder(BorderFactory.createEmptyBorder());
        arrow.setBackground(new Color(250, 250, 250));
        arrow.setForeground(TEXT_DARK);
        arrow.setFocusPainted(false);
        return arrow;
    }
});

    // Same renderer design
    cbPaymentMethod.setRenderer(new DefaultListCellRenderer() {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {

            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (isSelected) {
                c.setBackground(new Color(255, 237, 180));
                c.setForeground(Color.BLACK);
            } else {
                c.setBackground(Color.WHITE);
                c.setForeground(Color.BLACK);
            }

            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            return c;
        }
    });

    cbPaymentMethod.setPreferredSize(new Dimension(190, 34));



// Modern arrow
cbPaymentStatus.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
    @Override
    protected JButton createArrowButton() {
        JButton arrow = new JButton("▼");
        arrow.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        arrow.setBorder(BorderFactory.createEmptyBorder());
        arrow.setBackground(new Color(250, 250, 250));
        arrow.setForeground(TEXT_DARK);
        arrow.setFocusPainted(false);
        return arrow;
    }
});


// Modern popup menu
cbPaymentStatus.setRenderer(new DefaultListCellRenderer() {
    @Override
    public Component getListCellRendererComponent(
            JList<?> list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {


        Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);


        if (isSelected) {
            c.setBackground(new Color(255, 237, 180));
            c.setForeground(Color.BLACK);
        } else {
            c.setBackground(Color.WHITE);
            c.setForeground(Color.BLACK);
        }


        setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        return c;
    }
});


cbPaymentStatus.setPreferredSize(new Dimension(190, 34));


        // Buttons
        btnSummary = new JButton("Order Summary");
        btnSummary.setBackground(new Color(240, 189, 91));
        btnSummary.setForeground(TEXT_DARK);
        btnSummary.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSummary.setFocusPainted(false);


        btnPlace = new JButton("Place Order");
        btnPlace.setBackground(GREEN);
        btnPlace.setForeground(TEXT_DARK);
        btnPlace.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnPlace.setPreferredSize(new Dimension(160, 40));


        rightBottomPanel.add(lblPaymentStatus);
        rightBottomPanel.add(cbPaymentStatus);

        rightBottomPanel.add(lblPaymentMethod);   // NEW
        rightBottomPanel.add(cbPaymentMethod);    // NEW

        rightBottomPanel.add(btnSummary);
        rightBottomPanel.add(btnPlace);


        // Add to bottom panel
        bottom.add(rightBottomPanel, BorderLayout.EAST);
        getContentPane().add(bottom, BorderLayout.SOUTH);


        // ---------- Load data and wire events ----------
        loadCustomers();
        loadProducts();


        // product selection -> autofill price
        cbProduct.addActionListener(e -> {
            ProductComboItem item = (ProductComboItem) cbProduct.getSelectedItem();

            if (item == null || item.productId == null) {
                tfUnitPrice.setText("0");
                return;
            }

            // Load product info
            Product p = productDAO.getProductById(item.productId);
            if (p == null) return;

            // Parse materials
            List<String> materials = parseMaterials(p.getMaterialUsed());

            List<String> lowStock = new ArrayList<>();
            List<String> outOfStock = new ArrayList<>();

            // Check each inventory item
            for (String mat : materials) {
                String status = getInventoryStatusByName(mat);

                if (status == null) continue;

                switch (status) {
                    case "Low":
                        lowStock.add(mat);
                        break;
                    case "Out of Stock":
                        outOfStock.add(mat);
                        break;
                }
            }

            // If ANY are Out of Stock → block the selection
            if (!outOfStock.isEmpty()) {
                JOptionPane.showMessageDialog(
                    this,
                    "Cannot select this product.\nOut of stock materials:\n" +
                    String.join("\n• ", outOfStock),
                    "Out of Stock Materials",
                    JOptionPane.ERROR_MESSAGE
                );

                // Undo selection
                cbProduct.setSelectedIndex(-1);
                tfUnitPrice.setText("0");
                return;
            }

            // If some are low stock → warn but allow
            if (!lowStock.isEmpty()) {
                JOptionPane.showMessageDialog(
                    this,
                    "⚠️ Warning: Some materials are LOW in stock:\n" +
                    String.join("\n• ", lowStock),
                    "Low Stock Warning",
                    JOptionPane.WARNING_MESSAGE
                );
            }

            // Normal behavior: update price
            tfUnitPrice.setText(item.price.toPlainString());
        });



        // Add item
        btnAdd.addActionListener(e -> onAddItem());


        // Remove selected item
        btnRemove.addActionListener(e -> {
            int sel = table.getSelectedRow();
            if (sel >= 0) {
                orderDetails.remove(sel);
                tableModel.removeRow(sel);
                // reindex #
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    tableModel.setValueAt(i+1, i, 0);
                }
                recalcTotal();
            } else {
                JOptionPane.showMessageDialog(this, "Please select an item to remove.", "No selection", JOptionPane.INFORMATION_MESSAGE);
            }
        });


        // Clear all items
        btnClear.addActionListener(e -> {
            if (tableModel.getRowCount() == 0) return;
            int res = JOptionPane.showConfirmDialog(this, "Clear all items?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (res == JOptionPane.YES_OPTION) {
                tableModel.setRowCount(0);
                orderDetails.clear();
                recalcTotal();
            }
        });


        // Place order using OrderService
        btnPlace.addActionListener(e -> onPlaceOrder());


        // Order Summary button -> open modal
        btnSummary.addActionListener(e -> showOrderSummaryDialog());


        // convenience enter handling
        tfQty.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) btnAdd.doClick();
            }
        });
        tfDiscount.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) btnAdd.doClick();
            }
        });
        tfUnitPrice.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) btnAdd.doClick();
            }
        });


        // When this window closes, ensure owner is fronted
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (owner != null) {
                    if (owner instanceof com.println.ui.Staff.HomeScreen) {
                        SwingUtilities.invokeLater(() -> ((com.println.ui.Staff.HomeScreen) owner).refreshOrders());
                    }
                    owner.toFront();
                    owner.requestFocus();
                }
            }
        });


        // Initial recalc (in case)
        recalcTotal();
    }


    /**
     * Helper: wrap cardFooter right area
     */
    private JPanel btnFooterRightWrap(JPanel parentFooter) {
        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        wrap.setOpaque(false);
        parentFooter.add(wrap, BorderLayout.EAST);
        return wrap;
    }


    /**
     * Show the floating Payment Summary dialog.
     * - Theme matches existing yellow/light theme.
     * - Down Payment auto-initializes to 50% of current total, but is editable.
     * - Balance updates as user types.
     * - Uses totalFinal (final) for listener scope safety.
     * - Uses GridBag layout to keep labels aligned left and values right.
     */
    private void showOrderSummaryDialog() {


    BigDecimal subtotal = BigDecimal.ZERO;
    BigDecimal total = BigDecimal.ZERO;


    for (OrderDetail d : orderDetails) {
        BigDecimal unit = d.getUnitPrice() != null ? d.getUnitPrice() : BigDecimal.ZERO;
        BigDecimal qtyBD = BigDecimal.valueOf(d.getQuantity());
        BigDecimal line = unit.multiply(qtyBD).setScale(2, RoundingMode.HALF_UP);
        subtotal = subtotal.add(line);


        BigDecimal discount = d.getDiscount() != null ? d.getDiscount() : BigDecimal.ZERO;
        BigDecimal factor = BigDecimal.ONE.subtract(discount.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP));
        BigDecimal lineAfter = unit.multiply(qtyBD).multiply(factor).setScale(2, RoundingMode.HALF_UP);
        total = total.add(lineAfter);
    }


    BigDecimal totalDiscount = subtotal.subtract(total).setScale(2, RoundingMode.HALF_UP);
    final BigDecimal totalFinal = total.setScale(2, RoundingMode.HALF_UP);


    BigDecimal dpFixed = totalFinal.multiply(new BigDecimal("0.50")).setScale(2, RoundingMode.HALF_UP);
    BigDecimal balFixed = totalFinal.subtract(dpFixed).setScale(2, RoundingMode.HALF_UP);


    JDialog dlg = new JDialog(this, "Payment Summary", Dialog.ModalityType.APPLICATION_MODAL);
    dlg.setSize(420, 380);              // tighter height
    dlg.setResizable(false);
    dlg.setLocationRelativeTo(this);


    JPanel root = new JPanel(new GridBagLayout());
    root.setBackground(Color.WHITE);
    root.setBorder(new CompoundBorder(
            new LineBorder(YELLOW_BORDER, 3, false),
            new EmptyBorder(10, 14, 14, 14)   // reduced top padding from 14 -> 10
    ));


    GridBagConstraints g = new GridBagConstraints();
    g.insets = new Insets(6, 0, 6, 0);
    g.weightx = 1;
    g.fill = GridBagConstraints.HORIZONTAL;


    // Title - centered
    JLabel title = new JLabel("Payment Summary", SwingConstants.CENTER);
    title.setFont(new Font("Segoe UI", Font.BOLD, 20));
    title.setForeground(TEXT_DARK);
    GridBagConstraints tg = (GridBagConstraints) g.clone();
    tg.gridx = 0; tg.gridy = 0; tg.anchor = GridBagConstraints.CENTER;
    root.add(title, tg);


    // Content grid: 2 columns (label left, value right)
    JPanel grid = new JPanel(new GridBagLayout());
    grid.setOpaque(false);
    GridBagConstraints sg = new GridBagConstraints();
    sg.insets = new Insets(6, 6, 6, 6);
    sg.fill = GridBagConstraints.HORIZONTAL;


    // left column (labels)
    sg.gridx = 0;
    sg.gridy = 0;
    sg.weightx = 1.0;
    sg.anchor = GridBagConstraints.WEST;
    JLabel lSub = new JLabel("Subtotal:");
    lSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    grid.add(lSub, sg);


    // right column (values) - right aligned
    sg.gridx = 1;
    sg.anchor = GridBagConstraints.EAST;
    JLabel vSub = new JLabel(String.format("₱%.2f", subtotal.doubleValue()));
    vSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    grid.add(vSub, sg);


    // Discount row
    sg.gridy++;
    sg.gridx = 0;
    sg.anchor = GridBagConstraints.WEST;
    JLabel lDisc = new JLabel("Discount:");
    lDisc.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    grid.add(lDisc, sg);


    sg.gridx = 1;
    sg.anchor = GridBagConstraints.EAST;
    JLabel vDisc = new JLabel(String.format("- ₱%.2f", totalDiscount.doubleValue()));
    vDisc.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    vDisc.setForeground(new Color(200,30,30));
    grid.add(vDisc, sg);


    // Total Amount (bold)
    sg.gridy++;
    sg.gridx = 0;
    sg.anchor = GridBagConstraints.WEST;
    JLabel lTotal = new JLabel("Total Amount:");
    lTotal.setFont(new Font("Segoe UI", Font.BOLD, 15));
    grid.add(lTotal, sg);


    sg.gridx = 1;
    sg.anchor = GridBagConstraints.EAST;
    JLabel vTotal = new JLabel(String.format("₱%.2f", totalFinal.doubleValue()));
    vTotal.setFont(new Font("Segoe UI", Font.BOLD, 15));
    grid.add(vTotal, sg);


    // Down Payment (label-only, 50%)
    sg.gridy++;
    sg.gridx = 0;
    sg.anchor = GridBagConstraints.WEST;
    JLabel lDp = new JLabel("Down Payment (50%):");
    lDp.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    grid.add(lDp, sg);


    sg.gridx = 1;
    sg.anchor = GridBagConstraints.EAST;
    JLabel vDp = new JLabel(String.format("₱%.2f", dpFixed.doubleValue()));
    vDp.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    vDp.setForeground(new Color(0,120,60));
    grid.add(vDp, sg);


    // Balance
    sg.gridy++;
    sg.gridx = 0;
    sg.anchor = GridBagConstraints.WEST;
    JLabel lBal = new JLabel("Balance:");
    lBal.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    grid.add(lBal, sg);


    sg.gridx = 1;
    sg.anchor = GridBagConstraints.EAST;
    JLabel vBal = new JLabel(String.format("₱%.2f", balFixed.doubleValue()));
    vBal.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    vBal.setForeground(new Color(150,80,0));
    grid.add(vBal, sg);


    // Payment Status (LAST LINE)
    sg.gridy++;
    sg.gridx = 0;
    sg.anchor = GridBagConstraints.WEST;
    JLabel lPayStatus = new JLabel("Payment Status:");
    lPayStatus.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    grid.add(lPayStatus, sg);


    sg.gridx = 1;
    sg.anchor = GridBagConstraints.EAST;
    JLabel vPayStatus = new JLabel(cbPaymentStatus.getSelectedItem().toString());
    vPayStatus.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    vPayStatus.setForeground(new Color(0, 90, 160));
    grid.add(vPayStatus, sg);




    // Add the grid below the title
    GridBagConstraints cg = (GridBagConstraints) g.clone();
    cg.gridx = 0; cg.gridy = 1; cg.anchor = GridBagConstraints.CENTER;
    root.add(grid, cg);


    // separator
    JSeparator sep = new JSeparator();
    GridBagConstraints sgc = (GridBagConstraints) g.clone();
    sgc.gridx = 0; sgc.gridy = 2; sgc.insets = new Insets(8, 0, 8, 0);
    root.add(sep, sgc);


    // Buttons (two equal buttons, horizontally)
    JPanel btnRow = new JPanel(new GridLayout(1, 2, 12, 0));
    btnRow.setOpaque(false);
    JButton btn1 = new JButton("Print Down Payment Receipt");
    styleReceiptBtn(btn1);
    JButton btn2 = new JButton("Print Full Payment Receipt");
    styleReceiptBtn(btn2);
    btnRow.add(btn1);
    btnRow.add(btn2);


    GridBagConstraints bgc = (GridBagConstraints) g.clone();
    bgc.gridx = 0; bgc.gridy = 3; bgc.insets = new Insets(6, 0, 0, 0);
    root.add(btnRow, bgc);


    dlg.setContentPane(root);
    dlg.setVisible(true);
}




    private void loadCustomers() {
        try {
            allCustomerList = customerDAO.getAllCustomers();
            List<Customer> customers = allCustomerList;


            cbCustomer.removeAllItems();
            cbCustomer.addItem(new CustomerComboItem(null, "-- Select Customer --"));
            for (Customer c : customers) {
                cbCustomer.addItem(new CustomerComboItem(c.getCustomerId(), c.getName()));
            }
        } catch (Exception e) {
            showError("Failed to load customers: " + e.getMessage());
        }
    }


    private void loadProducts() {
        try {
            List<Product> products = productDAO.getAllProducts();
            cbProduct.removeAllItems();
            cbProduct.addItem(new ProductComboItem(null, "-- Select Product --", BigDecimal.ZERO));
            productMap.clear();
            for (Product p : products) {
                BigDecimal price = p.getPrice() != null ? p.getPrice() : BigDecimal.ZERO;
                cbProduct.addItem(new ProductComboItem(p.getProductId(), p.getProductName(), price));
                if (p.getProductId() != 0) productMap.put(p.getProductId(), p);
            }
        } catch (Exception e) {
            showError("Failed to load products: " + e.getMessage());
        }
    }


    private void onAddItem() {
        ProductComboItem pItem = (ProductComboItem) cbProduct.getSelectedItem();
        if (pItem == null || pItem.productId == null) {
            JOptionPane.showMessageDialog(this, "Please select a product.", "Missing Product", JOptionPane.WARNING_MESSAGE);
            return;
        }


        String qtyText = tfQty.getText().trim();
        if (qtyText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter quantity.", "Missing Quantity", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int qty;
        try {
            qty = Integer.parseInt(qtyText);
            if (qty <= 0) { JOptionPane.showMessageDialog(this, "Quantity must be > 0.", "Invalid Quantity", JOptionPane.WARNING_MESSAGE); return; }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid quantity.", "Invalid Quantity", JOptionPane.WARNING_MESSAGE);
            return;
        }


        String priceText = tfUnitPrice.getText().trim();
        BigDecimal price;
        try {
            priceText = priceText.replace("₱", "").replace(",", "").trim();
            price = new BigDecimal(priceText);
            if (price.compareTo(BigDecimal.ZERO) < 0) { JOptionPane.showMessageDialog(this, "Price must be >= 0", "Invalid Price", JOptionPane.WARNING_MESSAGE); return; }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid unit price.", "Invalid Price", JOptionPane.WARNING_MESSAGE);
            return;
        }


        // parse discount
        String discText = tfDiscount.getText().trim();
        BigDecimal discount = BigDecimal.ZERO;
        try {
            if (!discText.isEmpty()) {
                discText = discText.replace("%", "").replace(",", "").trim();
                double dVal = Double.parseDouble(discText);
                if (dVal < 0 || dVal > 100) {
                    JOptionPane.showMessageDialog(this, "Discount must be between 0 and 100.", "Invalid Discount", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                discount = BigDecimal.valueOf(dVal);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid discount percentage.", "Invalid Discount", JOptionPane.WARNING_MESSAGE);
            return;
        }


        OrderDetail d = new OrderDetail();
        d.setProductId(pItem.productId);
        d.setQuantity(qty);
        d.setUnitPrice(price);
        d.setDiscount(discount);
        d.setCreatedBy(DEFAULT_USER_ID);


        orderDetails.add(d);


        int rowNo = tableModel.getRowCount() + 1;
        String prodName = pItem.name;
        BigDecimal qtyBD = BigDecimal.valueOf(qty);
        BigDecimal factor = BigDecimal.ONE.subtract(discount.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP));
        BigDecimal subtotal = price.multiply(qtyBD).multiply(factor).setScale(2, RoundingMode.HALF_UP);


        tableModel.addRow(new Object[]{rowNo, prodName, qty, String.format("₱%.2f", price.doubleValue()), String.format("₱%.2f", subtotal.doubleValue())});


        tfQty.setText("");
        tfDiscount.setText("0");
        tfQty.requestFocusInWindow();


        recalcTotal();
    }


    private void recalcTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderDetail d : orderDetails) {
            BigDecimal unit = d.getUnitPrice() != null ? d.getUnitPrice() : BigDecimal.ZERO;
            BigDecimal qtyBD = BigDecimal.valueOf(d.getQuantity());
            BigDecimal discount = d.getDiscount() != null ? d.getDiscount() : BigDecimal.ZERO;
            BigDecimal factor = BigDecimal.ONE.subtract(discount.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP));
            BigDecimal sub = unit.multiply(qtyBD).multiply(factor).setScale(2, RoundingMode.HALF_UP);
            total = total.add(sub);
        }


        // Update TOTAL
        lblTotal.setText("Total Amount: ₱" + String.format("%.2f", total.doubleValue()));


        // update DOWN PAYMENT (auto 50%)
        BigDecimal dp = total.multiply(new BigDecimal("0.50")).setScale(2, RoundingMode.HALF_UP);
        lblDownPayment.setText("Down Payment: ₱" + String.format("%.2f", dp.doubleValue()));
    }


    private void onPlaceOrder() {
        if (orderDetails.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No items to place. Add items first.", "Empty Order", JOptionPane.WARNING_MESSAGE);
            return;
        }


        Object sel = cbCustomer.getSelectedItem();
        Integer customerId = null;
        String customerName = null;


        if (sel instanceof CustomerComboItem) {
            // User selected from dropdown
            CustomerComboItem item = (CustomerComboItem) sel;
            customerId = item.customerId;
            customerName = item.name;
        } else if (sel instanceof String) {
            // User typed a custom name (new customer)
            customerName = ((String) sel).trim();
        }


    // If user typed a new name, ensure it's not empty
    if (customerName == null || customerName.isEmpty() || customerName.equals("-- Select Customer --")) {
        JOptionPane.showMessageDialog(this, "Please enter a customer name.", "Missing Customer", JOptionPane.WARNING_MESSAGE);
        return;
    }




        Order order = new Order();
        order.setCustomerId(customerId);
        order.setUserId(DEFAULT_USER_ID);
        order.setRemarks(tfRemarks.getText().trim());


        // Parse and set target date (MM/DD/YYYY) if provided -> saved to dueDate
        String rawDate = tfTargetDate.getText().trim();
        if (!rawDate.isEmpty()) {
            if (!rawDate.matches("^(0[1-9]|1[0-2])/(0[1-9]|[12][0-9]|3[01])/([0-9]{4})$")) {
                JOptionPane.showMessageDialog(this,
                        "Target Date must be in MM/DD/YYYY format.",
                        "Invalid Date", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String[] parts = rawDate.split("/");
            if (parts.length != 3) {
                JOptionPane.showMessageDialog(this,
                        "Target Date must be in MM/DD/YYYY format.",
                        "Invalid Date", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                int mm = Integer.parseInt(parts[0]);
                int dd = Integer.parseInt(parts[1]);
                int yyyy = Integer.parseInt(parts[2]);


                LocalDate target = LocalDate.of(yyyy, mm, dd); // may throw DateTimeException
                order.setDueDate(target); // store into dueDate field on Order model
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this,
                        "Target Date contains invalid numbers.",
                        "Invalid Date", JOptionPane.ERROR_MESSAGE);
                return;
            } catch (DateTimeException dte) {
                JOptionPane.showMessageDialog(this,
                        "Target Date is not a valid calendar date.",
                        "Invalid Date", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }


        order.setDateCompleted(LocalDate.now());


        // Convert our local list into a new list copy for service
        List<OrderDetail> detailsToSend = new ArrayList<>();
        for (OrderDetail d : orderDetails) {
            OrderDetail copy = new OrderDetail();
            copy.setProductId(d.getProductId());
            copy.setQuantity(d.getQuantity());
            copy.setUnitPrice(d.getUnitPrice());
            copy.setDiscount(d.getDiscount());
            copy.setTax(d.getTax());
            copy.setMaterialUsed(d.getMaterialUsed());
            copy.setPrintSize(d.getPrintSize());
            copy.setColorType(d.getColorType());
            copy.setRemarks(d.getRemarks());
            copy.setCreatedBy(DEFAULT_USER_ID);
            detailsToSend.add(copy);
        }

        order.setPaymentStatus(cbPaymentStatus.getSelectedItem().toString());
        order.setPaymentMethod(cbPaymentMethod.getSelectedItem().toString());

        boolean ok = orderService.placeOrder(order, detailsToSend, DEFAULT_USER_ID);
        if (ok) {
            String ref = order.getOrderReference();
            if (ref != null) {
                JOptionPane.showMessageDialog(this, "Order placed!\nReference: " + ref, "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Order placed! (reference generated in DB)", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
            // Clear UI
            orderDetails.clear();
            tableModel.setRowCount(0);
            tfRemarks.setText("");
            tfDiscount.setText("0");
            tfTargetDate.setText("");
            recalcTotal();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to place order. Check console for details.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }


    // ---------- small combo wrapper classes ----------
    private static class CustomerComboItem {
        Integer customerId;
        String name;
        CustomerComboItem(Integer id, String name) { this.customerId = id; this.name = name; }
        @Override public String toString() { return name; }
    }


    private static class ProductComboItem {
        Integer productId;
        String name;
        BigDecimal price;
        ProductComboItem(Integer id, String name, BigDecimal price) { this.productId = id; this.name = name; this.price = price; }
        @Override public String toString() { return name; }
    }


    // helper styling for controls
    private void styleControl(JComponent comp) {
        comp.setBackground(Color.WHITE);
        comp.setForeground(TEXT_DARK);
        comp.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        comp.setBorder(BorderFactory.createLineBorder(YELLOW_BORDER, 2));
        if (comp instanceof JComboBox) {
            ((JComboBox<?>) comp).setOpaque(true);
        }
    }


    private JLabel smallLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(TEXT_DARK);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return l;
    }


    // Helper label/value creators used in dialog
    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        l.setForeground(TEXT_DARK);
        l.setHorizontalAlignment(SwingConstants.LEFT);
        return l;
    }


    private JLabel makeLabelBold(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 16));
        l.setForeground(TEXT_DARK);
        l.setHorizontalAlignment(SwingConstants.LEFT);
        return l;
    }


    private JLabel makeValueLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        l.setHorizontalAlignment(SwingConstants.RIGHT);
        return l;
    }


    private JLabel makeValueLabelBold(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 16));
        l.setHorizontalAlignment(SwingConstants.RIGHT);
        return l;
    }


    private void styleReceiptBtn(JButton b) {
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBackground(new Color(70,80,95));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(10,16,10,16));
    }


    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new OrderManagementUI());
    }


    // ---------- RoundedPanel helper (inner class) ----------
    private static class RoundedPanel extends JPanel {
        private final int radius;
        public RoundedPanel(int radius) {
            super();
            this.radius = radius;
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            int width = getWidth();
            int height = getHeight();
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, width, height, radius, radius);
                g2.setStroke(new BasicStroke(1.5f));
                g2.setColor(YELLOW_BORDER);
                g2.drawRoundRect(0, 0, width - 1, height - 1, radius, radius);
            } finally {
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }


    // ---------- TopRoundedPanel helper (inner class) ----------
    private static class TopRoundedPanel extends JPanel {
        private final Color headerColor;
        private final int radius;
        private final int headerHeight;
        public TopRoundedPanel(Color headerColor, int radius, int headerHeight) {
            super();
            this.headerColor = headerColor;
            this.radius = radius;
            this.headerHeight = headerHeight;
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            int w = getWidth();
            int h = getHeight();
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int arc = Math.max(radius, 12) * 2;
                RoundRectangle2D roundedHeader = new RoundRectangle2D.Float(0, 0, w, headerHeight + radius/2f, arc, arc);
                g2.setColor(headerColor);
                g2.fill(roundedHeader);
            } finally {
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }
    // =============================================
// AUTO-COMPLETE SUPPORT FOR COMBOBOX
// =============================================
private void setupAutoComplete(final JComboBox comboBox) {


    final JTextField editor = (JTextField) comboBox.getEditor().getEditorComponent();
    comboBox.setEditable(true);


    editor.addKeyListener(new KeyAdapter() {
        @Override
        public void keyReleased(KeyEvent e) {
            String text = editor.getText();


            SwingUtilities.invokeLater(() -> {
                if (text.isEmpty()) {
                    comboBox.hidePopup();
                    resetCustomerList(comboBox);
                    return;
                }


                DefaultComboBoxModel model = (DefaultComboBoxModel) comboBox.getModel();
                model.removeAllElements();


                // search from loaded items
                for (int i = 0; i < allCustomerList.size(); i++) {
                    Customer c = allCustomerList.get(i);
                    if (c.getName().toLowerCase().contains(text.toLowerCase())) {
                        model.addElement(new CustomerComboItem(c.getCustomerId(), c.getName()));
                    }
                }


                editor.setText(text);
                comboBox.showPopup();
            });
        }
    });
}


/** Reset list to all customers again */
private void resetCustomerList(JComboBox combo) {
    DefaultComboBoxModel model = (DefaultComboBoxModel) combo.getModel();
    model.removeAllElements();
    model.addElement(new CustomerComboItem(null, "-- Select Customer --"));
    for (Customer c : allCustomerList)
        model.addElement(new CustomerComboItem(c.getCustomerId(), c.getName()));
}




    // ---------- Custom PlaceholderFormattedField ----------
    private static class PlaceholderFormattedField extends JFormattedTextField {


        private final String placeholder;
        private final String maskPattern;


        public PlaceholderFormattedField(MaskFormatter mf, String placeholder) {
            super();
            setFormatterFactory(new DefaultFormatterFactory(mf));
            this.placeholder = placeholder;
            this.maskPattern = mf.getMask();
            setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
            setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        }


        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);


            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);


                Insets ins = getInsets();
                FontMetrics fm = g2.getFontMetrics();


                int y = ins.top + fm.getAscent();
                int x = ins.left;


                String text = getText();


                g2.setColor(new Color(150, 150, 150, 160));


                for (int i = 0; i < maskPattern.length(); i++) {
                    char typed = text.charAt(i);
                    char mask = maskPattern.charAt(i);
                    char place = placeholder.charAt(i);


                    boolean isDigitSlot = (mask == '#' || Character.isDigit(mask));
                    boolean empty = (typed == '_' || typed == ' ');


                    if (isDigitSlot && empty) {
                        g2.drawString(String.valueOf(place), x, y);
                    }


                    x += fm.charWidth('W');
                }


            } finally {
                g2.dispose();
            }
        }
    }
}
