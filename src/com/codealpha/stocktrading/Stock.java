package com.codealpha.stocktrading;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Represents a publicly traded stock in the market.
 * Demonstrates Object-Oriented principles such as Encapsulation
 * through private fields, getters, setters, and business logic.
 */
public class Stock {
    private String symbol;
    private String companyName;
    private double currentPrice;
    private double previousPrice;

    /**
     * Constructs a Stock object with an initial price.
     *
     * @param symbol       Ticker symbol (e.g., "AAPL", "TCS")
     * @param companyName  Full company name
     * @param initialPrice Initial trading price
     */
    public Stock(String symbol, String companyName, double initialPrice) {
        this.symbol = symbol.toUpperCase();
        this.companyName = companyName;
        this.currentPrice = Math.round(initialPrice * 100.0) / 100.0;
        this.previousPrice = this.currentPrice;
    }

    /**
     * Simulates market price movement for the stock.
     * Moves price randomly within a range of +/- 5.0%.
     * Keeps track of the previous price before updating.
     */
    public void updatePrice() {
        this.previousPrice = this.currentPrice;

        // Random percentage between -5.0% and +5.0%
        // ThreadLocalRandom is thread-safe and efficient
        double percentChange = ThreadLocalRandom.current().nextDouble(-5.0, 5.0);
        double priceDelta = this.currentPrice * (percentChange / 100.0);
        double newPrice = this.currentPrice + priceDelta;

        // Ensure stock price doesn't drop below a nominal floor (Rs. 1.00)
        if (newPrice < 1.0) {
            newPrice = 1.0;
        }

        // Round to 2 decimal places for financial accuracy
        this.currentPrice = Math.round(newPrice * 100.0) / 100.0;
    }

    /**
     * Calculates the percentage change from previous price to current price.
     *
     * @return Percentage change as a double (e.g. +2.45 or -1.80)
     */
    public double getChangePercentage() {
        if (previousPrice == 0) {
            return 0.0;
        }
        double change = ((currentPrice - previousPrice) / previousPrice) * 100.0;
        return Math.round(change * 100.0) / 100.0;
    }

    // --- Getters and Setters ---

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol.toUpperCase();
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = Math.round(currentPrice * 100.0) / 100.0;
    }

    public double getPreviousPrice() {
        return previousPrice;
    }

    public void setPreviousPrice(double previousPrice) {
        this.previousPrice = Math.round(previousPrice * 100.0) / 100.0;
    }

    @Override
    public String toString() {
        return String.format("%s (%s): \u20B9%.2f [%+.2f%%]", 
                symbol, companyName, currentPrice, getChangePercentage());
    }
}
