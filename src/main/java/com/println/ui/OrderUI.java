package com.println.ui;

import com.println.dao.CustomerDAO;
import com.println.dao.ProductDAO;
import com.println.model.Customer;
import com.println.model.Order;
import com.println.model.OrderDetail;
import com.println.model.Product;
import com.println.service.OrderService;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class OrderUI extends JFrame {

    private final CustomerDAO customerDAO = new CustomerDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final OrderService orderService = new OrderService();

    private JComboBox<CustomerComboItem> cbCustomer;
    private JComboBox<ProductComboItem> cbProduct;
    private JTextField tfUnitPrice;
    private JTextField tfQuantity;
    private JTextField tfRemarks;
    private JTable tblItems;
    private DefaultTableModel tableModel;
    private JLabel lblRunningTotal;
    private JButton btnAddItem;
    private JButton btnRemoveItem;
    private JButton btnClearItems;
    private JButton btnPlaceOrder;

    // Local list of details (keeps OrderDetail objects to send to service)
    private final List<OrderDetail> orderDetails = new ArrayList<>();
    // Map productId -> Product for quick lookup
    private final Map<Integer, Product> productMap = new HashMap<>();

    // Default createdBy user id (adjust as needed or wire to login)
    private final int DEFAULT_USER_ID = 1;

    public OrderUI() {
        setTitle("Order Management - PrintLnSystem");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        initComponents();
        loadCustomers();
        loadProducts();
        setupListeners();
    }

    private void initComponents() {
        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBorder(new EmptyBorder(12, 12, 12, 12));
        setContentPane(content);

        // Top panel: selection and inputs
        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBorder(BorderFactory.createTitledBorder("Create Order"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        // Customer
        gbc.gridx = 0; gbc.gridy = 0;
        topPanel.add(new JLabel("Customer:"), gbc);
        cbCustomer = new JComboBox<>();
        cbCustomer.setPreferredSize(new Dimension(300, 28));
        gbc.gridx = 1; gbc.gridy = 0; gbc.gridwidth = 3;
        topPanel.add(cbCustomer, gbc);

        // Product
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        topPanel.add(new JLabel("Product:"), gbc);
        cbProduct = new JComboBox<>();
        cbProduct.setPreferredSize(new Dimension(300, 28));
        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 1;
        topPanel.add(cbProduct, gbc);

        // Unit Price
        gbc.gridx = 2; gbc.gridy = 1;
        topPanel.add(new JLabel("Unit Price:"), gbc);
        tfUnitPrice = new JTextField(10);
        tfUnitPrice.setHorizontalAlignment(JTextField.RIGHT);
        gbc.gridx = 3; gbc.gridy = 1;
        topPanel.add(tfUnitPrice, gbc);

        // Quantity
        gbc.gridx = 0; gbc.gridy = 2;
        topPanel.add(new JLabel("Quantity:"), gbc);
        tfQuantity = new JTextField(6);
        tfQuantity.setHorizontalAlignment(JTextField.RIGHT);
        gbc.gridx = 1; gbc.gridy = 2;
        topPanel.add(tfQuantity, gbc);

        // Add / Remove Item buttons
        btnAddItem = new JButton("Add Item");
        btnRemoveItem = new JButton("Remove Selected");
        btnClearItems = new JButton("Clear Items");

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btnPanel.add(btnAddItem);
        btnPanel.add(btnRemoveItem);
        btnPanel.add(btnClearItems);

        gbc.gridx = 2; gbc.gridy = 2; gbc.gridwidth = 2;
        topPanel.add(btnPanel, gbc);

        // Remarks
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        topPanel.add(new JLabel("Order Remarks:"), gbc);
        tfRemarks = new JTextField();
        tfRemarks.setPreferredSize(new Dimension(400, 28));
        gbc.gridx = 1; gbc.gridy = 3; gbc.gridwidth = 3;
        topPanel.add(tfRemarks, gbc);

        content.add(topPanel, BorderLayout.NORTH);

        // Center: table with items
        String[] cols = {"#", "Product", "Qty", "Unit Price", "Subtotal"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // edits happen via inputs only
            }
        };
        tblItems = new JTable(tableModel);
        tblItems.setFillsViewportHeight(true);
        tblItems.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane sp = new JScrollPane(tblItems);
        sp.setBorder(BorderFactory.createTitledBorder("Order Items"));
        content.add(sp, BorderLayout.CENTER);

        // Bottom: running total + Place Order
        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        JPanel leftBottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lblRunningTotal = new JLabel("Total: 0.00");
        lblRunningTotal.setFont(lblRunningTotal.getFont().deriveFont(Font.BOLD, 16f));
        leftBottom.add(lblRunningTotal);

        btnPlaceOrder = new JButton("Place Order");
        JPanel rightBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightBottom.add(btnPlaceOrder);

        bottom.add(leftBottom, BorderLayout.WEST);
        bottom.add(rightBottom, BorderLayout.EAST);
        content.add(bottom, BorderLayout.SOUTH);
    }

    private void loadCustomers() {
        try {
            List<Customer> customers = customerDAO.getAllCustomers();
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
                cbProduct.addItem(new ProductComboItem(p.getProductId(), p.getProductName(), p.getPrice() != null ? p.getPrice() : BigDecimal.ZERO));
                if (p.getProductId() != 0) productMap.put(p.getProductId(), p);
            }
        } catch (Exception e) {
            showError("Failed to load products: " + e.getMessage());
        }
    }

    private void setupListeners() {
        // When product selection changes, auto-fill unit price (but keep editable)
        cbProduct.addActionListener(e -> {
            ProductComboItem item = (ProductComboItem) cbProduct.getSelectedItem();
            if (item != null && item.productId != null) {
                tfUnitPrice.setText(item.price.toPlainString());
            } else {
                tfUnitPrice.setText("");
            }
        });

        // Add item button
        btnAddItem.addActionListener(e -> onAddItem());

        // Remove selected item
        btnRemoveItem.addActionListener(e -> {
            int sel = tblItems.getSelectedRow();
            if (sel >= 0) {
                orderDetails.remove(sel);
                tableModel.removeRow(sel);
                refreshRunningTotal();
            } else {
                showInfo("Please select an item to remove.");
            }
        });

        // Clear items
        btnClearItems.addActionListener(e -> {
            if (!orderDetails.isEmpty()) {
                int ok = JOptionPane.showConfirmDialog(this, "Clear all items?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (ok == JOptionPane.YES_OPTION) {
                    orderDetails.clear();
                    tableModel.setRowCount(0);
                    refreshRunningTotal();
                }
            }
        });

        // Place order
        btnPlaceOrder.addActionListener(e -> onPlaceOrder());

        // Make Enter add item when focus is on quantity
        tfQuantity.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) onAddItem();
            }
        });
    }

    private void onAddItem() {
        ProductComboItem pItem = (ProductComboItem) cbProduct.getSelectedItem();
        if (pItem == null || pItem.productId == null) {
            showInfo("Please select a product.");
            return;
        }

        String qtyText = tfQuantity.getText().trim();
        if (qtyText.isEmpty()) {
            showInfo("Please enter quantity.");
            return;
        }
        int qty;
        try {
            qty = Integer.parseInt(qtyText);
            if (qty <= 0) { showInfo("Quantity must be > 0."); return; }
        } catch (NumberFormatException ex) {
            showInfo("Invalid quantity.");
            return;
        }

        String priceText = tfUnitPrice.getText().trim();
        BigDecimal price;
        try {
            price = new BigDecimal(priceText);
            if (price.compareTo(BigDecimal.ZERO) < 0) { showInfo("Price must be >= 0"); return; }
        } catch (Exception ex) {
            showInfo("Invalid unit price.");
            return;
        }

        // Build OrderDetail
        OrderDetail d = new OrderDetail();
        d.setProductId(pItem.productId);
        d.setQuantity(qty);
        d.setUnitPrice(price);
        // optional: d.setDiscount(BigDecimal.ZERO); d.setTax(BigDecimal.ZERO);

        orderDetails.add(d);

        // Add to table
        int rowNo = tableModel.getRowCount() + 1;
        String prodName = pItem.name;
        BigDecimal subtotal = price.multiply(BigDecimal.valueOf(qty));
        tableModel.addRow(new Object[]{rowNo, prodName, qty, price.toPlainString(), subtotal.toPlainString()});

        // Clear small inputs for next
        tfQuantity.setText("");
        // keep price as-is (editable), keep product selection

        refreshRunningTotal();
    }

    private void refreshRunningTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderDetail d : orderDetails) {
            BigDecimal sub = d.getUnitPrice().multiply(BigDecimal.valueOf(d.getQuantity()));
            total = total.add(sub);
        }
        lblRunningTotal.setText("Total: " + total.toPlainString());
    }

    private void onPlaceOrder() {
        if (orderDetails.isEmpty()) {
            showInfo("Please add at least one item to the order.");
            return;
        }

        CustomerComboItem cust = (CustomerComboItem) cbCustomer.getSelectedItem();
        Integer customerId = (cust != null && cust.customerId != null) ? cust.customerId : null;

        Order order = new Order();
        order.setCustomerId(customerId);
        order.setUserId(DEFAULT_USER_ID); // using default user; adapt when you add login
        order.setRemarks(tfRemarks.getText().trim());
        // Do not set totalAmount - backend will compute final total

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

        // Call service
        boolean ok = orderService.placeOrder(order, detailsToSend, DEFAULT_USER_ID);
        if (ok) {
            // If DAO set the order reference into the order object, show it. Otherwise, instruct user to check DB.
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
            refreshRunningTotal();
        } else {
            showError("Failed to place order. Check console for details.");
        }
    }

    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
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

    // ---------- main runner ----------
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            OrderUI ui = new OrderUI();
            ui.setVisible(true);
        });
    }
}
