package com.freelancer.model;

import java.math.BigDecimal;

/** Row of the Jobs table. */
public class Job {
    private int jobId;
    private int clientId;
    private String title;
    private BigDecimal budgetMax;
    private String status;

    public Job() {
    }

    public Job(int jobId, int clientId, String title, BigDecimal budgetMax, String status) {
        this.jobId = jobId;
        this.clientId = clientId;
        this.title = title;
        this.budgetMax = budgetMax;
        this.status = status;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getBudgetMax() {
        return budgetMax;
    }

    public void setBudgetMax(BigDecimal budgetMax) {
        this.budgetMax = budgetMax;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
