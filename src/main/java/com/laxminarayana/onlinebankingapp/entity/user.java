package com.laxminarayana.onlinebankingapp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Builder
@Entity
@Table(name="users")
public class user {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;

    private String firstname;
    private String lastname;
    private String gender;
    private String address;
    private String stateOfOrigin;
    private String accountNumber;
    private BigDecimal accountBalance;
    private String mail;

    private String phoneNumber;
    private String alternativePhoneNumber;
    private String status;
    @CreationTimestamp

    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime modifiedAt;


}
