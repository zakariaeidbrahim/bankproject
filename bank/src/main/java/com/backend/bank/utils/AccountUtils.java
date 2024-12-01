package com.backend.bank.utils;

import java.time.Year;

public class AccountUtils {

    public static final String ACCOUNT_EXISTS_CODE="001";

    public static final String ACCOUNT_EXISTS_MESSAGE="this user already has an account Created !!";

    public static final String ACCOUNT_CREATION_SUCCESS_CODE="002";

    public static final String ACCOUNT_CREATION_SUCCESS_MESSAGE="Account has been successfully created!";

    public static final String ACCOUNT_NOT_FOUND_CODE="003";

    public static final String ACCOUNT_NOT_FOUND_MESSAGE="User with Provided Account Number does Not Exist!!";

    public static final String ACCOUNT_FOUND_CODE="004";

    public static final String ACCOUNT_FOUND_MESSAGE="USER ACCOUNT FOUND!";

    public static final String ACCOUNT_CREDITED_SUCCESS="005";

    public static final String ACCOUNT_CREDITED_SUCCESS_MESSAGE="USER ACCOUNT CREDITED WITH SUCCESS!";

    public static final String INSUFFICIENT_BALANCE_CODE="006";

    public static final String INSUFFICIENT_BALANCE_MESSAGE="Insufficient Balance !";

    public static final String ACCOUNT_DEBITED_SUCCES_CODE="007";

    public static final String ACCOUNT_DEBITED_SUCCES_MESSAGE="Account has been successfully debited!";

    public static final String TRANSFER_SUCCESSFUL_CODE="008";

    public static final String TRANSFER_SUCCESSFUL_MESSAGE="Transfer Successful!";


    public static String generateAccountNumber(){
        //AccountNumber =2024 + randomSixDigits
        Year currentYear = Year.now();
        int min = 100000;
        int max =999999;

        // generate a random number beween min and max
        int randNumber = (int) Math.floor(Math.random() * (max - min + 1) +min);
        //convert the currnet and randomNumber to string , then concatenate them together

        String year = String.valueOf(currentYear);
        String randomNumber=String.valueOf(randNumber);

        StringBuilder accountNumber= new StringBuilder();

        return accountNumber.append(year).append(randNumber).toString();
    }

}
