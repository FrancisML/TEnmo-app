package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;

import java.math.BigDecimal;

public interface AccountDAO {
  BigDecimal getAccountBalancebyId(int id);

   Account getAccountById(int id);
    Account addAmount(int id, BigDecimal amount);
    Account subtractAmount(int id, BigDecimal amount);
    public Account getAccountByAccountId(int id);

}
