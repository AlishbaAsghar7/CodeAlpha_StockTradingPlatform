package model;

import java.util.LinkedHashMap;
import java.util.Map;

public class Market {
    private Map<String, Stock> stocks = new LinkedHashMap<>();

    public Market() {
 
        stocks.put("AAPL",  new Stock("AAPL",  "Apple Inc.",          18500, +250));
        stocks.put("GOOGL", new Stock("GOOGL", "Alphabet (Google)",   14200, -180));
        stocks.put("MSFT",  new Stock("MSFT",  "Microsoft Corp.",     11800, +120));
        stocks.put("AMZN",  new Stock("AMZN",  "Amazon.com Inc.",     16300, -350));
        stocks.put("TSLA",  new Stock("TSLA",  "Tesla Inc.",           8700, +430));
        stocks.put("META",  new Stock("META",  "Meta Platforms",       6200,  +90));
        stocks.put("NFLX",  new Stock("NFLX",  "Netflix Inc.",         7800, -110));
        stocks.put("NVDA",  new Stock("NVDA",  "NVIDIA Corp.",        22100, +680));
    }

    public void updatePrices() {
        for (Stock stock : stocks.values()) {
            stock.simulatePriceChange();
        }
    }

    public Stock getStock(String symbol)          { return stocks.get(symbol); }
    public Map<String, Stock> getAllStocks()       { return stocks; }
    public boolean hasStock(String symbol)        { return stocks.containsKey(symbol); }
}