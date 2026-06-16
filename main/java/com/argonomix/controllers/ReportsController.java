package com.argonomix.controllers;

import com.argonomix.utils.FinancialCalculations;
import com.argonomix.utils.NavigationUtil;
import com.argonomix.utils.SessionManager;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ReportsController implements Initializable {
    
    @FXML private TabPane reportsTabPane;
    @FXML private Tab incomeExpensesTab;
    @FXML private Tab netWorthTab;
    @FXML private Tab cashFlowTab;
    @FXML private Tab savingsRateTab;
    @FXML private BarChart<String, Number> incomeExpensesChart;
    @FXML private LineChart<String, Number> cashFlowChart;
    @FXML private LineChart<String, Number> savingsRateChart;
    @FXML private Label netWorthLabel;
    @FXML private Label assetsLabel;
    @FXML private Label liabilitiesLabel;
    @FXML private ComboBox<String> dateRangeCombo;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (!SessionManager.isLoggedIn()) {
            try {
                NavigationUtil.switchToScene("/fxml/Login.fxml");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        
        dateRangeCombo.getItems().addAll("Last 3 Months", "Last 6 Months");
        dateRangeCombo.setValue("Last 6 Months");
        dateRangeCombo.setOnAction(e -> loadReports());
        
        loadReports();
    }
    
    private void loadReports() {
        try {
            int userId = SessionManager.getCurrentUserId();
            int months = dateRangeCombo.getValue().equals("Last 3 Months") ? 3 : 6;
            
            loadIncomeVsExpenses(userId, months);
            loadNetWorth(userId);
            loadCashFlow(userId);
            loadSavingsRate(userId, months);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void loadIncomeVsExpenses(int userId, int months) throws Exception {
        List<FinancialCalculations.MonthlyData> data = 
            FinancialCalculations.calculateMonthlyIncomeVsExpenses(userId, months);
        
        incomeExpensesChart.getData().clear();
        
        XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
        incomeSeries.setName("Income");
        
        XYChart.Series<String, Number> expensesSeries = new XYChart.Series<>();
        expensesSeries.setName("Expenses");
        
        for (FinancialCalculations.MonthlyData monthData : data) {
            incomeSeries.getData().add(new XYChart.Data<>(monthData.getMonth(), monthData.getIncome()));
            expensesSeries.getData().add(new XYChart.Data<>(monthData.getMonth(), monthData.getExpenses()));
        }
        
        incomeExpensesChart.getData().addAll(incomeSeries, expensesSeries);
    }
    
    private void loadNetWorth(int userId) throws Exception {
        FinancialCalculations.NetWorthResult result = 
            FinancialCalculations.calculateNetWorth(userId);
        
        assetsLabel.setText(String.format("$%.2f", result.getAssets()));
        liabilitiesLabel.setText(String.format("$%.2f", result.getLiabilities()));
        netWorthLabel.setText(String.format("$%.2f", result.getNetWorth()));
        
        if (result.getNetWorth() >= 0) {
            netWorthLabel.setStyle("-fx-text-fill: #10b981; -fx-font-size: 32px; -fx-font-weight: bold;");
        } else {
            netWorthLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 32px; -fx-font-weight: bold;");
        }
    }
    
    private void loadCashFlow(int userId) throws Exception {
        List<FinancialCalculations.CashFlowForecast> data = 
            FinancialCalculations.calculateCashFlowForecast(userId);
        
        cashFlowChart.getData().clear();
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Forecast");
        
        for (FinancialCalculations.CashFlowForecast forecast : data) {
            series.getData().add(new XYChart.Data<>(forecast.getMonth(), forecast.getForecast()));
        }
        
        cashFlowChart.getData().add(series);
    }
    
    private void loadSavingsRate(int userId, int months) throws Exception {
        List<FinancialCalculations.SavingsRateData> data = 
            FinancialCalculations.calculateSavingsRateOverTime(userId, months);
        
        savingsRateChart.getData().clear();
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Savings Rate %");
        
        for (FinancialCalculations.SavingsRateData rateData : data) {
            series.getData().add(new XYChart.Data<>(rateData.getMonth(), rateData.getRate()));
        }
        
        savingsRateChart.getData().add(series);
    }
    
    @FXML
    private void handleExportPDF() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export PDF Report");
        fileChooser.setInitialFileName("argonomix-report-" + java.time.LocalDate.now().toString() + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        
        Stage stage = (Stage) reportsTabPane.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        
        if (file != null) {
            try {
                exportToPDF(file);
                showSuccess("PDF exported successfully!");
            } catch (Exception e) {
                showError("Error exporting PDF: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    @FXML
    private void handleExportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export CSV Report");
        fileChooser.setInitialFileName("argonomix-report-" + java.time.LocalDate.now().toString() + ".csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        
        Stage stage = (Stage) reportsTabPane.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        
        if (file != null) {
            try {
                exportToCSV(file);
                showSuccess("CSV exported successfully!");
            } catch (Exception e) {
                showError("Error exporting CSV: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private void exportToPDF(File file) throws Exception {
        int userId = SessionManager.getCurrentUserId();
        int months = dateRangeCombo.getValue().equals("Last 3 Months") ? 3 : 6;
        
        PdfWriter writer = new PdfWriter(file);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);
        
        document.add(new Paragraph("ARGonomix Financial Report")
            .setFontSize(24).setBold());
        document.add(new Paragraph("Generated: " + java.time.LocalDate.now().toString())
            .setFontSize(12));
        document.add(new Paragraph("\n"));
        
        // Income vs Expenses
        List<FinancialCalculations.MonthlyData> monthlyData = 
            FinancialCalculations.calculateMonthlyIncomeVsExpenses(userId, months);
        
        Table table = new Table(4);
        table.addHeaderCell("Month");
        table.addHeaderCell("Income");
        table.addHeaderCell("Expenses");
        table.addHeaderCell("Net");
        
        for (FinancialCalculations.MonthlyData data : monthlyData) {
            table.addCell(data.getMonth());
            table.addCell(String.format("$%.2f", data.getIncome()));
            table.addCell(String.format("$%.2f", data.getExpenses()));
            table.addCell(String.format("$%.2f", data.getNet()));
        }
        
        document.add(table);
        document.close();
    }
    
    private void exportToCSV(File file) throws Exception {
        int userId = SessionManager.getCurrentUserId();
        int months = dateRangeCombo.getValue().equals("Last 3 Months") ? 3 : 6;
        
        List<FinancialCalculations.MonthlyData> monthlyData = 
            FinancialCalculations.calculateMonthlyIncomeVsExpenses(userId, months);
        
        try (FileWriter writer = new FileWriter(file)) {
            writer.append("Month,Income,Expenses,Net\n");
            for (FinancialCalculations.MonthlyData data : monthlyData) {
                writer.append(String.format("%s,%.2f,%.2f,%.2f\n", 
                    data.getMonth(), data.getIncome(), data.getExpenses(), data.getNet()));
            }
        }
    }
    
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}


