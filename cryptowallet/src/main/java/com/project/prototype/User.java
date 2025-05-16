package com.project.prototype;
import com.project.core.*;

// User class representing all users of our system.
public class User {

    private int userID;

    UserHandler handler = UserHandler.getInstance();

    // Constructor for the User class.
    public User(int userID) {  
        this.userID = userID;  
    }

    // Getter for the userID attribute.
    public int getUserID() {
        return userID;
    }
}