package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction {
    private String type;       
    private String symbol;     
    private int    quantity;   
    private double pricePerShare;
    private double totalAmount;
    private String dateTime;

    public Transaction(String type, String symbol, int quantity, double pricePerShare) {
        this.type          = type;
        this.symbol        = symbol;
        this.quantity      = quantity;
        this.pricePerShare = pricePerShare;
        this.totalAmount   = Math.round((quantity * pricePerShare) * 100.0) / 100.0;
        this.dateTime      = LocalDateTime.now()
                               .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public Transaction(String type, String symbol, int quantity,
                       double pricePerShare, double totalAmount, String dateTime) {
        this.type          = type;
        this.symbol        = symbol;
        this.quantity      = quantity;
        this.pricePerShare = pricePerShare;
        this.totalAmount   = totalAmount;
        this.dateTime      = dateTime;
    }


    public String getType()           { return type; }
    public String getSymbol()         { return symbol; }
    public int    getQuantity()       { return quantity; }
    public double getPricePerShare()  { return pricePerShare; }
    public double getTotalAmount()    { return totalAmount; }
    public String getDateTime()       { return dateTime; }


    public String toString() {
        return dateTime + " | " + type + " | " + symbol +
               " | Qty: " + quantity + " | @ Rs " + pricePerShare +
               " | Total: Rs " + totalAmount;
    }
}