package com.freelancer.ui;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;
import java.awt.Dimension;

/** Top-level window holding one tab per table plus the Dashboard. */
public class MainFrame extends JFrame {

    public MainFrame() {
        super("Freelancer Marketplace — JDBC Desktop Client");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 600));
        setLocationByPlatform(true);

        ClientPanel clientPanel = new ClientPanel();
        FreelancerPanel freelancerPanel = new FreelancerPanel();
        JobPanel jobPanel = new JobPanel();
        OfferPanel offerPanel = new OfferPanel();
        ContractPanel contractPanel = new ContractPanel();

        // Awarding an offer touches Jobs and Contracts; completing/cancelling a
        // contract touches Jobs — refresh those tabs when it happens.
        offerPanel.setOnWorkflowChange(() -> {
            jobPanel.reload();
            contractPanel.reload();
        });
        contractPanel.setOnWorkflowChange(jobPanel::reload);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Clients", clientPanel);
        tabs.addTab("Freelancers", freelancerPanel);
        tabs.addTab("Jobs", jobPanel);
        tabs.addTab("Offers", offerPanel);
        tabs.addTab("Contracts", contractPanel);
        tabs.addTab("Dashboard / Reports", new DashboardPanel());

        setContentPane(tabs);
        pack();
        setSize(1000, 650);
    }
}
