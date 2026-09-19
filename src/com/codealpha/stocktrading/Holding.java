package com.codealpha.stocktrading;

/**
 * Represents the shares owned by an investor for a specific stock ticker.
 * Encapsulates position tracking and dynamic average cost basis calculation.
 */
public class Holding {
    private final String stockSymbol;
    private int quantity;
    private double averageBuyPrice;

    /**
     * Constructs an initial holding for a stock.
     *
     * @param stockSymbol     Stock ticker symbol
     * @param initialQuantity Initial number of shares purchased
     * @param buyPrice        Purchase price per share
     */
    public Holding(String stockSymbol, int initialQuantity, double buyPrice) {
        this.stockSymbol = stockSymbol.toUpperCase();
        this.quantity = initialQuantity;
        this.averageBuyPrice = Math.round(buyPrice * 100.0) / 100.0;
    }

    /**
     * Adds additional shares to this position and recalculates the weighted average buy price.
     * Formula: ((currentQty * currentAvgPrice) + (newQty * newPrice)) / (currentQty + newQty)
     *
     * @param qty   Number of new shares purchased
     * @param price Price per share paid for the new purchase
     */
    public void addShares(int qty, double price) {
        if (qty <= 0) {
            throw new IllegalArgumentException("Quantity to add must be positive.");
        }
        double totalCostBefore = this.quantity * this.averageBuyPrice;
        double newCost = qty * price;
        this.quantity += qty;
        this.averageBuyPrice = Math.round(((totalCostBefore + newCost) / this.quantity) * 100.0) / 100.0;
    }

    /**
     * Removes shares when sold.
     * The average buy price remains unchanged for the remaining shares.
     *
     * @param qty Number of shares to sell/remove
     * @return true if successfully removed, false if insufficient shares
     */
    public boolean removeShares(int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("Quantity to remove must be positive.");
        }
        if (qty > this.quantity) {
            return false;
        }
        this.quantity -= qty;
        if (this.quantity == 0) {
            this.averageBuyPrice = 0.0;
        }
        return true;
    }

    // --- Getters and Setters ---

    public String getStockSymbol() {
        return stockSymbol;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getAverageBuyPrice() {
        return averageBuyPrice;
    }

    public void setAverageBuyPrice(double averageBuyPrice) {
        this.averageBuyPrice = Math.round(averageBuyPrice * 100.0) / 100.0;
    }

    /**
     * Calculates the total amount invested in this position.
     *
     * @return Total cost basis (quantity * averageBuyPrice)
     */
    public double getTotalInvested() {
        return Math.round((quantity * averageBuyPrice) * 100.0) / 100.0;
    }

    @Override
    public String toString() {
        return String.format("%s: %d shares @ \u20B9%.2f (Invested: \u20B9%.2f)",
                stockSymbol, quantity, averageBuyPrice, getTotalInvested());
    }
}
