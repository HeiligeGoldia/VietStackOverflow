package com.se.kltn.vietstack.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.se.kltn.vietstack.model.Account;
import com.twilio.Twilio;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

    FirebaseAuth auth = FirebaseAuth.getInstance();

    public String register1(String email) throws FirebaseAuthException {
        Twilio.init("AC96e6a911a4dbdeb1ba8e7d5aaabedd76", "a46fcbb8d7640d91183e38d5e8e0a73d");
        Verification verification = Verification.creator(
                        "VA9a5636ec411f18ae4d68159a9c9518e9",
                email,"email").create();
        return "email sent";
    }

    public String register2(String email, String otp){
        VerificationCheck verificationCheck = VerificationCheck.creator("VA9a5636ec411f18ae4d68159a9c9518e9", otp)
                .setTo(email)
                .create();
        if(verificationCheck.getStatus().equals("approved")){
            return "Approved";
        }
        else {
            return "OTP invalid";
        }
    }

    public String create(Account account) throws FirebaseAuthException {
        UserRecord.CreateRequest ur = new UserRecord.CreateRequest();
        ur.setEmail(account.getEmail());
        ur.setPassword(account.getPassword());
        String id = auth.createUser(ur).getUid();
        return id;
    }

}
