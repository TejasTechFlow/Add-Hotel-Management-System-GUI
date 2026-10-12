package com.hotel;

import com.hotel.ui.LoginFrame;
import com.hotel.util.DatabaseConnection;

import javax.swing.*;

/**
 * HotelManagementApp — GUI entry point.
 *
 * Sets the system Look & Feel, verifies DB connectivity,
 * then launches the LoginFrame on the Swing Event Dispatch Thread.
 */
public class HotelManagementApp {

    public static void main(String[] args) {

        // ── Look & Feel ───────────────────────────────────────────────────────
        try {
            // Nimbus gives a cleaner cross-platform appearance
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
            // Global rendering hint for all Swing components
            System.setProperty("awt.useSystemAAFontSettings", "on");
            System.setProperty("swing.aatext", "true");
        } catch (Exception e) {
            // Fall back to default L&F — not fatal
        }

        // ── Database connectivity check ────────────────────────────────────────
        try {
            DatabaseConnection.getInstance();
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(null,
                "<html><b>Cannot connect to database.</b><br><br>"
              + e.getMessage() + "<br><br>"
              + "Please check:<br>"
              + "• MySQL is running on localhost:3306<br>"
              + "• Database 'hotel_management' exists (run hotel_schema.sql)<br>"
              + "• Credentials in DatabaseConnection.java are correct<br>"
              + "• mysql-connector-j.jar is in the classpath</html>",
                "Database Connection Error",
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // ── Shutdown hook ─────────────────────────────────────────────────────
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            DatabaseConnection.closeConnection();
            System.out.println("[App] DB connection closed.");
        }));

        // ── Launch GUI on EDT ─────────────────────────────────────────────────
        SwingUtilities.invokeLater(() -> {
            new LoginFrame();
        });
    }
}
