package com.project.core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import com.project.prototype.*;

// A singleton class that provides user authentication and database access functionality.
public class UserHandler {
    
    private static UserHandler instance = null;
    DatabaseHandler db = DatabaseHandler.getInstance();

    private UserHandler() {
    }

    // Returns an instance of the UserHandler class.
    public static UserHandler getInstance() {
        if (instance == null) {
            instance = new UserHandler();
        }
        return instance;
    }

    // Validates a customer's password.
    public boolean validateCustomerPassword(int customerID, String customerPass) {
        if (db.checkCustomerPassword(customerID, customerPass)) {
            System.out.println("test: Customer Password Validated");
            return true;
        } else {
            System.out.println("test: Customer Password Incorrect");
            return false;
        }
    }

    // Validates a customer's PIN login attempt.
    public boolean validatePinLoginAttempt(String pin) {
        System.out.println("Pin value: " + pin);

        DatabaseHandler dh = DatabaseHandler.getInstance();
        if (dh.checkCustomerPin(getLocalID(), pin)) {
            System.out.println("Pin login success.");
            return true;
        } else {
            System.out.println("Pin login failed.");
            return false;
        }
    }

    /* Returns the customer ID stored in a local file for this prototype.
       Future integrations will obviously not have customer ID in a local file.
    */
    public int getLocalID() {

        String fileName = "";
        String customerID = null;
        int custID = 0;
        try {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            customerID = bufferedReader.readLine();
            bufferedReader.close();
            System.out.println("Successfully read customerID from file " + fileName);
            custID = Integer.parseInt(customerID);

        } catch (IOException e) {
            System.out.println("An error occurred while reading from file " + fileName);
            e.printStackTrace();
        }
        System.out.println("The value of customerID is: " + custID);
        return custID;
    }
}