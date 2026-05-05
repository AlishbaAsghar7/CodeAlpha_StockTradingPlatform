package service;

import model.*;
import java.util.ArrayList;
import java.util.List;

public class TradingService {

    private User             user;
    private Portfolio        portfolio;
    private Market           market;
    private List<Transaction> transactions;

    public TradingService(User user, Portfolio portfolio, Market market) {
        this.user         = user;
        this.portfolio    = portfolio;
        this.market       = market;
        this.transactions = new ArrayList<>();
    }

    public String buyStock(String symbol, int quantity) {

        if (quantity <= 0) {
            return "ERROR:Quantity must be greater than 0!";
        }
        if (!market.hasStock(symbol)) {
            return "ERROR:Stock symbol not found in market!";
        }

        Stock  stock       = market.getStock(symbol);
        double totalCost   = Math.round((stock.getPrice() * quantity) * 100.0) / 100.0;

        if (user.getBalance() < totalCost) {
            return "ERROR:Insufficient balance! Need Rs " + totalCost +
                   " but have Rs " + user.getBalance();
        }

        user.deductBalance(totalCost);
        portfolio.addShares(symbol, quantity, stock.getPrice());

        Transaction tx = new Transaction("BUY", symbol, quantity, stock.getPrice());
        transactions.add(tx);

        return "SUCCESS:" + symbol + ":" + quantity + ":" + stock.getPrice() + ":" + totalCost;
    }

    public String sellStock(String symbol, int quantity) {
      
        if (quantity <= 0) {
            return "ERROR:Quantity must be greater than 0!";
        }
        if (!market.hasStock(symbol)) {
            return "ERROR:Stock symbol not found in market!";
        }

        int ownedQty = portfolio.getQuantity(symbol);
        if (ownedQty == 0) {
            return "ERROR:You do not own any shares of " + symbol + "!";
        }
        if (ownedQty < quantity) {
            return "ERROR:You only own " + ownedQty + " shares of " + symbol + "!";
        }

        Stock  stock       = market.getStock(symbol);
        double totalEarned = Math.round((stock.getPrice() * quantity) * 100.0) / 100.0;

        portfolio.removeShares(symbol, quantity);
        user.addBalance(totalEarned);

        Transaction tx = new Transaction("SELL", symbol, quantity, stock.getPrice());
        transactions.add(tx);

        return "SUCCESS:" + symbol + ":" + quantity + ":" + stock.getPrice() + ":" + totalEarned;
    }

    public List<Transaction> getTransactions() { return transactions; }
    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }
}