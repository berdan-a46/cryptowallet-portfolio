package com.project.prototype;

/* Note: This class is not being utilised fully and will be implemented in the second version of our prototype (post-presentation).
   This is a specific type of subscription so it extends Subscription.
   Paid users will hold a reference to a paid subscription.
*/
public class PaidSubscription extends Subscription {

    // The constructor for PaidSubscription passes a String for the type of subscription being created
    public PaidSubscription() {
        super("Paid");
    }

    public void viewDetails() {
        // Implement viewDetails
    }

    public void cancelSubscription() {
        // Implement cancelSubscription
    } 
}
