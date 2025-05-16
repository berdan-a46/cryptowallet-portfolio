package com.project.core;

import java.util.ArrayList;
import com.project.prototype.*;


public class Customer extends User {

	private Subscription subscriptionType;
	private String pin;
	private String password;
	private Wallet wallet;
	private ArrayList<CoinPriceAlert> alerts;
	private ArrayList<ConsultancySession> sessions = new ArrayList<>();

	private DatabaseHandler dh = DatabaseHandler.getInstance();

	// Customer constructor
	public Customer(int customerID, String pin, String password, Wallet wallet, ArrayList<ConsultancySession> sessions) { 
		super(customerID);
		this.pin = pin;
		this.password = password;
		this.wallet = wallet;
		this.sessions = sessions;
	}

	// returns the customer's ID.
	public int getCustomerID() {
		return this.getUserID();
	}

	// books a consultancy session for the customer.
	public void bookConsultancySession(ConsultancySession cs) {
		sessions.add(cs);
	}

	// cancels a consultancy session for the customer.
	public void cancelConsultancySession(int sessionID) {
		for (int i=0; i<sessions.size(); i++) {
			if (sessions.get(i).getsessionID() == sessionID) {
				sessions.remove(sessions.get(i));
				return;
			}
		}
	}

	// signs up the customer to a paid subscription.
	public void signUpToPaid() {
		if (subscriptionType.getSubscriptionName().equals("Free")) {
			System.out.println("You want to upgrade to our Paid subscription.");
			System.out.println("We'll take you to our partners site to fill out more details.");
			try {
				new Thread();
				Thread.currentThread();
				Thread.sleep(4000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("Thank you for completing our signup form and paying for our services.");
			subscriptionType = new PaidSubscription();
		} else {
			System.out.println("You are already on paid subscription");
		}
	}

	// creates a coin price alert for the customer.
	public void createAlert(float level, Cryptocurrency crypto) {
		// TODO - implement Customer.createAlert
		throw new UnsupportedOperationException();
	}

	// cancels a coin price alert for the customer.
	public void cancelAlert(CoinPriceAlert alert) {
		alerts.remove(alert);
	}

	// changes the customer's password to a new one.
	public void changePassword(String newPassword) {
	// TODO - implement Customer.changePassword
	throw new UnsupportedOperationException();
	}
	
	// returns the customer's wallet object.
	public Wallet getWallet() {
	return this.wallet;
	}
	
	// returns the list of coin price alerts for the customer.
	public ArrayList<CoinPriceAlert> getAlerts() {
	return this.alerts;
	}
	
	// Returns the customer's PIN.
	public String getPin() {
	return this.pin;
	}

	// returns the customer's password.
	public String getPassword() {
	return this.password;
	}
	
	// returns the customer's subscription type.
	public Subscription getSubscription() {
	return subscriptionType;
	}
	
	// returns the list of private keys for the customer.
	public ArrayList<String> getPrivateKeys(String password) {
	// TODO - implement Customer.getPrivateKeys
	throw new UnsupportedOperationException();
	}
	
	// sets the customer's PIN.
	public void setPin(String pin) {
	this.pin = pin;
	}
	
	// sets the customer's password.
	public void setPassword(String password) {
	this.password = password;
	}
	
	// returns the list of consultancy sessions for the customer.
	public ArrayList<ConsultancySession> getConsultancySessions() {
	return sessions;
	}
}