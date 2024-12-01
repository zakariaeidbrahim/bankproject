package com.backend.bank.controller;


import com.backend.bank.dto.*;
import com.backend.bank.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping
    public BankResponse createAccount(@RequestBody UserRequest userRequest){
        System.out.println("Request received: " + userRequest);
        return userService.createAccount(userRequest);
    }

    @GetMapping ("/balanceEnquiry")
    public BankResponse balanceEnquiry(@RequestBody EnquiryRerquest rerquest){
        return userService.balanceEnquiry(rerquest);

    }
    @GetMapping("/nameEnquiry")
    public String nameEnquiry(@RequestBody EnquiryRerquest rerquest){
        return userService.nameEnquiry(rerquest);

    }

    @PostMapping("credit")
    public BankResponse creditAccount(@RequestBody CreditDebitRequest request){
        return userService.creditAccount(request);
    }

    @PostMapping("debit")
    public BankResponse debitAccount(@RequestBody CreditDebitRequest request){
        return userService.debitAccount(request);
    }

    @PostMapping("transfer")
    public BankResponse transfer(@RequestBody TransferRequest request){
        return userService.transfer(request);
    }
}
