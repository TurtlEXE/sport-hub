package com.mvc.mock_project.service;

import com.mvc.mock_project.dto.request.ContactMessageRequest;
import com.mvc.mock_project.entities.ContactMessage;
import com.mvc.mock_project.repository.ContactMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContactService {

    private final EmailService emailService;
    private final ContactMessageRepository contactMessageRepository;

    public void processMessage(ContactMessageRequest request) {
        log.info("Processing contact message from: {}", request.getEmail());
        
        // Save to Database
        ContactMessage contactMessage = ContactMessage.builder()
                .name(request.getName())
                .email(request.getEmail())
                .message(request.getMessage())
                .createdAt(LocalDateTime.now())
                .build();
        contactMessageRepository.save(contactMessage);

        // Send notification email to Admin
        emailService.sendContactEmail(request.getName(), request.getEmail(), request.getMessage());
        
        // Send automated confirmation email to Customer (HTML)
        emailService.sendFeedbackConfirmationEmail(request.getName(), request.getEmail(), request.getMessage());
    }
}
