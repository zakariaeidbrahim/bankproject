package com.backend.bank.service;

import com.backend.bank.dto.*;

public interface UserService {
    BankResponse createAccount(UserRequest userRequest);

    BankResponse balanceEnquiry(EnquiryRerquest rerquest);

    String nameEnquiry(EnquiryRerquest rerquest );

    BankResponse creditAccount(CreditDebitRequest request);

    BankResponse debitAccount(CreditDebitRequest request);

    BankResponse transfer(TransferRequest request);

}
