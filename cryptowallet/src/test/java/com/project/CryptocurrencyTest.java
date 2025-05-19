
package com.project;

import org.junit.jupiter.api.Test;

import com.project.core.Cryptocurrency;

import static org.junit.jupiter.api.Assertions.*;

public class CryptocurrencyTest {

    @Test
    public void testGetCurrentBitcoinPrice() {

        Cryptocurrency crypto = new Cryptocurrency(null, null, null, 0);

        try {
            double price = crypto.getCurrentCryptoPrice("bitcoin");
            System.out.println("BTC Price: " + price);
            assertTrue(price > 0, "Price should be greater than zero");
        } catch (Exception e) {
            fail("Exception occurred while fetching crypto price: " + e.getMessage());
        }
    }

    @Test
    public void testGetCurrentEthereumPrice() {

        Cryptocurrency crypto = new Cryptocurrency(null, null, null, 0);

        try {
            double price = crypto.getCurrentCryptoPrice("ethereum");
            System.out.println("ETH Price: " + price);
            assertTrue(price > 0, "Price should be greater than zero");
        } catch (Exception e) {
            fail("Exception occurred while fetching crypto price: " + e.getMessage());
        }
    }
}
