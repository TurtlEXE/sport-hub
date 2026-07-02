package com.mvc.mock_project.security;

import com.mvc.mock_project.entities.Account;
import com.mvc.mock_project.entities.enums.Role;
import com.mvc.mock_project.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final AccountRepository accountRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        try {
            return processOAuth2User(userRequest, oAuth2User);
        } catch (Exception ex) {
            // Throwing an instance of AuthenticationException will trigger the OAuth2AuthenticationFailureHandler
            throw new OAuth2AuthenticationException(ex.getMessage());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        
        String email = "";
        String name = "";
        String id = "";
        String avatarUrl = "";

        if (registrationId.equalsIgnoreCase("google")) {
            email = oAuth2User.getAttribute("email");
            name = oAuth2User.getAttribute("name");
            id = oAuth2User.getAttribute("sub");
            avatarUrl = oAuth2User.getAttribute("picture");
        } else if (registrationId.equalsIgnoreCase("facebook")) {
            email = oAuth2User.getAttribute("email");
            name = oAuth2User.getAttribute("name");
            id = oAuth2User.getAttribute("id");
            // For facebook, picture is nested: picture.data.url, skipped for simplicity or get it if needed.
        }

        if (email == null || email.isEmpty()) {
            throw new RuntimeException("Email not found from OAuth2 provider");
        }

        Optional<Account> accountOptional = accountRepository.findByEmail(email);
        Account account;
        if (accountOptional.isPresent()) {
            account = accountOptional.get();
            if (account.getGoogleId() == null && registrationId.equalsIgnoreCase("google")) {
                account.setGoogleId(id);
            }
            if (account.getAvatarPath() == null && avatarUrl != null) {
                account.setAvatarPath(avatarUrl);
            }
            account = accountRepository.save(account);
        } else {
            account = new Account();
            account.setEmail(email);
            account.setFullName(name);
            account.setPasswordHash(""); // No password for oauth2
            account.setRole(Role.CUSTOMER); // Default role
            account.setIsActive(true); // Auto verified
            account.setCreatedAt(LocalDateTime.now());
            account.setAvatarPath(avatarUrl);

            if (registrationId.equalsIgnoreCase("google")) {
                account.setGoogleId(id);
            }
            
            account = accountRepository.save(account);
        }

        return new CustomOAuth2User(oAuth2User, account);
    }
}
