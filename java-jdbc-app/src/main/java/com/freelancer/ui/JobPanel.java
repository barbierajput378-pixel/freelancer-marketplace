package com.freelancer.ui;

import com.freelancer.dao.ClientDAO;
import com.freelancer.dao.JobDAO;
import com.freelancer.model.Client;
import com.freelancer.model.Job;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.util.List;

/** CRUD tab for the Jobs table. */
public class JobPanel extends JPanel {

    private final JobDAO dao = new JobDAO();
    private final ClientDAO clientDao = new ClientDAO();

    private final JTextField idField = new JTextField(10);
    private final JComboBox<ComboItem> clientBox = new JComboBox<>();
    private final JTextField titleField = new JTextField(20);
    private final JTextField budgetField = new JTextField(12);
    private final JComboBox<String> statusBox =
            new JComboBox<>(new String[]{"Open", "Awarded", "Completed", "Cancelled"});

    private final DefaultTableModel tableModel =
            UiUtils.readOnlyModel(new String[]{"job_id", "client_id", "title", "budget_max", "status"});
    private final JTable table = new JTable(tableModel);

    public JobPanel() {
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

        reload();
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Job details (note: the title is uppercased by a trigger)"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;

        addLabeled(form, c, 0, "Job ID:", idField);
        addLabeled(form, c, 1, "Client:", clientBox);
        addLabeled(form, c, 2, "Title:", titleField);
        addLabeled(form, c, 3, "Budget max:", budgetField);
        addLabeled(form, c, 4, "Status:", statusBox);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(form, BorderLayout.CENTER);
        wrapper.add(buildButtons(), BorderLayout.SOUTH);
        return wrapper;
    }

    private void addLabeled(JPanel form, GridBagConstraints c, int row, String label, Component field) {
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
        refresh.addActionListener(e -> reload());

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
            UiUtils.showInfo(this, "Job added.");
            reload();
            clearForm();
        } catch (Exception ex) {
            UiUtils.showError(this, "add the job", ex);
        }
    }

    private void onUpdate() {
        try {
            dao.update(readForm());
            UiUtils.showInfo(this, "Job updated.");
            reload();
        } catch (Exception ex) {
            UiUtils.showError(this, "update the job", ex);
        }
    }

    private void onDelete() {
        try {
            int id = UiUtils.parseInt(idField.getText(), "Job ID");
            if (!UiUtils.confirmDelete(this, "job " + id)) {
                return;
            }
            dao.delete(id);
            UiUtils.showInfo(this, "Job deleted.");
            reload();
            clearForm();
        } catch (Exception ex) {
            UiUtils.showError(this, "delete the job", ex);
        }
    }

    private Job readForm() {
        int id = UiUtils.parseInt(idField.getText(), "Job ID");
        int clientId = UiUtils.selectedId(clientBox, "Client");
        String title = UiUtils.requireText(titleField.getText(), "Title");
        BigDecimal budget = UiUtils.parseDecimal(budgetField.getText(), "Budget max");
        if (budget.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Budget max must be greater than 0.");
        }
        String status = UiUtils.requireText(String.valueOf(statusBox.getSelectedItem()), "Status");
        return new Job(id, clientId, title, budget, status);
    }

    private void clearForm() {
        titleField.setText("");
        budgetField.setText("");
        statusBox.setSelectedItem("Open");
        if (clientBox.getItemCount() > 0) {
            clientBox.setSelectedIndex(0);
        }
        table.clearSelection();
        prefillNextId();
    }

    private void fillFormFromSelection() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        idField.setText(String.valueOf(tableModel.getValueAt(row, 0)));
        UiUtils.selectId(clientBox, Integer.parseInt(String.valueOf(tableModel.getValueAt(row, 1))));
        titleField.setText(String.valueOf(tableModel.getValueAt(row, 2)));
        budgetField.setText(String.valueOf(tableModel.getValueAt(row, 3)));
        statusBox.setSelectedItem(String.valueOf(tableModel.getValueAt(row, 4)));
    }

    /** Public entry point so other tabs can refresh this one after a workflow action. */
    public void reload() {
        populateClients();
        refreshTable();
        prefillNextId();
    }

    private void populateClients() {
        try {
            ComboItem previous = (ComboItem) clientBox.getSelectedItem();
            clientBox.removeAllItems();
            List<Client> clients = clientDao.findAll();
            for (Client cl : clients) {
                clientBox.addItem(new ComboItem(cl.getClientId(), cl.getClientId() + " — " + cl.getName()));
            }
            if (previous != null) {
                UiUtils.selectId(clientBox, previous.getId());
            }
        } catch (Exception ex) {
            UiUtils.showError(this, "load clients", ex);
        }
    }

    private void prefillNextId() {
        try {
            idField.setText(String.valueOf(dao.nextId()));
        } catch (Exception ex) {
            UiUtils.showError(this, "compute the next job id", ex);
        }
    }

    private void refreshTable() {
        try {
            tableModel.setRowCount(0);
            List<Job> list = dao.findAll();
            for (Job j : list) {
                tableModel.addRow(new Object[]{
                        j.getJobId(), j.getClientId(), j.getTitle(), j.getBudgetMax(), j.getStatus()});
            }
        } catch (Exception ex) {
            UiUtils.showError(this, "load jobs", ex);
        }
    }
}
