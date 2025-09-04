package application;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.*;

import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class Main extends Application {

    private TableView<Expense> table = new TableView<>();
    private ObservableList<Expense> data = FXCollections.observableArrayList();
    private PieChart pieChart = new PieChart();

    @Override
    public void start(Stage primaryStage) {

        ExpenseManager.initDB(); // initialize DB + tables

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Top toolbar
        Button btnAdd = new Button("Add Expense");
        Button btnDelete = new Button("Delete Selected");
        Button btnExport = new Button("Export CSV");
        DatePicker from = new DatePicker(LocalDate.now().withDayOfMonth(1));
        DatePicker to = new DatePicker(LocalDate.now());
        Button btnFilter = new Button("Filter");
        HBox top = new HBox(8, btnAdd, btnDelete, new Label("From:"), from, new Label("To:"), to, btnFilter, btnExport);
        top.setAlignment(Pos.CENTER_LEFT);
        top.setPadding(new Insets(0,0,10,0));
        root.setTop(top);

        // Table columns
        TableColumn<Expense, LocalDate> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colDate.setMinWidth(100);

        TableColumn<Expense, String> colTitle = new TableColumn<>("Title");
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colTitle.setMinWidth(180);

        TableColumn<Expense, String> colCategory = new TableColumn<>("Category");
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colCategory.setMinWidth(120);

        TableColumn<Expense, BigDecimal> colAmount = new TableColumn<>("Amount");
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colAmount.setMinWidth(80);

        TableColumn<Expense, String> colPay = new TableColumn<>("Payment");
        colPay.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        colPay.setMinWidth(100);

        table.getColumns().addAll(colDate, colTitle, colCategory, colAmount, colPay);
        table.setItems(data);

        // Right side: PieChart
        VBox right = new VBox(10);
        right.setPadding(new Insets(0,0,0,10));
        right.getChildren().addAll(new Label("Category breakdown (this month)"), pieChart);
        pieChart.setPrefSize(300, 300);

        root.setCenter(table);
        root.setRight(right);

        // actions
        btnAdd.setOnAction(e -> openAddDialog(primaryStage));
        btnDelete.setOnAction(e -> deleteSelected());
        btnFilter.setOnAction(e -> loadBetween(from.getValue(), to.getValue()));
        btnExport.setOnAction(e -> exportCSV(primaryStage));

        // initial load
        loadAll();
        updateChart(LocalDate.now().withDayOfMonth(1), LocalDate.now());

        Scene scene = new Scene(root, 1000, 600);
        primaryStage.setTitle("Smart Expense Tracker");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void loadAll() {
        Platform.runLater(() -> {
            data.clear();
            List<Expense> list = ExpenseManager.getAllExpenses();
            data.addAll(list);
        });
    }

    private void loadBetween(LocalDate from, LocalDate to) {
        if (from == null || to == null) { showAlert("Filter", "Please select both dates."); return; }
        Platform.runLater(() -> {
            data.clear();
            List<Expense> list = ExpenseManager.getAllExpenses(); // simple: load all and filter manually
            for (Expense e : list) {
                if (!e.getDate().isBefore(from) && !e.getDate().isAfter(to)) data.add(e);
            }
            updateChart(from, to);
        });
    }

    private void updateChart(LocalDate from, LocalDate to) {
        pieChart.getData().clear();
        List<Expense> list = data; // use already loaded filtered data
        list.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                    Expense::getCategory,
                    java.util.stream.Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
            ))
            .forEach((cat, amt) -> {
                PieChart.Data slice = new PieChart.Data((cat==null?"Uncategorized":cat) + " (" + amt + ")", amt.doubleValue());
                pieChart.getData().add(slice);
            });
    }

    private void openAddDialog(Stage owner) {
        Dialog<Expense> dialog = new Dialog<>();
        dialog.setTitle("Add Expense");
        dialog.initOwner(owner);
        ButtonType saveBtnType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(8); grid.setVgap(8); grid.setPadding(new Insets(10));
        TextField title = new TextField();
        TextField amount = new TextField();
        DatePicker datePicker = new DatePicker(LocalDate.now());
        ComboBox<String> category = new ComboBox<>();
        category.getItems().addAll("Food","Transport","Groceries","Bills","Entertainment","Health","Others");
        category.setEditable(true);
        ComboBox<String> payment = new ComboBox<>();
        payment.getItems().addAll("Cash","Card","UPI","NetBanking","Other");
        TextArea notes = new TextArea();
        notes.setPrefRowCount(3);

        grid.add(new Label("Title"), 0,0); grid.add(title,1,0);
        grid.add(new Label("Amount"), 0,1); grid.add(amount,1,1);
        grid.add(new Label("Date"), 0,2); grid.add(datePicker,1,2);
        grid.add(new Label("Category"), 0,3); grid.add(category,1,3);
        grid.add(new Label("Payment"), 0,4); grid.add(payment,1,4);
        grid.add(new Label("Notes"), 0,5); grid.add(notes,1,5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveBtnType) {
                try {
                    String t = title.getText().trim();
                    BigDecimal amt = new BigDecimal(amount.getText().trim());
                    LocalDate d = datePicker.getValue();
                    String cat = category.getValue();
                    String pay = payment.getValue();
                    String n = notes.getText();
                    return new Expense(t, amt, d, cat, pay, n, null);
                } catch (Exception ex) {
                    showAlert("Validation", "Please check the inputs. Amount must be a number.");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(expense -> {
            ExpenseManager.addExpense(expense);
            data.add(0, expense);
            updateChart(LocalDate.now().withDayOfMonth(1), LocalDate.now());
        });
    }

    private void deleteSelected() {
        Expense sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { showAlert("Delete", "No row selected."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete selected expense?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                if (ExpenseManager.deleteExpense(sel.getId())) {
                    data.remove(sel);
                }
            }
        });
    }

    private void exportCSV(Stage owner) {
        FileChooser fc = new FileChooser();
        fc.setInitialFileName("expenses.csv");
        File f = fc.showSaveDialog(owner);
        if (f == null) return;
        try (PrintWriter pw = new PrintWriter(f)) {
            pw.println("id,title,amount,date,category,payment_method,notes");
            for (Expense e : data) {
                pw.printf("%d,\"%s\",%s,%s,\"%s\",\"%s\",\"%s\"%n",
                        e.getId(),
                        e.getTitle().replace("\"","\"\""),
                        e.getAmount().toString(),
                        e.getDate().toString(),
                        (e.getCategory()==null?"":e.getCategory()).replace("\"","\"\""),
                        (e.getPaymentMethod()==null?"":e.getPaymentMethod()).replace("\"","\"\""),
                        (e.getNotes()==null?"":e.getNotes()).replace("\"","\"\"")
                );
            }
            showAlert("Export", "CSV exported: " + f.getAbsolutePath());
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Export Error", ex.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
