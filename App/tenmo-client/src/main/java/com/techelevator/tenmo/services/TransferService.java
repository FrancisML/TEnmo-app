package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.AuthenticatedUser;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import com.techelevator.util.BasicLogger;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class TransferService {
    private String BASE_URL;
    private final RestTemplate restTemplate = new RestTemplate();

    AuthenticatedUser currentUser;
    private final Scanner scanner = new Scanner(System.in);

    public TransferService(String BASE_URL, AuthenticatedUser currentUser) {
        this.BASE_URL = BASE_URL;
        this.currentUser = currentUser;
    }
    private ConsoleService consoleService = new ConsoleService(); //initialize new console service Object

    private HttpEntity<Transfer> makeTransferEntity(Transfer transfer) { // the makeTransferEntity method makes the process of creating an HttpEntity object modular
        HttpHeaders headers = new HttpHeaders(); // This creates a new instance of HttpHeaders, which represents the HTTP headers of the request.
        headers.setContentType(MediaType.APPLICATION_JSON); //This sets the Content-Type header of the request to application/json. It indicates that the content of the request body is in JSON format.
        headers.setBearerAuth(currentUser.getToken()); //This sets the Authorization header of the request using a bearer token
        HttpEntity<Transfer> entity = new HttpEntity<>(transfer, headers); // This creates a new instance of HttpEntity parameterized with Transfer. It encapsulates both the headers and the transfer data.
        return entity;
    }

    public void sendTEBucks() {
        Transfer transfer = new Transfer();
        AccountService accountService = new AccountService(BASE_URL, currentUser);
        consoleService.printTransferableUsers();
        accountService.getUsers();
        System.out.println("-----------------------------");
        System.out.println("Please Enter User id To Send to.");
        System.out.println("-----------------------------");
        try {
            transfer.setAccountTo(Integer.parseInt(scanner.nextLine()));
            transfer.setAccountFrom(currentUser.getUser().getId());
        } catch (NumberFormatException ex) {
            System.out.println("Error: Invalid user ID input");
            return;
        }

        System.out.println("-----------------------------");
        System.out.println("---Please Enter the Amount---.");
        System.out.println("-----------------------------");
        String amountInput = scanner.nextLine();
        // Validate input before setting it to BigDecimal
        if (isValidAmountInput(amountInput)) {
            transfer.setAmount(new BigDecimal(amountInput));
            String result = restTemplate.exchange(BASE_URL + "transfer", HttpMethod.POST, makeTransferEntity(transfer), String.class).getBody();
            System.out.println(result);
        } else {
            System.out.println("Invalid input. Please enter a valid amount.");
        }
    }

    private boolean isValidAmountInput(String input) {
        try {
            new BigDecimal(input);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    public void transferDetails(Transfer transfer){
        System.out.println("-----------------------------"); // prints ui for user input
        System.out.println("       Transfer Details      ");
        System.out.println("-----------------------------");
        System.out.println("ID: " + transfer.getTransferId());
        System.out.println("From: " + transfer.getUserFrom());
        System.out.println("To: " + transfer.getUserTo());
            String type = null;
            if (transfer.getTransferTypeId() == 1){
                type = "Request";
            } else if (transfer.getTransferTypeId() == 2) {
                type = "Send";
            }
            System.out.println("Type: " + type);
            String status = null;
            if (transfer.getTransferStatusId() == 1) {
                status = "Pending";
            } else if (transfer.getTransferStatusId() == 2) {
                status = "Approved";
            } else if (transfer.getTransferStatusId() == 3) {
                status = "Rejected";
            }
        System.out.println("Status: " + status);
        System.out.println("Amount: " + transfer.getAmount());
    }

    public void requestBucks() {
        Transfer transfer = new Transfer();
        AccountService accountService = new AccountService(BASE_URL, currentUser);
        consoleService.printRequestableUsers();
        accountService.getUsers();
        System.out.println("-----------------------------");
        System.out.println("Please Enter User id To Request From.");
        System.out.println("-----------------------------");
        try {
            transfer.setAccountTo(currentUser.getUser().getId());
            transfer.setAccountFrom(Integer.parseInt(scanner.nextLine()));
        } catch (NumberFormatException ex) {
            System.out.println("Error: Invalid user ID input");
            return;
        }

        System.out.println("-----------------------------");
        System.out.println("---Please Enter the Amount---.");
        System.out.println("-----------------------------");
        String amountInput = scanner.nextLine();
        // Validate input before setting it to BigDecimal
        if (isValidAmountInput(amountInput)) {
            transfer.setAmount(new BigDecimal(amountInput));
            String result = restTemplate.exchange(BASE_URL + "request", HttpMethod.POST, makeTransferEntity(transfer), String.class).getBody();
            System.out.println(result);
        } else {
            System.out.println("Invalid input. Please enter a valid amount.");
        }
    }

    public void getPastTransfers() {
        consoleService.printPastTransfers(); // calls the method that prints the heading
        ResponseEntity<List<Transfer>> responseEntity = restTemplate.exchange(  // this sends GET request to server for a <List> of the transfers
                BASE_URL + "/transfers/" + currentUser.getUser().getId(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Transfer>>() {}
        );
        List<Transfer> transfers = responseEntity.getBody(); //initializes a list of transfers using the data from the server response
        for (Transfer transfer : transfers) {   // for each loop that iterates through each transfer and then prints each transfer to the UI(user interface)
            String ToFrom = ""; //variable we will use to to set a string, From or To depending on the type of transfer
            String userName = ""; //variable we will use to to set a string for the user that's receiving or sending depending on the type of transfer
            if (transfer.getTransferTypeId() == 1) { // if the transfer_id equals 1(which is the "code" for funds coming in)
                ToFrom = "From:";  // sets the string we will use later one to print which type of transfer it is
                userName = transfer.getUserFrom(); // sets the string we will use later one to print the name of the other user involved
            } else if (transfer.getTransferTypeId() == 2) { // if the transfer_id equals 2(which is the "code" for funds being sent)
                ToFrom = "To:"; // sets the string we will use later one to print which type of transfer it is
                userName = transfer.getUserTo(); // sets the string we will use later one to print the name of the other user involved
            }
            System.out.println(transfer.getTransferId() + "     " +ToFrom + " " + userName + "     " + transfer.getAmount()); // prints a line including the transfer id, ToFrom(type of transfer),the users name, and the amount
        }
    }

    public void getPendingTransfers() {
        boolean restart = true; // Flag to control whether to restart the method or not
        while (restart) { // Loop to restart the method
            consoleService.printPendingTransfers();
            ResponseEntity<List<Transfer>> responseEntity = restTemplate.exchange(
                    BASE_URL + "/pending/" + currentUser.getUser().getId(),
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Transfer>>() {}
            );
            List<Transfer> transfers = responseEntity.getBody();
            for (Transfer transfer : transfers) {
                String userName = transfer.getUserTo();
                System.out.println(transfer.getTransferId() + "        " + userName + "       " + transfer.getAmount());
            }
            System.out.println();
            System.out.println("Please enter transfer ID to approve/reject (0 to cancel):");
            try {
                int userInput = Integer.parseInt(scanner.nextLine());
                boolean found = false;
                for (Transfer transfer : transfers) {
                    if (transfer.getTransferId() == userInput) {
                        found = true;
                        transferDetails(transfer);
                        consoleService.printApprovalMenu();
                        int menuSelection = consoleService.promptForApprovalSelection(" Please Select an option:");
                        if (menuSelection == 1){
                            approveTransfer(transfer);
                        } else if (menuSelection == 2) {
                            rejectTransfer(transfer);
                        } else if (menuSelection == 3) {
                            // Handle other menu selections
                        } else {
                            System.out.println("Invalid Selection, Please make a valid selection:");
                            consoleService.printApprovalMenu();
                            menuSelection = consoleService.promptForApprovalSelection(" Please Select an option:");
                        }
                        break;
                    }
                }
                if (!found && userInput != 0) {
                    System.out.println("No such transfer exists");
                    System.out.println("Press Enter to continue...");
                    scanner.nextLine(); // Wait for the user to press Enter
                    continue; // Restart the loop
                }
                // If the transfer exists, you can add the rest of your logic here
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid integer.");
            }
            restart = false; // Set restart to false to exit the loop
        }
    }

   public void approveTransfer(Transfer transfer) {
       String result = restTemplate.exchange(BASE_URL + "/approve", HttpMethod.POST, makeTransferEntity(transfer), String.class).getBody();//sends request to server
       System.out.println("Transfer Approved");
   }
    public void rejectTransfer(Transfer transfer) {
        String result = restTemplate.exchange(BASE_URL + "/reject", HttpMethod.POST, makeTransferEntity(transfer), String.class).getBody();//sends request to server
        System.out.println("Transfer Rejected");
    }
}

