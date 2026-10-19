package com.freelancer.ui;

import com.freelancer.dao.FreelancerDAO;
import com.freelancer.dao.JobDAO;
import com.freelancer.dao.OfferDAO;
import com.freelancer.model.Freelancer;
import com.freelancer.model.Job;
import com.freelancer.model.Offer;

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

/** CRUD tab for the Offers table, including the "Award this offer" workflow. */
public class OfferPanel extends JPanel {

    private final OfferDAO dao = new OfferDAO();
    private final JobDAO jobDao = new JobDAO();
    private final FreelancerDAO freelancerDao = new FreelancerDAO();

    private final JTextField idField = new JTextField(10);
    private final JComboBox<ComboItem> jobBox = new JComboBox<>();
    private final JComboBox<ComboItem> freelancerBox = new JComboBox<>();
    private final JTextField amountField = new JTextField(12);
    private final JTextField durationField = new JTextField(8);
    private final JComboBox<String> statusBox =
            new JComboBox<>(new String[]{"Pending", "Selected", "Rejected"});

    private final DefaultTableModel tableModel = UiUtils.readOnlyModel(
            new String[]{"offer_id", "job_id", "freelancer_id", "offer_amount", "proposed_duration", "status"});
    private final JTable table = new JTable(tableModel);

    /** Runs after Award so sibling tabs (Jobs, Contracts) can refresh. */
    private Runnable onWorkflowChange = () -> { };

    public OfferPanel() {
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
        form.setBorder(BorderFactory.createTitledBorder("Offer details"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;

        addLabeled(form, c, 0, "Offer ID:", idField);
        addLabeled(form, c, 1, "Job:", jobBox);
        addLabeled(form, c, 2, "Freelancer:", freelancerBox);
        addLabeled(form, c, 3, "Offer amount:", amountField);
        addLabeled(form, c, 4, "Proposed duration (days):", durationField);
        addLabeled(form, c, 5, "Status:", statusBox);

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
        JButton award = new JButton("Award this offer");
        JButton clear = new JButton("Clear");
        JButton refresh = new JButton("Refresh");

        add.addActionListener(e -> onAdd());
        update.addActionListener(e -> onUpdate());
        delete.addActionListener(e -> onDelete());
        award.addActionListener(e -> onAward());
        clear.addActionListener(e -> clearForm());
        refresh.addActionListener(e -> reload());

        buttons.add(add);
        buttons.add(update);
        buttons.add(delete);
        buttons.add(award);
        buttons.add(clear);
        buttons.add(refresh);
        return buttons;
    }

    private void onAdd() {
        try {
            dao.insert(readForm());
            UiUtils.showInfo(this, "Offer added.");
            reload();
            clearForm();
        } catch (Exception ex) {
            UiUtils.showError(this, "add the offer", ex);
        }
    }

    private void onUpdate() {
        try {
            dao.update(readForm());
            UiUtils.showInfo(this, "Offer updated.");
            reload();
        } catch (Exception ex) {
            UiUtils.showError(this, "update the offer", ex);
        }
    }

    private void onDelete() {
        try {
            int id = UiUtils.parseInt(idField.getText(), "Offer ID");
            if (!UiUtils.confirmDelete(this, "offer " + id)) {
                return;
            }
            dao.delete(id);
            UiUtils.showInfo(this, "Offer deleted.");
            reload();
            clearForm();
        } catch (Exception ex) {
            UiUtils.showError(this, "delete the offer", ex);
        }
    }

    private void onAward() {
        try {
            int id = UiUtils.parseInt(idField.getText(), "Offer ID");
            if (!UiUtils.confirm(this, "Confirm award",
                    "Award offer " + id + "?\n\nThis creates a contract, rejects the other offers "
                            + "on the job, and marks the job Awarded.")) {
                return;
            }
            dao.awardOffer(id);
            UiUtils.showInfo(this, "Offer " + id + " awarded. A contract was created and the job is now Awarded.");
            reload();
            onWorkflowChange.run();   // refresh Jobs and Contracts tabs
        } catch (Exception ex) {
            UiUtils.showError(this, "award the offer", ex);
        }
    }

    private Offer readForm() {
        int id = UiUtils.parseInt(idField.getText(), "Offer ID");
        int jobId = UiUtils.selectedId(jobBox, "Job");
        int freelancerId = UiUtils.selectedId(freelancerBox, "Freelancer");
        BigDecimal amount = UiUtils.parseDecimal(amountField.getText(), "Offer amount");
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Offer amount must be greater than 0.");
        }
        int duration = UiUtils.parseInt(durationField.getText(), "Proposed duration");
        if (duration <= 0) {
            throw new IllegalArgumentException("Proposed duration must be greater than 0.");
        }
        String status = UiUtils.requireText(String.valueOf(statusBox.getSelectedItem()), "Status");
        return new Offer(id, jobId, freelancerId, amount, duration, status);
    }

    private void clearForm() {
        amountField.setText("");
        durationField.setText("");
        statusBox.setSelectedItem("Pending");
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
        durationField.setText(String.valueOf(tableModel.getValueAt(row, 4)));
        statusBox.setSelectedItem(String.valueOf(tableModel.getValueAt(row, 5)));
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
            UiUtils.showError(this, "compute the next offer id", ex);
        }
    }

    private void refreshTable() {
        try {
            tableModel.setRowCount(0);
            List<Offer> list = dao.findAll();
            for (Offer o : list) {
                tableModel.addRow(new Object[]{
                        o.getOfferId(), o.getJobId(), o.getFreelancerId(),
                        o.getOfferAmount(), o.getProposedDuration(), o.getStatus()});
            }
        } catch (Exception ex) {
            UiUtils.showError(this, "load offers", ex);
        }
    }
}
