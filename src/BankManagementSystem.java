import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class BankManagementSystem extends JFrame implements ActionListener {
    private JTextField accountNumberField, accountHolderNameField, balanceField, depositAmountField,
            withdrawAmountField;
    private JButton createAccountBtn, depositBtn, withdrawBtn, viewDetailsBtn, deleteAccountBtn;
    private Connection conn;

    public BankManagementSystem() {
        initializeDatabase();
        initializeUI();
    }

    private void initializeDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/database_name", "username_here", "password_here");
            System.out.println("Successfully connected to database...");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void initializeUI() {
        setTitle("Skiv Bank Management System");
        setLayout(new GridLayout(8, 2, 10, 10));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        add(new JLabel("Account Number:"));
        accountNumberField = new JTextField(20);
        add(accountNumberField);

        add(new JLabel("Account Holder Name:"));
        accountHolderNameField = new JTextField(20);
        add(accountHolderNameField);

        add(new JLabel("Balance:"));
        balanceField = new JTextField(20);
        add(balanceField);

        add(new JLabel("Deposit Amount:"));
        depositAmountField = new JTextField(20);
        add(depositAmountField);

        add(new JLabel("Withdraw Amount:"));
        withdrawAmountField = new JTextField(20);
        add(withdrawAmountField);

        createAccountBtn = new JButton("Create Account");
        createAccountBtn.addActionListener(this);
        add(createAccountBtn);

        depositBtn = new JButton("Deposit");
        depositBtn.addActionListener(this);
        add(depositBtn);

        withdrawBtn = new JButton("Withdraw");
        withdrawBtn.addActionListener(this);
        add(withdrawBtn);

        viewDetailsBtn = new JButton("View Details");
        viewDetailsBtn.addActionListener(this);
        add(viewDetailsBtn);

        deleteAccountBtn = new JButton("Delete Account");
        deleteAccountBtn.addActionListener(this);
        add(deleteAccountBtn);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void createAccount() {
        String accountHolderName = accountHolderNameField.getText();
        double initialBalance = Double.parseDouble(balanceField.getText());

        String insertAccountSql = "INSERT INTO accounts (account_holder_name, balance) VALUES (?, ?)";

        try (PreparedStatement insertAccountStmt = conn.prepareStatement(insertAccountSql,
                Statement.RETURN_GENERATED_KEYS)) {
            insertAccountStmt.setString(1, accountHolderName);
            insertAccountStmt.setDouble(2, initialBalance);
            int rowsInserted = insertAccountStmt.executeUpdate();

            if (rowsInserted > 0) {
                ResultSet generatedKeys = insertAccountStmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int accountNumber = generatedKeys.getInt(1);
                    JOptionPane.showMessageDialog(this,
                            "Account created successfully with account number: " + accountNumber, "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Failed to create account", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error creating account: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deposit() {
        int accountNumber = Integer.parseInt(accountNumberField.getText());
        double depositAmount = Double.parseDouble(depositAmountField.getText());

        try {
            // Update balance in accounts table
            String updateBalanceSql = "UPDATE accounts SET balance = balance + ? WHERE account_number = ?";
            PreparedStatement updateBalanceStmt = conn.prepareStatement(updateBalanceSql);
            updateBalanceStmt.setDouble(1, depositAmount);
            updateBalanceStmt.setInt(2, accountNumber);
            int rowsUpdated = updateBalanceStmt.executeUpdate();
            if (rowsUpdated == 0) {
                JOptionPane.showMessageDialog(this, "Account not found", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Insert record into transactions table
            String insertTransactionSql = "INSERT INTO transactions (account_number, transaction_type, amount) VALUES (?, ?, ?)";
            PreparedStatement insertTransactionStmt = conn.prepareStatement(insertTransactionSql);
            insertTransactionStmt.setInt(1, accountNumber);
            insertTransactionStmt.setString(2, "DEPOSIT");
            insertTransactionStmt.setDouble(3, depositAmount);
            insertTransactionStmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Amount deposited successfully", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error depositing amount: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void withdraw() {
        int accountNumber = Integer.parseInt(accountNumberField.getText());
        double withdrawAmount = Double.parseDouble(withdrawAmountField.getText());

        try {
            // Check if account has sufficient balance
            String checkBalanceSql = "SELECT balance FROM accounts WHERE account_number = ?";
            PreparedStatement checkBalanceStmt = conn.prepareStatement(checkBalanceSql);
            checkBalanceStmt.setInt(1, accountNumber);
            ResultSet rs = checkBalanceStmt.executeQuery();
            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "Account not found", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            double balance = rs.getDouble("balance");
            if (balance < withdrawAmount) {
                JOptionPane.showMessageDialog(this, "Insufficient balance", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Update balance in accounts table
            String updateBalanceSql = "UPDATE accounts SET balance = balance - ? WHERE account_number = ?";
            PreparedStatement updateBalanceStmt = conn.prepareStatement(updateBalanceSql);
            updateBalanceStmt.setDouble(1, withdrawAmount);
            updateBalanceStmt.setInt(2, accountNumber);
            int rowsUpdated = updateBalanceStmt.executeUpdate();
            if (rowsUpdated == 0) {
                JOptionPane.showMessageDialog(this, "Account not found", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Insert record into transactions table
            String insertTransactionSql = "INSERT INTO transactions (account_number, transaction_type, amount) VALUES (?, ?, ?)";
            PreparedStatement insertTransactionStmt = conn.prepareStatement(insertTransactionSql);
            insertTransactionStmt.setInt(1, accountNumber);
            insertTransactionStmt.setString(2, "WITHDRAWAL");
            insertTransactionStmt.setDouble(3, withdrawAmount);
            insertTransactionStmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Amount withdrawn successfully", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error withdrawing amount: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewDetails() {
        int accountNumber = Integer.parseInt(accountNumberField.getText());

        try {
            // Retrieve account details from accounts table
            String getAccountDetailsSql = "SELECT * FROM accounts WHERE account_number = ?";
            PreparedStatement getAccountDetailsStmt = conn.prepareStatement(getAccountDetailsSql);
            getAccountDetailsStmt.setInt(1, accountNumber);
            ResultSet rs = getAccountDetailsStmt.executeQuery();

            if (rs.next()) {
                String accountHolderName = rs.getString("account_holder_name");
                double balance = rs.getDouble("balance");

                String message = "Account Number: " + accountNumber + "\n"
                        + "Account Holder Name: " + accountHolderName + "\n"
                        + "Balance: " + balance;
                JOptionPane.showMessageDialog(this, message, "Account Details", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Account not found", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error retrieving account details: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteAccount() {
        int accountNumber = Integer.parseInt(accountNumberField.getText());

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this account?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // First, delete all transactions related to the account
                String deleteTransactionsSql = "DELETE FROM transactions WHERE account_number = ?";
                PreparedStatement deleteTransactionsStmt = conn.prepareStatement(deleteTransactionsSql);
                deleteTransactionsStmt.setInt(1, accountNumber);
                deleteTransactionsStmt.executeUpdate();

                // Then, delete the account
                String deleteAccountSql = "DELETE FROM accounts WHERE account_number = ?";
                PreparedStatement deleteAccountStmt = conn.prepareStatement(deleteAccountSql);
                deleteAccountStmt.setInt(1, accountNumber);
                int rowsDeleted = deleteAccountStmt.executeUpdate();

                if (rowsDeleted > 0) {
                    JOptionPane.showMessageDialog(this,
                            "Account deleted successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Account not found!",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                        "Error deleting account: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Handle button clicks
        if (e.getSource() == createAccountBtn) {
            createAccount();
        } else if (e.getSource() == depositBtn) {
            deposit();
        } else if (e.getSource() == withdrawBtn) {
            withdraw();
        } else if (e.getSource() == viewDetailsBtn) {
            viewDetails();
        } else if (e.getSource() == deleteAccountBtn) {
            deleteAccount();
        }

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BankManagementSystem frame = new BankManagementSystem();
            frame.setTitle("Skiv Bank Management System");
            frame.setSize(600, 400);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }
}
