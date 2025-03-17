# Bank Management System

## Overview
Bank Management System is a Java-based application developed using Swing for the GUI and MySQL for database management. This system allows an admin to manage bank accounts, perform transactions such as deposits and withdrawals, view account details, and delete accounts.

## Features
- Create a new bank account
- Deposit money into an account
- Withdraw money from an account
- View account details
- Delete an account

## Technologies Used
- **Programming Language**: Java
- **GUI Framework**: Swing
- **Database**: MySQL
- **JDBC**: Java Database Connectivity for MySQL connection

## Installation and Setup
### Prerequisites
- Install Java Development Kit (JDK)
- Install MySQL Server
- Install an IDE like IntelliJ IDEA, Eclipse, or NetBeans

### Database Setup
1. Start MySQL and create a new database:
   ```sql
   CREATE DATABASE bank_management;
   ```
2. Switch to the newly created database:
   ```sql
   USE bank_management;
   ```
3. Create the `accounts` table:
   ```sql
   CREATE TABLE accounts (
       account_number INT AUTO_INCREMENT PRIMARY KEY,
       account_holder_name VARCHAR(255) NOT NULL,
       balance DOUBLE NOT NULL
   );
   ```
4. Create the `transactions` table:
   ```sql
   CREATE TABLE transactions (
       transaction_id INT AUTO_INCREMENT PRIMARY KEY,
       account_number INT,
       transaction_type VARCHAR(50),
       amount DOUBLE,
       transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
       FOREIGN KEY (account_number) REFERENCES accounts(account_number)
   );
   ```

### Running the Application
1. Clone this repository:
   ```sh
   git clone https://github.com/your-username/BankManagementSystem.git
   ```
2. Open the project in your preferred Java IDE.
3. Update the database connection details in `BankManagementSystem.java`:
   ```java
   conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/bank_management", "your_username", "your_password");
   ```
4. Compile and run the `BankManagementSystem.java` file.

## Usage
- Enter account details and click **Create Account** to register a new bank account.
- Enter account number and deposit amount, then click **Deposit** to add funds.
- Enter account number and withdrawal amount, then click **Withdraw** to remove funds.
- Enter an account number and click **View Details** to check account balance and details.
- Enter an account number and click **Delete Account** to remove an account permanently.

## Contact
For any inquiries or issues, feel free to reach out via email or GitHub Issues.

