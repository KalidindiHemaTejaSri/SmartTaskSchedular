package application;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ExpenseManager {

    //DB credentials
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/dbschema";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    //Get connection
    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    //Initialize tables if not exists
    public static void initDB() {
        String createExpenses = "CREATE TABLE IF NOT EXISTS expenses ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "title VARCHAR(255) NOT NULL,"
                + "amount DECIMAL(12,2) NOT NULL,"
                + "date DATE NOT NULL,"
                + "category VARCHAR(100),"
                + "payment_method VARCHAR(50),"
                + "notes TEXT,"
                + "receipt_path VARCHAR(500),"
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                + ")";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(createExpenses);
            System.out.println("✅ Tables initialized");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Insert an expense
    public static Expense addExpense(Expense e) {
        String sql = "INSERT INTO expenses(title, amount, date, category, payment_method, notes, receipt_path) "
                + "VALUES(?,?,?,?,?,?,?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, e.getTitle());
            ps.setBigDecimal(2, e.getAmount());
            ps.setDate(3, java.sql.Date.valueOf(e.getDate()));
            ps.setString(4, e.getCategory());
            ps.setString(5, e.getPaymentMethod());
            ps.setString(6, e.getNotes());
            ps.setString(7, e.getReceiptPath());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    e.setId(rs.getInt(1));
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return e;
    }

    //Get all expenses
    public static List<Expense> getAllExpenses() {
        List<Expense> list = new ArrayList<>();
        String sql = "SELECT * FROM expenses ORDER BY date DESC, id DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Expense e = new Expense();
                e.setId(rs.getInt("id"));
                e.setTitle(rs.getString("title"));
                e.setAmount(rs.getBigDecimal("amount"));

                Date d = rs.getDate("date");
                if (d != null) e.setDate(d.toLocalDate());

                e.setCategory(rs.getString("category"));
                e.setPaymentMethod(rs.getString("payment_method"));
                e.setNotes(rs.getString("notes"));
                e.setReceiptPath(rs.getString("receipt_path"));

                list.add(e);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    //Delete expense by ID
    public static boolean deleteExpense(int id) {
        String sql = "DELETE FROM expenses WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            int affected = ps.executeUpdate();
            return affected > 0;

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }
}
