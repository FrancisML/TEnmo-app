package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;

import java.math.BigDecimal;
import java.util.List;

public interface TransferDAO {
    String sendBucks(int userFrom, int userTo, BigDecimal amount);
    public String requestBucks(int userFrom, int userTo, BigDecimal amount);
    public List<Transfer> getPastTransfers(int accountId);
    public List<Transfer> getPendingTransfers(int userId);
    public String approveTransaction(Transfer transfer);
    public String rejectTransaction(Transfer transfer);
}
