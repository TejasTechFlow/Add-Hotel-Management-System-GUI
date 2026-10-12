package com.hotel.ui.components;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * UIUtils — factory methods for styled Swing components.
 */
public class UIUtils {

    private UIUtils() {}

    // ── Buttons ───────────────────────────────────────────────────────────────

    /** Solid primary/danger/success action button. */
    public static JButton button(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color fill = getModel().isPressed()   ? bg.darker()
                           : getModel().isRollover()  ? bg.brighter()
                           : bg;
                g2.setColor(fill);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(Theme.FONT_BTN);
        btn.setForeground(fg);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 20, 34));
        return btn;
    }

    public static JButton primaryBtn(String text) { return button(text, Theme.PRIMARY, Color.WHITE); }
    public static JButton dangerBtn(String text)  { return button(text, Theme.DANGER,  Color.WHITE); }
    public static JButton successBtn(String text) { return button(text, Theme.ACCENT,  Color.WHITE); }

    /** Ghost/outline button. */
    public static JButton outlineBtn(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isRollover() ? Theme.PRIMARY_LIGHT : Theme.BG_CARD;
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.setColor(Theme.PRIMARY);
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth()-2, getHeight()-2, 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(Theme.FONT_BTN);
        btn.setForeground(Theme.PRIMARY);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 20, 34));
        return btn;
    }

    // ── Inputs ────────────────────────────────────────────────────────────────

    public static JTextField textField(String placeholder) {
        JTextField tf = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && placeholder != null) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(Theme.TEXT_SECONDARY);
                    g2.setFont(getFont().deriveFont(Font.ITALIC));
                    FontMetrics fm = g2.getFontMetrics();
                    int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                    g2.drawString(placeholder, 8, y);
                    g2.dispose();
                }
            }
        };
        tf.setFont(Theme.FONT_INPUT);
        tf.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(Theme.BORDER, 6),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        tf.setPreferredSize(new Dimension(200, 36));
        return tf;
    }

    public static JPasswordField passwordField() {
        JPasswordField pf = new JPasswordField();
        pf.setFont(Theme.FONT_INPUT);
        pf.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(Theme.BORDER, 6),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        pf.setPreferredSize(new Dimension(200, 36));
        return pf;
    }

    public static JComboBox<String> comboBox(String... items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(Theme.FONT_INPUT);
        cb.setBackground(Theme.BG_CARD);
        cb.setPreferredSize(new Dimension(200, 36));
        return cb;
    }

    // ── Labels ────────────────────────────────────────────────────────────────

    public static JLabel label(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }

    public static JLabel title(String text)   { return label(text, Theme.FONT_TITLE,   Theme.TEXT_PRIMARY); }
    public static JLabel heading(String text) { return label(text, Theme.FONT_HEADING, Theme.TEXT_PRIMARY); }
    public static JLabel subhead(String text) { return label(text, Theme.FONT_SUBHEAD, Theme.TEXT_PRIMARY); }
    public static JLabel body(String text)    { return label(text, Theme.FONT_BODY,    Theme.TEXT_PRIMARY); }
    public static JLabel muted(String text)   { return label(text, Theme.FONT_BODY,    Theme.TEXT_SECONDARY); }

    // ── Status chips ──────────────────────────────────────────────────────────

    public static JLabel statusChip(String text, Color bg) {
        JLabel chip = new JLabel(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        Color textColor = bg.darker().darker();
        chip.setForeground(textColor);
        chip.setFont(Theme.FONT_SMALL.deriveFont(Font.BOLD));
        chip.setOpaque(false);
        chip.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        return chip;
    }

    public static Color statusColor(String status) {
        return switch (status.toUpperCase()) {
            case "AVAILABLE"   -> new Color(220, 252, 231);
            case "BOOKED"      -> new Color(254, 243, 199);
            case "MAINTENANCE" -> new Color(254, 226, 226);
            case "CONFIRMED"   -> new Color(219, 234, 254);
            case "CHECKED_IN"  -> new Color(220, 252, 231);
            case "CHECKED_OUT" -> new Color(241, 245, 249);
            case "CANCELLED"   -> new Color(254, 226, 226);
            case "PAID"        -> new Color(220, 252, 231);
            case "PENDING"     -> new Color(254, 243, 199);
            default            -> new Color(241, 245, 249);
        };
    }

    // ── Cards ─────────────────────────────────────────────────────────────────

    /** A white rounded card panel. */
    public static JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(Theme.BG_CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
            new ShadowBorder(),
            BorderFactory.createEmptyBorder(16, 20, 16, 20)
        ));
        return p;
    }

    // ── Table styling ─────────────────────────────────────────────────────────

    public static void styleTable(JTable table) {
        table.setFont(Theme.FONT_TABLE);
        table.setRowHeight(Theme.ROW_HEIGHT);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setGridColor(Theme.BORDER);
        table.setSelectionBackground(Theme.PRIMARY_LIGHT);
        table.setSelectionForeground(Theme.TEXT_PRIMARY);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.getTableHeader().setFont(Theme.FONT_TABLE_HDR);
        table.getTableHeader().setBackground(new Color(241, 245, 249));
        table.getTableHeader().setForeground(Theme.TEXT_PRIMARY);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0,0,2,0, Theme.BORDER));
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));

        // Alternating row colours
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean selected, boolean focused, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, selected, focused, row, col);
                if (!selected) c.setBackground(row % 2 == 0 ? Theme.BG_CARD : Theme.BG_TABLE_ALT);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return c;
            }
        });
    }

    public static JScrollPane scrollPane(JComponent comp) {
        JScrollPane sp = new JScrollPane(comp);
        sp.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        sp.getViewport().setBackground(Theme.BG_CARD);
        return sp;
    }

    // ── Separator ─────────────────────────────────────────────────────────────

    public static JSeparator separator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(Theme.BORDER);
        return sep;
    }

    // ── Form row ─────────────────────────────────────────────────────────────

    /** Returns a label+component row for forms. */
    public static JPanel formRow(String labelText, JComponent input) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        JLabel lbl = label(labelText, Theme.FONT_SUBHEAD, Theme.TEXT_PRIMARY);
        lbl.setPreferredSize(new Dimension(140, 36));
        row.add(lbl, BorderLayout.WEST);
        row.add(input, BorderLayout.CENTER);
        return row;
    }

    // ── Toast notification ────────────────────────────────────────────────────

    public static void toast(JFrame owner, String msg, boolean success) {
        JDialog toast = new JDialog(owner, false);
        toast.setUndecorated(true);
        JLabel lbl = new JLabel(" " + msg + " ");
        lbl.setFont(Theme.FONT_BODY);
        lbl.setForeground(Color.WHITE);
        lbl.setOpaque(true);
        lbl.setBackground(success ? Theme.ACCENT : Theme.DANGER);
        lbl.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        toast.add(lbl);
        toast.pack();
        // Position bottom-centre of owner
        Point ownerLoc = owner.getLocationOnScreen();
        Dimension ownerSize = owner.getSize();
        toast.setLocation(
            ownerLoc.x + (ownerSize.width  - toast.getWidth())  / 2,
            ownerLoc.y +  ownerSize.height - toast.getHeight()  - 60);
        toast.setVisible(true);
        new Timer(2500, e -> toast.dispose()).start();
    }

    // ── Inner helper borders ──────────────────────────────────────────────────

    public static class RoundedBorder extends AbstractBorder {
        private final Color color;
        private final int radius;
        public RoundedBorder(Color c, int r) { color = c; radius = r; }

        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c.hasFocus() ? Theme.BORDER_FOCUS : color);
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(new RoundRectangle2D.Float(x+1, y+1, w-2, h-2, radius, radius));
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) { return new Insets(2,2,2,2); }
    }

    public static class ShadowBorder extends AbstractBorder {
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0, 0, 0, 18));
            g2.fill(new RoundRectangle2D.Float(x+2, y+2, w-2, h-2, 12, 12));
            g2.setColor(Theme.BG_CARD);
            g2.fill(new RoundRectangle2D.Float(x, y, w-2, h-2, 12, 12));
            g2.setColor(Theme.BORDER);
            g2.setStroke(new BasicStroke(1f));
            g2.draw(new RoundRectangle2D.Float(x, y, w-2, h-2, 12, 12));
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) { return new Insets(4, 4, 6, 6); }
    }
}
