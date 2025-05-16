package com.project.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.Calendar;
import com.project.prototype.*;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.store.MemoryBlockStore;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.BlockChain;
import org.bitcoinj.wallet.SendRequest;

import org.bitcoinj.core.Coin;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.http.HttpService;

import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;


@RestController
public class Controller {

    private DatabaseHandler dh = DatabaseHandler.getInstance();
    private UserHandler uh = UserHandler.getInstance();
    private Customer customer = dh.createCustomer(uh.getLocalID());

    // Validates the customer's login attempt using a PIN and creates a new customer object.
    @PostMapping(path = "/customerPin/")
    public ResponseEntity<String> postPin(@RequestBody String pin) {
        boolean validation = uh.validatePinLoginAttempt(pin);
        if (validation) {
            customer = dh.createCustomer(uh.getLocalID());
        } 
        return new ResponseEntity<>(Boolean.toString(validation), HttpStatus.OK);
    }
    
    // Creates a new consultancy session object and adds it to the customer's session list.
    @PostMapping(path = "/createBooking/")
    public ResponseEntity<String> createBooking(@RequestBody String[] sessionDetails) throws ParseException {
        int consultantID = Integer.parseInt(sessionDetails[0]);
        String consultantName = sessionDetails[1];
        int customerID = uh.getLocalID();

        Calendar bookingTimeStamp = Calendar.getInstance();
        Calendar sessionDateTime = Calendar.getInstance();
        bookingTimeStamp.setTime(new Date());
        sessionDateTime.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(sessionDetails[2]));
        String meetingURL = "https://meet.google.com/nnv-vxch-ddz"; //Change in future integrations

        ConsultancySession cs = new ConsultancySession(consultantID, consultantName, customerID, bookingTimeStamp, sessionDateTime, meetingURL);
        boolean response = dh.createConsultancySession(cs, customer);

        return new ResponseEntity<>(Boolean.toString(response), HttpStatus.OK);
    }

    // Cancels a consultancy session object and removes it from the customer's session list.
    @PostMapping(path = "/cancelBooking/")
    public ResponseEntity<String> cancelBooking(@RequestBody int sessionID) throws ParseException {        
        boolean response = dh.cancelConsultancySession(sessionID, customer);

        return new ResponseEntity<>(Boolean.toString(response), HttpStatus.OK);
    }


    // Returns a list of booked sessions for the customer.
    @GetMapping(path ="/viewBookedSessions")
    public ResponseEntity<ArrayList<String[]>> getBookedSessions() {
        ArrayList<String[]> booked = new ArrayList<>();
        ArrayList<ConsultancySession> sessions = customer.getConsultancySessions();
        for (ConsultancySession session : sessions) {
            String[] sessionDetails = {
                String.valueOf(session.getsessionID()),
                String.valueOf(session.getConsultantID()),
                session.getConsultantName(),
                String.valueOf(session.getCustomerID()),
                session.getBookingTimestamp().getTime().toString(),
                session.getSessionDateTime().getTime().toString(),
                session.getMeetingURL()
            };
            booked.add(sessionDetails);
        }
        return new ResponseEntity<>(booked, HttpStatus.OK);
    }

    // Processes a Bitcoin transaction.
    @PostMapping(path = "/makeTransactionBTC/")
    public ResponseEntity<String> makeBitcoinTransaction(@RequestBody String[] transactionDetails, @Autowired Wallet wallet) throws ParseException {
        String receiverPublicAddr = transactionDetails[0];
        float amount = Float.parseFloat(transactionDetails[1]);
        ArrayList<Cryptocurrency> cryptocurrencies = wallet.getCryptocurrencies();
        for(Cryptocurrency cryptocurrency : cryptocurrencies){
            if(cryptocurrency.getName().equals("Bitcoin")){
                try {
                    NetworkParameters params = MainNetParams.get();
                    DumpedPrivateKey dumpedPrivateKey = DumpedPrivateKey.fromBase58(params, cryptocurrency.getPrivateKey());
                    ECKey ecKey = dumpedPrivateKey.getKey();
                    org.bitcoinj.wallet.Wallet bitcoinWallet = new org.bitcoinj.wallet.Wallet(params);
                    bitcoinWallet.importKey(ecKey);
                    Address recipient = Address.fromBase58(params, receiverPublicAddr);
                    SendRequest req = SendRequest.to(recipient, Coin.valueOf((long) (amount * 1e8)));
                    bitcoinWallet.completeTx(req);
                    org.bitcoinj.core.Transaction btcTransaction = req.tx;
                    System.out.println("Transaction hash: " + btcTransaction.getHashAsString());
                    PeerGroup peerGroup = new PeerGroup(params, new BlockChain(params, bitcoinWallet, new MemoryBlockStore(params)));
                    peerGroup.start();
                    peerGroup.addWallet(bitcoinWallet);
                    peerGroup.broadcastTransaction(btcTransaction).broadcast();
                } catch (Exception e) {
                    return new ResponseEntity<>("Error creating Bitcoin transaction: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        }
        return new ResponseEntity<>(Boolean.toString(true), HttpStatus.OK);
    }


    // Processes an Ethereum transaction
    @PostMapping(path = "/makeTransactionETH/")
    public ResponseEntity<String> makeEthereumTransaction(@RequestBody String[] transactionDetails) throws ParseException {
        String senderPublicAddr = transactionDetails[0];
        String receiverPublicAddr = transactionDetails[1];
        float amount = Float.parseFloat(transactionDetails[3]);
        Wallet wallet = customer.getWallet();
        ArrayList<Cryptocurrency> cryptosInWallet = wallet.getCryptocurrencies();
        Cryptocurrency cryptocurrency = null;

        for (Cryptocurrency cr : cryptosInWallet) {
            if (cr.getName().equalsIgnoreCase("eth")) {
                cryptocurrency = cr;
                break;
            }
        }

        if (cryptocurrency == null) {
            return new ResponseEntity<>("Ethereum wallet not found", HttpStatus.BAD_REQUEST);
        }

        try {
            Credentials credentials = Credentials.create(cryptocurrency.getPrivateKey());
            Web3j web3j = Web3j.build(new HttpService("https://mainnet.infura.io/v3/infuraID"));

            EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(senderPublicAddr, DefaultBlockParameterName.LATEST).send();
            BigInteger nonce = ethGetTransactionCount.getTransactionCount();
            BigInteger gasPrice = BigInteger.valueOf(20_000_000_000L);
            BigInteger gasLimit = BigInteger.valueOf(21_000);

            BigInteger value = Convert.toWei(BigDecimal.valueOf(amount), Convert.Unit.ETHER).toBigInteger();
            RawTransaction rawTransaction = RawTransaction.createEtherTransaction(nonce, gasPrice, gasLimit, receiverPublicAddr, value);

            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
            String hexValue = Numeric.toHexString(signedMessage);

            EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).sendAsync().get();

            if (ethSendTransaction.getTransactionHash() != null) {
                String txHash = ethSendTransaction.getTransactionHash();
                return new ResponseEntity<>(txHash, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Transaction failed", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error occurred while making transaction", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Processes a cryptocurrency transaction for the customer and store in DB.
    @PostMapping(path = "/makeTransaction/")
    public ResponseEntity<String> makeTransaction(@RequestBody String[] transactionDetails) throws ParseException {
        int transactionID = 0;  //Just a placeholder. Does get changed later with setTransactionId()
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = dateFormat.parse(transactionDetails[0]);
        String crypto = transactionDetails[1];
        Boolean suspicious = Boolean.parseBoolean(transactionDetails[2]);
        String receiverPublicAddr = transactionDetails[3];
        String senderPublicAddr = transactionDetails[4];
        float amount = Float.parseFloat(transactionDetails[5]);
        System.out.println(""+transactionID+", "+date+", "+crypto+", "+receiverPublicAddr+", "+senderPublicAddr+", "+amount+", "+suspicious);

        Transaction transaction = new Transaction(transactionID, date, crypto, receiverPublicAddr, senderPublicAddr, amount, suspicious);
        boolean response = false;
        if (dh.validateTransactionAmount(transaction.getSenderPA(), transaction.getAmount(), transaction.getCrypto()) == false)
            {
                return new ResponseEntity<>("Can't afford", HttpStatus.BAD_REQUEST);
            }
        //iterate through for loop of cryptos customer has
        //get the one that matches the crypto of the transaction
        Cryptocurrency cryptocurrency = null;
        for (Cryptocurrency cr : customer.getWallet().getCryptocurrencies())
        {
            if (cr.getName().equals(crypto))
            {
                cryptocurrency = cr;
            }
        }
        if (cryptocurrency == null) {
            return new ResponseEntity<>("Cryptocurrency not found", HttpStatus.NOT_FOUND);
        }
        
        if (dh.createTransaction(transaction)) {
            transaction.setTransactionID(dh.getTransactionId(transaction));
            customer.getWallet().addTransaction(transaction);
            customer.getWallet().setBalance(customer.getWallet().getBalance() - amount);
            cryptocurrency.setBalance(cryptocurrency.getBalance() - amount);
            response = true;
        }

        return new ResponseEntity<>(Boolean.toString(response), HttpStatus.OK);
    }

    // Return's the logged in user's transaction history
    @GetMapping(path ="/transactionHistory")
    public ResponseEntity<ArrayList<String[]>> getTransactionHistory() throws Exception {
        ArrayList<Transaction> transactions = dh.getTransactionHistory(customer.getCustomerID());
        ArrayList<String[]> transactionDetails = new ArrayList<>();
        for (Transaction transaction : transactions) {
            String[] details = {
                String.valueOf(transaction.getID()),
                transaction.getDate().toString(),
                transaction.getCrypto(),
                String.valueOf(transaction.getSuspicious()),
                transaction.getReceiverPA(),
                transaction.getSenderPA(),
                String.valueOf(transaction.getAmount())
            };
            transactionDetails.add(details);
        }
        return new ResponseEntity<>(transactionDetails, HttpStatus.OK);
    }

    // Retrieves available sessions for the current day and returns them in an ArrayList of String arrays.
    @GetMapping(path = "/viewAvailableSessions")
    public ResponseEntity<ArrayList<String[]>> viewAvailableSessions() {
        ArrayList<String[]> available = new ArrayList<>();
        ArrayList<String[]> consultantsAvailable = dh.getAvailableConsultants();
        
        if (consultantsAvailable.isEmpty()) {
            return new ResponseEntity<>(available, HttpStatus.NO_CONTENT);
        }
        
        int consultantID = Integer.parseInt(consultantsAvailable.get(0)[0]);
        String consultantName = consultantsAvailable.get(0)[1];
        
        ArrayList<Calendar> sessionDates;
        try {
            sessionDates = dh.getAvailableSessionsToday(consultantID);
            for (Calendar sessionDate : sessionDates) {
                String[] sessionDetails = {
                    String.valueOf(consultantID),
                    consultantName,
                    sessionDate.getTime().toString()
                };
                available.add(sessionDetails);
            }
            return new ResponseEntity<>(available, HttpStatus.OK);
        } catch (ParseException e) {
            System.out.println("Error while fetching available sessions");
            e.printStackTrace();
            return new ResponseEntity<>(available, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Retrieves and returns various details about the customer's wallet, 
    // including total balance and details for each cryptocurrency.
    @GetMapping(path = "/walletDetails")
    public ResponseEntity<HashMap<String, String[]>> getWalletDetails()
    {

        float totalBalance = customer.getWallet().getBalance();
        HashMap<String,String[]> walletDetails = new HashMap<>();
        String[] totalBalanceInArray = {String.valueOf(totalBalance)};
        walletDetails.put("Total balance", totalBalanceInArray);

        ArrayList<Cryptocurrency> cryptos = customer.getWallet().getCryptocurrencies();
        for (Cryptocurrency crypto : cryptos)
        {
            ArrayList<String> details = new ArrayList<>();
            details.add(String.valueOf(crypto.getPrice()));
            details.add(String.valueOf(crypto.getBalance()));
            details.add(crypto.getPublicKey());
            details.add(crypto.getPrivateKey());
            String[] detailsArray = details.toArray(new String[details.size()]);
            walletDetails.put(crypto.getName(),detailsArray);
        }

        System.out.println(walletDetails);
        System.out.println(Arrays.toString(walletDetails.get("Bitcoin")));
        System.out.println(Arrays.toString(walletDetails.get("Ethereum")));

        return new ResponseEntity<>(walletDetails, HttpStatus.OK);
    }
}