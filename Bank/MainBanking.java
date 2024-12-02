import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

public class MainBanking {

    private static final String url = "jdbc:mysql://localhost:3306/bank";
    private static final String username = "root";
    private static final String password = "admin";

    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            Scanner scanner = new Scanner(System.in);

            //Creating objects of other classes in this main class and passing the instance of connection and scanner
            //Creating one instance and sharing them helps in performance
            AccountManager accountManager = new AccountManager(connection, scanner);
            Accounts accounts = new Accounts(connection, scanner);
            User user = new User(connection, scanner);

            showMainMenu(scanner, accounts, user, accountManager);
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
        }
    }

    private static void showMainMenu(Scanner scanner, Accounts accounts, User user, AccountManager accountManager) {
        while (true) {
            System.out.println("Welcome to the system.");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    user.register();
                    break;
                case 2:
                    handleLogin(scanner, accounts, user, accountManager);
                    break;
                case 3:
                    System.out.println("Thank you for using the system. Exiting...");
                    return;
                default:
                    System.out.println("Enter correct choice.");
                    break;
            }
        }
    }

    private static void handleLogin(Scanner scanner, Accounts accounts, User user, AccountManager accountManager) {
        String email = user.login();
        if (email != null) {
            System.out.println("Logged In.");
            if (!accounts.accountExists(email)) {
                System.out.println("1. Open a new Bank Account");
                System.out.println("2. Exit");
                if (scanner.nextInt() == 1) {
                    long accountNumber = accounts.openNewAccount(email);
                    System.out.println("Account Created Successfully. Your Account Number is: " + accountNumber);
                } else {
                    return;
                }
            }

            long accountNumber = accounts.getAccountNumber(email);
            showAccountOptions(scanner, accountManager, accountNumber);
        } else {
            System.out.println("Incorrect Email or Password");
        }
    }

    private static void showAccountOptions(Scanner scanner, AccountManager accountManager, long accountNumber) {
        int option;
        do {
            System.out.println("1. Debit Money");
            System.out.println("2. Credit Money");
            System.out.println("3. Transfer Money");
            System.out.println("4. Check Balance");
            System.out.println("5. Log Out");
            System.out.print("Enter your choice: ");
            option = scanner.nextInt();

            switch (option) {
                case 1:
                    accountManager.debitMoney(accountNumber);
                    break;
                case 2:
                    accountManager.creditMoney(accountNumber);
                    break;
                case 3:
                    accountManager.transferBalance(accountNumber);
                    break;
                case 4:
                    accountManager.getBalance(accountNumber);
                    break;
                case 5:
                    System.out.println("Logging out...");
                    break;
                default:
                    System.out.println("Incorrect choice.");
                    break;
            }
        } while (option != 5);
    }
}
