package com.hotel.ui;

import com.hotel.exception.ValidationException;
import com.hotel.model.User;
import com.hotel.service.AuthService;
import com.hotel.ui.components.Theme;
import com.hotel.ui.components.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * LoginFrame — splash screen and authentication window.
 */
public class LoginFrame extends JFrame {

    private JTextField     usernameField;
    private JPasswordField passwordField;
    private JLabel         errorLabel;
    private JButton        loginBtn;

    public LoginFrame() {
        setTitle("Hotel Management System — Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setUndecorated(false);
        setResizable(false);
        buildUI();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Gradient left panel background
                GradientPaint gp = new GradientPaint(0, 0, new Color(17, 24, 39),
                                                      0, getHeight(), new Color(30, 58, 138));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth()/2, getHeight());
                g2.setColor(Theme.BG_MAIN);
                g2.fillRect(getWidth()/2, 0, getWidth()/2, getHeight());
                g2.dispose();
            }
        };
        root.setPreferredSize(new Dimension(900, 580));

        root.add(buildLeftPanel(),  BorderLayout.WEST);
        root.add(buildRightPanel(), BorderLayout.CENTER);
        setContentPane(root);
    }

    // ── Left branding panel ───────────────────────────────────────────────────

    private JPanel buildLeftPanel() {
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setPreferredSize(new Dimension(420, 0));
        left.setLayout(new GridBagLayout());

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel hotelIcon = new JLabel("🏨");
        hotelIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60));
        hotelIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("HotelMS");
        title.setFont(new Font("Segoe UI", Font.BOLD, 36));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Hotel Management System");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitle.setForeground(new Color(148, 163, 184));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel ver = new JLabel("v1.0 · Built with Java Swing + JDBC + MySQL");
        ver.setFont(Theme.FONT_SMALL);
        ver.setForeground(new Color(100, 116, 139));
        ver.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Features list
        String[] features = {"User & Role Management", "Room Booking & Availability",
                             "Customer Records", "Billing & Reports"};
        JPanel featurePanel = new JPanel();
        featurePanel.setOpaque(false);
        featurePanel.setLayout(new BoxLayout(featurePanel, BoxLayout.Y_AXIS));
        featurePanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        for (String f : features) {
            JLabel fl = new JLabel("  ✓  " + f);
            fl.setFont(Theme.FONT_BODY);
            fl.setForeground(new Color(148, 163, 184));
            fl.setAlignmentX(Component.CENTER_ALIGNMENT);
            featurePanel.add(fl);
            featurePanel.add(Box.createRigidArea(new Dimension(0, 6)));
        }

        content.add(hotelIcon);
        content.add(Box.createRigidArea(new Dimension(0, 12)));
        content.add(title);
        content.add(Box.createRigidArea(new Dimension(0, 6)));
        content.add(subtitle);
        content.add(Box.createRigidArea(new Dimension(0, 4)));
        content.add(ver);
        content.add(featurePanel);

        left.add(content);
        return left;
    }

    // ── Right login form ──────────────────────────────────────────────────────

    private JPanel buildRightPanel() {
        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(Theme.BG_MAIN);

        JPanel card = new JPanel();
        card.setBackground(Theme.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            new UIUtils.ShadowBorder(),
            BorderFactory.createEmptyBorder(36, 36, 36, 36)
        ));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(360, 420));

        JLabel welcome = new JLabel("Welcome back");
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 24));
        welcome.setForeground(Theme.TEXT_PRIMARY);
        welcome.setAlignmentX(LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Sign in to your account");
        sub.setFont(Theme.FONT_BODY);
        sub.setForeground(Theme.TEXT_SECONDARY);
        sub.setAlignmentX(LEFT_ALIGNMENT);

        usernameField = UIUtils.textField(null);
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        passwordField = UIUtils.passwordField();
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        loginBtn = UIUtils.primaryBtn("  Sign In  ");
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        loginBtn.setAlignmentX(LEFT_ALIGNMENT);
        loginBtn.addActionListener(e -> attemptLogin());

        // Allow Enter key to trigger login
        KeyAdapter enterKey = new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) attemptLogin();
            }
        };
        usernameField.addKeyListener(enterKey);
        passwordField.addKeyListener(enterKey);

        errorLabel = new JLabel(" ");
        errorLabel.setFont(Theme.FONT_SMALL);
        errorLabel.setForeground(Theme.DANGER);
        errorLabel.setAlignmentX(LEFT_ALIGNMENT);

        // Default credentials hint
        JLabel hint = new JLabel("Demo: admin / admin123  |  staff1 / staff123");
        hint.setFont(Theme.FONT_SMALL);
        hint.setForeground(Theme.TEXT_SECONDARY);
        hint.setAlignmentX(LEFT_ALIGNMENT);

        card.add(welcome);
        card.add(Box.createRigidArea(new Dimension(0, 4)));
        card.add(sub);
        card.add(Box.createRigidArea(new Dimension(0, 28)));
        card.add(fieldLabel("Username"));
        card.add(Box.createRigidArea(new Dimension(0, 6)));
        card.add(usernameField);
        card.add(Box.createRigidArea(new Dimension(0, 16)));
        card.add(fieldLabel("Password"));
        card.add(Box.createRigidArea(new Dimension(0, 6)));
        card.add(passwordField);
        card.add(Box.createRigidArea(new Dimension(0, 8)));
        card.add(errorLabel);
        card.add(Box.createRigidArea(new Dimension(0, 8)));
        card.add(loginBtn);
        card.add(Box.createRigidArea(new Dimension(0, 20)));
        card.add(UIUtils.separator());
        card.add(Box.createRigidArea(new Dimension(0, 12)));
        card.add(hint);

        right.add(card);
        return right;
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_SUBHEAD);
        l.setForeground(Theme.TEXT_PRIMARY);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    // ── Login logic ───────────────────────────────────────────────────────────

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        errorLabel.setText(" ");
        loginBtn.setEnabled(false);
        loginBtn.setText("Signing in...");

        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override protected User doInBackground() {
                return AuthService.getInstance().login(username, password);
            }
            @Override protected void done() {
                loginBtn.setEnabled(true);
                loginBtn.setText("  Sign In  ");
                try {
                    User user = get();
                    dispose();
                    new MainFrame(user);
                } catch (Exception ex) {
                    String msg = ex.getCause() instanceof ValidationException
                            ? ex.getCause().getMessage() : "Login failed. Try again.";
                    errorLabel.setText(msg);
                    passwordField.setText("");
                }
            }
        };
        worker.execute();
    }
}
