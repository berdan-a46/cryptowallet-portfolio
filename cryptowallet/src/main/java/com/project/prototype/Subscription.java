package com.project.prototype;


// Note: this enumeration is not being utilised fully and will be implemented in the second version of our prototype (post-presentation).
// Abstract class and serves as a base class for different types of subscriptions.
public abstract class Subscription {
    
    private String subscriptionName;

    // Instantiates different types of subscriptions and sets the name.
    public Subscription(String subscriptionName) {
        this.subscriptionName = subscriptionName;
    }

    // Returns the name of the subscription plan.
    public String getSubscriptionName() {
        return subscriptionName;
    }
}
