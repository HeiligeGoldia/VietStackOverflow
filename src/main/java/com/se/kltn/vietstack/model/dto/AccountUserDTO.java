package com.se.kltn.vietstack.model.dto;

import com.se.kltn.vietstack.model.user.Account;
import com.se.kltn.vietstack.model.user.User;
import lombok.Data;

@Data
public class AccountUserDTO {

    private Account account;
    private User user;

}
