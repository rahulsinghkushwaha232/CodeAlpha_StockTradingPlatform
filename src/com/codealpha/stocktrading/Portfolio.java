package com.codealpha.stocktrading;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Manages an investor's cash balance, active stock holdings, and historical transactions.
 * Provides portfolio valuation, profit/loss calculations, and ledger reporting.
 */
public class Portfolio {
    private double cashBalance;
    private final Map<String, Holding> holdings;
    private final List<Transaction> transactionHistory;

    // ANSI styling constants
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BOLD = "\u001B[1m";

    /**
     * Initializes a new portfolio with the default starting cash of Rs. 100,000.
     */
    public Portfolio() {
        this(100000.00);
    }

    /**
     * Initializes a portfolio with a designated cash balance.
     *
     * @param initialCash Initial virtual funds
     */
    public Portfolio(double initialCash) {
        this.cashBalance = Math.round(initialCash * 100.0) / 100.0;
        this.holdings = new TreeMap<>(); // Alphabetically ordered ticker symbols
        this.transactionHistory = new ArrayList<>();
    }

    /**
     * Executes a stock purchase order.
     * Deducts cash balance, updates or creates holding, and records a BUY transaction.
     *
     * @param stock    The stock to buy
     * @param quantity Number of shares to purchase
     * @return true if trade succeeded; false if validation failed
     */
    public boolean buyStock(Stock stock, int quantity) {
        if (stock == null || quantity <= 0) {
            return false;
        }

        double totalCost = Math.round((quantity * stock.getCurrentPrice()) * 100.0) / 100.0;
        if (totalCost > this.cashBalance) {
            return false; // Insufficient funds
        }

        // Deduct cash balance
        this.cashBalance = Math.round((this.cashBalance - totalCost) * 100.0) / 100.0;

        // Update or create holding
        String symbol = stock.getSymbol();
        if (holdings.containsKey(symbol)) {
            holdings.get(symbol).addShares(quantity, stock.getCurrentPrice());
        } else {
            holdings.put(symbol, new Holding(symbol, quantity, stock.getCurrentPrice()));
        }

        // Record audit transaction
        Transaction transaction = new Transaction(symbol, TransactionType.BUY, quantity, stock.getCurrentPrice());
        transactionHistory.add(transaction);

        return true;
    }

    /**
     * Executes a stock selling order.
     * Credits cash balance, decrements holdings, and records a SELL transaction.
     *
     * @param stock    The stock to sell
     * @param quantity Number of shares to sell
     * @return true if trade succeeded; false if validation failed
     */
    public boolean sellStock(Stock stock, int quantity) {
        if (stock == null || quantity <= 0) {
            return false;
        }

        String symbol = stock.getSymbol();
        Holding holding = holdings.get(symbol);
        if (holding == null || holding.getQuantity() < quantity) {
            return false; // User does not own the stock or has insufficient quantity
        }

        double totalRevenue = Math.round((quantity * stock.getCurrentPrice()) * 100.0) / 100.0;

        // Credit cash balance
        this.cashBalance = Math.round((this.cashBalance + totalRevenue) * 100.0) / 100.0;

        // Decrement position
        holding.removeShares(quantity);
        if (holding.getQuantity() == 0) {
            holdings.remove(symbol);
        }

        // Record audit transaction
        Transaction transaction = new Transaction(symbol, TransactionType.SELL, quantity, stock.getCurrentPrice());
        transactionHistory.add(transaction);

        return true;
    }

    /**
     * Calculates the total current market value of all held stocks.
     *
     * @param market Market instance for live pricing
     * @return Sum of (quantity * currentPrice)
     */
    public double getHoldingsMarketValue(Market market) {
        double total = 0.0;
        for (Holding holding : holdings.values()) {
            Stock stock = market.getStock(holding.getStockSymbol());
            double price = (stock != null) ? stock.getCurrentPrice() : holding.getAverageBuyPrice();
            total += holding.getQuantity() * price;
        }
        return Math.round(total * 100.0) / 100.0;
    }

    /**
     * Calculates the total historical cost basis (invested capital) for current holdings.
     *
     * @return Sum of (quantity * averageBuyPrice)
     */
    public double getTotalInvestedAmount() {
        double invested = 0.0;
        for (Holding holding : holdings.values()) {
            invested += holding.getTotalInvested();
        }
        return Math.round(invested * 100.0) / 100.0;
    }

    /**
     * Computes the total net worth of the portfolio (Cash + Stocks Market Value).
     *
     * @param market Market instance for live pricing
     * @return Total portfolio valuation
     */
    public double getTotalPortfolioValue(Market market) {
        return Math.round((cashBalance + getHoldingsMarketValue(market)) * 100.0) / 100.0;
    }

    /**
     * Computes unrealized profit/loss on current holdings.
     * Formula: Holdings Market Value - Holdings Cost Basis
     *
     * @param market Market instance for live pricing
     * @return Unrealized P/L in Rupees
     */
    public double getProfitLoss(Market market) {
        return Math.round((getHoldingsMarketValue(market) - getTotalInvestedAmount()) * 100.0) / 100.0;
    }

    /**
     * Computes unrealized profit/loss percentage on current holdings.
     *
     * @param market Market instance for live pricing
     * @return Percentage profit or loss
     */
    public double getProfitLossPercentage(Market market) {
        double invested = getTotalInvestedAmount();
        if (invested == 0.0) {
            return 0.0;
        }
        double pnl = getProfitLoss(market);
        return Math.round(((pnl / invested) * 100.0) * 100.0) / 100.0;
    }

    /**
     * Renders a comprehensive, color-coded breakdown of user portfolio holdings.
     * Displays current prices, invested capital, current valuation, and individual P/L.
     *
     * @param market Market instance for live pricing
     */
    public void displayHoldings(Market market) {
        System.out.println("\n" + ANSI_CYAN + "=".repeat(88) + ANSI_RESET);
        System.out.printf(ANSI_BOLD + ANSI_CYAN + " %-8s | %-6s | %-12s | %-12s | %-12s | %-12s | %-10s %n" + ANSI_RESET,
                "SYMBOL", "SHARES", "AVG BUY (\u20B9)", "CURRENT (\u20B9)", "INVESTED (\u20B9)", "VALUATION(\u20B9)", "P/L");
        System.out.println(ANSI_CYAN + "-".repeat(88) + ANSI_RESET);

        if (holdings.isEmpty()) {
            System.out.println("  No active stock holdings in portfolio. Select option (2) to buy shares.");
        } else {
            for (Holding holding : holdings.values()) {
                Stock stock = market.getStock(holding.getStockSymbol());
                double currentPrice = (stock != null) ? stock.getCurrentPrice() : holding.getAverageBuyPrice();
                double invested = holding.getTotalInvested();
                double currentVal = Math.round((holding.getQuantity() * currentPrice) * 100.0) / 100.0;
                double pnl = Math.round((currentVal - invested) * 100.0) / 100.0;
                double pnlPercent = (invested > 0) ? Math.round(((pnl / invested) * 100.0) * 100.0) / 100.0 : 0.0;

                String color = ANSI_RESET;
                String symbol = "\u25AC";
                if (pnl > 0) {
                    color = ANSI_GREEN;
                    symbol = "\u25B2";
                } else if (pnl < 0) {
                    color = ANSI_RED;
                    symbol = "\u25BC";
                }

                System.out.printf(" %-8s | %-6d | \u20B9%-11.2f | \u20B9%-11.2f | \u20B9%-11.2f | \u20B9%-11.2f | %s%s %+.2f%%%s%n",
                        holding.getStockSymbol(),
                        holding.getQuantity(),
                        holding.getAverageBuyPrice(),
                        currentPrice,
                        invested,
                        currentVal,
                        color,
                        symbol,
                        pnlPercent,
                        ANSI_RESET);
            }
        }

        System.out.println(ANSI_CYAN + "=".repeat(88) + ANSI_RESET);

        // Portfolio Summary Metrics
        double totalInvested = getTotalInvestedAmount();
        double holdingsVal = getHoldingsMarketValue(market);
        double totalPortfolio = getTotalPortfolioValue(market);
        double netPnl = getProfitLoss(market);
        double netPnlPercent = getProfitLossPercentage(market);

        String pnlColor = ANSI_RESET;
        String pnlSymbol = "\u25AC";
        if (netPnl > 0) {
            pnlColor = ANSI_GREEN;
            pnlSymbol = "\u25B2";
        } else if (netPnl < 0) {
            pnlColor = ANSI_RED;
            pnlSymbol = "\u25BC";
        }

        System.out.printf(" Cash Balance          : " + ANSI_BOLD + "\u20B9%.2f" + ANSI_RESET + "%n", cashBalance);
        System.out.printf(" Total Invested Capital: \u20B9%.2f%n", totalInvested);
        System.out.printf(" Holdings Market Value : \u20B9%.2f%n", holdingsVal);
        System.out.printf(" Total Portfolio Value : " + ANSI_BOLD + ANSI_CYAN + "\u20B9%.2f" + ANSI_RESET + "%n", totalPortfolio);
        System.out.printf(" Unrealized Profit/Loss: %s%s %s\u20B9%.2f (%+.2f%%)%s%n",
                pnlColor, ANSI_BOLD, pnlSymbol, netPnl, netPnlPercent, ANSI_RESET);
        System.out.println(ANSI_CYAN + "=".repeat(88) + ANSI_RESET);
    }

    /**
     * Renders the complete transaction ledger in a clean, chronologically ordered table.
     */
    public void displayTransactionHistory() {
        System.out.println("\n" + ANSI_CYAN + "=".repeat(80) + ANSI_RESET);
        System.out.printf(ANSI_BOLD + ANSI_CYAN + " %-19s | %-5s | %-8s | %-6s | %-12s | %-12s %n" + ANSI_RESET,
                "TIMESTAMP", "TYPE", "SYMBOL", "SHARES", "PRICE (\u20B9)", "TOTAL (\u20B9)");
        System.out.println(ANSI_CYAN + "-".repeat(80) + ANSI_RESET);

        if (transactionHistory.isEmpty()) {
            System.out.println("  No transactions executed yet.");
        } else {
            for (Transaction tx : transactionHistory) {
                String typeColor = (tx.getType() == TransactionType.BUY) ? ANSI_GREEN : ANSI_YELLOW;
                System.out.printf(" %-19s | %s%-5s%s | %-8s | %-6d | \u20B9%-11.2f | \u20B9%-11.2f %n",
                        tx.getFormattedTimestamp(),
                        typeColor,
                        tx.getType(),
                        ANSI_RESET,
                        tx.getStockSymbol(),
                        tx.getQuantity(),
                        tx.getPricePerShare(),
                        tx.getTotalAmount());
            }
        }
        System.out.println(ANSI_CYAN + "=".repeat(80) + ANSI_RESET);
    }

    // --- Direct Accessors for File I/O Persistence ---

    public double getCashBalance() {
        return cashBalance;
    }

    public void setCashBalance(double cashBalance) {
        this.cashBalance = Math.round(cashBalance * 100.0) / 100.0;
    }

    public Map<String, Holding> getHoldings() {
        return holdings;
    }

    public List<Transaction> getTransactionHistory() {
        return Collections.unmodifiableList(transactionHistory);
    }

    /**
     * Directly injects a restored holding during file loading.
     *
     * @param holding Restored Holding object
     */
    public void addRestoredHolding(Holding holding) {
        holdings.put(holding.getStockSymbol().toUpperCase(), holding);
    }

    /**
     * Directly injects a restored transaction during file loading.
     *
     * @param transaction Restored Transaction object
     */
    public void addRestoredTransaction(Transaction transaction) {
        transactionHistory.add(transaction);
    }
}
