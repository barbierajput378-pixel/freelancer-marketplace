package com.freelancer.model;

import java.math.BigDecimal;

/** Row of the Contracts table. */
public class Contract {
    private int contractId;
    private int jobId;
    private int freelancerId;
    private BigDecimal finalAmount;
    private String status;

    public Contract() {
    }

    public Contract(int contractId, int jobId, int freelancerId, BigDecimal finalAmount, String status) {
        this.contractId = contractId;
        this.jobId = jobId;
        this.freelancerId = freelancerId;
        this.finalAmount = finalAmount;
        this.status = status;
    }

    public int getContractId() {
        return contractId;
    }

    public void setContractId(int contractId) {
        this.contractId = contractId;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public int getFreelancerId() {
        return freelancerId;
    }

    public void setFreelancerId(int freelancerId) {
        this.freelancerId = freelancerId;
    }

    public BigDecimal getFinalAmount() {
        return finalAmount;
    }

    public void setFinalAmount(BigDecimal finalAmount) {
        this.finalAmount = finalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
