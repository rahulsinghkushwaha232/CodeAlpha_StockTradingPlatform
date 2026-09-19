package com.codealpha.stocktrading;

import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Main entry point and interactive console controller for the Stock Trading Platform.
 * Developed for the CodeAlpha Internship project submission.
 *
 * Implements a robust menu-driven interface with defensive input validation,
 * market simulation, portfolio management, and automatic file persistence.
 */
public class StockTradingPlatform {

    // ANSI Escape Codes for rich terminal visual styling
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_BOLD = "\u001B[1m";

    // Symbol constants
    private static final String OK_SYMBOL = "\u2714";
    private static final String ERR_SYMBOL = "\u2718";
    private static final String RUPEE_SYMBOL = "\u20B9";

    private static final String DATA_FILE = "portfolio.dat";

    private final Market market;
    private User user;
    private final Scanner scanner;

    /**
     * Initializes the platform with an exchange market and default scanner.
     */
    public StockTradingPlatform() {
        this.market = new Market();
        // Use UTF-8 InputStreamReader for correct symbol input on Windows
        this.scanner = new Scanner(new InputStreamReader(System.in, StandardCharsets.UTF_8));
    }

    /**
     * Main method launching the Stock Trading Platform application.
     *
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        try {
            // Force UTF-8 output for Windows terminal (fixes ₹, ▲, ▼, ✔ symbols)
            PrintStream utf8Out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
            PrintStream utf8Err = new PrintStream(System.err, true, StandardCharsets.UTF_8);
            System.setOut(utf8Out);
            System.setErr(utf8Err);
        } catch (Exception ignored) {
        }
        StockTradingPlatform app = new StockTradingPlatform();
        app.start();
    }

    /**
     * Starts the application, manages profile loading, and drives the main loop.
     */
    public void start() {
        printBanner();

        // Check for existing persistent portfolio file
        File saveFile = new File(DATA_FILE);
        if (saveFile.exists() && saveFile.length() > 0) {
            user = DataManager.loadPortfolio(DATA_FILE);
            if (user != null) {
                System.out.println(ANSI_GREEN + OK_SYMBOL + " Existing portfolio found and auto-loaded for trader: " 
                        + ANSI_BOLD + user.getUsername() + ANSI_RESET);
                System.out.printf("  Current Cash Balance: " + RUPEE_SYMBOL + "%.2f | Total Holdings: %d stocks%n",
                        user.getPortfolio().getCashBalance(),
                        user.getPortfolio().getHoldings().size());
            } else {
                createNewUserProfile();
            }
        } else {
            createNewUserProfile();
        }

        // Main Menu Loop
        boolean running = true;
        while (running) {
            displayMenu();
            int choice = readIntInput("Select an option (1-7): ", 1, 7);

            switch (choice) {
                case 1:
                    market.displayMarketData();
                    break;
                case 2:
                    handleBuyStock();
                    break;
                case 3:
                    handleSellStock();
                    break;
                case 4:
                    user.getPortfolio().displayHoldings(market);
                    break;
                case 5:
                    user.getPortfolio().displayTransactionHistory();
                    break;
                case 6:
                    handleSimulateDay();
                    break;
                case 7:
                    handleSaveAndExit();
                    running = false;
                    break;
                default:
                    System.out.println(ANSI_RED + "Invalid selection. Please try again." + ANSI_RESET);
                    break;
            }

            if (running) {
                promptEnterKey();
            }
        }

        scanner.close();
    }

    /**
     * Welcomes a new user and sets up initial virtual cash balance.
     */
    private void createNewUserProfile() {
        System.out.println(ANSI_CYAN + "Welcome to your new trading journey! Let's set up your profile." + ANSI_RESET);
        String username = readStringInput("Enter your Trader / Investor name: ");
        if (username.isEmpty()) {
            username = "CodeAlpha Trader";
        }
        user = new User(username);
        System.out.printf(ANSI_GREEN + OK_SYMBOL + " Account created for %s with starting virtual cash: " + RUPEE_SYMBOL + "%.2f%n" + ANSI_RESET,
                user.getUsername(), user.getPortfolio().getCashBalance());
    }

    /**
     * Displays the stylized platform banner.
     */
    private void printBanner() {
        System.out.println(ANSI_CYAN + ANSI_BOLD);
        System.out.println("========================================================================");
        System.out.println("                CODEALPHA STOCK TRADING PLATFORM                        ");
        System.out.println("            Real-time Simulation & Portfolio Management                 ");
        System.out.println("========================================================================");
        System.out.println(ANSI_RESET);
    }

    /**
     * Prints the primary application navigation menu.
     */
    private void displayMenu() {
        System.out.println("\n" + ANSI_BOLD + "====== MAIN TRADING DASHBOARD ======" + ANSI_RESET);
        System.out.println(" [1] View Market Data");
        System.out.println(" [2] Buy Stock");
        System.out.println(" [3] Sell Stock");
        System.out.println(" [4] View Portfolio & Profit/Loss");
        System.out.println(" [5] View Transaction History");
        System.out.println(" [6] Simulate Next Trading Day");
        System.out.println(" [7] Save & Exit");
        System.out.println("====================================");
    }

    /**
     * Handles stock purchasing with comprehensive validation:
     * - verifies stock existence in market
     * - verifies positive quantity
     * - verifies sufficient cash funds
     */
    private void handleBuyStock() {
        System.out.println("\n" + ANSI_BOLD + "--- BUY STOCK ORDER ---" + ANSI_RESET);
        market.displayMarketData();

        String symbol = readStringInput("\nEnter Stock Symbol to BUY (or 'C' to cancel): ").toUpperCase();
        if (symbol.equalsIgnoreCase("C") || symbol.isEmpty()) {
            System.out.println("Order cancelled.");
            return;
        }

        Stock stock = market.getStock(symbol);
        if (stock == null) {
            System.out.println(ANSI_RED + ERR_SYMBOL + " Error: Stock symbol '" + symbol + "' not listed in the market." + ANSI_RESET);
            return;
        }

        double availableCash = user.getPortfolio().getCashBalance();
        double price = stock.getCurrentPrice();
        int maxAffordable = (int) (availableCash / price);

        System.out.printf("Selected: %s (%s) | Current Price: " + RUPEE_SYMBOL + "%.2f%n",
                stock.getSymbol(), stock.getCompanyName(), price);
        System.out.printf("Available Cash: " + RUPEE_SYMBOL + "%.2f (Max affordable shares: %d)%n",
                availableCash, maxAffordable);

        if (maxAffordable <= 0) {
            System.out.println(ANSI_RED + ERR_SYMBOL + " Insufficient funds to buy even 1 share of " + stock.getSymbol() + "." + ANSI_RESET);
            return;
        }

        int quantity = readIntInput(String.format("Enter quantity to purchase (1-%d): ", maxAffordable), 1, Integer.MAX_VALUE);
        double totalCost = Math.round((quantity * price) * 100.0) / 100.0;

        if (totalCost > availableCash) {
            System.out.printf(ANSI_RED + ERR_SYMBOL + " Insufficient cash balance! Required: " + RUPEE_SYMBOL + "%.2f | Available: " + RUPEE_SYMBOL + "%.2f%n" + ANSI_RESET,
                    totalCost, availableCash);
            return;
        }

        boolean success = user.getPortfolio().buyStock(stock, quantity);
        if (success) {
            System.out.println(ANSI_GREEN + OK_SYMBOL + " Order Executed Successfully!" + ANSI_RESET);
            System.out.printf("  Bought %d shares of %s at " + RUPEE_SYMBOL + "%.2f (Total: " + RUPEE_SYMBOL + "%.2f)%n",
                    quantity, stock.getSymbol(), price, totalCost);
            System.out.printf("  Remaining Cash Balance: " + RUPEE_SYMBOL + "%.2f%n", user.getPortfolio().getCashBalance());
        } else {
            System.out.println(ANSI_RED + ERR_SYMBOL + " Trade execution failed. Please check inputs." + ANSI_RESET);
        }
    }

    /**
     * Handles stock selling with validation:
     * - verifies stock exists in user's holdings
     * - verifies positive quantity
     * - verifies quantity does not exceed owned shares
     */
    private void handleSellStock() {
        System.out.println("\n" + ANSI_BOLD + "--- SELL STOCK ORDER ---" + ANSI_RESET);

        if (user.getPortfolio().getHoldings().isEmpty()) {
            System.out.println(ANSI_YELLOW + "You do not own any stocks to sell." + ANSI_RESET);
            return;
        }

        user.getPortfolio().displayHoldings(market);

        String symbol = readStringInput("\nEnter Stock Symbol to SELL (or 'C' to cancel): ").toUpperCase();
        if (symbol.equalsIgnoreCase("C") || symbol.isEmpty()) {
            System.out.println("Order cancelled.");
            return;
        }

        Holding holding = user.getPortfolio().getHoldings().get(symbol);
        if (holding == null || holding.getQuantity() <= 0) {
            System.out.printf(ANSI_RED + ERR_SYMBOL + " Error: You do not own any shares of '%s'.%n" + ANSI_RESET, symbol);
            return;
        }

        Stock stock = market.getStock(symbol);
        if (stock == null) {
            System.out.println(ANSI_RED + ERR_SYMBOL + " Error: Stock currently unavailable on exchange." + ANSI_RESET);
            return;
        }

        int ownedQty = holding.getQuantity();
        double currentPrice = stock.getCurrentPrice();

        System.out.printf("Selected: %s | Owned: %d shares | Current Market Price: " + RUPEE_SYMBOL + "%.2f%n",
                stock.getSymbol(), ownedQty, currentPrice);

        int quantity = readIntInput(String.format("Enter quantity to sell (1-%d): ", ownedQty), 1, ownedQty);
        double totalRevenue = Math.round((quantity * currentPrice) * 100.0) / 100.0;

        boolean success = user.getPortfolio().sellStock(stock, quantity);
        if (success) {
            System.out.println(ANSI_GREEN + OK_SYMBOL + " Sell Order Executed Successfully!" + ANSI_RESET);
            System.out.printf("  Sold %d shares of %s at " + RUPEE_SYMBOL + "%.2f (Proceeds: " + RUPEE_SYMBOL + "%.2f)%n",
                    quantity, stock.getSymbol(), currentPrice, totalRevenue);
            System.out.printf("  Updated Cash Balance: " + RUPEE_SYMBOL + "%.2f%n", user.getPortfolio().getCashBalance());
        } else {
            System.out.println(ANSI_RED + ERR_SYMBOL + " Sell order execution failed." + ANSI_RESET);
        }
    }

    /**
     * Simulates the next trading day by recalculating all stock prices
     * within a +/- 5% fluctuation range and displaying updated figures.
     */
    private void handleSimulateDay() {
        System.out.println("\n" + ANSI_YELLOW + "[~] Simulating the next trading session on the exchange..." + ANSI_RESET);
        market.simulateMarketUpdate();
        System.out.println(ANSI_GREEN + OK_SYMBOL + " Market prices updated with day's fluctuations (+/- 5%)!" + ANSI_RESET);
        market.displayMarketData();
    }

    /**
     * Persists portfolio state to file and terminates the program cleanly.
     */
    private void handleSaveAndExit() {
        System.out.println("\n" + ANSI_CYAN + "Saving your trading portfolio to '" + DATA_FILE + "'..." + ANSI_RESET);
        boolean saved = DataManager.savePortfolio(user, DATA_FILE);
        if (saved) {
            System.out.println(ANSI_GREEN + OK_SYMBOL + " Portfolio data successfully saved!" + ANSI_RESET);
        } else {
            System.out.println(ANSI_RED + ERR_SYMBOL + " Notice: Could not save portfolio data to disk." + ANSI_RESET);
        }

        System.out.println(ANSI_CYAN + "\nThank you for trading with CodeAlpha Stock Trading Platform. Happy Investing!" + ANSI_RESET);
    }

    /**
     * Safely reads an integer with try-catch validation against InputMismatchException.
     * Protects the console from crashing when the user types invalid data or non-integers.
     *
     * @param prompt Text prompt to present to the user
     * @param min    Minimum acceptable integer
     * @param max    Maximum acceptable integer
     * @return Validated integer within range
     */
    private int readIntInput(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            try {
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    System.out.println(ANSI_RED + "Input cannot be empty. Please enter a valid number." + ANSI_RESET);
                    continue;
                }
                int value = Integer.parseInt(input);
                if (value < min || value > max) {
                    System.out.printf(ANSI_RED + "Value out of range! Please enter a number between %d and %d.%n" + ANSI_RESET, min, max);
                    continue;
                }
                return value;
            } catch (NumberFormatException | InputMismatchException e) {
                System.out.println(ANSI_RED + "Invalid input! Please enter an integer number." + ANSI_RESET);
            } catch (NoSuchElementException e) {
                // Return default fallback if stream closed abruptly
                return min;
            }
        }
    }

    /**
     * Safely reads non-null string input from scanner.
     *
     * @param prompt Text prompt to display
     * @return Trimmed string input
     */
    private String readStringInput(String prompt) {
        System.out.print(prompt);
        try {
            if (scanner.hasNextLine()) {
                String input = scanner.nextLine().trim();
                input = input.replaceAll("^[\\uFEFF\\u00EF\\u00BB\\u00BF\\s]+", "").trim();
                return input;
            }
        } catch (Exception e) {
            return "";
        }
        return "";
    }

    /**
     * Pauses the console and prompts user to press enter to proceed.
     */
    private void promptEnterKey() {
        System.out.print("\nPress [Enter] to return to the dashboard menu...");
        try {
            if (scanner.hasNextLine()) {
                scanner.nextLine();
            }
        } catch (Exception ignored) {
        }
    }
}
