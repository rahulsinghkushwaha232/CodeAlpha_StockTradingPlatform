package com.codealpha.stocktrading;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Locale;

/**
 * Handles persistent File I/O operations for saving and loading user portfolios.
 * Demonstrates robust stream handling using try-with-resources and defensive IOException handling.
 */
public class DataManager {

    private static final String DEFAULT_FILE_NAME = "portfolio.dat";

    /**
     * Saves user account information, cash balance, active holdings, and transaction history
     * to a persistent data file using try-with-resources.
     *
     * @param user     User object containing portfolio data
     * @param filePath Destination file path
     * @return true if save was successful, false otherwise
     */
    public static boolean savePortfolio(User user, String filePath) {
        if (user == null || user.getPortfolio() == null) {
            System.err.println("Error: Cannot save empty user or portfolio.");
            return false;
        }

        File file = new File(filePath != null ? filePath : DEFAULT_FILE_NAME);
        Portfolio portfolio = user.getPortfolio();

        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            // Write User header
            String cleanUsername = user.getUsername().replaceAll("^[\\uFEFF\\u00EF\\u00BB\\u00BF\\s]+", "").trim();
            writer.write("USER," + cleanUsername);
            writer.newLine();

            // Write Cash balance
            writer.write(String.format(Locale.US, "CASH,%.2f", portfolio.getCashBalance()));
            writer.newLine();

            // Write active holdings
            for (Holding holding : portfolio.getHoldings().values()) {
                writer.write(String.format(Locale.US, "HOLDING,%s,%d,%.2f",
                        holding.getStockSymbol(),
                        holding.getQuantity(),
                        holding.getAverageBuyPrice()));
                writer.newLine();
            }

            // Write transaction history
            for (Transaction tx : portfolio.getTransactionHistory()) {
                writer.write(String.format(Locale.US, "TRANSACTION,%s,%s,%d,%.2f,%s",
                        tx.getStockSymbol(),
                        tx.getType().name(),
                        tx.getQuantity(),
                        tx.getPricePerShare(),
                        tx.getTimestamp().toString()));
                writer.newLine();
            }

            return true;
        } catch (IOException e) {
            System.err.println("Failed to save portfolio data: " + e.getMessage());
            return false;
        }
    }

    /**
     * Loads user profile, cash balance, holdings, and transactions from the data file.
     * Uses try-with-resources to ensure file streams are safely closed.
     *
     * @param filePath Path to the saved portfolio file
     * @return Loaded User object with reconstructed portfolio, or null if file doesn't exist
     */
    public static User loadPortfolio(String filePath) {
        File file = new File(filePath != null ? filePath : DEFAULT_FILE_NAME);
        if (!file.exists() || file.length() == 0) {
            return null; // No existing save file found
        }

        String username = "Default Trader";
        Portfolio portfolio = new Portfolio();

        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.replaceAll("^[\\uFEFF\\u00EF\\u00BB\\u00BF\\s]+", "").trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue; // Skip empty lines and comments
                }

                String[] parts = line.split(",");
                if (parts.length < 2) {
                    continue;
                }

                String recordType = parts[0].trim().toUpperCase();

                switch (recordType) {
                    case "USER":
                        username = parts[1].trim();
                        break;

                    case "CASH":
                        double cash = Double.parseDouble(parts[1].trim());
                        portfolio.setCashBalance(cash);
                        break;

                    case "HOLDING":
                        if (parts.length >= 4) {
                            String symbol = parts[1].trim();
                            int qty = Integer.parseInt(parts[2].trim());
                            double avgPrice = Double.parseDouble(parts[3].trim());
                            portfolio.addRestoredHolding(new Holding(symbol, qty, avgPrice));
                        }
                        break;

                    case "TRANSACTION":
                        if (parts.length >= 6) {
                            String symbol = parts[1].trim();
                            TransactionType type = TransactionType.valueOf(parts[2].trim());
                            int qty = Integer.parseInt(parts[3].trim());
                            double price = Double.parseDouble(parts[4].trim());
                            LocalDateTime timestamp = LocalDateTime.parse(parts[5].trim());
                            portfolio.addRestoredTransaction(new Transaction(symbol, type, qty, price, timestamp));
                        }
                        break;

                    default:
                        // Ignore unrecognized record types for forward compatibility
                        break;
                }
            }
            return new User(username, portfolio);
        } catch (IOException e) {
            System.err.println("Error reading portfolio file: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("Warning: Corrupted records encountered while parsing portfolio. " + e.getMessage());
            return new User(username, portfolio);
        }
    }
}
