package com.se.kltn.vietstack.repository;

import com.se.kltn.vietstack.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {

    @Query(value = "select email from account where email = :email", nativeQuery = true)
    String checkEmail(@Param("email") String email);

    @Modifying
    @Transactional
    @Query(value = "insert into account values (:email, :password, :uid)", nativeQuery = true)
    void createAccount(@Param("email") String email, @Param("password") String password, @Param("uid") String uid);

}
