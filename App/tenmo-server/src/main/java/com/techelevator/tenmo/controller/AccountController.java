package com.techelevator.tenmo.controller;

import javax.validation.Valid;

import com.techelevator.tenmo.dao.AccountDAO;
import com.techelevator.tenmo.dao.JdbcUserDao;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.exception.DaoException;
import com.techelevator.tenmo.model.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.techelevator.tenmo.model.Account;


import com.techelevator.tenmo.dao.AccountDAO;
import com.techelevator.tenmo.security.jwt.TokenProvider;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@RestController
//@PreAuthorize("isAuthenticated()")
public class AccountController {

        private AccountDAO accountDAO;
        private UserDao userDao;

        public AccountController(AccountDAO accountDAO, UserDao userDao) {
                this.accountDAO = accountDAO;
                this.userDao = userDao;
        }

        @RequestMapping(path = "account/{id}", method = RequestMethod.GET)
        public BigDecimal getBalance(@PathVariable int id){
                BigDecimal balance =  accountDAO.getAccountBalancebyId(id);
                return balance;
        }

        @RequestMapping(path = "/tenmo_user", method = RequestMethod.GET)
        public List<User> listUsers() {
                List<User> users = userDao.getUsers();
                return users;
        }

//        @RequestMapping(path = "/account/{id}/deposit", method = RequestMethod.PUT)
//        public Account updateAddAccount(@RequestBody Account account, @PathVariable int id) {
//                Account updatedAccount;
//                BigDecimal amountToAdd = account.getBalance();
//                try {
//                updatedAccount = accountDAO.addAmount(id, amountToAdd);
//        } catch (DaoException e) {
//                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
//        }
//                return updatedAccount;
//        }
//
//        @RequestMapping(path = "/account/{id}/withdraw", method = RequestMethod.PUT)
//        public Account updateSubtractAccount(@RequestBody Account account, @PathVariable int id) {
//                Account updatedAccount;
//                BigDecimal amountToSubtract = account.getBalance();
//                try {
//                        updatedAccount = accountDAO.subtractAmount(id, amountToSubtract);
//                } catch (DaoException e) {
//                        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
//                }
//                return updatedAccount;
//        }
//
//        @PutMapping("/transfer")
//        public String transferMoney(@RequestBody TransferDto transferDto, Principal p) {
//                // get user TO and increase his amount
//                // user FROM is p.getName() and decrease his amount
//                return p.getName();
//        }
}
