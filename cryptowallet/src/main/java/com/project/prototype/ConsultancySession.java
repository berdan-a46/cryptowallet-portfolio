package com.project.prototype;

import java.util.Calendar;

// Note: this class is not being utilised fully and will be implemented in the second version of our prototype (post-presentation).
// ConsultancySession class to keep track of consultancy sessions that our paid-subscription customers book.
public class ConsultancySession {
    private int sessionID;
    private int consultantID;
    private String consultantName;
    private int customerID;
    private Calendar bookingTimeStamp;
    private Calendar sessionDateTime;
    private String meetingURL;

    // ConsultancySession constructor.
    public ConsultancySession(int consultantID, String consultantName, int customerID, Calendar bookingTimeStamp, Calendar sessionDateTime, String meetingURL) {
        this.consultantID = consultantID;
        this.consultantName = consultantName;
        this.customerID = customerID;
        this.bookingTimeStamp = bookingTimeStamp;
        this.sessionDateTime = sessionDateTime;
        this.meetingURL = meetingURL;
    }

    // set the session ID for the consultancy session our customer books.
    public void setSessionID(int sessionID) {
        this.sessionID=sessionID;
    }

    // get the session ID from the consultancy session instance.
    public int getsessionID() {
        return sessionID;
    }

    // get the ID of the consultant carrying out the consultancy service.
    public int getConsultantID() {
        return consultantID;
    }
    
    // get the name of the consultant carrying out the consultancy service.
    public String getConsultantName() {
        return consultantName;
    }

    // get the ID of the customer requesting a consultant's service.
    public int getCustomerID() {
        return customerID;
    }

    // get the timestamp of the time that our customer made a booking.
    public Calendar getBookingTimestamp() {
        return bookingTimeStamp;
    }

    // get the date and time of the consultancy session.
    public Calendar getSessionDateTime() {
        return sessionDateTime;
    }

    // set the meeting URL to the consultancy session (meeting takes place externally).
    public void setMeetingURL(String meetingURL) {
        this.meetingURL = meetingURL;
    }

    // set the meeting URL to the consultancy session.    
    public String getMeetingURL() {
        if (meetingURL != null) {
            return meetingURL;
        }
        return "Your consultant will provide an external meeting URL soon.";
    }
}
