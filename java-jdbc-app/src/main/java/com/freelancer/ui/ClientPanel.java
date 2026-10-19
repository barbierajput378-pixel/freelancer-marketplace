package com.freelancer.ui;

import com.freelancer.dao.ClientDAO;
import com.freelancer.model.Client;

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
import java.util.List;

/** CRUD tab for the Clients table. */
public class ClientPanel extends JPanel {

    private final ClientDAO dao = new ClientDAO();

    private final JTextField idField = new JTextField(10);
    private final JTextField nameField = new JTextField(18);
    private final JTextField emailField = new JTextField(18);
    private final JTextField companyField = new JTextField(18);

    private final DefaultTableModel tableModel =
            UiUtils.readOnlyModel(new String[]{"client_id", "name", "email", "company_name"});
    private final JTable table = new JTable(tableModel);

    public ClientPanel() {
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
        form.setBorder(BorderFactory.createTitledBorder("Client details"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;

        addLabeled(form, c, 0, "Client ID:", idField);
        addLabeled(form, c, 1, "Name:", nameField);
        addLabeled(form, c, 2, "Email:", emailField);
        addLabeled(form, c, 3, "Company:", companyField);

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
            Client client = readForm();
            dao.insert(client);
            UiUtils.showInfo(this, "Client added.");
            clearForm();
            refresh();
        } catch (IllegalArgumentException ex) {
            UiUtils.showError(this, "add the client", ex);
        } catch (Exception ex) {
            UiUtils.showError(this, "add the client", ex);
        }
    }

    private void onUpdate() {
        try {
            Client client = readForm();
            dao.update(client);
            UiUtils.showInfo(this, "Client updated.");
            refresh();
        } catch (Exception ex) {
            UiUtils.showError(this, "update the client", ex);
        }
    }

    private void onDelete() {
        try {
            int id = UiUtils.parseInt(idField.getText(), "Client ID");
            if (!UiUtils.confirmDelete(this, "client " + id)) {
                return;
            }
            dao.delete(id);
            UiUtils.showInfo(this, "Client deleted.");
            clearForm();
            refresh();
        } catch (Exception ex) {
            UiUtils.showError(this, "delete the client", ex);
        }
    }

    private Client readForm() {
        int id = UiUtils.parseInt(idField.getText(), "Client ID");
        String name = UiUtils.requireText(nameField.getText(), "Name");
        String email = UiUtils.requireText(emailField.getText(), "Email");
        String company = UiUtils.requireText(companyField.getText(), "Company");
        return new Client(id, name, email, company);
    }

    private void clearForm() {
        nameField.setText("");
        emailField.setText("");
        companyField.setText("");
        table.clearSelection();
        prefillNextId();
    }

    /** Pre-fills the primary-key box with the next free id (MAX + 1). */
    private void prefillNextId() {
        try {
            idField.setText(String.valueOf(dao.nextId()));
        } catch (Exception ex) {
            UiUtils.showError(this, "compute the next client id", ex);
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
        companyField.setText(String.valueOf(tableModel.getValueAt(row, 3)));
    }

    private void refresh() {
        try {
            tableModel.setRowCount(0);
            List<Client> clients = dao.findAll();
            for (Client client : clients) {
                tableModel.addRow(new Object[]{
                        client.getClientId(), client.getName(),
                        client.getEmail(), client.getCompanyName()});
            }
        } catch (Exception ex) {
            UiUtils.showError(this, "load clients", ex);
        }
    }
}
