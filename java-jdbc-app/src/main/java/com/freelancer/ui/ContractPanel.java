package com.freelancer.ui;

import com.freelancer.dao.ContractDAO;
import com.freelancer.dao.FreelancerDAO;
import com.freelancer.dao.JobDAO;
import com.freelancer.model.Contract;
import com.freelancer.model.Freelancer;
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

/** CRUD tab for the Contracts table, plus Mark Completed / Cancel actions. */
public class ContractPanel extends JPanel {

    private final ContractDAO dao = new ContractDAO();
    private final JobDAO jobDao = new JobDAO();
    private final FreelancerDAO freelancerDao = new FreelancerDAO();

    private final JTextField idField = new JTextField(10);
    private final JComboBox<ComboItem> jobBox = new JComboBox<>();
    private final JComboBox<ComboItem> freelancerBox = new JComboBox<>();
    private final JTextField amountField = new JTextField(12);
    private final JComboBox<String> statusBox =
            new JComboBox<>(new String[]{"In Progress", "Completed", "Cancelled"});

    private final DefaultTableModel tableModel = UiUtils.readOnlyModel(
            new String[]{"contract_id", "job_id", "freelancer_id", "final_amount", "status"});
    private final JTable table = new JTable(tableModel);

    /** Runs after a status change so the Jobs tab can refresh. */
    private Runnable onWorkflowChange = () -> { };

    public ContractPanel() {
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

    public void setOnWorkflowChange(Runnable callback) {
        this.onWorkflowChange = (callback == null) ? () -> { } : callback;
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder(
                "Contract details (job is UNIQUE — one contract per job)"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;

        addLabeled(form, c, 0, "Contract ID:", idField);
        addLabeled(form, c, 1, "Job:", jobBox);
        addLabeled(form, c, 2, "Freelancer:", freelancerBox);
        addLabeled(form, c, 3, "Final amount:", amountField);
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
        JButton markCompleted = new JButton("Mark Completed");
        JButton cancel = new JButton("Cancel");
        JButton clear = new JButton("Clear");
        JButton refresh = new JButton("Refresh");

        add.addActionListener(e -> onAdd());
        update.addActionListener(e -> onUpdate());
        delete.addActionListener(e -> onDelete());
        markCompleted.addActionListener(e -> onSetStatus("Completed"));
        cancel.addActionListener(e -> onSetStatus("Cancelled"));
        clear.addActionListener(e -> clearForm());
        refresh.addActionListener(e -> reload());

        buttons.add(add);
        buttons.add(update);
        buttons.add(delete);
        buttons.add(markCompleted);
        buttons.add(cancel);
        buttons.add(clear);
        buttons.add(refresh);
        return buttons;
    }

    private void onAdd() {
        try {
            dao.insert(readForm());
            UiUtils.showInfo(this, "Contract added. (The job's status is set to 'Awarded' by a trigger.)");
            reload();
            clearForm();
        } catch (Exception ex) {
            UiUtils.showError(this, "add the contract", ex);
        }
    }

    private void onUpdate() {
        try {
            dao.update(readForm());
            UiUtils.showInfo(this, "Contract updated.");
            reload();
            onWorkflowChange.run();
        } catch (Exception ex) {
            UiUtils.showError(this, "update the contract", ex);
        }
    }

    private void onDelete() {
        try {
            int id = UiUtils.parseInt(idField.getText(), "Contract ID");
            if (!UiUtils.confirmDelete(this, "contract " + id)) {
                return;
            }
            dao.delete(id);
            UiUtils.showInfo(this, "Contract deleted. (The job's status is reset to 'Open' by a trigger.)");
            reload();
            clearForm();
            onWorkflowChange.run();
        } catch (Exception ex) {
            UiUtils.showError(this, "delete the contract", ex);
        }
    }

    private void onSetStatus(String newStatus) {
        try {
            int id = UiUtils.parseInt(idField.getText(), "Contract ID");
            dao.updateStatus(id, newStatus);
            statusBox.setSelectedItem(newStatus);
            UiUtils.showInfo(this, "Contract " + id + " marked " + newStatus
                    + ". (The job's status is updated by a trigger.)");
            reload();
            onWorkflowChange.run();   // refresh the Jobs tab
        } catch (Exception ex) {
            UiUtils.showError(this, "update the contract status", ex);
        }
    }

    private Contract readForm() {
        int id = UiUtils.parseInt(idField.getText(), "Contract ID");
        int jobId = UiUtils.selectedId(jobBox, "Job");
        int freelancerId = UiUtils.selectedId(freelancerBox, "Freelancer");
        BigDecimal amount = UiUtils.parseDecimal(amountField.getText(), "Final amount");
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Final amount must be greater than 0.");
        }
        String status = UiUtils.requireText(String.valueOf(statusBox.getSelectedItem()), "Status");
        return new Contract(id, jobId, freelancerId, amount, status);
    }

    private void clearForm() {
        amountField.setText("");
        statusBox.setSelectedItem("In Progress");
        if (jobBox.getItemCount() > 0) {
            jobBox.setSelectedIndex(0);
        }
        if (freelancerBox.getItemCount() > 0) {
            freelancerBox.setSelectedIndex(0);
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
        UiUtils.selectId(jobBox, Integer.parseInt(String.valueOf(tableModel.getValueAt(row, 1))));
        UiUtils.selectId(freelancerBox, Integer.parseInt(String.valueOf(tableModel.getValueAt(row, 2))));
        amountField.setText(String.valueOf(tableModel.getValueAt(row, 3)));
        statusBox.setSelectedItem(String.valueOf(tableModel.getValueAt(row, 4)));
    }

    /** Public entry point so other tabs can refresh this one after a workflow action. */
    public void reload() {
        populateDropdowns();
        refreshTable();
        prefillNextId();
    }

    private void populateDropdowns() {
        try {
            ComboItem prevJob = (ComboItem) jobBox.getSelectedItem();
            ComboItem prevFreelancer = (ComboItem) freelancerBox.getSelectedItem();

            jobBox.removeAllItems();
            for (Job j : jobDao.findAll()) {
                jobBox.addItem(new ComboItem(j.getJobId(), j.getJobId() + " — " + j.getTitle()));
            }
            freelancerBox.removeAllItems();
            for (Freelancer f : freelancerDao.findAll()) {
                freelancerBox.addItem(new ComboItem(f.getFreelancerId(), f.getFreelancerId() + " — " + f.getName()));
            }

            if (prevJob != null) {
                UiUtils.selectId(jobBox, prevJob.getId());
            }
            if (prevFreelancer != null) {
                UiUtils.selectId(freelancerBox, prevFreelancer.getId());
            }
        } catch (Exception ex) {
            UiUtils.showError(this, "load jobs/freelancers", ex);
        }
    }

    private void prefillNextId() {
        try {
            idField.setText(String.valueOf(dao.nextId()));
        } catch (Exception ex) {
            UiUtils.showError(this, "compute the next contract id", ex);
        }
    }

    private void refreshTable() {
        try {
            tableModel.setRowCount(0);
            List<Contract> list = dao.findAll();
            for (Contract ct : list) {
                tableModel.addRow(new Object[]{
                        ct.getContractId(), ct.getJobId(), ct.getFreelancerId(),
                        ct.getFinalAmount(), ct.getStatus()});
            }
        } catch (Exception ex) {
            UiUtils.showError(this, "load contracts", ex);
        }
    }
}
