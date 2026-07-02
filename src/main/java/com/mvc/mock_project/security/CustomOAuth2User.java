package com.mvc.mock_project.security;

import com.mvc.mock_project.entities.Account;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {

    private OAuth2User oAuth2User;
    private Account account;

    public CustomOAuth2User(OAuth2User oAuth2User, Account account) {
        this.oAuth2User = oAuth2User;
        this.account = account;
    }

    public Account getAccount() {
        return account;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oAuth2User.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + account.getRole().name()));
    }

    @Override
    public String getName() {
        return account.getFullName();
    }
}
