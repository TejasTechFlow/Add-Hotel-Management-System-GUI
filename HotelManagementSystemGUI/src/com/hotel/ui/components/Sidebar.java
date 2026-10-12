package com.hotel.ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Sidebar — the dark left-rail navigation panel.
 * Fires an ActionEvent with the module name as command on selection.
 */
public class Sidebar extends JPanel {

    public interface NavListener { void onNavigate(String module); }

    private static final String[] MODULES = {
        "Dashboard", "Rooms", "Customers", "Bookings", "Billing", "Reports", "Users"
    };
    private static final String[] ICONS = {
        "⊞", "🏠", "👤", "📅", "💰", "📊", "👥"
    };

    private String active = "Dashboard";
    private final List<NavListener> listeners = new ArrayList<>();
    private JLabel usernameLabel;
    private JLabel roleLabel;

    public Sidebar() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG_SIDEBAR);
        setPreferredSize(new Dimension(Theme.SIDEBAR_WIDTH, 0));
        add(buildTop(),   BorderLayout.NORTH);
        add(buildNav(),   BorderLayout.CENTER);
        add(buildBottom(),BorderLayout.SOUTH);
    }

    // ── Top branding ──────────────────────────────────────────────────────────

    private JPanel buildTop() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Theme.BG_SIDEBAR);
        top.setBorder(BorderFactory.createEmptyBorder(24, 20, 16, 20));

        JLabel icon = new JLabel("🏨");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        icon.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));

        JLabel name = new JLabel("HotelMS");
        name.setFont(new Font("Segoe UI", Font.BOLD, 18));
        name.setForeground(Color.WHITE);

        JLabel tagline = new JLabel("Management System");
        tagline.setFont(Theme.FONT_SMALL);
        tagline.setForeground(new Color(148, 163, 184));

        JPanel text = new JPanel(new GridLayout(2, 1, 0, 2));
        text.setOpaque(false);
        text.add(name);
        text.add(tagline);

        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.add(icon, BorderLayout.WEST);
        row.add(text, BorderLayout.CENTER);
        top.add(row, BorderLayout.CENTER);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(30, 41, 59));
        top.add(sep, BorderLayout.SOUTH);
        return top;
    }

    // ── Nav items ─────────────────────────────────────────────────────────────

    private JPanel buildNav() {
        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBackground(Theme.BG_SIDEBAR);
        nav.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));

        for (int i = 0; i < MODULES.length; i++) {
            nav.add(buildNavItem(MODULES[i], ICONS[i]));
            nav.add(Box.createRigidArea(new Dimension(0, 2)));
        }
        return nav;
    }

    private JPanel buildNavItem(String module, String icon) {
        JPanel item = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (module.equals(active)) {
                    g2.setColor(Theme.SIDEBAR_ACTIVE);
                    g2.fill(new RoundRectangle2D.Float(8, 0, getWidth()-16, getHeight(), 8, 8));
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        item.setOpaque(false);
        item.setMaximumSize(new Dimension(Theme.SIDEBAR_WIDTH, 42));
        item.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));

        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        iconLbl.setForeground(module.equals(active) ? Color.WHITE : new Color(148, 163, 184));
        iconLbl.setPreferredSize(new Dimension(28, 42));

        JLabel textLbl = new JLabel(module);
        textLbl.setFont(module.equals(active) ? Theme.FONT_NAV_BOLD : Theme.FONT_NAV);
        textLbl.setForeground(module.equals(active) ? Color.WHITE : new Color(203, 213, 225));

        item.add(iconLbl, BorderLayout.WEST);
        item.add(textLbl, BorderLayout.CENTER);

        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        item.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (!module.equals(active)) item.setBackground(Theme.SIDEBAR_HOVER);
            }
            @Override public void mouseExited(MouseEvent e)  { item.setBackground(null); }
            @Override public void mouseClicked(MouseEvent e) {
                active = module;
                repaint();
                // Refresh the entire nav so all items re-paint their active state
                Container parent = item.getParent();
                if (parent != null) parent.repaint();
                listeners.forEach(l -> l.onNavigate(module));
            }
        });

        return item;
    }

    // ── Bottom user info ──────────────────────────────────────────────────────

    private JPanel buildBottom() {
        JPanel bottom = new JPanel(new BorderLayout(10, 0));
        bottom.setBackground(new Color(30, 41, 59));
        bottom.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JLabel avatar = new JLabel("👤");
        avatar.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));

        usernameLabel = new JLabel("—");
        usernameLabel.setFont(Theme.FONT_NAV_BOLD);
        usernameLabel.setForeground(Color.WHITE);

        roleLabel = new JLabel("—");
        roleLabel.setFont(Theme.FONT_SMALL);
        roleLabel.setForeground(new Color(148, 163, 184));

        JPanel info = new JPanel(new GridLayout(2, 1, 0, 1));
        info.setOpaque(false);
        info.add(usernameLabel);
        info.add(roleLabel);

        bottom.add(avatar, BorderLayout.WEST);
        bottom.add(info,   BorderLayout.CENTER);
        return bottom;
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public void addNavListener(NavListener l) { listeners.add(l); }

    public void setActiveModule(String module) {
        active = module;
        repaint();
    }

    public void setUser(String username, String role) {
        usernameLabel.setText(username);
        roleLabel.setText(role);
    }
}
