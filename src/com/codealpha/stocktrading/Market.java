package com.codealpha.stocktrading;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents the stock exchange market hosting multiple tradeable securities.
 * Encapsulates the collection of active stocks and simulates trading day fluctuations.
 */
public class Market {
    // LinkedHashMap preserves predictable listing order for user display
    private final Map<String, Stock> stocks;

    // ANSI Color Constants for terminal output
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BOLD = "\u001B[1m";

    /**
     * Initializes the Market with at least 8 pre-loaded premier stocks
     * covering tech titans and major Indian blue-chip corporations.
     */
    public Market() {
        stocks = new LinkedHashMap<>();
        initializeStocks();
    }

    /**
     * Populates the market with default stocks and their baseline prices.
     */
    private void initializeStocks() {
        addStock(new Stock("AAPL", "Apple Inc.", 185.50));
        addStock(new Stock("GOOGL", "Alphabet Inc.", 175.25));
        addStock(new Stock("TSLA", "Tesla Inc.", 240.80));
        addStock(new Stock("MSFT", "Microsoft Corp.", 420.10));
        addStock(new Stock("AMZN", "Amazon.com Inc.", 180.60));
        addStock(new Stock("INFY", "Infosys Ltd.", 1620.00));
        addStock(new Stock("TCS", "Tata Consultancy Services", 3950.00));
        addStock(new Stock("RELIANCE", "Reliance Industries Ltd.", 2980.50));
    }

    /**
     * Helper to add a stock to the exchange registry.
     *
     * @param stock Stock instance to register
     */
    public void addStock(Stock stock) {
        stocks.put(stock.getSymbol().toUpperCase(), stock);
    }

    /**
     * Retrieves a stock by ticker symbol.
     *
     * @param symbol Ticker symbol (case-insensitive)
     * @return Stock object or null if not found
     */
    public Stock getStock(String symbol) {
        if (symbol == null) return null;
        return stocks.get(symbol.trim().toUpperCase());
    }

    /**
     * Checks if a stock exists in the market.
     *
     * @param symbol Ticker symbol to query
     * @return true if listed, false otherwise
     */
    public boolean containsStock(String symbol) {
        if (symbol == null) return false;
        return stocks.containsKey(symbol.trim().toUpperCase());
    }

    /**
     * Simulates the transition to the next trading day.
     * Every registered stock experiences random price variation (+/- 5%).
     */
    public void simulateMarketUpdate() {
        for (Stock stock : stocks.values()) {
            stock.updatePrice();
        }
    }

    /**
     * Renders a beautifully formatted, aligned console table
     * displaying all market securities, prices, and daily % change.
     */
    public void displayMarketData() {
        System.out.println("\n" + ANSI_CYAN + "=".repeat(78) + ANSI_RESET);
        System.out.printf(ANSI_BOLD + ANSI_CYAN + " %-8s | %-28s | %-12s | %-10s | %-8s %n" + ANSI_RESET,
                "SYMBOL", "COMPANY NAME", "PRICE (\u20B9)", "PREV (\u20B9)", "CHANGE %");
        System.out.println(ANSI_CYAN + "-".repeat(78) + ANSI_RESET);

        for (Stock stock : stocks.values()) {
            double change = stock.getChangePercentage();
            String color = ANSI_RESET;
            String directionSymbol = "\u25AC";

            if (change > 0) {
                color = ANSI_GREEN;
                directionSymbol = "\u25B2";
            } else if (change < 0) {
                color = ANSI_RED;
                directionSymbol = "\u25BC";
            }

            System.out.printf(" %-8s | %-28s | \u20B9%-11.2f | \u20B9%-9.2f | %s%s %+.2f%%%s%n",
                    stock.getSymbol(),
                    stock.getCompanyName().length() > 28 ? stock.getCompanyName().substring(0, 25) + "..." : stock.getCompanyName(),
                    stock.getCurrentPrice(),
                    stock.getPreviousPrice(),
                    color,
                    directionSymbol,
                    change,
                    ANSI_RESET);
        }
        System.out.println(ANSI_CYAN + "=".repeat(78) + ANSI_RESET);
    }

    /**
     * Returns an unmodifiable view of all stocks in the market.
     *
     * @return Read-only map of stocks
     */
    public Map<String, Stock> getStocks() {
        return Collections.unmodifiableMap(stocks);
    }
}
