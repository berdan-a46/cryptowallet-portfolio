package com.project.core;

import java.util.*;
import com.project.prototype.*;

// The Wallet class represents a digital wallet that holds a balance and various cryptocurrencies in compartments.
// It also tracks transactions that occur within the wallet.
class Wallet
{
  private float balance;
  private String seed;
  private ArrayList<Transaction> transactions = new ArrayList<Transaction>();
  private ArrayList<Compartment> compartments = new ArrayList<Compartment>();
  private ArrayList<Cryptocurrency> cryptos = new ArrayList<Cryptocurrency>();
  
  // Constructor for the Wallet class that sets the initial balance and seed, and initialises the cryptocurrencies and transactions.
  public Wallet(String seed, float balance, ArrayList<Cryptocurrency> cryptos, ArrayList<Transaction> transactions) {
    this.seed = seed;
    this.balance = balance;
    initialiseCryptos(cryptos);
    initialiseTransactions(transactions);
  }

  /* Sets the initial cryptocurrencies for the wallet.
     This method may be extended in future versions for functionalities 
     such as sorting, automatic compartment assignment, etc.
  */
  public void initialiseCryptos(ArrayList<Cryptocurrency> cryptos)
  {
    this.cryptos = cryptos;
  }

  /* Sets the initial transactions for the wallet.
     This method may be extended in future versions for functionalities 
     such as sorting, filtering, analytics, etc.
  */
  public void initialiseTransactions(ArrayList<Transaction> transactions)
  {
    this.transactions = transactions;
  }
  
  // Gets the seed for the wallet.
  public String getSeed() {
    return this.seed;
  }
  
  // Gets the transactions for the wallet.
  public ArrayList<Transaction> getTransactions() {
    return this.transactions;
  }
  
  // Gets the compartments for the wallet.
  public ArrayList<Compartment> getCompartments() {
    return this.compartments;
  }
  
  // Gets the cryptocurrencies for the wallet.
  public ArrayList<Cryptocurrency> getCryptocurrencies() {
    return this.cryptos;
  }
  
  // Gets the current balance for the wallet.
  public float getBalance() {
    return this.balance;
  }
  
  // Sets the current balance for the wallet.
  public void setBalance(float balance) {
    this.balance = balance;
  }
  
  // Adds a new transaction to the wallet's list of transactions.
  public void addTransaction(Transaction transaction) {
    transactions.add(transaction);
  }
  
  // Creates a new compartment within the wallet.
  public void createCompartments() {
  }
  
  // Moves a specified cryptocurrency into a specified compartment within the wallet.
  public void moveCryptoIntoCompartment(Cryptocurrency crypto, Compartment comp) {
  }
  
  // Deletes a specified compartment from the wallet.
  public void deleteCompartment(Compartment comp) {
  }
}
