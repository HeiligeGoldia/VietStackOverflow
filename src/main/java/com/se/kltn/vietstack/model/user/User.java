package com.se.kltn.vietstack.model.user;

import lombok.Data;

@Data
public class User {

    private String uid;
    private String name;
    private String email;
    private String location;
    private String avatar;
    private String role;
    private String about;

}
