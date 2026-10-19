package com.freelancer.model;

import java.math.BigDecimal;

/** Row of the Freelancers table. */
public class Freelancer {
    private int freelancerId;
    private String name;
    private String email;
    private BigDecimal avgRating;

    public Freelancer() {
    }

    public Freelancer(int freelancerId, String name, String email, BigDecimal avgRating) {
        this.freelancerId = freelancerId;
        this.name = name;
        this.email = email;
        this.avgRating = avgRating;
    }

    public int getFreelancerId() {
        return freelancerId;
    }

    public void setFreelancerId(int freelancerId) {
        this.freelancerId = freelancerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public BigDecimal getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(BigDecimal avgRating) {
        this.avgRating = avgRating;
    }
}
