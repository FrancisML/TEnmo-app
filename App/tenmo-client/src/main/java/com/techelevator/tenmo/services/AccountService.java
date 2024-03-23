package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.AuthenticatedUser;
import com.techelevator.tenmo.model.User;
import com.techelevator.util.BasicLogger;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AccountService {

    private  String BASE_URL = "http://localhost:8080";
    private final RestTemplate restTemplate = new RestTemplate();

    AuthenticatedUser currentUser;

    public AccountService(String url, AuthenticatedUser currentUser ){
        this.currentUser = currentUser;
        BASE_URL = url;
    }

    public void getUsers() {
        ResponseEntity<List<User>> responseEntity = restTemplate.exchange(
                BASE_URL + "/tenmo_user",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<User>>() {}
        );
        List<User> users = responseEntity.getBody();
        for (User user : users) {
            if (user.getId() == currentUser.getUser().getId()) {
            }else
            System.out.println(user.getId() + "      " + user.getUsername());
        }
    }

    public BigDecimal getBalance() {
        BigDecimal balance = new BigDecimal(0);
        balance = restTemplate.getForObject(BASE_URL + "account/" + currentUser.getUser().getId(), BigDecimal.class);
        System.out.println("-----------------------------");
        System.out.println("Current Balance");
        System.out.println("-----------------------------");
        System.out.println(balance);
        return balance;

    }

}
