package com.laxminarayana.onlinebankingapp.repository;

import com.laxminarayana.onlinebankingapp.entity.user;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;

public interface UserRepository extends JpaRepository<user,Long> {
    Boolean existsByMail(String mail);
    Boolean existsByAccountNumber(String accountNumber);
    user findByAccountNumber(String accountNumber);
}
