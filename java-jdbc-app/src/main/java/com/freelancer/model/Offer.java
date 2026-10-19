package com.freelancer.model;

import java.math.BigDecimal;

/** Row of the Offers table. */
public class Offer {
    private int offerId;
    private int jobId;
    private int freelancerId;
    private BigDecimal offerAmount;
    private int proposedDuration;
    private String status;

    public Offer() {
    }

    public Offer(int offerId, int jobId, int freelancerId, BigDecimal offerAmount,
                 int proposedDuration, String status) {
        this.offerId = offerId;
        this.jobId = jobId;
        this.freelancerId = freelancerId;
        this.offerAmount = offerAmount;
        this.proposedDuration = proposedDuration;
        this.status = status;
    }

    public int getOfferId() {
        return offerId;
    }

    public void setOfferId(int offerId) {
        this.offerId = offerId;
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

    public BigDecimal getOfferAmount() {
        return offerAmount;
    }

    public void setOfferAmount(BigDecimal offerAmount) {
        this.offerAmount = offerAmount;
    }

    public int getProposedDuration() {
        return proposedDuration;
    }

    public void setProposedDuration(int proposedDuration) {
        this.proposedDuration = proposedDuration;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
