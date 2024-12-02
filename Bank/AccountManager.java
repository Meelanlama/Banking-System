import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class AccountManager {
    private Connection connection;
    private Scanner scanner;

    public AccountManager(Connection connection, Scanner scanner) {
        this.connection = connection;
        this.scanner = scanner;
    }

    public void creditMoney(long accountNo) {
        scanner.nextLine();
        System.out.print("Enter Amount: ");
        double amount = scanner.nextDouble();
        scanner.nextLine();
        System.out.print("Enter Security Pin: ");
        String securityPin = scanner.nextLine();

        try {
            connection.setAutoCommit(false);
            if (accountNo != 0 && verifyAccount(accountNo, securityPin)) {
                String creditQuery = "UPDATE accounts SET balance = balance + ? WHERE account_number = ?";
                try (PreparedStatement ps = connection.prepareStatement(creditQuery)) {
                    ps.setDouble(1, amount);
                    ps.setLong(2, accountNo);
                    int rowsAffected = ps.executeUpdate();
                    if (rowsAffected > 0) {
                        System.out.println("Rs. " + amount + " has been credited successfully.");
                        connection.commit();
                    } else {
                        System.out.println("Transaction failed.");
                        connection.rollback();
                    }
                }
            } else {
                System.out.println("Invalid Pin.");
            }
        } catch (SQLException e) {
            System.err.println("Error during credit transaction: " + e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Error resetting auto-commit: " + e.getMessage());
            }
        }
    }

    public void debitMoney(long accountNo) {
        scanner.nextLine();
        System.out.print("Enter Amount: ");
        double amount = scanner.nextDouble();
        scanner.nextLine();
        System.out.print("Enter Security Pin: ");
        String securityPin = scanner.nextLine();

        try {
            connection.setAutoCommit(false);
            if (accountNo != 0 && verifyAccount(accountNo, securityPin)) {
                String debitQuery = "UPDATE accounts SET balance = balance - ? WHERE account_number = ?";
                try (PreparedStatement ps = connection.prepareStatement(debitQuery)) {
                    ps.setDouble(1, amount);
                    ps.setLong(2, accountNo);
                    int rowsAffected = ps.executeUpdate();
                    if (rowsAffected > 0) {
                        System.out.println("Rs. " + amount + " has been debited successfully.");
                        connection.commit();
                    } else {
                        System.out.println("Transaction failed.");
                        connection.rollback();
                    }
                }
            } else {
                System.out.println("Not enough balance or incorrect pin.");
            }
        } catch (SQLException e) {
            System.err.println("Error during debit transaction: " + e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Error resetting auto-commit: " + e.getMessage());
            }
        }
    }

    public void transferBalance(long senderAccNo) {
        scanner.nextLine();
        System.out.print("Enter Receiver Account Number: ");
        long receiverAccNo = scanner.nextLong();
        System.out.print("Enter Amount: ");
        double amount = scanner.nextDouble();
        scanner.nextLine();
        System.out.print("Enter your Pin Number: ");
        String pin = scanner.nextLine();

        try {
            connection.setAutoCommit(false);
            if (verifyAccount(senderAccNo, pin) && verifyBalance(senderAccNo, amount)) {
                String creditQuery = "UPDATE accounts SET balance = balance + ? WHERE account_number = ?";
                String debitQuery = "UPDATE accounts SET balance = balance - ? WHERE account_number = ?";

                try (PreparedStatement creditPs = connection.prepareStatement(creditQuery);
                     PreparedStatement debitPs = connection.prepareStatement(debitQuery)) {
                    creditPs.setDouble(1, amount);
                    creditPs.setLong(2, receiverAccNo);
                    debitPs.setDouble(1, amount);
                    debitPs.setLong(2, senderAccNo);

                    int creditRows = creditPs.executeUpdate();
                    int debitRows = debitPs.executeUpdate();

                    if (creditRows > 0 && debitRows > 0) {
                        System.out.println("Transaction successful. Rs." + amount + " transferred.");
                        connection.commit();
                    } else {
                        System.out.println("Transaction failed.");
                        connection.rollback();
                    }
                }
            } else {
                System.out.println("Insufficient balance or incorrect pin.");
            }
        } catch (SQLException e) {
            System.err.println("Error during transfer transaction: " + e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Error resetting auto-commit: " + e.getMessage());
            }
        }
    }

    public void getBalance(long accountNo) {
        scanner.nextLine();
        System.out.print("Enter your pin: ");
        String pin = scanner.nextLine();

        String query = "SELECT balance FROM accounts WHERE account_number = ? AND security_pin = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setLong(1, accountNo);
            ps.setString(2, pin);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                double balance = rs.getDouble("balance");
                System.out.println("Your balance: " + balance);
            } else {
                System.out.println("Wrong Pin number.");
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving balance: " + e.getMessage());
        }
    }

    private boolean verifyAccount(long accountNo, String pin) throws SQLException {
        String query = "SELECT * FROM accounts WHERE account_number = ? AND security_pin = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setLong(1, accountNo);
            ps.setString(2, pin);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }
    }

    private boolean verifyBalance(long accountNo, double amount) throws SQLException {
        String query = "SELECT balance FROM accounts WHERE account_number = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setLong(1, accountNo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                double balance = rs.getDouble("balance");
                return balance >= amount;
            }
        }
        return false;
    }
}
