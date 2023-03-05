package com.se.kltn.vietstack.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name="account")
@Data
public class Account {

    @Id
    private String email;
    private String password;
    private String uid;

}
