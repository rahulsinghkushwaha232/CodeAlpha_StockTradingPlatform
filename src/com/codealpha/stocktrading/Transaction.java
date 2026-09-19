package com.codealpha.stocktrading;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents an immutable record of a stock trade (BUY or SELL).
 * Demonstrates audit logging and timestamped financial transaction recording.
 */
public class Transaction {
    private final String stockSymbol;
    private final TransactionType type;
    private final int quantity;
    private final double pricePerShare;
    private final LocalDateTime timestamp;
    private final double totalAmount;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Constructs a Transaction with automatic total computation and current timestamp.
     *
     * @param stockSymbol   The symbol of the stock traded
     * @param type          TransactionType.BUY or TransactionType.SELL
     * @param quantity      Number of shares traded
     * @param pricePerShare Execution price per share
     */
    public Transaction(String stockSymbol, TransactionType type, int quantity, double pricePerShare) {
        this(stockSymbol, type, quantity, pricePerShare, LocalDateTime.now());
    }

    /**
     * Constructs a Transaction with a specified timestamp (useful for persistence re-loading).
     *
     * @param stockSymbol   The symbol of the stock traded
     * @param type          TransactionType.BUY or TransactionType.SELL
     * @param quantity      Number of shares traded
     * @param pricePerShare Execution price per share
     * @param timestamp     Exact date-time when transaction occurred
     */
    public Transaction(String stockSymbol, TransactionType type, int quantity, double pricePerShare, LocalDateTime timestamp) {
        this.stockSymbol = stockSymbol.toUpperCase();
        this.type = type;
        this.quantity = quantity;
        this.pricePerShare = Math.round(pricePerShare * 100.0) / 100.0;
        this.timestamp = timestamp;
        this.totalAmount = Math.round((quantity * pricePerShare) * 100.0) / 100.0;
    }

    // --- Getters ---

    public String getStockSymbol() {
        return stockSymbol;
    }

    public TransactionType getType() {
        return type;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPricePerShare() {
        return pricePerShare;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public String getFormattedTimestamp() {
        return timestamp.format(FORMATTER);
    }

    @Override
    public String toString() {
        return String.format("[%s] %-4s | Symbol: %-8s | Qty: %-4d | Price: \u20B9%-8.2f | Total: \u20B9%-10.2f",
                getFormattedTimestamp(), type, stockSymbol, quantity, pricePerShare, totalAmount);
    }
}
