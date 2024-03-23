package com.techelevator.tenmo.dao;
import com.techelevator.tenmo.exception.DaoException;
import com.techelevator.tenmo.model.Account;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class JdbcAccountDAO implements AccountDAO {


    private final JdbcTemplate jdbcTemplate;

    public JdbcAccountDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public BigDecimal getAccountBalancebyId(int id) {
        BigDecimal balance = null;

        String sql = "SELECT balance FROM account WHERE user_id = ?;";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, id);
            if (results.next()) {

                balance = new BigDecimal(results.getString("balance"));

            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } return balance;
    }

    @Override
    public Account getAccountById(int id) {
        Account userAccount = new Account();
        String sql = "SELECT account_id, user_id, balance FROM account WHERE user_id = ?;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, id);
        if (results.next()) {
            userAccount = mapRowToAccount(results);
        }
        return userAccount;

    }
    @Override
    public Account getAccountByAccountId(int id) {
        Account userAccount = new Account();
        String sql = "SELECT account_id, user_id, balance FROM account WHERE account_id = ?;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, id);
        if (results.next()) {
            userAccount = mapRowToAccount(results);
        }
        return userAccount;
    }

    @Override
    public Account addAmount(int id, BigDecimal amount) {
        Account account = getAccountById(id);
        int accountid = account.getAccountId();
        BigDecimal newBalance =  account.getBalance().add(amount);
        String sql = "UPDATE account SET balance = ? WHERE account_id = ?;";
//        try {
             jdbcTemplate.update(sql, newBalance, accountid);
//            if (rowsChanged == 0) {
//                throw new DaoException("zero rows affected, expected at least one");
//            } else {
//                account.setBalance(newBalance);
//            }
//        } catch (CannotGetJdbcConnectionException e) {
//            throw new DaoException("Unable to connect to server or database", e);
//        }catch (DataIntegrityViolationException e) {
//            throw new DaoException("Data Integrity violation");
//        }
        return account;
    }

    @Override
    public Account subtractAmount(int id, BigDecimal amount) {
        Account account = getAccountById(id);
        int accountid = account.getAccountId();
        BigDecimal newBalance =  account.getBalance().subtract(amount);
        String sql = "UPDATE account SET balance = ? WHERE account_id = ?;";
         jdbcTemplate.update(sql, newBalance, accountid);
//            if (rowsChanged == 0) {
//                throw new DaoException("zero rows affected, expected at least one");
//            } else {
//                account.setBalance(newBalance);
//            }
//        } catch (CannotGetJdbcConnectionException e) {
//            throw new DaoException("Unable to connect to server or database", e);
//        }catch (DataIntegrityViolationException e) {
//            throw new DaoException("Data Integrity violation");
//        }
        return account;
    }



    private Account mapRowToAccount(SqlRowSet results) {
       Account account = new Account();
       account.setAccountId(results.getInt("account_id"));
       account.setUserId(results.getInt("user_id"));
       account.setBalance(results.getBigDecimal("balance"));

        return account;
    }
}

