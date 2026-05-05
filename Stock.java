package model;

public class Stock {
    private String symbol;      
    private String companyName; 
    private double price;       
    private double change;      
    private double changePercent;

    public Stock(String symbol, String companyName, double price, double change) {
        this.symbol        = symbol;
        this.companyName   = companyName;
        this.price         = price;
        this.change        = change;
        this.changePercent = (change / (price - change)) * 100;
    }

    public void simulatePriceChange() {
        double fluctuation = (Math.random() - 0.5) * 0.04; 
        double oldPrice    = this.price;
        this.price         = Math.round((this.price * (1 + fluctuation)) * 100.0) / 100.0;
        this.change        = Math.round((this.price - oldPrice) * 100.0) / 100.0;
        this.changePercent = Math.round((fluctuation * 100) * 100.0) / 100.0;
    }
    public String getSymbol()        { return symbol; }
    public String getCompanyName()   { return companyName; }
    public double getPrice()         { return price; }
    public double getChange()        { return change; }
    public double getChangePercent() { return changePercent; }
    
    public void setPrice(double price) { this.price = price; }

    public String toString() {
        return symbol + " | " + companyName + " | Rs " + price;
    }
}