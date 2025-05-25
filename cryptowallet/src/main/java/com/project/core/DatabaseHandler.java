package com.project.core;

import java.sql.*;
import java.util.ArrayList;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Date;
import com.project.prototype.*;


/* The DatabaseHandler class contains methods that communicate with the MySQL database to retrieve or update data. 
   The class is a singleton, which ensures that there is only one instance of the class in the entire application. 
   The class has private fields that are accessed by getters for additional class security. 
*/
public class DatabaseHandler {
    // Private fields accessed by getters for additional class security
    private static DatabaseHandler instance = null;
    private static final String dbUrl = "jdbc:mysql://localhost/wallet";
    private static final String user = "root";
    private static final String password = "admin";


    private DatabaseHandler() {
    }

    public static DatabaseHandler getInstance() {
        if (instance == null) {
            instance = new DatabaseHandler();
        }
        return instance;
    }

    private String getDbUrl() {
        return dbUrl;
    }

    private String getUser() {
        return user;
    }

    private String getPassword() {
        return password;
    }



    /* The checkCustomerPassword method takes in a customer ID and password and checks.
       if the password matches the password of the customer with the given ID. 
       It returns a boolean value indicating whether the password is valid or not. 
    */
    public boolean checkCustomerPassword(int customerID, String customerPassword) {
        
        try (
            Connection connection = DriverManager.getConnection(getDbUrl(), getUser(), getPassword()); 
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery("SELECT cPassword FROM Customer WHERE custID=" + customerID); 
        ) { while (results.next()) {
                System.out.println(results.getString("cPassword"));
                if (customerPassword.equals(results.getString("cPassword"))) {
                    statement.close();
                    return true; 
                }
            }
            statement.close();
            connection.close();
        } catch (SQLException except) {
            System.out.println("Connection Failed. Customer security data not retrieved/validated.");
            System.out.println(except.getMessage());
        }
        System.out.println("test: Credentials invalid.");
        return false;
    }

    /* The checkCustomerPin method takes in a customer ID and PIN and checks.
       if the PIN matches the PIN of the customer with the given ID.
       It returns a boolean value indicating whether the PIN is valid or not.
    */
    public boolean checkCustomerPin(int customerID, String pin) {
        try (
            Connection connection = DriverManager.getConnection(getDbUrl(), getUser(), getPassword()); 
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery("SELECT cPin FROM Customer WHERE custID=" + customerID); 
        ) { while (results.next()) {
                pin = pin.replace("\"", "");
                if (pin.equals(results.getString("cPin"))) {
                    System.out.println("test: Credentials Validated");
                    statement.close();
                    return true; 
                }
            }
            statement.close();
            connection.close();
        } catch (SQLException except) {
            System.out.println("Connection Failed. Pin not validated.");
            System.out.println(except.getMessage());
        }
        System.out.println("PIN: "+pin);
        System.out.println("test: Credentials invalidated.");
        return false;
    }

    /* The createCustomer method takes in a local customer ID and retrieves the customer's 
       PIN, password, and wallet ID from the database. It calls the createWallet method to 
       create a wallet object and then creates a customer object using the retrieved data 
       and the created wallet object. It returns the created customer object. 
    */
    public Customer createCustomer(int customerLocalID)
    {
        try (
            Connection connection = DriverManager.getConnection(getDbUrl(), getUser(), getPassword()); 
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery("SELECT * FROM Customer WHERE custID=" + customerLocalID); 
        ) { while (results.next()) {
                String pin = results.getString("cPin");
                String passw = results.getString("cPassword");
                int walletID = results.getInt("walletID");
                Wallet wallet = createWallet(walletID);
                ArrayList<ConsultancySession> booked = getBookedSessions("customer",customerLocalID);
                Customer customer = new Customer(customerLocalID, pin, passw, wallet, booked);
                return customer;
            }
            statement.close();
            connection.close();
        } catch (SQLException except) {
            System.out.println("Connection Failed. Customer not created.");
            System.out.println(except.getMessage());
        }
        return null;
    }
     
    /* The createWallet method takes in a wallet ID and retrieves the wallet's balance, seed, and transaction 
       and cryptocurrency records associated with it from the database. 
       It creates a wallet object using the retrieved data and returns the created wallet object.
    */
       public Wallet createWallet(int walletID)
    {
        try (
            Connection connection = DriverManager.getConnection(getDbUrl(), getUser(), getPassword()); 
            Statement statement = connection.createStatement();
            Statement statement2 = connection.createStatement();
            Statement statement3 = connection.createStatement();
            ResultSet walletResult = statement.executeQuery("SELECT * FROM Wallet WHERE walletID=" + walletID);
            ResultSet transactionResults = statement2.executeQuery("SELECT * FROM Transaction,Cryptocurrency WHERE "+ walletID +" = Cryptocurrency.walletID AND (senderPA=Cryptocurrency.publicAddress OR receiverPA=Cryptocurrency.publicAddress)"); 
            ResultSet cryptoResults = statement3.executeQuery("SELECT * FROM Cryptocurrency WHERE walletID =" + walletID);

            ) { 
                float balance;
                String seed;
                if (walletResult.next()) {
                    balance = (float) walletResult.getDouble("balance");
                    seed = walletResult.getString("seed");
                }
                else{
                    throw new SQLException ("Wrong walletID");
                }
                ArrayList<Cryptocurrency> cryptos = new ArrayList<>();
                ArrayList<Transaction> transactions = new ArrayList<>();
                // Iterate through cryptoResults and process results into wallet
                while (cryptoResults.next())
                {
                    String name = cryptoResults.getString("cryptoName");
                    String pubA = cryptoResults.getString("publicAddress");
                    String privateK = cryptoResults.getString("privateKey");
                    float cryptoBalance = (float) cryptoResults.getDouble("balance");
                    cryptos.add(new Cryptocurrency(name, pubA, privateK,cryptoBalance));
                }
                // Iterate through transactionResults and process results into transaction
                while (transactionResults.next())
                {
                    int tID = transactionResults.getInt("tID");
                    String senderAddr = transactionResults.getString("senderPA");
                    String recieverAddr = transactionResults.getString("receiverPA");
                    float amount = (float) transactionResults.getDouble("amount");
                    String crypto = transactionResults.getString("cryptocurrency");
                    Timestamp timestamp = transactionResults.getTimestamp("datePlaced");
                    boolean suspicious = transactionResults.getBoolean("suspicious");
                    Date date = new Date(timestamp.getTime());
                    Transaction transaction = new Transaction(tID, date, recieverAddr, senderAddr, crypto, amount, suspicious);
                    transactions.add(transaction);
                }
            statement.close();
            statement2.close();
            statement3.close();
            connection.close();
            Wallet wallet = new Wallet(seed,balance, cryptos, transactions);
            return wallet;
        } catch (SQLException except) {
            System.out.println("Connection Failed. Wallet not created.");
            System.out.println(except.getMessage());
            return new Wallet("", (float) 100.2, new ArrayList<>(), new ArrayList<>());
        }
    }
    
    /* Returns an ArrayList of Calendar objects representing available sessions 
       today for a given consultant. Uses the current booked sessions to determine availability. 
    */
    public ArrayList<Calendar> getAvailableSessionsToday(int consultantID) throws ParseException {
        ArrayList<Calendar> availableSessionsToday = new ArrayList<>();

        try (
            Connection connection = DriverManager.getConnection(getDbUrl(), getUser(), getPassword()); 
            Statement statement = connection.createStatement();
            ResultSet consultantAvailability = statement.executeQuery("SELECT startingHour, endingHour FROM Consultant WHERE consID= "+consultantID);

        ) { ArrayList<ConsultancySession> allBookedSessionsCS = getBookedSessions("all",0);
            ArrayList<String> bookedSessionsString = new ArrayList<>();
    
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            for (int i=0; i<allBookedSessionsCS.size(); i++) {
                bookedSessionsString.add(dateFormat.format(allBookedSessionsCS.get(i).getSessionDateTime().getTime()));
            }

            consultantAvailability.next();
            Time startingTime = consultantAvailability.getTime("startingHour");
            Time endingTime = consultantAvailability.getTime("endingHour");
            
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH");
            int startingHour = Integer.parseInt(timeFormat.format(startingTime));
            int endingHour = Integer.parseInt(timeFormat.format(endingTime));

            for (int i=startingHour; i<=endingHour-1; i++) {
                boolean add = true;
            
                for (int j=0; j<allBookedSessionsCS.size(); j++) {

                    int bookedHour = Integer.parseInt(bookedSessionsString.get(j).split(" ")[1].split(":")[0]);

                    if (i == bookedHour) {
                        add = false;
                        break;
                    }
                }
                if (add) { 
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(allBookedSessionsCS.get(0).getSessionDateTime().getTimeInMillis());
                    calendar.set(Calendar.HOUR_OF_DAY, i);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    availableSessionsToday.add(calendar);
                }
            }            
            statement.close();
            connection.close();
        } catch (SQLException except) {
            System.out.println("Error");
            System.out.println(except.getMessage());
        }

        return availableSessionsToday;
    }

    /* Returns an ArrayList of String arrays representing all available consultants and their IDs. */
    public ArrayList<String[]> getAvailableConsultants() {
        ArrayList<String[]> consultants = new ArrayList<>();

        try (
            Connection connection = DriverManager.getConnection(getDbUrl(), getUser(), getPassword()); 
            Statement statement = connection.createStatement();
            ResultSet consultantDetails = statement.executeQuery("SELECT consID, consName FROM Consultant");

        ) { while (consultantDetails.next()) {
                String[] details = {
                    consultantDetails.getString("consID"),
                    consultantDetails.getString("consName")
                };
                consultants.add(details);
            }
        }catch (SQLException except) {
            System.out.println("Error");
            System.out.println(except.getMessage());
        }
        return consultants;
    }

    /* Returns an ArrayList of ConsultancySession 
       objects representing booked sessions. The method can retrieve all booked sessions, or sessions 
       for a specific customer or consultant. 
    */
    public ArrayList<ConsultancySession> getBookedSessions(String subject, int subjectID) {

        String query;
        if (subject.equals("all")) {
            query = "SELECT sessionID, consID, consName, custID, bookingTimeStamp, sessionDateTime, meetingURL FROM BookedSessions";
        }
        else if ((subject.equals("customer") || subject.equals("consultant")) && subjectID != 0) {
            query = "SELECT sessionID, consID, consName, custID, bookingTimeStamp, sessionDateTime, meetingURL FROM BookedSessions WHERE custID= "+ subjectID;
        }
        else {
            return new ArrayList<ConsultancySession>();
        }

        ArrayList<ConsultancySession> bookedSessions = new ArrayList();

        try (
            Connection connection = DriverManager.getConnection(getDbUrl(), getUser(), getPassword()); 
            Statement statement = connection.createStatement();
            ResultSet sessions = statement.executeQuery(query);

        ) { 
            while (sessions.next()) {
                int sessionID = sessions.getInt("sessionID");
                int consID = sessions.getInt("consID");
                String consName = sessions.getString("consName");
                int custID = sessions.getInt("custID"); 
                
                Calendar bookingTimeStamp = Calendar.getInstance();
                Calendar sessionDateTime = Calendar.getInstance();

                bookingTimeStamp.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(sessions.getString("bookingTimeStamp")));
                sessionDateTime.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(sessions.getString("sessionDateTime")));

                String meetingURL = sessions.getNString("meetingURL");

                ConsultancySession cs = new ConsultancySession(consID, consName, custID, bookingTimeStamp, sessionDateTime, meetingURL);
                cs.setSessionID(sessionID);
                bookedSessions.add(cs);
            }

            statement.close();
            connection.close();
        }catch (SQLException | ParseException except) {
            System.out.println("Error");
            System.out.println(except.getMessage());
        }
        return bookedSessions;
    }

    /* Creates a new consultancy session in the database with the given ConsultancySession object and Customer object. 
       Returns true if the operation was successful, false otherwise. 
    */
    public boolean createConsultancySession(ConsultancySession cs, Customer c) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String bookingTimeStamp = dateFormat.format(cs.getBookingTimestamp().getTime());
        String sessionDateTime = dateFormat.format(cs.getSessionDateTime().getTime());
        
        String query = "insert into BookedSessions(consID, consName, custID, bookingTimeStamp, sessionDateTime, meetingURL)"
                        + "values('"+cs.getConsultantID()+"', '"+cs.getConsultantName()+"', '"+cs.getCustomerID()
                        +"', '"+bookingTimeStamp+"', '"+sessionDateTime+"', '"+"')";

        try (
            Connection connection = DriverManager.getConnection(getDbUrl(), getUser(), getPassword()); 
            Statement statement = connection.createStatement();

        ) { int numRowsAffected = statement.executeUpdate(query);
            System.out.println(numRowsAffected + " row(s) inserted");
            ResultSet session = statement.executeQuery("SELECT sessionID from BookedSessions WHERE sessionDateTime='"+sessionDateTime+"' AND consID="+cs.getConsultantID());
            session.next();
            cs.setSessionID(session.getInt("sessionID"));
            c.bookConsultancySession(cs);

            statement.close();
            connection.close();
            return true;
        } catch (SQLException except) {
            System.out.println("Error");
            System.out.println(except.getMessage());
        }
        return false;
    }
    

    /* Method is deleting the row with matching sessionID from the BookedSessions table. 
       After deleting the row, it is calling the cancelConsultancySession method of the Customer object passed as a parameter. 
       Returns true if the operation is successful and false if an exception is thrown. 
    */
    public boolean cancelConsultancySession(int sessionID, Customer c) {
        String query = "DELETE FROM BookedSessions WHERE sessionID = " + sessionID;
    
        try (
            Connection connection = DriverManager.getConnection(getDbUrl(), getUser(), getPassword());
            Statement statement = connection.createStatement();
        ) {
            int numRowsAffected = statement.executeUpdate(query);
            System.out.println(numRowsAffected + " row(s) deleted");
            if (numRowsAffected > 0) {
                c.cancelConsultancySession(sessionID);
                return true;
            }
        } catch (SQLException except) {
            System.out.println("Error");
            System.out.println(except.getMessage());
        }
    
        return false;
    }
    
    /* getCustomerKeys() method is fetching the cryptocurrency keys of a customer from the database and returning them 
       as a HashMap with the cryptocurrency name as key and private key as value. It executes a SQL query to fetch the 
       data from the database and puts it in the HashMap. If an exception is thrown, it prints an error message 
       and returns an empty HashMap. 
    */
    public HashMap<String,String> getCustomerKeys()
    {
        UserHandler uh = UserHandler.getInstance();
        HashMap<String,String> cryptoKeysMap = new HashMap<>();

        try (
            Connection connection = DriverManager.getConnection(getDbUrl(), getUser(), getPassword()); 
            Statement statement = connection.createStatement();
            ResultSet keys = statement.executeQuery("SELECT Crypto.cryptoName, publicAddress, privateKey "
                                                            + "FROM Cryptocurrency Crypto, Customer Cust "
                                                            + "WHERE Crypto.walletID=Cust.walletID AND "
                                                            + "Cust.CustID=" + uh.getLocalID());

        ) { while (keys.next()) {
                cryptoKeysMap.put(keys.getString("cryptoName"),keys.getString("privateKey"));
            }
            statement.close();
            connection.close();
        }catch (SQLException except) {
            System.out.println("Error");
            System.out.println(except.getMessage());
        }
        return cryptoKeysMap;
    }

    /* Checks if a given cryptocurrency transaction is valid by querying the customer's wallet and cryptocurrency balances. */
    public boolean validateTransactionAmount(String publicAddress, float amount, String crypto)
    {
        boolean validated = false;
        try (
            Connection connection = DriverManager.getConnection(getDbUrl(), getUser(), getPassword()); 
            Statement statement = connection.createStatement();
            Statement statement2 = connection.createStatement();
            ResultSet walletIDResult = statement.executeQuery("SELECT Customer.walletID FROM Transaction, Wallet, Customer, Cryptocurrency WHERE Transaction.senderPA='"+publicAddress+"' AND Transaction.senderPA = Cryptocurrency.publicAddress AND Wallet.walletID = Cryptocurrency.walletID AND Wallet.walletID = Customer.walletID LIMIT 1");
            ) {
                if (walletIDResult.next())
                {
                    int walletID = walletIDResult.getInt("walletID");
                    ResultSet customerCryptoBalance = statement2.executeQuery("SELECT Cryptocurrency.balance FROM Cryptocurrency WHERE Cryptocurrency.walletID=" + walletID + " AND Cryptocurrency.cryptoName='" + crypto + "'");
                    if (customerCryptoBalance.next())
                    {
                        float cryptoBalance = (float) customerCryptoBalance.getDouble("balance");
                        if (cryptoBalance >= amount)
                        {
                            validated = true;
                        }
                    }
                } 
                statement.close();
                connection.close();
                return validated;
            }catch (SQLException except) {
                System.out.println("Connection Failed. Validate transaction failed.");
                System.out.println(except.getMessage());
                return validated;
            }
        } 
    
    /* Creates a new transaction record and updates the sender's wallet and cryptocurrency balances in the database. */
    public boolean createTransaction(Transaction transaction) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String transactionDate = dateFormat.format(transaction.getDate());

        String insertTransactionQuery = "insert into Transaction(senderPA, receiverPA, amount, datePlaced, cryptocurrency, suspicious)"
            + "values('"+transaction.getSenderPA()+"', '"+transaction.getReceiverPA()+"', '"+transaction.getAmount()
            +"', '"+transactionDate+"', '"+transaction.getCrypto()+"', "+transaction.getSuspicious()+")";
        String updateWalletBalanceQuery = "UPDATE Wallet JOIN Cryptocurrency ON Cryptocurrency.walletID = Wallet.walletID SET Wallet.balance = Wallet.balance - "+transaction.getAmount()+" WHERE Cryptocurrency.publicAddress='"+transaction.getSenderPA()+"';";
        String updateCryptoBalanceQuery = "UPDATE Cryptocurrency SET balance = balance - "+transaction.getAmount()+" WHERE Cryptocurrency.publicAddress='"+transaction.getSenderPA()+"';";
        try (
            Connection connection = DriverManager.getConnection(getDbUrl(), getUser(), getPassword()); 
            Statement statement = connection.createStatement();
            Statement statement2 = connection.createStatement();
            Statement statement3 = connection.createStatement();
        ) { int numRowsAffected = statement.executeUpdate(insertTransactionQuery);
            System.out.println(numRowsAffected + " row(s) inserted");
            statement2.executeUpdate(updateWalletBalanceQuery);
            statement3.executeUpdate(updateCryptoBalanceQuery);
            statement.close();
            statement2.close();
            statement3.close();
            connection.close();
            return true;
        } catch (SQLException except) {
            System.out.println("Error");
            System.out.println(except.getMessage());
        }
        return false;
    }

    public int getTransactionId(Transaction transaction)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String transactionDate = dateFormat.format(transaction.getDate());
        try (
            Connection connection = DriverManager.getConnection(getDbUrl(), getUser(), getPassword()); 
            Statement statement = connection.createStatement();
            ResultSet transactionIDResult = statement.executeQuery("SELECT * FROM Transaction WHERE senderPA = '" + transaction.getSenderPA() + "' AND receiverPA = '" + transaction.getReceiverPA() + "' AND datePlaced = '" + transactionDate + "'");
            ) { 
            transactionIDResult.next();
            int transactionID = transactionIDResult.getInt("tID");
            statement.close();
            connection.close();
            return transactionID;

        } catch (SQLException except) {
            System.out.println("Connection Failed. Transaction ID not recieved.");
            System.out.println(except.getMessage());
        }
        return 0;
    }

    /* Retrieves a list of all transactions associated with a given customer ID, 
       by querying the database for records matching the customer's wallet ID. 
    */
    public ArrayList<Transaction> getTransactionHistory(int customerID) throws Exception
    {
        ArrayList<Transaction> transactions = new ArrayList<>();
        try (
            Connection connection = DriverManager.getConnection(getDbUrl(), getUser(), getPassword()); 
            Statement statement = connection.createStatement();
            Statement statement2 = connection.createStatement();
            ResultSet walletIDResult = statement.executeQuery("SELECT * FROM Customer WHERE custID=" + customerID);

            ) { 
                int walletID;
                if (walletIDResult.next())
                {
                    walletID = walletIDResult.getInt("walletID");
                    statement.close();
                }
                else{
                    connection.close();
                    throw new Exception ("Connection Failed. Tranactions not recieved due to wallet ID retrieval failure.");
                }
                {
                    ResultSet transactionResults = statement2.executeQuery("SELECT * FROM Transaction,Cryptocurrency WHERE "+ walletID +" = Cryptocurrency.walletID AND (senderPA=Cryptocurrency.publicAddress OR receiverPA=Cryptocurrency.publicAddress)");
                    while (transactionResults.next())
                    {
                        int transactionID = transactionResults.getInt("tID");
                        String senderAddr = transactionResults.getString("senderPA");
                        String receiverAddr = transactionResults.getString("receiverPA");
                        float amount = (float) transactionResults.getDouble("amount");
                        String crypto = transactionResults.getString("cryptocurrency");
                        Timestamp timestamp = transactionResults.getTimestamp("datePlaced");
                        boolean suspicious = transactionResults.getBoolean("suspicious");
                        Date date = new Date(timestamp.getTime());
                        Transaction transaction = new Transaction(transactionID, date, crypto, receiverAddr, senderAddr, amount, suspicious);
                        transactions.add(transaction);
                    }
                }                
            statement.close();
            statement2.close();
            connection.close();
            return transactions;
        } catch (SQLException except) {
            System.out.println("Connection Failed. Transaction history not recieved.");
            System.out.println(except.getMessage());
            return new ArrayList<>();
        }
    }

}




