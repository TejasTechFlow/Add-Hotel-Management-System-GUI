package com.hotel.ui;

import com.hotel.model.User;
import com.hotel.service.AuthService;
import com.hotel.ui.components.Sidebar;
import com.hotel.ui.components.Theme;
import com.hotel.ui.components.UIUtils;
import com.hotel.ui.panels.*;
import com.hotel.util.DatabaseConnection;

import javax.swing.*;
import java.awt.*;

/**
 * MainFrame — shell window containing Sidebar + CardLayout content area.
 */
public class MainFrame extends JFrame {

    private final Sidebar      sidebar;
    private final JPanel       contentArea;
    private final CardLayout   cardLayout;
    private final User         currentUser;

    // Panels (lazy-init on first visit to avoid slow startup)
    private DashboardPanel   dashboardPanel;
    private RoomPanel        roomPanel;
    private CustomerPanel    customerPanel;
    private BookingPanel     bookingPanel;
    private BillingPanel     billingPanel;
    private ReportPanel      reportPanel;
    private UserPanel        userPanel;

    public MainFrame(User user) {
        this.currentUser = user;
        setTitle("Hotel Management System  —  " + user.getFullName());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1260, 760);
        setMinimumSize(new Dimension(1100, 680));
        setLocationRelativeTo(null);

        // Root layout
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG_MAIN);

        // Sidebar
        sidebar = new Sidebar();
        sidebar.setUser(user.getFullName(), user.getRole().name());
        sidebar.addNavListener(this::navigate);

        // Content area (CardLayout)
        cardLayout  = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(Theme.BG_MAIN);

        // Build top bar
        root.add(sidebar,     BorderLayout.WEST);
        root.add(buildTopBar(), BorderLayout.NORTH);
        root.add(contentArea, BorderLayout.CENTER);

        setContentPane(root);
        setVisible(true);

        // Show dashboard by default
        navigate("Dashboard");
    }

    // ── Top bar ───────────────────────────────────────────────────────────────

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Theme.BG_CARD);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
            BorderFactory.createEmptyBorder(0, 24, 0, 24)
        ));
        bar.setPreferredSize(new Dimension(0, 52));

        JLabel appName = UIUtils.heading("Hotel Management System");

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);

        JLabel userInfo = UIUtils.muted(currentUser.getFullName() + "  (" + currentUser.getRole() + ")");

        JButton logoutBtn = UIUtils.dangerBtn("Logout");
        logoutBtn.addActionListener(e -> logout());

        right.add(userInfo);
        right.add(logoutBtn);

        bar.add(appName, BorderLayout.WEST);
        bar.add(right,   BorderLayout.EAST);
        return bar;
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    private void navigate(String module) {
        sidebar.setActiveModule(module);

        // Lazy-create panels on first visit
        if (!hasCard(module)) {
            JPanel panel = createPanel(module);
            contentArea.add(panel, module);
        }
        cardLayout.show(contentArea, module);
    }

    private boolean hasCard(String name) {
        for (Component c : contentArea.getComponents())
            if (name.equals(((JComponent) c).getClientProperty("card"))) return true;
        // Simpler: check if component named 'name' exists
        for (Component c : contentArea.getComponents()) {
            if (c instanceof JPanel p) {
                Object prop = p.getClientProperty("card");
                if (name.equals(prop)) return true;
            }
        }
        return false;
    }

    private JPanel createPanel(String module) {
        JPanel panel = switch (module) {
            case "Dashboard" -> { dashboardPanel = new DashboardPanel(currentUser); yield dashboardPanel; }
            case "Rooms"     -> { roomPanel    = new RoomPanel(this, currentUser); yield roomPanel; }
            case "Customers" -> { customerPanel = new CustomerPanel(this, currentUser); yield customerPanel; }
            case "Bookings"  -> { bookingPanel  = new BookingPanel(this, currentUser); yield bookingPanel; }
            case "Billing"   -> { billingPanel  = new BillingPanel(this, currentUser); yield billingPanel; }
            case "Reports"   -> { reportPanel   = new ReportPanel(currentUser); yield reportPanel; }
            case "Users"     -> { userPanel     = new UserPanel(this, currentUser); yield userPanel; }
            default -> new JPanel();
        };
        panel.putClientProperty("card", module);
        return panel;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    public void refreshDashboard() {
        if (dashboardPanel != null) dashboardPanel.refresh();
    }

    private void logout() {
        int r = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?", "Logout",
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (r == JOptionPane.YES_OPTION) {
            AuthService.getInstance().logout();
            DatabaseConnection.closeConnection();
            dispose();
            new LoginFrame();
        }
    }
}
