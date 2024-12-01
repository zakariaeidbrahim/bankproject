package com.backend.bank.service;

import com.backend.bank.dto.*;
import com.backend.bank.entity.User;
import com.backend.bank.repository.UserRepository;
import com.backend.bank.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;

@Service
public class UserServiceImpl implements UserService{

    @Autowired
    UserRepository userRepository;

    @Autowired
    EmailService emailService;



    @Override
    public BankResponse createAccount(UserRequest userRequest) {
        //creating an account - saving a new user into the db
        //check if user already has an account:
        if (userRepository.existsByEmail(userRequest.getEmail())){
            return BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_EXISTS_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_EXISTS_MESSAGE)
                    .accountInfo(null)
                    .build();

        }
        User newUser = User.builder()
                .firstName(userRequest.getFirstName())
                .lastName(userRequest.getLastName())
                .otherName(userRequest.getOtherName())
                .gender(userRequest.getGender())
                .address(userRequest.getAddress())
                .stateOfOrigin(userRequest.getStateOfOrigin())
                .accountNumber(AccountUtils.generateAccountNumber())
                .email(userRequest.getEmail())
                .phoneNumber(userRequest.getPhoneNumber())
                .alternativePhoneNumber(userRequest.getAlternativePhoneNumber())
                .status("ACTIVE")
                .accountBalance(BigDecimal.ZERO)
                .build();


        User saveUser = userRepository.save(newUser);

        //dend email alert

        EmailDetails emailDetails =EmailDetails.builder()
                .recipient(saveUser.getEmail())
                .subject("ACCOUNT CREATION")
                .messageBody("Congratulation! Your Account has been successfuly Created!! \n Your Account Details  :\n" +
                        "\n Account Name : "+ saveUser.getFirstName() +" "+saveUser.getLastName()+" " + saveUser.getOtherName() +"\n Account Number : " +saveUser.getAccountNumber() )
                .build();
        emailService.sendEmailAlert(emailDetails);

        return BankResponse.builder()
                .responseCode(AccountUtils.ACCOUNT_CREATION_SUCCESS_CODE)
                .responseMessage(AccountUtils.ACCOUNT_CREATION_SUCCESS_MESSAGE)
                .accountInfo(AccountInfo.builder()
                        .accountBalance(saveUser.getAccountBalance())
                        .accountNumber(saveUser.getAccountNumber())
                        .accountName(saveUser.getFirstName() + " " +saveUser.getLastName() + " " + saveUser.getOtherName())
                        .build())
                .build();


        // balance Enquiry , name Enquiry , credit , debit , transfer

    }

    @Override
    public BankResponse balanceEnquiry(EnquiryRerquest rerquest) {
        //check if the provided account nmber exist in db
        boolean isAccountExist = userRepository.existsByAccountNumber(rerquest.getAccountNumber());
        if (!isAccountExist){
            return BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_NOT_FOUND_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_NOT_FOUND_MESSAGE)
                    .accountInfo(null)
                    .build();
        }

        User foundUser =userRepository.findByAccountNumber(rerquest.getAccountNumber());
        return BankResponse.builder()
                .responseCode(AccountUtils.ACCOUNT_FOUND_CODE)
                .responseMessage(AccountUtils.ACCOUNT_FOUND_MESSAGE)
                .accountInfo(AccountInfo.builder()
                        .accountBalance(foundUser.getAccountBalance())
                        .accountNumber(rerquest.getAccountNumber())
                        .accountName(foundUser.getFirstName() +" "+ foundUser.getLastName() +" "+ foundUser.getOtherName())
                        .build())
                .build();
    }

    @Override
    public String nameEnquiry(EnquiryRerquest request) {
        boolean isAccountExist = userRepository.existsByAccountNumber(request.getAccountNumber());
        if (!isAccountExist){
            return AccountUtils.ACCOUNT_NOT_FOUND_MESSAGE;
        }
        User foundUser = userRepository.findByAccountNumber(request.getAccountNumber());
        return foundUser.getFirstName() +" "+ foundUser.getLastName() +" "+ foundUser.getOtherName();
    }

    @Override
    public BankResponse creditAccount(CreditDebitRequest request) {
        //cheking if the accounnt exist
        boolean isCreditAccountExist = userRepository.existsByAccountNumber(request.getAccountNumber());
        if (!isCreditAccountExist){
            return BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_NOT_FOUND_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_NOT_FOUND_MESSAGE)
                    .accountInfo(null)
                    .build();
        }
        User userToCredit =userRepository.findByAccountNumber(request.getAccountNumber());
        userToCredit.setAccountBalance(userToCredit.getAccountBalance().add(request.getAmount()));
        userRepository.save(userToCredit);

         return BankResponse.builder()
                 .responseCode(AccountUtils.ACCOUNT_CREDITED_SUCCESS)
                 .responseMessage(AccountUtils.ACCOUNT_CREDITED_SUCCESS_MESSAGE)
                 .accountInfo(AccountInfo.builder()
                         .accountName(userToCredit.getFirstName() +" "+userToCredit.getLastName()+" "+ userToCredit.getOtherName())
                         .accountBalance(userToCredit.getAccountBalance())
                         .accountNumber(userToCredit.getAccountNumber())
                         .build())
                 .build();
    }

    @Override
    public BankResponse debitAccount(CreditDebitRequest request) {
        //check if the account exist
        boolean isDebitAccountExist = userRepository.existsByAccountNumber(request.getAccountNumber());
        if (!isDebitAccountExist){
            return BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_NOT_FOUND_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_NOT_FOUND_MESSAGE)
                    .accountInfo(null)
                    .build();
        }
        //check if the amount you intend to withdraw is not more than the urrent account
        User userToDebit =userRepository.findByAccountNumber(request.getAccountNumber());
        BigInteger availableBalance=userToDebit.getAccountBalance().toBigInteger();
        BigInteger debitAmount= request.getAmount().toBigInteger();

        if(availableBalance.intValue()<debitAmount.intValue()){
            return BankResponse.builder()

                    .responseCode(AccountUtils.INSUFFICIENT_BALANCE_CODE)
                    .responseMessage(AccountUtils.INSUFFICIENT_BALANCE_MESSAGE)
                    .accountInfo(null)
                    .build();
        }
        else {
            userToDebit.setAccountBalance(userToDebit.getAccountBalance().subtract(request.getAmount()));
            userRepository.save(userToDebit);
            return BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_DEBITED_SUCCES_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_DEBITED_SUCCES_MESSAGE)
                    .accountInfo(AccountInfo.builder()
                            .accountNumber(request.getAccountNumber())
                            .accountName(userToDebit.getFirstName() +" "+userToDebit.getLastName()+" " +userToDebit.getOtherName())
                            .accountBalance(userToDebit.getAccountBalance())
                            .build())

                    .build();
        }
    }
    @Override
    public BankResponse transfer(TransferRequest request){

        //get the account to debit(check if the acc exist)
        boolean isDestAccountExist =userRepository.existsByAccountNumber(request.getDestinationAccountNumber());
        if (!isDestAccountExist){
            return BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_NOT_FOUND_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_NOT_FOUND_MESSAGE)
                    .accountInfo(null)
                    .build();
        }
        User sourceAccountUser = userRepository.findByAccountNumber(request.getSourceAccountNumber());

        //check if the amount im debiting is not more than the current balance

        if (request.getAmount().compareTo(sourceAccountUser.getAccountBalance()) >0){
            return BankResponse.builder()
                    .responseCode(AccountUtils.INSUFFICIENT_BALANCE_CODE)
                    .responseMessage(AccountUtils.INSUFFICIENT_BALANCE_MESSAGE)
                    .accountInfo(null)
                    .build();
        }
        sourceAccountUser.setAccountBalance(sourceAccountUser.getAccountBalance().subtract(request.getAmount()));
        userRepository.save(sourceAccountUser);
        String sourceName= sourceAccountUser.getFirstName()+" "+sourceAccountUser.getLastName()+" "+sourceAccountUser.getOtherName();

        EmailDetails debitAlert =EmailDetails.builder()
                .subject("DEBIT ALERT")
                .recipient(sourceAccountUser.getEmail())
                .messageBody("The sum of "+request.getAmount() + "has beed debuted to  from Your Account!  \n Your current balance is "+sourceAccountUser.getAccountBalance() +"$")
                .build();
        emailService.sendEmailAlert(debitAlert);

        //get the account to credit  and credit the account
        User destAccountUser = userRepository.findByAccountNumber(request.getDestinationAccountNumber());
        destAccountUser.setAccountBalance(destAccountUser.getAccountBalance().add(request.getAmount()));

        userRepository.save(destAccountUser);
        //String desteName= destAccountUser.getFirstName()+" "+destAccountUser.getLastName()+" "+destAccountUser.getOtherName();

        EmailDetails creditAlert =EmailDetails.builder()
                    .subject("CREDIT ALERT")
                .recipient(destAccountUser.getEmail())
                .messageBody("The sum of  "+request.getAmount() + "$  has beed credited from  "+sourceName+" in Your Account! \n your current balance is  "+destAccountUser.getAccountBalance()+"$")
                .build();
        emailService.sendEmailAlert(creditAlert);

    return BankResponse.builder()
            .responseCode(AccountUtils.TRANSFER_SUCCESSFUL_CODE)
            .responseMessage(AccountUtils.TRANSFER_SUCCESSFUL_MESSAGE)
            .accountInfo(null)
            .build();
    }
}
