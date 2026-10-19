package com.freelancer;

import com.freelancer.db.DBConnection;
import com.freelancer.ui.MainFrame;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Application entry point.
 *
 * <p>Architecture:  Swing GUI  ->  DAO layer  ->  JDBC  ->  MySQL on Aiven.
 */
public class Main {

    public static void main(String[] args) {
        // System look-and-feel for a native, clean appearance.
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Fall back to the default look-and-feel silently.
        }

        SwingUtilities.invokeLater(() -> {
            // Verify connectivity up front so configuration problems are obvious.
            try {
                DBConnection.testConnection();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null,
                        "Could not connect to the database.\n\n" + ex.getMessage()
                                + "\n\nCheck that DB_HOST, DB_PORT, DB_USER, DB_PASSWORD and DB_NAME "
                                + "are set correctly, then restart the app.",
                        "Connection failed", JOptionPane.ERROR_MESSAGE);
                // Still open the UI; individual actions will report errors too.
            }

            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
