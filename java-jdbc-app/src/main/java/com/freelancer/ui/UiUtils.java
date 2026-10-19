package com.freelancer.ui;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.Component;
import java.awt.Font;
import java.sql.SQLException;

/** Small shared helpers for the Swing panels. */
final class UiUtils {

    private UiUtils() {
    }

    /** Builds a table model whose cells cannot be edited directly. */
    static DefaultTableModel readOnlyModel(String[] columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    /** Applies a slightly bolder header font for readability. */
    static void styleTable(JTable table) {
        table.setRowHeight(24);
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setShowGrid(true);
    }

    /** Shows a friendly error dialog, including the SQL message when present. */
    static void showError(Component parent, String action, Exception ex) {
        String message = ex.getMessage();
        if (ex instanceof SQLException sqlEx) {
            message = "Database error (" + sqlEx.getSQLState() + ", code " + sqlEx.getErrorCode() + "):\n"
                    + sqlEx.getMessage();
        }
        JOptionPane.showMessageDialog(parent,
                "Could not " + action + ".\n\n" + message,
                "Error", JOptionPane.ERROR_MESSAGE);
    }

    static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    static boolean confirmDelete(Component parent, String what) {
        int choice = JOptionPane.showConfirmDialog(parent,
                "Are you sure you want to delete " + what + "?",
                "Confirm delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        return choice == JOptionPane.YES_OPTION;
    }

    /** A generic yes/no confirmation dialog. */
    static boolean confirm(Component parent, String title, String message) {
        int choice = JOptionPane.showConfirmDialog(parent, message, title,
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return choice == JOptionPane.YES_OPTION;
    }

    /** Validates that a field holds a non-blank integer, else throws with a friendly message. */
    static int parseInt(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(fieldName + " must be a whole number.");
        }
    }

    /** Validates that a field holds a non-blank decimal, else throws with a friendly message. */
    static java.math.BigDecimal parseDecimal(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        try {
            return new java.math.BigDecimal(value.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(fieldName + " must be a number.");
        }
    }

    static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        return value.trim();
    }

    /** Selects the combo entry whose id matches, or clears the selection if none. */
    static void selectId(JComboBox<ComboItem> box, int id) {
        for (int i = 0; i < box.getItemCount(); i++) {
            if (box.getItemAt(i).getId() == id) {
                box.setSelectedIndex(i);
                return;
            }
        }
        box.setSelectedItem(null);
    }

    /** Reads the selected foreign-key id, throwing a friendly error if nothing is chosen. */
    static int selectedId(JComboBox<ComboItem> box, String fieldName) {
        Object item = box.getSelectedItem();
        if (item == null) {
            throw new IllegalArgumentException(fieldName + " is required — please choose one.");
        }
        return ((ComboItem) item).getId();
    }
}
