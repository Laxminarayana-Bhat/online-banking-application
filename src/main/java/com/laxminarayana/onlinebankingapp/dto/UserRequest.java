package com.laxminarayana.onlinebankingapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {

    private String firstname;
    private String lastname;
    private String gender;
    private String address;
    private String stateOfOrigin;

    private String mail;

    private String phoneNumber;
    private String alternativePhoneNumber;

}
