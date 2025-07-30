package com.laxminarayana.onlinebankingapp.service.impl;

import com.laxminarayana.onlinebankingapp.dto.*;
import com.laxminarayana.onlinebankingapp.entity.user;
import com.laxminarayana.onlinebankingapp.repository.UserRepository;
import com.laxminarayana.onlinebankingapp.utils.AccountUtils;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserRepository userRepository;

    @Autowired
    EmailService emailService;

    @Override

    public BankResponse createAccount(UserRequest userRequest) {
//        create an account to save user in db

        if (userRepository.existsByMail(userRequest.getMail())) {
            return BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_EXISTS_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_EXISTS_MESSAGE)
                    .accountInfo(null)
                    .build();
        }

        user newUser = user.builder()
                .firstname(userRequest.getFirstname())
                .lastname(userRequest.getLastname())
                .gender(userRequest.getGender())
                .address(userRequest.getAddress())
                .stateOfOrigin(userRequest.getStateOfOrigin())
                .accountNumber(AccountUtils.generateAccountNumber())
                .accountBalance(BigDecimal.ZERO)
                .mail(userRequest.getMail())
                .phoneNumber(userRequest.getPhoneNumber())
                .alternativePhoneNumber(userRequest.getAlternativePhoneNumber())
                .status("Active")
                .build();
        user savedUser = userRepository.save(newUser);
        //sendemail alert
        EmailDetails emailDetails = EmailDetails.builder()
                .recipient(savedUser.getMail())
                .subject("ACCOUNT CREATION")
                .messageBody("CONGRATS , ACCT CREATED\n Your acct details \n" +
                        "Account name:" + savedUser.getFirstname() + " " + savedUser.getLastname() + "\n Acct no." +
                        savedUser.getAccountNumber())


                .build();
        emailService.sendEmailAlert(emailDetails);
        return BankResponse.builder()
                .responseCode(AccountUtils.ACCOUNT_CREATION_SUCCESS)
                .responseMessage(AccountUtils.ACCOUNT_CREATION_MESSAGE)
                .accountInfo(AccountInfo.builder()
                        .accountBalance(savedUser.getAccountBalance())
                        .accountNumber(savedUser.getAccountNumber())
                        .accountName(savedUser.getFirstname() + " " + savedUser.getLastname())
                        .build())
                .build();
    }

    @Override
    public BankResponse balanceEnquiry(EnquiryRequest request) {
        boolean isAccountExist = userRepository.existsByAccountNumber(request.getAccountNumber());
        if (!isAccountExist) {
            return BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_NOT_EXIST_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_NOT_EXIST_MESSAGE)
                    .accountInfo(null)
                    .build();
        }

        user foundUser = userRepository.findByAccountNumber(request.getAccountNumber());
        return BankResponse.builder()
                .responseCode(AccountUtils.ACCOUNT_FOUND_CODE)
                .responseMessage(AccountUtils.ACCOUNT_FOUND_SUCCESS)
                .accountInfo(AccountInfo.builder()
                        .accountBalance(foundUser.getAccountBalance())
                        .accountNumber(foundUser.getAccountNumber())
                        .accountName(foundUser.getFirstname() + " " + foundUser.getLastname())

                        .build())
                .build();
    }

    @Override
    public String nameEnquiry(EnquiryRequest request) {
        boolean isAccountExist = userRepository.existsByAccountNumber(request.getAccountNumber());
        if (!isAccountExist) {
            return AccountUtils.ACCOUNT_NOT_EXIST_MESSAGE;

        }
        user founduser = userRepository.findByAccountNumber(request.getAccountNumber());
        return founduser.getFirstname() + " " + founduser.getLastname();
    }

    @Override
    public BankResponse creditAccount(CreditDebitRequest request) {
        //checking if the account exists
        boolean isAccountExist = userRepository.existsByAccountNumber(request.getAccountNumber());
        if (!isAccountExist) {
            return BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_NOT_EXIST_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_NOT_EXIST_MESSAGE)
                    .accountInfo(null)
                    .build();
        }
        user usertoCredit=userRepository.findByAccountNumber(request.getAccountNumber());
        usertoCredit.setAccountBalance(usertoCredit.getAccountBalance().add(request.getAmount()));
        userRepository.save(usertoCredit);
        return BankResponse.builder()
                .responseCode(AccountUtils.ACCOUNT_CREDITED_SUCCESS)
                .responseMessage(AccountUtils.ACCOUNT_CREDITED_SUCCESS_MESSAGE)
                .accountInfo(AccountInfo.builder()
                        .accountName(usertoCredit.getFirstname()+" "+usertoCredit.getLastname())
                        .accountBalance(usertoCredit.getAccountBalance())
                        .accountNumber(request.getAccountNumber())
                        .build())
                .build();
    }

    @Override
    public BankResponse debitAccount(CreditDebitRequest request) {
        //check account exist and amount intended to withdraw not more than available
        boolean isAccountExist = userRepository.existsByAccountNumber(request.getAccountNumber());
        if (!isAccountExist) {
            return BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_NOT_EXIST_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_NOT_EXIST_MESSAGE)
                    .accountInfo(null)
                    .build();
        }
        user userToDebit=userRepository.findByAccountNumber(request.getAccountNumber());
        BigInteger availableBalance= (userToDebit.getAccountBalance().toBigInteger());
        BigInteger debitAmount=(request.getAmount().toBigInteger());
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
                .responseCode(AccountUtils.ACCOUNT_DEBITED_SUCCESS)
                .responseMessage(AccountUtils.ACCOUNT_DEBITED_MESSAGE)
                .accountInfo(AccountInfo.builder()
                        .accountNumber(request.getAccountNumber())
                        .accountName(userToDebit.getFirstname()+" "+userToDebit.getLastname())
                        .accountBalance(userToDebit.getAccountBalance())
                        .build())

                .build();
        }
    }

    @Override
    public BankResponse transfer(TransferRequest request) {
        //get acct to debit , check ifr amt > crnt balance
        //debit the accnt , get accnt to credit , credit the accnt
       // boolean isSourceAccountExist=userRepository.existsByAccountNumber(request.getSourceAccountNumber());
        boolean isDestinationAccountExist= userRepository.existsByAccountNumber(request.getDestinationAccountNumber());
        if(!isDestinationAccountExist){
            return BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_NOT_EXIST_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_NOT_EXIST_MESSAGE)
                    .accountInfo(null)
                    .build();

        }

          user sourceAccountUser=userRepository.findByAccountNumber(request.getSourceAccountNumber());
        if(request.getAmount().compareTo(sourceAccountUser.getAccountBalance()) > 0){
            return BankResponse.builder()
                    .responseCode(AccountUtils.INSUFFICIENT_BALANCE_CODE)
                    .responseMessage(AccountUtils.INSUFFICIENT_BALANCE_MESSAGE)
                    .accountInfo(null)
                    .build();
        }
       sourceAccountUser.setAccountBalance(sourceAccountUser.getAccountBalance().subtract(request.getAmount()));
    userRepository.save(sourceAccountUser);
    EmailDetails debitAlert=EmailDetails.builder()
            .subject("DEBIT ALERT")
            .recipient(sourceAccountUser.getMail())
            .messageBody("the sum of "+request.getAmount()+"has been deducted  \n current balance"+sourceAccountUser.getAccountBalance())
            .build();
  emailService.sendEmailAlert(debitAlert);


    user destinationAccountUser=userRepository.findByAccountNumber(request.getDestinationAccountNumber());
    destinationAccountUser.setAccountBalance(destinationAccountUser.getAccountBalance().add(request.getAmount()));
    userRepository.save(destinationAccountUser);
    EmailDetails creditAlert=EmailDetails.builder()
                .subject("credit ALERT")
                .recipient(destinationAccountUser.getMail())
                .messageBody("the sum of "+request.getAmount()+"has been added from"+sourceAccountUser.getFirstname() +" current balance"+destinationAccountUser.getAccountBalance())
                .build();
        emailService.sendEmailAlert(creditAlert);

        return BankResponse.builder()
                .responseCode(AccountUtils.TRANSFER_SUCCESS_CODE)
                .responseMessage(AccountUtils.TRANSFER_SUCCESS_MESSAGE)
                .accountInfo(null)
                .build();
    }
    //balance enquiry
}
