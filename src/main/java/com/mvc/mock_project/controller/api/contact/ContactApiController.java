package com.mvc.mock_project.controller.api.contact;

import com.mvc.mock_project.dto.request.ContactMessageRequest;
import com.mvc.mock_project.dto.response.ApiResponse;
import com.mvc.mock_project.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
public class ContactApiController {

    private final ContactService contactService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> sendMessage(@Valid @RequestBody ContactMessageRequest request) {
        contactService.processMessage(request);
        return ResponseEntity.ok(ApiResponse.success("msg.success.contact_sent"));
    }
}
