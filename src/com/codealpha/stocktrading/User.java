package com.codealpha.stocktrading;

/**
 * Represents a registered user/investor on the trading platform.
 * Encapsulates the user's credentials and personal portfolio.
 */
public class User {
    private String username;
    private Portfolio portfolio;

    /**
     * Constructs a new User with default initial funds.
     *
     * @param username The investor's name or handle
     */
    public User(String username) {
        this.username = sanitizeUsername(username);
        this.portfolio = new Portfolio();
    }

    public User(String username, Portfolio portfolio) {
        this.username = sanitizeUsername(username);
        this.portfolio = portfolio;
    }

    private static String sanitizeUsername(String name) {
        if (name == null) return "Trader";
        // Strip BOM (\uFEFF) and ISO-8859-1 decoded BOM artifacts (\u00EF\u00BB\u00BF)
        String clean = name.replaceAll("^[\\uFEFF\\u00EF\\u00BB\\u00BF\\s]+", "").trim();
        return clean.isEmpty() ? "Trader" : clean;
    }

    // --- Getters and Setters ---

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = sanitizeUsername(username);
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    public void setPortfolio(Portfolio portfolio) {
        this.portfolio = portfolio;
    }
}
