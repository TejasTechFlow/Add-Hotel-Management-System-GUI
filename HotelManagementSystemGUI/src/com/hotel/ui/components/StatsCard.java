package com.hotel.ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * StatsCard — a metric tile displaying an icon, value and label.
 */
public class StatsCard extends JPanel {

    private JLabel valueLabel;
    private JLabel titleLabel;
    private final Color accentColor;

    public StatsCard(String title, String value, String icon, Color accentColor) {
        this.accentColor = accentColor;
        setLayout(new BorderLayout(0, 0));
        setBackground(Theme.BG_CARD);
        setBorder(BorderFactory.createCompoundBorder(
            new UIUtils.ShadowBorder(),
            BorderFactory.createEmptyBorder(18, 20, 18, 20)
        ));

        // Left content
        JPanel content = new JPanel(new GridLayout(2, 1, 0, 6));
        content.setOpaque(false);

        titleLabel = new JLabel(title);
        titleLabel.setFont(Theme.FONT_SMALL);
        titleLabel.setForeground(Theme.TEXT_SECONDARY);

        valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(Theme.TEXT_PRIMARY);

        content.add(titleLabel);
        content.add(valueLabel);

        // Icon circle (right)
        JLabel iconLbl = new JLabel(icon, SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = new Color(accentColor.getRed(), accentColor.getGreen(),
                                     accentColor.getBlue(), 30);
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        iconLbl.setPreferredSize(new Dimension(54, 54));
        iconLbl.setOpaque(false);

        add(content,  BorderLayout.CENTER);
        add(iconLbl,  BorderLayout.EAST);
    }

    public void setValue(String v) { valueLabel.setText(v); }
    public void setTitle(String t) { titleLabel.setText(t); }
}
