package service;

import model.*;
import java.io.*;
import java.util.*;

public class FileManager {

    private static final String PORTFOLIO_FILE   = "portfolio.txt";
    private static final String TRANSACTION_FILE = "transactions.txt";

    public static void savePortfolio(User user, Portfolio portfolio, Market market) {
        try (FileWriter fw = new FileWriter(PORTFOLIO_FILE)) {
            fw.write("===== PORTFOLIO DATA =====\n");
            fw.write("USER:" + user.getUsername() + "\n");
            fw.write("BALANCE:" + user.getBalance() + "\n");
            fw.write("INITIAL_BALANCE:" + user.getInitialBalance() + "\n");
            fw.write("---HOLDINGS---\n");

            Map<String, Integer> holdings = portfolio.getHoldings();
            for (Map.Entry<String, Integer> entry : holdings.entrySet()) {
                String symbol  = entry.getKey();
                int    qty     = entry.getValue();
                double avgPrice = portfolio.getAvgPrice(symbol);
                fw.write("HOLDING:" + symbol + ":" + qty + ":" + avgPrice + "\n");
            }
            fw.write("END\n");
            System.out.println("Portfolio saved to " + PORTFOLIO_FILE);

        } catch (Exception e) {
            System.out.println("Save portfolio error: " + e.getMessage());
        }
    }

    public static boolean loadPortfolio(User user, Portfolio portfolio) {
        File file = new File(PORTFOLIO_FILE);
        if (!file.exists()) return false;

        try (BufferedReader br = new BufferedReader(new FileReader(PORTFOLIO_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("BALANCE:")) {
                    user.setBalance(Double.parseDouble(line.split(":")[1]));
                } else if (line.startsWith("HOLDING:")) {
                    String[] parts  = line.split(":");
                    String symbol   = parts[1];
                    int    qty      = Integer.parseInt(parts[2]);
                    double avgPrice = Double.parseDouble(parts[3]);
                   
                    for (int i = 0; i < qty; i++) {
                        portfolio.addShares(symbol, 1, avgPrice);
                    }
                }
            }
            System.out.println("Portfolio loaded from " + PORTFOLIO_FILE);
            return true;

        } catch (Exception e) {
            System.out.println("Load portfolio error: " + e.getMessage());
            return false;
        }
    }

    public static void saveTransactions(List<Transaction> transactions) {
        try (FileWriter fw = new FileWriter(TRANSACTION_FILE)) {
            fw.write("===== TRANSACTION HISTORY =====\n\n");
            for (Transaction tx : transactions) {
                fw.write("Type       : " + tx.getType()         + "\n");
                fw.write("Symbol     : " + tx.getSymbol()       + "\n");
                fw.write("Quantity   : " + tx.getQuantity()     + "\n");
                fw.write("Price/Share: Rs " + tx.getPricePerShare() + "\n");
                fw.write("Total      : Rs " + tx.getTotalAmount()   + "\n");
                fw.write("Date/Time  : " + tx.getDateTime()     + "\n");
                fw.write("------------------------------\n");
            }
            System.out.println("Transactions saved to " + TRANSACTION_FILE);

        } catch (Exception e) {
            System.out.println("Save transactions error: " + e.getMessage());
        }
    }

    public static String exportFullReport(User user, Portfolio portfolio,
                                          Market market, List<Transaction> transactions) {
        String reportFile = "stock_report.txt";
        try (FileWriter fw = new FileWriter(reportFile)) {
            fw.write("========================================\n");
            fw.write("       STOCK TRADING PLATFORM REPORT    \n");
            fw.write("========================================\n\n");

            fw.write("TRADER      : " + user.getUsername() + "\n");
            fw.write("Cash Balance: Rs " + user.getBalance() + "\n");

            double portfolioValue = portfolio.getTotalValue(market.getAllStocks());
            double profitLoss     = portfolio.getProfitLoss(market.getAllStocks());
            double totalAssets    = user.getBalance() + portfolioValue;

            fw.write("Portfolio   : Rs " + portfolioValue + "\n");
            fw.write("Total Assets: Rs " + totalAssets + "\n");
            fw.write("Profit/Loss : Rs " + profitLoss +
                     (profitLoss >= 0 ? " (PROFIT)" : " (LOSS)") + "\n\n");

            fw.write("--- CURRENT HOLDINGS ---\n");
            Map<String, Integer> holdings = portfolio.getHoldings();
            if (holdings.isEmpty()) {
                fw.write("No stocks owned.\n");
            } else {
                for (Map.Entry<String, Integer> entry : holdings.entrySet()) {
                    String symbol   = entry.getKey();
                    int    qty      = entry.getValue();
                    Stock  stock    = market.getStock(symbol);
                    double avgPrice = portfolio.getAvgPrice(symbol);
                    double pl       = stock != null ?
                                     Math.round((stock.getPrice() - avgPrice) * qty * 100.0) / 100.0 : 0;
                    fw.write("  " + symbol + " | Qty: " + qty +
                             " | Avg Buy: Rs " + avgPrice +
                             " | Current: Rs " + (stock != null ? stock.getPrice() : "N/A") +
                             " | P/L: Rs " + pl + "\n");
                }
            }

            fw.write("\n--- TRANSACTION HISTORY ---\n");
            if (transactions.isEmpty()) {
                fw.write("No transactions yet.\n");
            } else {
                for (Transaction tx : transactions) {
                    fw.write(tx.toString() + "\n");
                }
            }

            fw.write("\n========================================\n");

        } catch (Exception e) {
            return "Export failed: " + e.getMessage();
        }

        return new File(reportFile).getAbsolutePath();
    }

    public static String getPortfolioFilePath() {
        return new File(PORTFOLIO_FILE).getAbsolutePath();
    }

    public static String getTransactionFilePath() {
        return new File(TRANSACTION_FILE).getAbsolutePath();
    }
}