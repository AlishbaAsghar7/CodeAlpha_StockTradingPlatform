package model;

import java.util.HashMap;
import java.util.Map;

public class Portfolio {
  
    private Map<String, Integer> holdings = new HashMap<>();
    
    private Map<String, Double> avgBuyPrice = new HashMap<>();

    public void addShares(String symbol, int quantity, double buyPrice) {
        int currentQty       = holdings.getOrDefault(symbol, 0);
        double currentAvg    = avgBuyPrice.getOrDefault(symbol, 0.0);

        double totalCost     = (currentQty * currentAvg) + (quantity * buyPrice);
        int newQty           = currentQty + quantity;
        double newAvg        = totalCost / newQty;

        holdings.put(symbol, newQty);
        avgBuyPrice.put(symbol, Math.round(newAvg * 100.0) / 100.0);
    }

    public boolean removeShares(String symbol, int quantity) {
        int currentQty = holdings.getOrDefault(symbol, 0);
        if (currentQty < quantity) return false;

        int remaining = currentQty - quantity;
        if (remaining == 0) {
            holdings.remove(symbol);
            avgBuyPrice.remove(symbol);
        } else {
            holdings.put(symbol, remaining);
        }
        return true;
    }

    public double getTotalValue(Map<String, Stock> marketStocks) {
        double total = 0;
        for (Map.Entry<String, Integer> entry : holdings.entrySet()) {
            Stock stock = marketStocks.get(entry.getKey());
            if (stock != null) {
                total += stock.getPrice() * entry.getValue();
            }
        }
        return Math.round(total * 100.0) / 100.0;
    }

    public double getProfitLoss(Map<String, Stock> marketStocks) {
        double profitLoss = 0;
        for (Map.Entry<String, Integer> entry : holdings.entrySet()) {
            String symbol  = entry.getKey();
            int    qty     = entry.getValue();
            Stock  stock   = marketStocks.get(symbol);
            double avgCost = avgBuyPrice.getOrDefault(symbol, 0.0);
            if (stock != null) {
                profitLoss += (stock.getPrice() - avgCost) * qty;
            }
        }
        return Math.round(profitLoss * 100.0) / 100.0;
    }

    public Map<String, Integer> getHoldings()    { return holdings; }
    public Map<String, Double>  getAvgBuyPrice() { return avgBuyPrice; }

    public int getQuantity(String symbol) {
        return holdings.getOrDefault(symbol, 0);
    }

    public double getAvgPrice(String symbol) {
        return avgBuyPrice.getOrDefault(symbol, 0.0);
    }
}