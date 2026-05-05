package ui;

import model.*;
import service.TradingService;
import service.FileManager;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.Map;

public class StockTradingUI extends Application {

    private User          user;
    private Portfolio     portfolio;
    private Market        market;
    private TradingService tradingService;

    private Label  balanceLabel;
    private Label  portfolioValueLabel;
    private Label  profitLossLabel;
    private Label  totalAssetsLabel;
    private TextArea outputArea;

    private TableView<Stock> marketTable;

    public void start(Stage stage) {

        user          = new User("Trader", 100000); 
        portfolio     = new Portfolio();
        market        = new Market();
        tradingService = new TradingService(user, portfolio, market);

        boolean loaded = FileManager.loadPortfolio(user, portfolio);


        Label titleLabel = new Label("STOCK TRADING PLATFORM");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        balanceLabel       = new Label();
        portfolioValueLabel = new Label();
        profitLossLabel    = new Label();
        totalAssetsLabel   = new Label();

        updateSummaryLabels();

        VBox balanceBox = summaryBox("Cash Balance", balanceLabel);
        VBox portfolioBox = summaryBox("Portfolio Value", portfolioValueLabel);
        VBox plBox = summaryBox("Profit / Loss", profitLossLabel);
        VBox totalBox = summaryBox("Total Assets", totalAssetsLabel);

        HBox summaryBar = new HBox(20, balanceBox, portfolioBox, plBox, totalBox);
        summaryBar.setPadding(new Insets(10));
        summaryBar.setStyle("-fx-background-color: #1e1e2e; -fx-border-color: #444; -fx-border-width: 0 0 1 0;");

        VBox topSection = new VBox(8, titleLabel, summaryBar);
        topSection.setPadding(new Insets(15, 15, 5, 15));
        topSection.setStyle("-fx-background-color: #1e1e2e;");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #00d4aa;");


        Label marketTitle = new Label("Live Market Data");
        marketTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #cccccc;");

        marketTable = new TableView<>();
        marketTable.setStyle("-fx-background-color: #2a2a3e;");
        marketTable.setPrefHeight(220);

        TableColumn<Stock, String> symbolCol = new TableColumn<>("Symbol");
        symbolCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSymbol()));
        symbolCol.setPrefWidth(80);

        TableColumn<Stock, String> nameCol = new TableColumn<>("Company");
        nameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCompanyName()));
        nameCol.setPrefWidth(180);

        TableColumn<Stock, String> priceCol = new TableColumn<>("Price (Rs)");
        priceCol.setCellValueFactory(d -> new SimpleStringProperty(
            String.format("%.2f", d.getValue().getPrice())));
        priceCol.setPrefWidth(110);

        TableColumn<Stock, String> changeCol = new TableColumn<>("Change");
        changeCol.setCellValueFactory(d -> new SimpleStringProperty(
            String.format("%+.2f", d.getValue().getChange())));
        changeCol.setPrefWidth(90);

        TableColumn<Stock, String> changePctCol = new TableColumn<>("Change %");
        changePctCol.setCellValueFactory(d -> new SimpleStringProperty(
            String.format("%+.2f%%", d.getValue().getChangePercent())));
        changePctCol.setPrefWidth(90);

        TableColumn<Stock, String> ownedCol = new TableColumn<>("Owned");
        ownedCol.setCellValueFactory(d -> new SimpleStringProperty(
            String.valueOf(portfolio.getQuantity(d.getValue().getSymbol()))));
        ownedCol.setPrefWidth(70);

        marketTable.getColumns().addAll(symbolCol, nameCol, priceCol, changeCol, changePctCol, ownedCol);
        refreshMarketTable();

        marketTable.setRowFactory(tv -> new TableRow<Stock>() {
          
            protected void updateItem(Stock stock, boolean empty) {
                super.updateItem(stock, empty);
                if (stock == null || empty) {
                    setStyle("");
                } else if (stock.getChange() >= 0) {
                    setStyle("-fx-background-color: #1a3a2a;"); 
                } else {
                    setStyle("-fx-background-color: #3a1a1a;"); 
                }
            }
        });

        Button refreshBtn = new Button("Refresh Prices");
        refreshBtn.setStyle("-fx-background-color: #444; -fx-text-fill: white;");
        refreshBtn.setOnAction(e -> {
            market.updatePrices();
            refreshMarketTable();
            updateSummaryLabels();
            outputArea.setText("Market prices updated!");
        });


        Timeline autoRefresh = new Timeline(new KeyFrame(Duration.seconds(10), e -> {
            market.updatePrices();
            Platform.runLater(() -> {
                refreshMarketTable();
                updateSummaryLabels();
            });
        }));
        autoRefresh.setCycleCount(Timeline.INDEFINITE);
        autoRefresh.play();

        HBox marketHeader = new HBox(10, marketTitle, refreshBtn);
        marketHeader.setAlignment(Pos.CENTER_LEFT);

        VBox marketSection = new VBox(8, marketHeader, marketTable);
        marketSection.setPadding(new Insets(10));
        marketSection.setStyle("-fx-background-color: #2a2a3e; -fx-border-color: #555; " +
                               "-fx-border-radius: 6; -fx-background-radius: 6;");


        Label tradeTitle = new Label("Buy / Sell Stocks");
        tradeTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #cccccc;");

        ComboBox<String> stockDropdown = new ComboBox<>();
        stockDropdown.setPromptText("Select Stock");
        stockDropdown.setPrefWidth(220);
        for (Stock s : market.getAllStocks().values()) {
            stockDropdown.getItems().add(s.getSymbol() + " - " + s.getCompanyName());
        }

        TextField qtyField = new TextField();
        qtyField.setPromptText("Quantity");
        qtyField.setPrefWidth(100);

        Label pricePreview = new Label("Estimated Cost: Rs --");
        pricePreview.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 12px;");

        Runnable updatePreview = () -> {
            try {
                String selected = stockDropdown.getValue();
                if (selected == null || qtyField.getText().isEmpty()) return;
                String symbol = selected.split(" - ")[0];
                int    qty    = Integer.parseInt(qtyField.getText().trim());
                Stock  stock  = market.getStock(symbol);
                if (stock != null) {
                    double cost = Math.round(stock.getPrice() * qty * 100.0) / 100.0;
                    pricePreview.setText("Estimated Cost: Rs " + cost +
                        "  |  Per Share: Rs " + stock.getPrice());
                }
            } catch (NumberFormatException ignored) {}
        };

        stockDropdown.setOnAction(e -> updatePreview.run());
        qtyField.textProperty().addListener((obs, oldVal, newVal) -> updatePreview.run());

        Button buyBtn  = new Button("BUY");
        Button sellBtn = new Button("SELL");

        buyBtn.setPrefWidth(100);
        sellBtn.setPrefWidth(100);
        buyBtn.setStyle("-fx-background-color: #00b050; -fx-text-fill: white; -fx-font-weight: bold;");
        sellBtn.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-weight: bold;");

        buyBtn.setOnAction(e -> {
            String selected = stockDropdown.getValue();
            if (selected == null) { outputArea.setText("Please select a stock!"); return; }
            try {
                String symbol   = selected.split(" - ")[0];
                int    quantity = Integer.parseInt(qtyField.getText().trim());
                String result   = tradingService.buyStock(symbol, quantity);

                if (result.startsWith("ERROR:")) {
                    outputArea.setText("Error: " + result.replace("ERROR:", ""));
                } else {
                    String[] parts = result.split(":");
                    outputArea.setText(
                        "BUY ORDER EXECUTED!\n" +
                        "Stock    : " + parts[1] + "\n" +
                        "Quantity : " + parts[2] + " shares\n" +
                        "Price    : Rs " + parts[3] + " per share\n" +
                        "Total    : Rs " + parts[4] + "\n" +
                        "Status   : CONFIRMED\n\n" +
                        "Remaining Balance: Rs " + user.getBalance()
                    );
                    refreshMarketTable();
                    updateSummaryLabels();
                    qtyField.clear();
                }
            } catch (NumberFormatException ex) {
                outputArea.setText("Please enter a valid quantity number.");
            }
        });

        sellBtn.setOnAction(e -> {
            String selected = stockDropdown.getValue();
            if (selected == null) { outputArea.setText("Please select a stock!"); return; }
            try {
                String symbol   = selected.split(" - ")[0];
                int    quantity = Integer.parseInt(qtyField.getText().trim());
                String result   = tradingService.sellStock(symbol, quantity);

                if (result.startsWith("ERROR:")) {
                    outputArea.setText("Error: " + result.replace("ERROR:", ""));
                } else {
                    String[] parts = result.split(":");
                    outputArea.setText(
                        "SELL ORDER EXECUTED!\n" +
                        "Stock    : " + parts[1] + "\n" +
                        "Quantity : " + parts[2] + " shares\n" +
                        "Price    : Rs " + parts[3] + " per share\n" +
                        "Earned   : Rs " + parts[4] + "\n" +
                        "Status   : CONFIRMED\n\n" +
                        "New Balance: Rs " + user.getBalance()
                    );
                    refreshMarketTable();
                    updateSummaryLabels();
                    qtyField.clear();
                }
            } catch (NumberFormatException ex) {
                outputArea.setText("Please enter a valid quantity number.");
            }
        });

        HBox tradeButtons = new HBox(10, buyBtn, sellBtn);
        VBox tradePanel   = new VBox(8,
            tradeTitle,
            new Label("Stock:") {{ setStyle("-fx-text-fill: #aaa;"); }},
            stockDropdown,
            new Label("Quantity:") {{ setStyle("-fx-text-fill: #aaa;"); }},
            qtyField,
            pricePreview,
            tradeButtons
        );
        tradePanel.setPadding(new Insets(10));
        tradePanel.setStyle("-fx-background-color: #2a2a3e; -fx-border-color: #555; " +
                            "-fx-border-radius: 6; -fx-background-radius: 6;");


        Label portfolioTitle = new Label("My Portfolio");
        portfolioTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #cccccc;");

        TableView<String[]> portfolioTable = new TableView<>();
        portfolioTable.setPrefHeight(180);
        portfolioTable.setStyle("-fx-background-color: #2a2a3e;");

        TableColumn<String[], String> pSymbol = new TableColumn<>("Symbol");
        pSymbol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[0]));
        pSymbol.setPrefWidth(80);

        TableColumn<String[], String> pQty = new TableColumn<>("Shares");
        pQty.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[1]));
        pQty.setPrefWidth(70);

        TableColumn<String[], String> pAvg = new TableColumn<>("Avg Buy Price");
        pAvg.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[2]));
        pAvg.setPrefWidth(120);

        TableColumn<String[], String> pCurrent = new TableColumn<>("Current Price");
        pCurrent.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[3]));
        pCurrent.setPrefWidth(120);

        TableColumn<String[], String> pValue = new TableColumn<>("Market Value");
        pValue.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[4]));
        pValue.setPrefWidth(120);

        TableColumn<String[], String> pPL = new TableColumn<>("Profit/Loss");
        pPL.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[5]));
        pPL.setPrefWidth(110);

        portfolioTable.getColumns().addAll(pSymbol, pQty, pAvg, pCurrent, pValue, pPL);

        Button refreshPortfolioBtn = new Button("Refresh Portfolio");
        refreshPortfolioBtn.setStyle("-fx-background-color: #444; -fx-text-fill: white;");

        Runnable refreshPortfolio = () -> {
            portfolioTable.getItems().clear();
            for (Map.Entry<String, Integer> entry : portfolio.getHoldings().entrySet()) {
                String symbol   = entry.getKey();
                int    qty      = entry.getValue();
                double avgPrice = portfolio.getAvgPrice(symbol);
                Stock  stock    = market.getStock(symbol);
                double currPrice = stock != null ? stock.getPrice() : 0;
                double mktValue  = Math.round(currPrice * qty * 100.0) / 100.0;
                double pl        = Math.round((currPrice - avgPrice) * qty * 100.0) / 100.0;

                portfolioTable.getItems().add(new String[]{
                    symbol,
                    String.valueOf(qty),
                    "Rs " + String.format("%.2f", avgPrice),
                    "Rs " + String.format("%.2f", currPrice),
                    "Rs " + String.format("%.2f", mktValue),
                    (pl >= 0 ? "+" : "") + "Rs " + String.format("%.2f", pl)
                });
            }
            if (portfolio.getHoldings().isEmpty()) {
                outputArea.setText("Portfolio is empty. Buy some stocks first!");
            }
        };

        refreshPortfolioBtn.setOnAction(e -> {
            refreshPortfolio.run();
            updateSummaryLabels();
        });

        autoRefresh.getKeyFrames().add(new KeyFrame(Duration.seconds(10),
            e -> Platform.runLater(refreshPortfolio::run)));

        VBox portfolioSection = new VBox(8,
            new HBox(10, portfolioTitle, refreshPortfolioBtn) {{
                setAlignment(Pos.CENTER_LEFT);
            }},
            portfolioTable
        );
        portfolioSection.setPadding(new Insets(10));
        portfolioSection.setStyle("-fx-background-color: #2a2a3e; -fx-border-color: #555; " +
                                  "-fx-border-radius: 6; -fx-background-radius: 6;");



        Button viewTxBtn  = new Button("View Transaction History");
        Button savePfBtn  = new Button("Save Portfolio");
        Button exportBtn  = new Button("Export Full Report");

        viewTxBtn.setStyle("-fx-background-color: #555; -fx-text-fill: white;");
        savePfBtn.setStyle("-fx-background-color: #2255aa; -fx-text-fill: white;");
        exportBtn.setStyle("-fx-background-color: #225588; -fx-text-fill: white;");

        viewTxBtn.setOnAction(e -> {
            if (tradingService.getTransactions().isEmpty()) {
                outputArea.setText("No transactions yet. Buy or sell some stocks first!");
                return;
            }
            StringBuilder sb = new StringBuilder("===== TRANSACTION HISTORY =====\n\n");
            for (Transaction tx : tradingService.getTransactions()) {
                sb.append(tx.toString()).append("\n");
            }
            outputArea.setText(sb.toString());
        });

        savePfBtn.setOnAction(e -> {
            FileManager.savePortfolio(user, portfolio, market);
            FileManager.saveTransactions(tradingService.getTransactions());
            outputArea.setText(
                "Portfolio saved successfully!\n\n" 
            );
        });

        exportBtn.setOnAction(e -> {
            String path = FileManager.exportFullReport(
                user, portfolio, market, tradingService.getTransactions());
            outputArea.setText(
                " Report exported!\n\n" 
            );
        });

        HBox fileRow = new HBox(10, viewTxBtn, savePfBtn, exportBtn);


        Label outputLabel = new Label("Output:");
        outputLabel.setStyle("-fx-text-fill: #aaaaaa;");

        outputArea = new TextArea(loaded ?
            "Welcome back! Portfolio .\nBalance: Rs " + user.getBalance() :
            "Welcome! Starting balance: Rs 100,000\nSelect a stock and quantity to start trading."
        );
        outputArea.setEditable(false);
        outputArea.setPrefRowCount(8);
        outputArea.setPrefHeight(160);
        outputArea.setStyle("-fx-control-inner-background: #1e1e2e; -fx-text-fill: #cccccc;");


        HBox middleRow = new HBox(15, tradePanel, portfolioSection);
        HBox.setHgrow(portfolioSection, Priority.ALWAYS);

        VBox root = new VBox(12,
            topSection,
            marketSection,
            middleRow,
            fileRow,
            new HBox(5, outputLabel),
            outputArea
        );
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #16162a;");

        Scene scene = new Scene(root, 800, 900);
        stage.setScene(scene);
        stage.setTitle("Stock Trading Platform");
        stage.setOnCloseRequest(e -> {
            // Auto-save on exit
            FileManager.savePortfolio(user, portfolio, market);
            FileManager.saveTransactions(tradingService.getTransactions());
        });
        stage.show();
    }

    private VBox summaryBox(String title, Label valueLabel) {
        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-text-fill: #888888; -fx-font-size: 11px;");
        valueLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 14px; -fx-font-weight: bold;");
        VBox box = new VBox(2, titleLbl, valueLabel);
        box.setPadding(new Insets(8, 16, 8, 16));
        box.setStyle("-fx-background-color: #2a2a3e; -fx-border-radius: 6; -fx-background-radius: 6;");
        return box;
    }

    private void updateSummaryLabels() {
        double pValue = portfolio.getTotalValue(market.getAllStocks());
        double pl     = portfolio.getProfitLoss(market.getAllStocks());
        double total  = Math.round((user.getBalance() + pValue) * 100.0) / 100.0;

        balanceLabel.setText("Rs " + String.format("%.2f", user.getBalance()));
        portfolioValueLabel.setText("Rs " + String.format("%.2f", pValue));
        totalAssetsLabel.setText("Rs " + String.format("%.2f", total));

        String plStr = (pl >= 0 ? "+" : "") + "Rs " + String.format("%.2f", pl);
        profitLossLabel.setText(plStr);
        profitLossLabel.setStyle(
            "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " +
            (pl >= 0 ? "#00d4aa" : "#ff4444") + ";"
        );
    }

    private void refreshMarketTable() {
        marketTable.getItems().clear();
        for (Stock stock : market.getAllStocks().values()) {
            marketTable.getItems().add(stock);
        }
    }

    public static void main(String[] args) {
        launch();
    }
}