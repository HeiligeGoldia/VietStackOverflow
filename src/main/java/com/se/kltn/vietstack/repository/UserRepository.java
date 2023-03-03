package com.se.kltn.vietstack.repository;

import com.se.kltn.vietstack.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {



}
