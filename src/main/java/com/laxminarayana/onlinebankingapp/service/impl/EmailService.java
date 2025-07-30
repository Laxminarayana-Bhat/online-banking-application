package com.laxminarayana.onlinebankingapp.service.impl;

import com.laxminarayana.onlinebankingapp.dto.EmailDetails;

public interface EmailService {
    void sendEmailAlert(EmailDetails emailDetails);
}
