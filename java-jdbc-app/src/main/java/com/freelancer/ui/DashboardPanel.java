package com.freelancer.ui;

import com.freelancer.dao.QueryResult;
import com.freelancer.dao.ReportDAO;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.math.BigDecimal;
import java.util.List;

/**
 * Read-only reporting tab. Each button runs one query/procedure/function via
 * {@link ReportDAO} and shows the result in the shared JTable below.
 */
public class DashboardPanel extends JPanel {

    private final ReportDAO dao = new ReportDAO();

    private final DefaultTableModel resultModel = UiUtils.readOnlyModel(new String[]{"(run a report)"});
    private final JTable resultTable = new JTable(resultModel);
    private final JLabel statusLabel = new JLabel("Choose a report above.");

    private final JComboBox<String> statusBox =
            new JComboBox<>(new String[]{"Open", "Awarded", "Completed", "Cancelled"});
    private final JComboBox<Integer> jobIdBox = new JComboBox<>();

    public DashboardPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(buildControls(), BorderLayout.NORTH);

        UiUtils.styleTable(resultTable);
        add(new JScrollPane(resultTable), BorderLayout.CENTER);

        statusLabel.setBorder(BorderFactory.createEmptyBorder(6, 2, 2, 2));
        add(statusLabel, BorderLayout.SOUTH);

        populateJobIds();
    }

    private JPanel buildControls() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        // Row 1: the three plain reports.
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row1.setBorder(BorderFactory.createTitledBorder("Reports"));
        JButton openJobs = new JButton("Open jobs, budget >= 70000");
        JButton activeFreelancers = new JButton("Freelancers with >= 1 offer");
        JButton completed = new JButton("Completed contracts per freelancer");
        openJobs.addActionListener(e -> runOpenHighBudget());
        activeFreelancers.addActionListener(e -> runFreelancersWithOffers());
        completed.addActionListener(e -> runCompletedSummary());
        row1.add(openJobs);
        row1.add(activeFreelancers);
        row1.add(completed);

        // Row 2: stored procedure.
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row2.setBorder(BorderFactory.createTitledBorder("Stored procedure: GetJobsByStatus (CallableStatement)"));
        JButton callProc = new JButton("Call GetJobsByStatus");
        callProc.addActionListener(e -> runJobsByStatus());
        row2.add(new JLabel("Status:"));
        row2.add(statusBox);
        row2.add(callProc);

        // Row 3: stored functions.
        JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row3.setBorder(BorderFactory.createTitledBorder("Stored functions (SELECT)"));
        JButton countOffers = new JButton("CountOffers(job_id)");
        countOffers.addActionListener(e -> runCountOffers());
        JButton avgContract = new JButton("AverageContractAmount()");
        avgContract.addActionListener(e -> runAverageContract());
        row3.add(new JLabel("Job ID:"));
        row3.add(jobIdBox);
        row3.add(countOffers);
        row3.add(avgContract);

        container.add(row1);
        container.add(row2);
        container.add(row3);
        return container;
    }

    private void populateJobIds() {
        try {
            jobIdBox.removeAllItems();
            List<Integer> ids = dao.allJobIds();
            for (Integer id : ids) {
                jobIdBox.addItem(id);
            }
        } catch (Exception ex) {
            UiUtils.showError(this, "load job ids", ex);
        }
    }

    // ----- report actions -----

    private void runOpenHighBudget() {
        try {
            display(dao.openHighBudgetJobs(), "Open jobs with budget >= 70000");
        } catch (Exception ex) {
            UiUtils.showError(this, "run the report", ex);
        }
    }

    private void runFreelancersWithOffers() {
        try {
            display(dao.freelancersWithOffers(), "Freelancers who submitted at least one offer");
        } catch (Exception ex) {
            UiUtils.showError(this, "run the report", ex);
        }
    }

    private void runCompletedSummary() {
        try {
            display(dao.completedContractsSummary(), "Completed contracts summary per freelancer");
        } catch (Exception ex) {
            UiUtils.showError(this, "run the report", ex);
        }
    }

    private void runJobsByStatus() {
        try {
            String status = String.valueOf(statusBox.getSelectedItem());
            if (status == null || status.isBlank()) {
                throw new IllegalArgumentException("Please choose a status.");
            }
            display(dao.jobsByStatus(status.trim()), "GetJobsByStatus('" + status.trim() + "')");
        } catch (Exception ex) {
            UiUtils.showError(this, "call the procedure", ex);
        }
    }

    private void runCountOffers() {
        try {
            Object selected = jobIdBox.getSelectedItem();
            if (selected == null) {
                throw new IllegalArgumentException("Please choose a job id.");
            }
            int jobId = (Integer) selected;
            int count = dao.countOffers(jobId);
            showSingleValue("CountOffers(" + jobId + ")", count);
        } catch (Exception ex) {
            UiUtils.showError(this, "call the function", ex);
        }
    }

    private void runAverageContract() {
        try {
            BigDecimal avg = dao.averageContractAmount();
            showSingleValue("AverageContractAmount()", avg);
        } catch (Exception ex) {
            UiUtils.showError(this, "call the function", ex);
        }
    }

    // ----- rendering helpers -----

    private void display(QueryResult result, String title) {
        resultModel.setColumnIdentifiers(result.getColumns());
        resultModel.setRowCount(0);
        for (Object[] row : result.getRows()) {
            resultModel.addRow(row);
        }
        statusLabel.setText(title + "  —  " + result.getRowCount() + " row(s).");
    }

    private void showSingleValue(String label, Object value) {
        resultModel.setColumnIdentifiers(new String[]{"result"});
        resultModel.setRowCount(0);
        resultModel.addRow(new Object[]{value});
        statusLabel.setText(label + "  —  " + (value == null ? "no data" : value.toString()));
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.width = Math.max(d.width, 820);
        return d;
    }

    // Keeps the status row left-aligned within BoxLayout wrappers if reused.
    @SuppressWarnings("unused")
    private static void leftAlign(Component comp) {
        if (comp instanceof JPanel panel) {
            panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        }
    }
}
