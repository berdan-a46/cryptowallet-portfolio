package com.project.core;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import org.json.JSONObject;

import java.util.Observable;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;


/*Note: this class is not being utilised fully and will be implemented in the second version of our prototype (post-presentation).
  The Cryptocurrency class represents a type of digital currency, which can be observed by other classes.
*/ 
public class Cryptocurrency extends Observable {

	private String name;
	private float price;
	private float balance;
	private float twentyFourHourVolume;
	private float hourlyPercChange;
	private float dailyPercChange;
	private float weeklyPercChange;
	private String publicKey;
	private String privateKey;

	// Cryptocurrency constructor
	public Cryptocurrency(String name, String publicKey, String privateKey, float balance) {
		this.name= name;
		this.publicKey = publicKey;
		this.privateKey = privateKey;
		this.balance = balance;
	}

	private String getCoinDeskKey() throws IOException {
		Properties prop = new Properties();
		FileInputStream input = new FileInputStream("config.properties");
		prop.load(input);
		return prop.getProperty("COINDESK_KEY");
	}

	/* At this time, the getCurrentCryptoPrice function is not in use
	   This is a feature that will be implemented in the future, as soon as the system involves real-time price fetching 
	   and integration with the project being fully developed at a later stage
	   Right now, the main emphasis is on creating the core framework and managing the data simulation
	*/
    public double getCurrentCryptoPrice(String crypto) {
        try {
            // Set the url depending on which crypto is passed in as a parameter
            String urlString = "";
            String abbreviation = "";
            if (crypto.equals("bitcoin")){
                urlString = "https://data-api.coindesk.com/spot/v1/latest/tick?market=coinbase&instruments=BTC-GBP&api_key=" + getCoinDeskKey();
                abbreviation = "BTC";
            }
            else if (crypto.equals("ethereum")){
                urlString = "https://data-api.coindesk.com/spot/v1/latest/tick?market=coinbase&instruments=ETH-GBP&api_key=" + getCoinDeskKey();
                abbreviation = "ETH";
            }
            URL url = URI.create(urlString).toURL();

            // Open a connection to the API
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET"); 
            connection.setConnectTimeout(5000); 
            connection.setReadTimeout(5000);

            // Check for a successful response and read the response
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Parse the JSON response and return the current price 
                JSONObject jsonResponse = new JSONObject(response.toString());

                JSONObject data = jsonResponse.getJSONObject("Data");
                JSONObject coinData = data.getJSONObject(abbreviation + "-GBP");
                double priceGBP = coinData.getDouble("PRICE");
                
                connection.disconnect();
                return priceGBP;
            }
            else {
                System.out.println("GET request failed. Response Code: " + responseCode);
                return -1.0;
            }          
        } 
        catch (Exception e) {
            e.printStackTrace();
            return -1.0;
        } 
    }

	// Get the name of the Cryptocurrency
	public String getName() {
		return this.name;
	}

	// Get the price of the Cryptocurrency
	public float getPrice() {
		return this.price;
	}

	// Get the balance of the Cryptocurrency
	public float getBalance() {
		return this.balance;
	}

	// Get the 24-hour trading volume of the Cryptocurrency
	public float getTwentyFourHourVolume() {
		return this.twentyFourHourVolume;
	}

	// Get the hourly percentage change in the price of the Cryptocurrency
	public float getHourlyPercChange() {
		return this.hourlyPercChange;
	}

	// Get the daily percentage change in the price of the Cryptocurrency
	public float getDailyPercChange() {
		return this.dailyPercChange;
	}

	// Get the weekly percentage change in the price of the Cryptocurrency
	public float getWeeklyPercChange() {
		return this.weeklyPercChange;
	}

	// Get the public key associated with the Cryptocurrency
	public String getPublicKey() {
		return this.publicKey;
	}

	// Get the private key associated with the Cryptocurrency
	public String getPrivateKey() {
		return this.privateKey;
	}

	// Set the balance of the Cryptocurrency
	public void setBalance(float balance) {
		this.balance = balance;
	}

	// Set the 24-hour trading volume of the Cryptocurrency
	public void setTwentyFourHourVolume(float twentyFourHourVolume) {
		this.twentyFourHourVolume = twentyFourHourVolume;
	}

	// Set the hourly percentage change in the price of the Cryptocurrency
	public void setHourlyPercChange(float hourlyPercChange) {
		this.hourlyPercChange = hourlyPercChange;
	}

	// Set the daily percentage change in the price of the Cryptocurrency
	public void setDailyPercChange(float dailyPercChange) {
		this.dailyPercChange = dailyPercChange;
	}

	// Set the weekly percentage change in the price of the Cryptocurrency
	public void setWeeklyPercChange(float weeklyPercChange) {
		this.weeklyPercChange = weeklyPercChange;
	}

	// Set the price of the Cryptocurrency
	public void setPrice(float price) {
		this.price = price;
	}
}


