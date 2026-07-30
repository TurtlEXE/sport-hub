package com.mvc.mock_project.service.impl;

import com.mvc.mock_project.dto.request.AccountFormDTO;
import com.mvc.mock_project.entities.Account;
import com.mvc.mock_project.repository.AccountRepository;
import com.mvc.mock_project.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import com.mvc.mock_project.entities.enums.Role;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<Account> findAll() {
        return accountRepository.findAll();
    }

    @Override
    public Account findById(Integer id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }

    @Override
    @Transactional
    public void save(AccountFormDTO form) {
        Account account = Account.builder()
                .fullName(form.getFullName())
                .email(form.getEmail())
                .phone(form.getPhone())
                .role(form.getRole())
                .isActive(form.getIsActive() != null ? form.getIsActive() : true)
                .build();
                
        if (form.getPassword() != null && !form.getPassword().trim().isEmpty()) {
            account.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        } else {
            // Default password if not provided
            account.setPasswordHash(passwordEncoder.encode("123456"));
        }
        
        accountRepository.save(account);
    }

    @Override
    @Transactional
    public void update(Integer id, AccountFormDTO form) {
        Account account = findById(id);

        // 1. Prevent changing the role of Super Admin (id = 1)
        if (id == 1 && account.getRole() != form.getRole()) {
            throw new RuntimeException("Cannot change the role of the system super admin (ID = 1).");
        }

        // 2. Prevent removing the last admin from the system (either by changing role or deactivating)
        if (account.getRole() == Role.ADMIN) {
            boolean isDemoting = form.getRole() != Role.ADMIN;
            boolean isDeactivating = form.getIsActive() != null && !form.getIsActive();

            if (isDemoting || isDeactivating) {
                long adminCount = accountRepository.countByRole(Role.ADMIN);
                if (adminCount <= 1) {
                    if (isDemoting) {
                        throw new RuntimeException("Cannot change role. This is the last active admin in the system.");
                    } else {
                        throw new RuntimeException("Cannot deactivate account. This is the last active admin in the system.");
                    }
                }
            }
        }

        account.setFullName(form.getFullName());
        account.setEmail(form.getEmail());
        account.setPhone(form.getPhone());
        account.setRole(form.getRole());
        account.setIsActive(form.getIsActive() != null ? form.getIsActive() : false);
        
        if (form.getPassword() != null && !form.getPassword().trim().isEmpty()) {
            account.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        }
        
        accountRepository.save(account);
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        if (id == 1) {
            throw new RuntimeException("Cannot delete the system super admin (ID = 1).");
        }

        Account account = findById(id);
        if (account.getRole() == Role.ADMIN) {
            long adminCount = accountRepository.countByRole(Role.ADMIN);
            if (adminCount <= 1) {
                throw new RuntimeException("Cannot delete account. This is the last admin in the system.");
            }
        }

        accountRepository.deleteById(id);
    }
}
