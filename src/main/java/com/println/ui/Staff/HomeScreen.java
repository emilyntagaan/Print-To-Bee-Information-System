package com.println.ui.Staff;

import com.println.dao.OrderDAO;
import com.println.dao.ProductDAO;
import com.println.model.Order;
import com.println.model.OrderDetail;
import com.println.model.Product;
import com.println.model.User;
import com.println.service.OrderService;
import com.println.ui.WrapLayout;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;

/**
 * HomeScreen with preserved functionality but updated GUI design
 */
public class HomeScreen extends JFrame {

    private static final Color NAVY = new Color(12, 12, 12);
    private static final Color HEADER_YELLOW = new Color(255, 204, 0);
    private static final Color BORDER_YELLOW = new Color(204, 153, 0);
    private static final Color CARD_BG = new Color(255, 255, 255);
    private static final Font HEAD_TITLE = new Font("Segoe UI", Font.BOLD, 30);
    private static final Font SUB_TITLE = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font REGULAR = new Font("Segoe UI", Font.PLAIN, 15);

    private JPanel cardsPanel;
    private JTextField searchField;
    private JComboBox<String> cbStatusFilter;

    private final OrderDAO orderDAO = new OrderDAO();
    private final OrderService orderService = new OrderService();
    private final ProductDAO productDAO = new ProductDAO();

    private List<Order> loadedOrders = new ArrayList<>();

    private final int DEFAULT_USER_ID = 1;

    private BufferedImage watermark;

    public HomeScreen(User user) {

        // Load watermark image (background)
        try {
            ImageIcon wmIcon = new ImageIcon(getClass().getResource("/resources/images/printtobee.png"));
            Image img = wmIcon.getImage();
            int iw = img.getWidth(null);
            int ih = img.getHeight(null);

            BufferedImage buffered = new BufferedImage(iw, ih, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = buffered.createGraphics();
            g2.drawImage(img, 0, 0, null);
            g2.dispose();

            watermark = buffered;
        } catch (Exception e) {
            watermark = null;
        }

        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle("PRINT TO BEE - Home");

        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        add(buildHeader(user), BorderLayout.NORTH);

        JPanel main = new JPanel(new BorderLayout());
        main.setOpaque(false);
        main.setBorder(new EmptyBorder(18, 18, 18, 18));

        main.add(buildControlsRow(), BorderLayout.NORTH);

        cardsPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 18, 18)) {

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (watermark != null) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    
                    int w = getWidth();
                    int h = getHeight();
                    int iw = watermark.getWidth();
                    int ih = watermark.getHeight();
                    
                    // MEDIUM WATERMARK: Changed to 1.5 for medium size
                    float scale = Math.min(w / (float) (iw * 1.5), h / (float) (ih * 1.5));
                    int dw = (int) (iw * scale);
                    int dh = (int) (ih * scale);
                    
                    // Medium opacity for subtle background
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.10f));
                    
                    // Center the watermark
                    g2.drawImage(watermark, (w - dw) / 2, (h - dh) / 2, dw, dh, null);
                    g2.dispose();
                }
            }
        };

        cardsPanel.setBackground(Color.WHITE);
        JScrollPane sp = new JScrollPane(cardsPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.getVerticalScrollBar().setUnitIncrement(14);
        sp.setBorder(null);
        sp.getViewport().setBackground(Color.WHITE);

        main.add(sp, BorderLayout.CENTER);

        add(main, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setPreferredSize(new Dimension(0, 12));
        add(bottom, BorderLayout.SOUTH);

        loadOrdersFromDB();
        setVisible(true);
    }

        // Placeholder for JTextField
    private static class TextFieldPlaceholder extends JTextField {
        private String placeholder;
        private Color placeholderColor = new Color(150, 150, 150);

        public TextFieldPlaceholder(String placeholder) {
            this.placeholder = placeholder;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty() && !isFocusOwner()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setFont(getFont().deriveFont(Font.ITALIC));
                g2.setColor(placeholderColor);
                Insets ins = getInsets();
                g2.drawString(placeholder, ins.left + 2, g.getFontMetrics().getMaxAscent() + ins.top + 2);
                g2.dispose();
            }
        }
    }

    private JPanel buildHeader(User user) {
        JPanel header = new JPanel(new BorderLayout());
        header.setPreferredSize(new Dimension(0, 100));
        header.setBackground(HEADER_YELLOW);
        header.setBorder(new MatteBorder(0, 0, 6, 0, new Color(0, 0, 0, 60)));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 12));
        left.setBackground(HEADER_YELLOW);

        // UPDATED LOGO USING REQUIRED PATH
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/resources/images/printtobee.png"));
            Image img = icon.getImage().getScaledInstance(56, 56, Image.SCALE_SMOOTH);
            JLabel logo = new JLabel(new ImageIcon(img));
            logo.setBorder(new EmptyBorder(0, 0, 0, 8));
            left.add(logo);
        } catch (Exception ex) {
            JLabel placeholder = new JLabel("🐝");
            placeholder.setFont(new Font("Segoe UI", Font.BOLD, 32));
            placeholder.setOpaque(false);
            placeholder.setBorder(new EmptyBorder(0, 0, 0, 8));
            left.add(placeholder);
        }

        JLabel title = new JLabel("PRINT TO BEE");
        title.setFont(HEAD_TITLE);
        title.setForeground(Color.BLACK);
        left.add(title);

        header.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 18, 20));
        right.setBackground(HEADER_YELLOW);

        String displayName = user != null ? (user.getFirstName() + " " + user.getLastName()) : "Staff";
        JLabel userLabel = new JLabel("Welcome, " + displayName);
        userLabel.setFont(SUB_TITLE);
        userLabel.setForeground(Color.BLACK);
        Font curreFont = userLabel.getFont();
        Font biggerFont = new Font(
            curreFont.getName(),
            curreFont.getStyle(),
            curreFont.getSize() + 12
        );
        userLabel.setFont(biggerFont);

        right.add(userLabel);

        JButton btnLogout = new JButton("⟲  Log Out");
        btnLogout.setFont(new Font("Segoe UI Symbol", Font.BOLD, 14));
        btnLogout.setBackground(NAVY);
        btnLogout.setForeground(HEADER_YELLOW);
        btnLogout.setBorder(new CompoundBorder(new LineBorder(NAVY, 3, true), new EmptyBorder(8, 12, 8, 12)));
        btnLogout.setFocusPainted(false);
        btnLogout.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to log out?",
                    "Confirm Logout",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (choice == JOptionPane.YES_OPTION) {
                dispose();
                SwingUtilities.invokeLater(() -> {
                    try {
                        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    } catch (Exception ignored) {
                    }
                    new com.println.ui.LoginUI().showUI();
                });
            }
        });
        right.add(btnLogout);
        header.add(right, BorderLayout.EAST);

        return header;
    }

    private String formatDateTime(LocalDateTime dt) {
        if (dt == null) return "";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return dt.format(fmt);
    }

    private JPanel buildControlsRow() {
        JPanel row = new JPanel(new BorderLayout(12, 12));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(18, 6, 18, 6));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);

        searchField = new TextFieldPlaceholder("Search Orders..");
        searchField.setColumns(36);
        searchField.setPreferredSize(new Dimension(680, 46));
        searchField.setBackground(new Color(247, 247, 248));
        searchField.setForeground(NAVY);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        searchField.setFont(REGULAR);
        searchField.setToolTipText("Search orders...");
        left.add(searchField);

        searchField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                searchField.repaint();
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                searchField.repaint();
            }
        });

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilters(); }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilters(); }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilters(); }
        });

        cbStatusFilter = new JComboBox<>(new String[]{"All", "Pending", "Completed", "Cancelled"});
        cbStatusFilter.setPreferredSize(new Dimension(140, 46));
        cbStatusFilter.setBackground(Color.WHITE);
        cbStatusFilter.setForeground(NAVY);
        cbStatusFilter.setFont(REGULAR);
        cbStatusFilter.addActionListener(e -> applyFilters());
        left.add(cbStatusFilter);

        JButton btnFilter = new JButton("Search");
        styleControlButton(btnFilter, HEADER_YELLOW, NAVY);
        btnFilter.addActionListener(e -> applyFilters());
        left.add(btnFilter);

        row.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);

        JButton btnCreate = new JButton("+  Create Order");
        btnCreate.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnCreate.setBackground(HEADER_YELLOW);
        btnCreate.setForeground(NAVY);
        btnCreate.setBorder(new CompoundBorder(new LineBorder(BORDER_YELLOW, 2, true), new EmptyBorder(10, 14, 10, 14)));
        btnCreate.setFocusPainted(false);
        btnCreate.addActionListener(e -> createOrderDialog());

        right.add(btnCreate);
        row.add(right, BorderLayout.EAST);

        return row;
    }

    private void styleControlButton(JButton b, Color bg, Color fg) {
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setBorder(new CompoundBorder(new LineBorder(BORDER_YELLOW, 2, true), new EmptyBorder(8, 12, 8, 12)));
        b.setFocusPainted(false);
    }

    private void loadOrdersFromDB() {
        try {
            loadedOrders = orderDAO.getAllOrders();
            applyFilters();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load orders: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refreshOrders() {
        SwingUtilities.invokeLater(this::loadOrdersFromDB);
    }

    private void applyFilters() {
        String kw = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        String statusFilter = (String) cbStatusFilter.getSelectedItem();

        List<Order> filtered = new ArrayList<>();
        for (Order o : loadedOrders) {
            boolean matches = true;
            if (!kw.isEmpty()) {
                String ref = safeString(o.getOrderReference()).toLowerCase();
                String cust = safeString(o.getCustomerName()).toLowerCase();
                String date = formatDateTime(o.getOrderDate()).toLowerCase();
                if (!(ref.contains(kw) || cust.contains(kw) || date.contains(kw))) {
                    matches = false;
                }
            }
            if (statusFilter != null && !"All".equalsIgnoreCase(statusFilter)) {
                if (!statusFilter.equalsIgnoreCase(safeString(o.getStatus()))) {
                    matches = false;
                }
            }
            if (matches) filtered.add(o);
        }

        cardsPanel.removeAll();
        for (Order o : filtered) {
            cardsPanel.add(buildOrderCard(o));
        }
        cardsPanel.revalidate();
        cardsPanel.repaint();
    }

    private JPanel buildOrderCard(Order order) {
        // Fetch order details
        List<OrderDetail> details = orderDAO.getOrderDetails(order.getOrderId());

        // Build summary string (multi-line)
        StringBuilder itemsSummary = new StringBuilder("Items:\n");

        for (OrderDetail d : details) {

            // Fetch product name
            Product p = productDAO.getProductById(d.getProductId());
            String productName = (p != null) ? p.getProductName() : ("Product ID " + d.getProductId());

            itemsSummary.append("• ")
                    .append(productName)
                    .append(" — Qty ").append(d.getQuantity())
                    .append("\n");
    }

        RoundedPanel card = new RoundedPanel(10, Color.WHITE);
        card.setPreferredSize(new Dimension(380, 380));
        card.setLayout(new BorderLayout());
        card.setBorder(new CompoundBorder(new LineBorder(BORDER_YELLOW, 3, true), new EmptyBorder(0, 0, 0, 0)));

        // TOP SECTION (Order No + Status + Menu)
        JPanel top = new JPanel(new BorderLayout());
        top.setPreferredSize(new Dimension(360, 86));
        top.setBackground(HEADER_YELLOW);
        top.setBorder(new EmptyBorder(12, 16, 12, 16));

        JPanel leftTop = new JPanel();
        leftTop.setLayout(new BoxLayout(leftTop, BoxLayout.Y_AXIS));
        leftTop.setOpaque(false);

        JLabel lblOrder = new JLabel("Order No.");
        lblOrder.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblOrder.setForeground(NAVY);
        leftTop.add(lblOrder);

        JLabel lblRef = new JLabel(safeString(order.getOrderReference()));
        lblRef.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblRef.setForeground(NAVY);
        leftTop.add(lblRef);

        top.add(leftTop, BorderLayout.WEST);

        // STATUS BADGE
        JPanel rightTop = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        rightTop.setOpaque(false);

        JLabel badge = new JLabel(order.getStatus());
        badge.setOpaque(true);
        badge.setFont(new Font("Segoe UI Symbol", Font.BOLD, 12));
        badge.setBorder(new EmptyBorder(5, 10, 5, 10));

        // Color logic
        switch (safeString(order.getStatus()).toLowerCase()) {
            case "completed":
                badge.setBackground(new Color(200, 255, 220));
                badge.setForeground(new Color(18, 110, 69));
                badge.setText("✓ Completed");
                break;
            case "cancelled":
                badge.setBackground(new Color(255, 220, 220));
                badge.setForeground(new Color(160, 30, 30));
                badge.setText("✖ Cancelled");
                break;
            default:
                badge.setBackground(new Color(255, 246, 230));
                badge.setForeground(new Color(102, 60, 8));
                badge.setText("⏱ Pending");
                break;
        }

        rightTop.add(badge);

        // Larger three-dot menu
        JButton menuBtn = new JButton("⋮");
        menuBtn.setFont(new Font("Segoe UI Symbol", Font.BOLD, 12));
        menuBtn.setBackground(new Color(0,0,0,0));
        menuBtn.setForeground(NAVY);
        menuBtn.setBorder(null);
        menuBtn.setFocusPainted(false);

        JPopupMenu menu = new JPopupMenu();
        JMenuItem miView = new JMenuItem("View / Details");
        miView.addActionListener(ev -> viewOrderDetails(order));
        menu.add(miView);

        JMenuItem miComplete = new JMenuItem("Mark as Completed");
        miComplete.addActionListener(ev -> {
            int ok = JOptionPane.showConfirmDialog(this,
                    "Mark order " + safeString(order.getOrderReference()) + " as Completed?",
                    "Confirm Complete",
                    JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) {
                boolean done = orderService.completeOrder(order.getOrderId(), DEFAULT_USER_ID);
                if (done) {
                    JOptionPane.showMessageDialog(this, "Order completed.");
                    loadOrdersFromDB();
                }
            }
        });
        menu.add(miComplete);

        JMenuItem miCancel = new JMenuItem("Cancel Order");
        miCancel.addActionListener(ev -> {
            int ok = JOptionPane.showConfirmDialog(this,
                    "Cancel order " + safeString(order.getOrderReference()) + " ?",
                    "Confirm Cancel",
                    JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) {
                boolean cancelled = orderService.cancelOrder(order.getOrderId(), DEFAULT_USER_ID);
                if (cancelled) {
                    JOptionPane.showMessageDialog(this, "Order cancelled.");
                    loadOrdersFromDB();
                }
            }
        });
        menu.add(miCancel);

        JMenuItem miDelete = new JMenuItem("Delete Order");
        miDelete.addActionListener(ev -> {
            int ok = JOptionPane.showConfirmDialog(this,
                    "Permanently delete order?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) {
                boolean removed = orderDAO.deleteOrder(order.getOrderId());
                if (removed) {
                    JOptionPane.showMessageDialog(this, "Order deleted.");
                    loadOrdersFromDB();
                }
            }
        });
        menu.add(miDelete);

        menuBtn.addActionListener(ev -> menu.show(menuBtn, 0, menuBtn.getHeight()));
        rightTop.add(menuBtn);

        top.add(rightTop, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        // BOTTOM SECTION
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(Color.WHITE);
        bottom.setBorder(new EmptyBorder(14, 16, 16, 16));

        JPanel infoLeft = new JPanel();
        infoLeft.setLayout(new BoxLayout(infoLeft, BoxLayout.Y_AXIS));
        infoLeft.setOpaque(false);
        infoLeft.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Customer Name
        JLabel lblCust = new JLabel(order.getCustomerName());
        lblCust.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblCust.setForeground(NAVY);
        infoLeft.add(lblCust);

        // DATE CREATED
        String dateText = "";
        if (order.getOrderDate() != null) {
            LocalDateTime dt = order.getOrderDate();
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            dateText = "Created: " + dt.format(fmt);
        }

        JLabel lblDate = new JLabel(dateText);
        lblDate.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDate.setForeground(new Color(100, 100, 100));
        infoLeft.add(lblDate);

        // DUE DATE
        String dueDateText = (order.getDueDate() != null)
                ? "Due Date: " + order.getDueDate().toString()
                : "Due Date: —";

        JLabel lblDue = new JLabel(dueDateText);
        lblDue.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDue.setForeground(new Color(120, 120, 120));
        infoLeft.add(lblDue);

        // PAYMENT METHOD
        JLabel lblPM = new JLabel("Payment Method: " + safeString(order.getPaymentMethod()));
        lblPM.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblPM.setForeground(new Color(120, 120, 120));
        infoLeft.add(lblPM);

        // PAYMENT STATUS (Colored)
        JLabel lblPS = new JLabel("Payment Status: " + safeString(order.getPaymentStatus()));
        lblPS.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        String pay = safeString(order.getPaymentStatus()).toLowerCase();
        switch (pay) {
            case "fully paid":
                lblPS.setForeground(new Color(18, 110, 69));
                break;
            case "down payment paid":
                lblPS.setForeground(new Color(204, 136, 30));
                break;
            default:
                lblPS.setForeground(new Color(160, 30, 30));
                break;
        }
        infoLeft.add(lblPS);

        infoLeft.add(Box.createVerticalStrut(10));

        // Separator
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(230, 230, 230));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        infoLeft.add(sep);

        infoLeft.add(Box.createVerticalStrut(10));

        // Qty
        JLabel qtyLabel = new JLabel("Qty");
        qtyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        qtyLabel.setForeground(new Color(100, 100, 100));
        infoLeft.add(qtyLabel);

        JLabel qtyValue = new JLabel(String.valueOf(order.getQuantityTotal()));
        qtyValue.setFont(new Font("Segoe UI", Font.BOLD, 18));
        qtyValue.setForeground(NAVY);
        infoLeft.add(qtyValue);

        // Items summary
        JTextArea summaryArea = new JTextArea(itemsSummary.toString());
        summaryArea.setEditable(false);
        summaryArea.setOpaque(false);
        summaryArea.setForeground(new Color(80, 80, 80));
        summaryArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        summaryArea.setBorder(null);
        summaryArea.setLineWrap(true);
        summaryArea.setWrapStyleWord(true);

        infoLeft.add(Box.createVerticalStrut(10));
        infoLeft.add(summaryArea);

        bottom.add(infoLeft, BorderLayout.CENTER);

        lblCust.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblDate.setAlignmentX(Component.LEFT_ALIGNMENT);
        qtyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        qtyValue.setAlignmentX(Component.LEFT_ALIGNMENT);
        summaryArea.setAlignmentX(Component.LEFT_ALIGNMENT);

        // RIGHT SIDE (PRICE)
        JPanel rightBottom = new JPanel();
        rightBottom.setLayout(new BoxLayout(rightBottom, BoxLayout.Y_AXIS));
        rightBottom.setOpaque(false);

        JLabel lblPriceLabel = new JLabel("Price");
        lblPriceLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblPriceLabel.setForeground(new Color(100, 100, 100));
        lblPriceLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        rightBottom.add(lblPriceLabel);

        JLabel lblPrice = new JLabel("₱" + String.format("%.2f", order.getTotalAmount()));
        lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblPrice.setForeground(new Color(204, 136, 30));
        lblPrice.setAlignmentX(Component.RIGHT_ALIGNMENT);
        rightBottom.add(lblPrice);

        bottom.add(rightBottom, BorderLayout.EAST);

        card.add(bottom, BorderLayout.CENTER);

        return card;
    }


    private void showContextMenu(java.awt.event.MouseEvent evt, Order order) {
        JPopupMenu menu = new JPopupMenu();
        
        JMenuItem miView = new JMenuItem("View / Details");
        miView.addActionListener(e -> viewOrderDetails(order));
        menu.add(miView);

        JMenuItem miComplete = new JMenuItem("Mark as Completed");
        miComplete.addActionListener(e -> {
            int ok = JOptionPane.showConfirmDialog(this,
                    "Mark order " + safeString(order.getOrderReference()) + " as Completed?",
                    "Confirm Complete",
                    JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) {
                boolean done = orderService.completeOrder(order.getOrderId(), DEFAULT_USER_ID);
                if (done) {
                    JOptionPane.showMessageDialog(this, "Order completed.");
                    loadOrdersFromDB();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to complete order. Check console.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        menu.add(miComplete);

        JMenuItem miCancel = new JMenuItem("Cancel Order");
        miCancel.addActionListener(e -> {
            int ok = JOptionPane.showConfirmDialog(this,
                    "Cancel order " + safeString(order.getOrderReference()) + " ? This will restore inventory.",
                    "Confirm Cancel",
                    JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) {
                boolean cancelled = orderService.cancelOrder(order.getOrderId(), DEFAULT_USER_ID);
                if (cancelled) {
                    JOptionPane.showMessageDialog(this, "Order cancelled and inventory restored.");
                    loadOrdersFromDB();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to cancel order. Check console.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        menu.add(miCancel);

        JMenuItem miDelete = new JMenuItem("Delete Order");
        miDelete.addActionListener(e -> {
            int ok = JOptionPane.showConfirmDialog(this,
                    "Permanently delete order " + safeString(order.getOrderReference()) + " ?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) {
                boolean removed = orderDAO.deleteOrder(order.getOrderId());
                if (removed) {
                    JOptionPane.showMessageDialog(this, "Order deleted.");
                    loadOrdersFromDB();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete order. Check console.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        menu.add(miDelete);

        menu.show(evt.getComponent(), evt.getX(), evt.getY());
    }

    private void viewOrderDetails(Order order) {
        try {
            java.util.List<com.println.model.OrderDetail> details = orderDAO.getOrderDetails(order.getOrderId());
            StringBuilder sb = new StringBuilder();
            sb.append("Order: ").append(safeString(order.getOrderReference())).append("\n")
                    .append("Customer: ").append(safeString(order.getCustomerName())).append("\n")
                    .append("Date: ").append(formatDateTime(order.getOrderDate())).append("\n\n")
                    .append("Items:\n");
            for (com.println.model.OrderDetail d : details) {
                sb.append("- Product ID: ").append(d.getProductId())
                        .append(" | Qty: ").append(d.getQuantity())
                        .append(" | Unit Price: ").append(d.getUnitPrice())
                        .append("\n");
            }
            JOptionPane.showMessageDialog(this, sb.toString(), "Order Details", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Unable to fetch details: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createOrderDialog() {
        SwingUtilities.invokeLater(() -> {
            try {
                OrderManagementUI orderUI = new OrderManagementUI(this);
                orderUI.setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Failed to open Order editor: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private String safeString(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    private static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color bg;

        public RoundedPanel(int radius, Color bg) {
            this.radius = radius;
            this.bg = bg;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Shape r = new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2.setColor(bg);
            g2.fill(r);
            g2.setColor(new Color(204, 153, 0));
            g2.setStroke(new BasicStroke(2f));
            g2.draw(r);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}