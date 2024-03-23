package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.AccountDAO;
import com.techelevator.tenmo.dao.JdbcTransferDAO;
import com.techelevator.tenmo.dao.TransferDAO;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
//@PreAuthorize("isAuthenticated()")
public class TransferController {
    private TransferDAO transferDAO;

    public TransferController(TransferDAO transferDAO) {
        this.transferDAO = transferDAO;
    }


    @RequestMapping(path = "transfer", method = RequestMethod.POST)
    public String sendBucks(@RequestBody Transfer transfer) {
        String results = transferDAO.sendBucks(transfer.getAccountFrom(), transfer.getAccountTo(), transfer.getAmount());
        return results;
    }

    @RequestMapping(path = "request", method = RequestMethod.POST)
    public String sendTransferRequest(@RequestBody Transfer transfer) {
        String results = transferDAO.requestBucks(transfer.getAccountFrom(), transfer.getAccountTo(), transfer.getAmount());
        return results;
    }


    @RequestMapping(path = "transfers/{id}", method = RequestMethod.GET)
    public List<Transfer> listTransfers(@PathVariable int id) {
        List<Transfer> transfers = transferDAO.getPastTransfers(id);
        return transfers;
    }

    @RequestMapping(path = "pending/{id}", method = RequestMethod.GET)
    public List<Transfer> listPendingTransfers(@PathVariable int id) {
        List<Transfer> transfers = transferDAO.getPendingTransfers(id);
        return transfers;
    }

    @RequestMapping(path = "approve", method = RequestMethod.POST)
    public String approveTransaction(@RequestBody Transfer transfer) {
        String results = transferDAO.approveTransaction(transfer);
        return results;
    }
    @RequestMapping(path = "reject", method = RequestMethod.POST)
    public String rejectTransaction(@RequestBody Transfer transfer) {
        String results = transferDAO.rejectTransaction(transfer);
        return results;
    }
}
