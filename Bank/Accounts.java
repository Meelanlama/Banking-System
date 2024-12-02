import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.Optional;

public class Accounts {
    private Connection connection;
    private Scanner scanner;

    public Accounts(Connection connection, Scanner scanner) {
        this.connection = connection;
        this.scanner = scanner;
    }

    public long openNewAccount(String email) {
        if (!accountExists(email)) {
            String openAccountQuery = "INSERT INTO accounts(account_number,full_name,email,balance,security_pin) VALUES (?,?,?,?,?)";
            scanner.nextLine();
            System.out.print("Enter Full Name: ");
            String fullName = scanner.nextLine();
            System.out.print("Enter Initial Amount: ");
            double balance = scanner.nextDouble();
            scanner.nextLine();
            System.out.print("Enter Security Pin: ");
            String securityPin = scanner.nextLine();

            try (PreparedStatement ps = connection.prepareStatement(openAccountQuery)) {
                long accountNumber = generateAccountNumber();
                ps.setLong(1, accountNumber);
                ps.setString(2, fullName);
                ps.setString(3, email);
                ps.setDouble(4, balance);
                ps.setString(5, securityPin);

                int affectedRows = ps.executeUpdate();
                if (affectedRows > 0) {
                    return accountNumber;
                }
            } catch (SQLException e) {
                System.err.println("Error creating account: " + e.getMessage());
            }
        }
        throw new RuntimeException("Account already exists.");
    }

    public long getAccountNumber(String email) {
        String query = "SELECT account_number FROM accounts WHERE email = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getLong("account_number");
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving account number: " + e.getMessage());
        }
        throw new RuntimeException("Account number does not exist.");
    }

    public boolean accountExists(String email) {
        String checkQuery = "SELECT * FROM accounts WHERE email = ?";
        try (PreparedStatement ps = connection.prepareStatement(checkQuery)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException("Error checking if account exists: " + e.getMessage());
        }
    }

    private long generateAccountNumber() {
        // Get the highest account number from the accounts table
        //ORDER BY account_number DESC will sort the rows in descending order and LIMIT 1 returns only one highest row
        try (PreparedStatement ps = connection.prepareStatement("SELECT account_number FROM accounts ORDER BY account_number DESC LIMIT 1")) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                long highestAccNo = rs.getLong("account_number");
                return highestAccNo + 1;
            }
        } catch (SQLException e) {
            System.err.println("Error generating account number: " + e.getMessage());
        }
        return 1000;
    }
}
