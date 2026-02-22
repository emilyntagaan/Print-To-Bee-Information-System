package com.println.ui;

import com.println.dao.UserDAO;
import com.println.model.User;
import com.println.ui.Staff.HomeScreen;
import com.println.ui.admin.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.net.URL;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class LoginUI {


    private final UserDAO userDAO = new UserDAO();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginUI().showUI());
    }

    public void showUI() {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setUndecorated(true);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        // UPDATED BACKGROUND (#EDEDED)
        frame.getContentPane().setBackground(new Color(0xEDEDED));
        frame.setLayout(new GridBagLayout());

        // NEW SOFT HONEY CREAM CARD BACKGROUND (#FFF8E6)
        GradientRoundedPanel card = new GradientRoundedPanel(
            28,
            new Color(255, 248, 230),  // top
            new Color(255, 248, 230)   // bottom
        );

        card.setPreferredSize(new Dimension(520, 620));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(30, 40, 40, 40));

        ImageIcon logoIcon = loadLogoIcon();
        Image scaledLogo = logoIcon != null ? logoIcon.getImage().getScaledInstance(160, 160, Image.SCALE_SMOOTH) : null;
        JLabel logoLabel = new JLabel(scaledLogo != null ? new ImageIcon(scaledLogo) : new JLabel("LOGO").getIcon());
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("PRINT TO BEE");
        title.setFont(new Font("Montserrat", Font.BOLD, 32));
        title.setForeground(new Color(255, 193, 7));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(new EmptyBorder(18, 0, 6, 0));

        JLabel subtitle = new JLabel("Your Digital Printing Solution");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitle.setForeground(new Color(200, 170, 60));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setBorder(new EmptyBorder(0, 0, 26, 0));

        JPanel inputs = new JPanel();
        inputs.setOpaque(false);
        inputs.setLayout(new BoxLayout(inputs, BoxLayout.Y_AXIS));
        inputs.setAlignmentX(Component.CENTER_ALIGNMENT);

        CustomField usernameField = new CustomField("Username");
        usernameField.setMaximumSize(new Dimension(420, 60));
        usernameField.setPreferredSize(new Dimension(420, 60));
        usernameField.setAlignmentX(Component.CENTER_ALIGNMENT);

        CustomPasswordField passwordField = new CustomPasswordField("Password");
        passwordField.setMaximumSize(new Dimension(420, 60));
        passwordField.setPreferredSize(new Dimension(420, 60));
        passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);

        inputs.add(usernameField);
        inputs.add(Box.createVerticalStrut(18));
        inputs.add(passwordField);
        inputs.add(Box.createVerticalStrut(28));

        JButton loginBtn = new JButton("Login");
        loginBtn.setFont(new Font("Arial", Font.BOLD, 16));
        loginBtn.setForeground(Color.BLACK);
        loginBtn.setBackground(new Color(255, 193, 7));
        loginBtn.setFocusPainted(false);
        loginBtn.setBorderPainted(false);
        loginBtn.setOpaque(true);
        loginBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginBtn.setPreferredSize(new Dimension(420, 56));
        loginBtn.setMaximumSize(new Dimension(420, 56));
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        loginBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                loginBtn.setBackground(new Color(255, 213, 79));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                loginBtn.setBackground(new Color(255, 193, 7));
            }
        });

        loginBtn.addActionListener(e -> {
            String username = usernameField.getValue().trim();
            String password = passwordField.getValue();

            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please enter username.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (password.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please enter password.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            User u = userDAO.validateLogin(username, password);
            if (u == null) {
                JOptionPane.showMessageDialog(frame, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String role = u.getRole();
            if ("Staff".equalsIgnoreCase(role)) {
                frame.dispose();
                new HomeScreen(u);
            } else if ("Admin".equalsIgnoreCase(role)) {
                frame.dispose();
                new AdminDashboard().setVisible(true);
            } else {
                JOptionPane.showMessageDialog(frame, "Unknown role. Contact admin.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        card.add(Box.createVerticalStrut(10));
        card.add(logoLabel);
        card.add(title);
        card.add(subtitle);
        card.add(inputs);
        card.add(loginBtn);

        card.setStrokeColor(new Color(255, 193, 7));
        card.setStrokeWidth(3);

        frame.add(card);
        frame.setVisible(true);
    }

    private ImageIcon loadLogoIcon() {
        try {
            // Match the same loading style used in other interfaces
            URL res = getClass().getResource("/resources/images/printtobee.png");
            if (res != null) {
                return new ImageIcon(res);
            }
        } catch (Exception ignored) {}

        // Fallback (avoid crashing)
        System.err.println("Logo load failed from resources. Fallback attempted.");
        return null;
    }

    /* ---------------- CUSTOM COMPONENTS WITH UPDATED COLORS ---------------- */

    static class GradientRoundedPanel extends JPanel {
        private int radius;
        private Color top;
        private Color bottom;
        private Color stroke = new Color(255, 193, 7);
        private int strokeWidth = 2;

        GradientRoundedPanel(int radius, Color top, Color bottom) {
            this.radius = radius;
            this.top = top;
            this.bottom = bottom;
            setOpaque(false);
        }

        void setStrokeColor(Color c) { this.stroke = c; }
        void setStrokeWidth(int w) { this.strokeWidth = w; }

        @Override
        protected void paintComponent(Graphics g) {
            int w = getWidth();
            int h = getHeight();
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            GradientPaint gp = new GradientPaint(0, 0, top, 0, h, bottom);
            g2.setPaint(gp);
            RoundRectangle2D.Float rr = new RoundRectangle2D.Float(0, 0, w, h, radius, radius);
            g2.fill(rr);
            g2.setStroke(new BasicStroke(strokeWidth));
            g2.setColor(stroke);
            g2.draw(rr);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ---------- USERNAME ----------
    static class CustomField extends JPanel {
        private final JTextField field;
        private final String placeholder;
        private boolean isPlaceholderVisible = true;

        CustomField(String placeholder) {
            this.placeholder = placeholder;
            setLayout(new BorderLayout(12, 0));
            setOpaque(false);
            setBorder(BorderFactory.createLineBorder(new Color(255, 193, 7), 2, true));

            JLabel icon = new JLabel("👤");
            icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
            icon.setBorder(new EmptyBorder(0, 10, 0, 0));
            icon.setForeground(new Color(255, 193, 7));
            add(icon, BorderLayout.WEST);

            field = new JTextField();
            field.setBorder(BorderFactory.createEmptyBorder(12, 8, 12, 8));
            field.setOpaque(false);
            field.setFont(new Font("Arial", Font.PLAIN, 15));

            // BLACK TYPED TEXT & CURSOR
            field.setForeground(Color.BLACK);
            field.setCaretColor(Color.BLACK);

            showPlaceholder();

            field.addKeyListener(new java.awt.event.KeyAdapter() {
                @Override
                public void keyTyped(java.awt.event.KeyEvent evt) {
                    if (isPlaceholderVisible) {
                        char ch = evt.getKeyChar();
                        if (Character.isISOControl(ch)) {
                            evt.consume();
                            return;
                        }
                        isPlaceholderVisible = false;
                        field.setText("");
                        field.setForeground(Color.BLACK);
                    }
                }

                @Override
                public void keyPressed(java.awt.event.KeyEvent evt) {
                    if (isPlaceholderVisible) {
                        field.setCaretPosition(0);
                        int code = evt.getKeyCode();
                        if (code == java.awt.event.KeyEvent.VK_BACK_SPACE || code == java.awt.event.KeyEvent.VK_DELETE) {
                            evt.consume();
                        }
                    }
                }
            });

            field.addFocusListener(new java.awt.event.FocusAdapter() {
                public void focusGained(java.awt.event.FocusEvent evt) {
                    if (isPlaceholderVisible) SwingUtilities.invokeLater(() -> field.setCaretPosition(0));
                }
                public void focusLost(java.awt.event.FocusEvent evt) {
                    if (field.getText().trim().isEmpty()) showPlaceholder();
                }
            });

            field.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mousePressed(java.awt.event.MouseEvent e) {
                    if (isPlaceholderVisible) {
                        SwingUtilities.invokeLater(() -> field.setCaretPosition(0));
                        e.consume();
                    }
                }
            });

            add(field, BorderLayout.CENTER);
        }

        private void showPlaceholder() {
            field.setForeground(new Color(180, 180, 180));
            field.setText(placeholder);
            isPlaceholderVisible = true;
            SwingUtilities.invokeLater(() -> field.setCaretPosition(0));
        }

        public String getValue() {
            return isPlaceholderVisible ? "" : field.getText();
        }
    }

    // ---------- PASSWORD ----------
    static class CustomPasswordField extends JPanel {
        private final JPasswordField field;
        private final String placeholder;
        private boolean isPlaceholderVisible = true;
        private final JLabel eyeIcon;
        private boolean showPassword = false;

        CustomPasswordField(String placeholder) {
            this.placeholder = placeholder;
            setLayout(new BorderLayout(12, 0));
            setOpaque(false);
            setBorder(BorderFactory.createLineBorder(new Color(255, 193, 7), 2, true));

            JLabel icon = new JLabel("🔒");
            icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
            icon.setForeground(new Color(255, 193, 7));
            icon.setBorder(new EmptyBorder(0, 10, 0, 0));
            add(icon, BorderLayout.WEST);

            field = new JPasswordField();
            field.setBorder(BorderFactory.createEmptyBorder(12, 8, 12, 8));
            field.setOpaque(false);
            field.setFont(new Font("Arial", Font.PLAIN, 15));

            // BLACK TYPED TEXT & CURSOR
            field.setCaretColor(Color.BLACK);
            field.setForeground(Color.BLACK);

            showPlaceholder();

            field.addKeyListener(new java.awt.event.KeyAdapter() {
                @Override
                public void keyTyped(java.awt.event.KeyEvent evt) {
                    if (isPlaceholderVisible) {
                        char ch = evt.getKeyChar();
                        if (Character.isISOControl(ch)) {
                            evt.consume();
                            return;
                        }
                        isPlaceholderVisible = false;
                        field.setText("");
                        field.setEchoChar('•');
                        field.setForeground(Color.BLACK);
                    }
                }

                @Override
                public void keyPressed(java.awt.event.KeyEvent evt) {
                    if (isPlaceholderVisible) {
                        field.setCaretPosition(0);
                        int code = evt.getKeyCode();
                        if (code == java.awt.event.KeyEvent.VK_BACK_SPACE || code == java.awt.event.KeyEvent.VK_DELETE) {
                            evt.consume();
                        }
                    }
                }
            });

            field.addFocusListener(new java.awt.event.FocusAdapter() {
                public void focusGained(java.awt.event.FocusEvent evt) {
                    if (isPlaceholderVisible) SwingUtilities.invokeLater(() -> field.setCaretPosition(0));
                }
                public void focusLost(java.awt.event.FocusEvent evt) {
                    String txt = String.valueOf(field.getPassword());
                    if (txt.trim().isEmpty()) showPlaceholder();
                }
            });

            add(field, BorderLayout.CENTER);

            eyeIcon = new JLabel("👁️");
            eyeIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
            eyeIcon.setForeground(new Color(255, 193, 7));
            eyeIcon.setBorder(new EmptyBorder(0, 0, 0, 10));
            eyeIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));

            eyeIcon.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    if (isPlaceholderVisible) return;
                    showPassword = !showPassword;
                    field.setEchoChar(showPassword ? (char) 0 : '•');
                    eyeIcon.setText(showPassword ? "🙈" : "👁️");
                }
            });

            add(eyeIcon, BorderLayout.EAST);
        }

        private void showPlaceholder() {
            field.setForeground(new Color(180, 180, 180));
            field.setText(placeholder);
            field.setEchoChar((char) 0);
            isPlaceholderVisible = true;
            SwingUtilities.invokeLater(() -> field.setCaretPosition(0));
        }

        public String getValue() {
            return isPlaceholderVisible ? "" : String.valueOf(field.getPassword());
        }
    }
}