package com.coworking.memberservice.model;

import jakarta.persistence.*;

@Entity
@Table(name = "members")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    private String email;

    @Enumerated(EnumType.STRING)
    private SubscriptionType subscriptionType;

    private boolean suspended;
    private Integer maxConcurrentBookings;

    public Member() {}

    public Member(Long id, String fullName, String email, SubscriptionType subscriptionType, boolean suspended, Integer maxConcurrentBookings) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.subscriptionType = subscriptionType;
        this.suspended = suspended;
        this.maxConcurrentBookings = maxConcurrentBookings;
    }

    @PrePersist
    @PreUpdate
    private void setMaxBookingsFromSubscription() {
        if (subscriptionType != null) {
            switch (subscriptionType) {
                case BASIC:
                    this.maxConcurrentBookings = 2;
                    break;
                case PRO:
                    this.maxConcurrentBookings = 5;
                    break;
                case ENTERPRISE:
                    this.maxConcurrentBookings = 10;
                    break;
            }
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public SubscriptionType getSubscriptionType() {
        return subscriptionType;
    }

    public void setSubscriptionType(SubscriptionType subscriptionType) {
        this.subscriptionType = subscriptionType;
    }

    public boolean isSuspended() {
        return suspended;
    }

    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }

    public Integer getMaxConcurrentBookings() {
        return maxConcurrentBookings;
    }

    public void setMaxConcurrentBookings(Integer maxConcurrentBookings) {
        this.maxConcurrentBookings = maxConcurrentBookings;
    }
}
