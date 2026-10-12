# CodeAlpha_StockTradingPlatform

A complete, production-grade Java console application developed for the **CodeAlpha Java Programming Internship** submission. The platform provides a simulated real-time financial stock exchange, interactive portfolio management, market fluctuation simulation, trade auditing, and robust file persistence.

---

## Table of Contents
1. [Project Overview](#project-overview)
2. [Key Features](#key-features)
3. [Object-Oriented Programming (OOP) Concepts](#object-oriented-programming-oop-concepts)
4. [Architecture & Class Design](#architecture--class-design)
5. [System Requirements](#system-requirements)
6. [Compilation & Execution Guide](#compilation--execution-guide)
7. [Sample Console Walkthrough](#sample-console-walkthrough)
8. [Data Persistence Specification](#data-persistence-specification)

---

## Project Overview

The **Stock Trading Platform** allows users to experience equity trading in a simulated environment starting with virtual capital of **₹100,000.00**. Investors can monitor live ticker prices of major multinational and Indian blue-chip corporations, buy and sell shares, simulate market trading sessions, track real-time portfolio valuations with weighted-average cost basis, and review chronological transaction ledgers.

---

## Key Features

- **Live Market Data Board**:
  - Displays real-time quotes for 8 prominent stocks across US and Indian exchanges:
    `AAPL`, `GOOGL`, `TSLA`, `MSFT`, `AMZN`, `INFY`, `TCS`, and `RELIANCE`.
  - Shows current price, previous close, percentage change, and trend direction symbols (`▲`, `▼`, `▬`).

- **Order Execution Engine**:
  - **Buy Orders**: Validates ticker existence, ensures non-zero positive quantities, and enforces strict cash balance limits. Automatically computes maximum affordable shares.
  - **Sell Orders**: Verifies owned holdings, prevents short sales or over-selling, updates remaining share counts, and releases proceeds instantly into available cash.

- **Dynamic Position Tracking**:
  - Automatically recalculates **weighted-average buy price** whenever additional shares of an existing holding are purchased:
    $$\text{Average Buy Price} = \frac{(\text{Existing Quantity} \times \text{Average Price}) + (\text{New Quantity} \times \text{New Price})}{\text{Existing Quantity} + \text{New Quantity}}$$

- **Portfolio & P/L Analytics**:
  - Real-time valuation comparing invested capital against current market value.
  - Color-coded Unrealized Profit/Loss with percentage yields and directional indicators:
    - `▲` **Green** for profitable positions
    - `▼` **Red** for losing positions
    - `▬` for break-even positions

- **Trading Session Simulation**:
  - Advances to the next trading day, simulating market volatility with realistic random price movements within `±5.0%` for all registered stocks.

- **Transaction Audit Ledger**:
  - Logs every executed trade with timestamp (`YYYY-MM-DD HH:MM:SS`), order type (`BUY` / `SELL`), ticker symbol, share count, execution price, and total settlement amount.

- **Defensive Input Handling**:
  - Zero-crash console interface: gracefully catches `InputMismatchException`, `NumberFormatException`, out-of-range choices, invalid string inputs, and stream interruptions.

- **File I/O Persistence**:
  - Saves the entire portfolio state (username, cash balance, holdings, and transaction history) to `portfolio.dat` on exit using **try-with-resources**.
  - Automatically detects and reloads `portfolio.dat` on startup, allowing traders to resume their progress seamlessly across sessions.

---

## Object-Oriented Programming (OOP) Concepts

This project is architected strictly around clean Java OOP principles:

### 1. Encapsulation
- All class fields across `Stock`, `Holding`, `Transaction`, `Portfolio`, and `User` are marked `private` or `private final`.
- Access is provided exclusively through calibrated public getters and business methods.
- Internal invariants (such as preventing negative balances, non-negative quantities, and floor stock prices) are strictly guarded inside the classes.

### 2. Abstraction
- The user interface in `StockTradingPlatform` interacts with high-level domain operations (`portfolio.buyStock(...)`, `market.simulateMarketUpdate()`, `DataManager.loadPortfolio(...)`) without exposing low-level mathematical formulas, hash structures, or file I/O streams.

### 3. Collections Framework
- **`Map<String, Stock>` (`LinkedHashMap`)**: Preserves predictable ticker listing order for exchange presentation.
- **`Map<String, Holding>` (`TreeMap`)**: Automatically organizes portfolio holdings alphabetically by ticker symbol.
- **`List<Transaction>` (`ArrayList`)**: Maintains an ordered, chronological audit ledger of financial transactions.
- **`Collections.unmodifiableList(...)` & `Collections.unmodifiableMap(...)`**: Prevents external code from modifying internal collections without invoking authorized transaction methods.

### 4. Type-Safe Enums
- `TransactionType` defines `BUY` and `SELL` constants, preventing arbitrary string errors and ensuring compile-time safety across order processing.

### 5. File I/O & Exception Handling
- Streams (`BufferedWriter`, `BufferedReader`, `Files`) are wrapped in **try-with-resources** blocks to guarantee safe resource release even if I/O exceptions occur.
- User input is validated with recursive/looped try-catch blocks to ensure that invalid or malformed entries never terminate the program abruptly.

---

## Architecture & Class Design

```
src/com/codealpha/stocktrading/
├── Stock.java               # Model for equities with price simulation & change calculation
├── TransactionType.java     # Type-safe enum for BUY / SELL order operations
├── Transaction.java         # Immutable record representing an executed trade
├── Holding.java             # Position entity with weighted-average cost basis logic
├── Portfolio.java           # Valuation engine, order executor, and P/L reporter
├── Market.java              # Exchange registry with pre-loaded stocks & volatility engine
├── User.java                # Investor entity holding credentials and portfolio instance
├── DataManager.java         # Persistent file I/O engine using try-with-resources
└── StockTradingPlatform.java# Console controller, menu dashboard, and input validator
```

---

## System Requirements

- **Java Development Kit (JDK)**: Version 8 or higher (tested with OpenJDK / Oracle JDK 17 & 21).
- **Operating System**: Windows 10/11, macOS, or Linux.
- **Terminal**: Any standard console, PowerShell, Command Prompt, or terminal emulator (supports ANSI color sequences and UTF-8).

---

## Compilation & Execution Guide

### 1. Open Terminal in the Repository Root
Navigate to the root directory where the `src/` folder is located.

```bash
cd CodeAlpha_StockTradingPlatform
```

### 2. Compile All Java Source Files
Compile all source files into the `bin` directory:

```bash
# Windows (PowerShell / Command Prompt)
javac -d bin src/com/codealpha/stocktrading/*.java

# macOS / Linux
javac -d bin src/com/codealpha/stocktrading/*.java
```

### 3. Run the Application
Launch the main application using the compiled classpath:

```bash
# Windows / macOS / Linux
java -cp bin com.codealpha.stocktrading.StockTradingPlatform
```

> [!TIP]
> On Windows PowerShell or Command Prompt, for the best visual experience with Indian Rupee (`₹`) and direction symbols (`▲`, `▼`), ensure your terminal is set to UTF-8 by running `chcp 65001` before executing the `java` command.

---

## Sample Console Walkthrough

### 1. Main Dashboard & Market Data
```text
========================================================================
                CODEALPHA STOCK TRADING PLATFORM                        
            Real-time Simulation & Portfolio Management                 
========================================================================

Welcome to your new trading journey! Let's set up your profile.
Enter your Trader / Investor name: Alexander
✔ Account created for Alexander with starting virtual cash: ₹100000.00

====== MAIN TRADING DASHBOARD ======
 [1] View Market Data
 [2] Buy Stock
 [3] Sell Stock
 [4] View Portfolio & Profit/Loss
 [5] View Transaction History
 [6] Simulate Next Trading Day
 [7] Save & Exit
====================================
Select an option (1-7): 1

==============================================================================
 SYMBOL   | COMPANY NAME                 | PRICE (₹)    | PREV (₹)   | CHANGE % 
------------------------------------------------------------------------------
 AAPL     | Apple Inc.                   | ₹185.50      | ₹185.50    | ▬ +0.00%
 GOOGL    | Alphabet Inc.                | ₹175.25      | ₹175.25    | ▬ +0.00%
 TSLA     | Tesla Inc.                   | ₹240.80      | ₹240.80    | ▬ +0.00%
 MSFT     | Microsoft Corp.              | ₹420.10      | ₹420.10    | ▬ +0.00%
 AMZN     | Amazon.com Inc.              | ₹180.60      | ₹180.60    | ▬ +0.00%
 INFY     | Infosys Ltd.                 | ₹1620.00     | ₹1620.00   | ▬ +0.00%
 TCS      | Tata Consultancy Services    | ₹3950.00     | ₹3950.00   | ▬ +0.00%
 RELIANCE | Reliance Industries Ltd.     | ₹2980.50     | ₹2980.50   | ▬ +0.00%
==============================================================================
```

### 2. Buying Stocks
```text
Select an option (1-7): 2

--- BUY STOCK ORDER ---
...
Enter Stock Symbol to BUY (or 'C' to cancel): AAPL
Selected: AAPL (Apple Inc.) | Current Price: ₹185.50
Available Cash: ₹100000.00 (Max affordable shares: 539)
Enter quantity to purchase (1-539): 10
✔ Order Executed Successfully!
  Bought 10 shares of AAPL at ₹185.50 (Total: ₹1855.00)
  Remaining Cash Balance: ₹98145.00
```

### 3. Simulating Next Trading Day & Live P/L
```text
Select an option (1-7): 6

[~] Simulating the next trading session on the exchange...
✔ Market prices updated with day's fluctuations (+/- 5%)!

==============================================================================
 SYMBOL   | COMPANY NAME                 | PRICE (₹)    | PREV (₹)   | CHANGE % 
------------------------------------------------------------------------------
 AAPL     | Apple Inc.                   | ₹189.21      | ₹185.50    | ▲ +2.00%
 GOOGL    | Alphabet Inc.                | ₹171.30      | ₹175.25    | ▼ -2.25%
 ...
==============================================================================

Select an option (1-7): 4

========================================================================================
 SYMBOL   | SHARES | AVG BUY (₹)  | CURRENT (₹)  | INVESTED (₹) | VALUATION(₹) | P/L        
----------------------------------------------------------------------------------------
 AAPL     | 10     | ₹185.50      | ₹189.21      | ₹1855.00     | ₹1892.10     | ▲ +2.00%
========================================================================================
 Cash Balance          : ₹98145.00
 Total Invested Capital: ₹1855.00
 Holdings Market Value : ₹1892.10
 Total Portfolio Value : ₹100037.10
 Unrealized Profit/Loss:  ▲₹37.10 (+2.00%)
========================================================================================
```

### 4. Transaction Ledger Audit
```text
Select an option (1-7): 5

================================================================================
 TIMESTAMP           | TYPE  | SYMBOL   | SHARES | PRICE (₹)    | TOTAL (₹)    
--------------------------------------------------------------------------------
 2026-09-18 16:29:46 | BUY   | AAPL     | 10     | ₹185.50      | ₹1855.00     
 2026-09-18 16:30:12 | SELL  | AAPL     | 5      | ₹189.21      | ₹946.05      
================================================================================
```

### 5. Persistence (Save & Auto-Reload)
```text
Select an option (1-7): 7

Saving your trading portfolio to 'portfolio.dat'...
✔ Portfolio data successfully saved!

Thank you for trading with CodeAlpha Stock Trading Platform. Happy Investing!
```

*Upon restarting:*
```text
✔ Existing portfolio found and auto-loaded for trader: Alexander
  Current Cash Balance: ₹99091.05 | Total Holdings: 1 stocks
```

---

## Data Persistence Specification

Portfolio state is persisted to `portfolio.dat` using structured text records:
- `USER,<username>`: Investor identity.
- `CASH,<amount>`: Current liquid cash balance.
- `HOLDING,<symbol>,<quantity>,<averageBuyPrice>`: Active shareholdings.
- `TRANSACTION,<symbol>,<type>,<quantity>,<pricePerShare>,<ISO_timestamp>`: Historical trade audits.

This format provides transparent human readability, cross-platform stability, and immunity to Java serialization version mismatches.

---

## Author & Submission Details
- **Author**: Rahul Singh Kushwaha
- **Internship**: CodeAlpha Java Programming Internship
- **Project**: Stock Trading Platform
- **Repository**: [CodeAlpha_StockTradingPlatform](https://github.com/rahulsinghkushwaha232/CodeAlpha_StockTradingPlatform)
- **GitHub**: [rahulsinghkushwaha232](https://github.com/rahulsinghkushwaha232)
- **Language**: Java (JDK 17)
