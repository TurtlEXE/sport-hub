package com.mvc.mock_project.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactMessageRequest {

    @NotBlank(message = "{contact.validation.name.required}")
    private String name;

    @NotBlank(message = "{contact.validation.email.required}")
    @Email(message = "{contact.validation.email.invalid}")
    private String email;

    @NotBlank(message = "{contact.validation.message.required}")
    private String message;
}
