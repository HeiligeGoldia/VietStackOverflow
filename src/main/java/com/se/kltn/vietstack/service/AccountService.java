package com.se.kltn.vietstack.service;

import com.se.kltn.vietstack.model.Account;
import com.se.kltn.vietstack.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

    @Autowired
    AccountRepository accountRepository;

    public boolean checkEmail(String email){
        if(accountRepository.checkEmail(email)==null){
            return true;
        }
        else return false;
    }

    public void createAccount(Account account){
        accountRepository.createAccount(account.getEmail(), account.getPassword(), account.getUid());
    }

}
