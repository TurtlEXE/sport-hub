package com.mvc.mock_project.service;

import com.mvc.mock_project.dto.request.ContactMessageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContactService {

    private final EmailService emailService;

    public void processMessage(ContactMessageRequest request) {
        log.info("Processing contact message from: {}", request.getEmail());
        emailService.sendContactEmail(request.getName(), request.getEmail(), request.getMessage());
    }
}
