package com.freelancer.ui;

import com.freelancer.dao.FreelancerDAO;
import com.freelancer.model.Freelancer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.util.List;

/** CRUD tab for the Freelancers table. */
public class FreelancerPanel extends JPanel {

    private final FreelancerDAO dao = new FreelancerDAO();

    private final JTextField idField = new JTextField(10);
    private final JTextField nameField = new JTextField(18);
    private final JTextField emailField = new JTextField(18);
    private final JTextField ratingField = new JTextField(6);

    private final DefaultTableModel tableModel =
            UiUtils.readOnlyModel(new String[]{"freelancer_id", "name", "email", "avg_rating"});
    private final JTable table = new JTable(tableModel);

    public FreelancerPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(buildForm(), BorderLayout.NORTH);

        UiUtils.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillFormFromSelection();
            }
        });
        add(new JScrollPane(table), BorderLayout.CENTER);

        refresh();
        prefillNextId();
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Freelancer details"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;

        addLabeled(form, c, 0, "Freelancer ID:", idField);
        addLabeled(form, c, 1, "Name:", nameField);
        addLabeled(form, c, 2, "Email:", emailField);
        addLabeled(form, c, 3, "Avg rating (0-5):", ratingField);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(form, BorderLayout.CENTER);
        wrapper.add(buildButtons(), BorderLayout.SOUTH);
        return wrapper;
    }

    private void addLabeled(JPanel form, GridBagConstraints c, int row, String label, JTextField field) {
        c.gridx = 0;
        c.gridy = row;
        form.add(new JLabel(label), c);
        c.gridx = 1;
        form.add(field, c);
    }

    private JPanel buildButtons() {
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton add = new JButton("Add");
        JButton update = new JButton("Update");
        JButton delete = new JButton("Delete");
        JButton clear = new JButton("Clear");
        JButton refresh = new JButton("Refresh");

        add.addActionListener(e -> onAdd());
        update.addActionListener(e -> onUpdate());
        delete.addActionListener(e -> onDelete());
        clear.addActionListener(e -> clearForm());
        refresh.addActionListener(e -> refresh());

        buttons.add(add);
        buttons.add(update);
        buttons.add(delete);
        buttons.add(clear);
        buttons.add(refresh);
        return buttons;
    }

    private void onAdd() {
        try {
            dao.insert(readForm());
            UiUtils.showInfo(this, "Freelancer added.");
            clearForm();
            refresh();
        } catch (Exception ex) {
            UiUtils.showError(this, "add the freelancer", ex);
        }
    }

    private void onUpdate() {
        try {
            dao.update(readForm());
            UiUtils.showInfo(this, "Freelancer updated. (Ratings above 5 are capped to 5 by a trigger.)");
            refresh();
        } catch (Exception ex) {
            UiUtils.showError(this, "update the freelancer", ex);
        }
    }

    private void onDelete() {
        try {
            int id = UiUtils.parseInt(idField.getText(), "Freelancer ID");
            if (!UiUtils.confirmDelete(this, "freelancer " + id)) {
                return;
            }
            dao.delete(id);
            UiUtils.showInfo(this, "Freelancer deleted.");
            clearForm();
            refresh();
        } catch (Exception ex) {
            UiUtils.showError(this, "delete the freelancer", ex);
        }
    }

    private Freelancer readForm() {
        int id = UiUtils.parseInt(idField.getText(), "Freelancer ID");
        String name = UiUtils.requireText(nameField.getText(), "Name");
        String email = UiUtils.requireText(emailField.getText(), "Email");
        BigDecimal rating = UiUtils.parseDecimal(ratingField.getText(), "Avg rating");
        if (rating.compareTo(BigDecimal.ZERO) < 0 || rating.compareTo(new BigDecimal("5")) > 0) {
            throw new IllegalArgumentException("Avg rating must be between 0 and 5.");
        }
        return new Freelancer(id, name, email, rating);
    }

    private void clearForm() {
        nameField.setText("");
        emailField.setText("");
        ratingField.setText("");
        table.clearSelection();
        prefillNextId();
    }

    /** Pre-fills the primary-key box with the next free id (MAX + 1). */
    private void prefillNextId() {
        try {
            idField.setText(String.valueOf(dao.nextId()));
        } catch (Exception ex) {
            UiUtils.showError(this, "compute the next freelancer id", ex);
        }
    }

    private void fillFormFromSelection() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        idField.setText(String.valueOf(tableModel.getValueAt(row, 0)));
        nameField.setText(String.valueOf(tableModel.getValueAt(row, 1)));
        emailField.setText(String.valueOf(tableModel.getValueAt(row, 2)));
        ratingField.setText(String.valueOf(tableModel.getValueAt(row, 3)));
    }

    private void refresh() {
        try {
            tableModel.setRowCount(0);
            List<Freelancer> list = dao.findAll();
            for (Freelancer f : list) {
                tableModel.addRow(new Object[]{
                        f.getFreelancerId(), f.getName(), f.getEmail(), f.getAvgRating()});
            }
        } catch (Exception ex) {
            UiUtils.showError(this, "load freelancers", ex);
        }
    }
}
