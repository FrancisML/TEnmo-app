package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.exception.DaoException;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcTransferDAO  implements TransferDAO {
    AccountDAO accountDAO; // declares a dependency injection point for an AccountDAO object
    private JdbcTemplate jdbcTemplate; //declares another dependency injection point, this time for a JdbcTemplate object
    public JdbcTransferDAO(JdbcTemplate jdbcTemplate, AccountDAO accountDAO) {  // constructor for the JdbcTransferDAO class
        this.jdbcTemplate = jdbcTemplate;
        this.accountDAO = accountDAO;
    }
    @Override
    public String sendBucks(int userFrom, int userTo, BigDecimal amount) { //Method that sends TE bucks by updating the account database with the users involved balances and inserts the transaction into the transfer table
        try {
        if (userFrom == userTo) { // if statement to decide if your trying to send money to yourself(which is not allowed) by comparing the current user to the sendBucks destination
            return "Prohibited Transfer: Unable to send money to yourself"; // prints an "error" that you cannot send money to yourself if the above condition is true
        }
        if (amount.compareTo(accountDAO.getAccountBalancebyId(userFrom)) >= 0 || amount.compareTo(new BigDecimal(0)) <= 0) { //if statement whose condition checks to see if the users balance is below 0 or if the current users balance is 0 or less
            return "Funds must be a positive non-zero integer. cannot transfer insufficient funds"; // prints message to alert user of a failure of  the above mentioned conditions
        } else {
            String sql = "INSERT INTO transfer (transfer_type_id, transfer_status_id, account_from, account_to, amount) " +
                    "VALUES (2, 2, (SELECT account_id FROM account WHERE user_id = ?),(SELECT account_id FROM account WHERE user_id = ?), ?);"; // SQL language that inserts a new transfer into the transfer table
            jdbcTemplate.update(sql, userFrom, userTo, amount); // executes an SQL update statement stored in the sql variable with the method parameters
            accountDAO.addAmount(userTo, amount);  // calls the method that updates the recipient's account with the added funds
            accountDAO.subtractAmount(userFrom, amount); // calls the method that updates the sender's account with the subtracted funds
            return "Transfer complete"; // Prints message to user that transfer was successful
        }
        } catch (Exception e) {
            throw new DaoException("Error occurred while sending bucks", e);
        }
    }
    public String requestBucks(int userFrom, int userTo, BigDecimal amount) { // Method to create a new transfer in the transfer table of the data base set as pending
        try {
        if (userFrom == userTo) {  //if statement that determines if your tying to request money from yourself by compareing the current user_id to the requested user_id
            return "Prohibited: you can not request money from yourself"; // message that prints to user if the user from and to match
        }
        if (amount.compareTo(new BigDecimal(0)) <= 0) { // if statement to determine if requested amount is above 0
            return "Funds must be a positive non-zero integer"; //message that prints to user is the amount is 0 or less
        } else {
            String sql = "INSERT INTO transfer (transfer_type_id, transfer_status_id, account_from, account_to, amount) " +  // SQL query that creates a new transfer in the transfer table with transfer type = 1(request) and transfer satatus = 1 (pending)
                    "VALUES (1, 1, (SELECT account_id FROM account WHERE user_id = ?),(SELECT account_id FROM account WHERE user_id = ?), ?);";
            jdbcTemplate.update(sql, userFrom, userTo, amount); // executes an SQL update statement stored in the sql variable with the method parameters
            return "Request pending";  // Prints message to user that transfer was successful (meaning the new transfer was added to the table)
        }
        } catch (Exception e) {
            throw new DaoException("Error occurred while requesting bucks", e);
        }
    }
    @Override
    public List<Transfer> getPastTransfers(int userId) { // Method to get all the transfers for the current user
        try {
        int accountId = accountDAO.getAccountById(userId).getAccountId(); // gets the account_id bu calling the getAccountbyid method in the accountDAO
        List<Transfer> transfers = new ArrayList<>(); // creates a new empty list of transfers
        String sql = "SELECT t.*, uf.username AS user_from, ut.username AS user_to FROM transfer t JOIN account af " +   // SQL that selects all transfers related to the current user
                "ON t.account_from = af.account_id JOIN account at ON t.account_to = at.account_id JOIN tenmo_user uf " +
                "ON af.user_id = uf.user_id JOIN tenmo_user ut ON at.user_id = ut.user_id WHERE (transfer_status_id = 2 " +
                "AND transfer_type_id = 1 AND account_to = ?) OR (transfer_status_id = 2 AND transfer_type_id = 2 AND account_from = ?); ";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql,accountId, accountId); //Executes SQL and sets the results to a SqlRowSet variable results
            while (results.next()) {  //while loop that iterates through the SqlRowSet
                Transfer transfer = mapRowToTransfer(results); // for each row set the mapper is called to set the values of the row set to a transfer object
                transfers.add(transfer); //once the values are mapped to a transfer object, it is added to the list transfers initialized above
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
        return transfers; // returns the list of transfers
        } catch (Exception e) {
            throw new DaoException("Error occurred while getting past transfers", e);
        }
    }
    @Override
    public List<Transfer> getPendingTransfers(int userId) {  // method that gets all the pending transfer for the current user
        try {
        int accountId = accountDAO.getAccountById(userId).getAccountId();  // gets the current users accunt id by invokeing the getAccoutById method from the accountDAO and sets it to the accountID variable
        List<Transfer> transfers = new ArrayList<>(); //creates a new list of transfers
        String sql = "SELECT t.*, uf.username AS user_from, ut.username AS user_to \n" +  //SQL query that selects all the transfers whose status is 1(pending) and whose account_from = the current users account_id
                "FROM transfer t \n" +
                "JOIN account af ON t.account_from = af.account_id \n" +
                "JOIN account at ON t.account_to = at.account_id \n" +
                "JOIN tenmo_user uf ON af.user_id = uf.user_id \n" +
                "JOIN tenmo_user ut ON at.user_id = ut.user_id \n" +
                "WHERE (transfer_status_id = 1  \n" +
                "AND account_from = ?); ";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql,accountId); //executes the sql with the current users acount id as a parameter
            while (results.next()) { //while loop that iterates through the list returned from the sql database
                Transfer transfer = mapRowToTransfer(results); //for each row set  a new transfer object is created by using the mapper on the results from the rowset
                transfers.add(transfer); // mapped transfer is then added to the list transfers from above
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
        return transfers; // list of pending transfers is returned
    } catch (Exception e) {
        throw new DaoException("Error occurred while getting pending transfers", e);
    }
    }
    @Override
    public String approveTransaction(Transfer transfer) {//method that sets a transfers status to approved
        try {
        String sql = "UPDATE transfer SET transfer_status_id = ? WHERE transfer_id = ?;"; //sql that updates the transfer status of one transfer in the database
        jdbcTemplate.update(sql, 2, transfer.getTransferId()); // executes the sql with 2 (Status code for approved) and the transfer_id as values
        accountDAO.addAmount(accountDAO.getAccountByAccountId(transfer.getAccountTo()).getUserId(), transfer.getAmount()); //Calls the addAmount method from the accountDAO, to add the amount from the transfer passed in, to the receiving parties account
        accountDAO.subtractAmount(accountDAO.getAccountByAccountId(transfer.getAccountFrom()).getUserId(),transfer.getAmount());//Calls the subtractAmount method from the accountDAO, to subtract the amount from the transfer passed in, to the sending parties account
        return "Transfer Approved"; // message prints to user if the method was successful
        } catch (Exception e) {
            throw new DaoException("Error occurred while approving transaction", e);
        }
    }

    @Override
    public String rejectTransaction(Transfer transfer) {  //method that sets a transfers status to rejected
        try {
        String sql = "UPDATE transfer SET transfer_status_id = ? WHERE transfer_id = ?;"; //sql that updates the transfer status of one transfer in the database
        jdbcTemplate.update(sql, 3, transfer.getTransferId()); //executes the sql with 3 (Status code for rejected) and the transfer_id as values
        return "Transfer Rejected"; // Prints message to user if sql was executed successfully
        } catch (Exception e) {
            throw new DaoException("Error occurred while rejecting transaction", e);
        }
    }

    private Transfer mapRowToTransfer(SqlRowSet results) { // this is the mapper that takes the results from a SqlRowSet and uses the data to create a transfer object
        Transfer transfer = new Transfer();
        transfer.setTransferId(results.getInt("transfer_id"));
        transfer.setTransferTypeId(results.getInt("transfer_type_id"));
        transfer.setTransferStatusId(results.getInt("transfer_status_id"));
        transfer.setAccountFrom(results.getInt("account_from"));
        transfer.setAccountTo(results.getInt("account_to"));
        transfer.setAmount(results.getBigDecimal("amount"));
        transfer.setUserFrom(results.getString("user_from"));
        transfer.setUserTo(results.getString("user_to"));
        return transfer; //returns a new transfer object built by the data out of a row in the database
    }

}

