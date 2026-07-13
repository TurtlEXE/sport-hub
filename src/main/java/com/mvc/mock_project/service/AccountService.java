package com.mvc.mock_project.service;

import com.mvc.mock_project.dto.request.AccountFormDTO;
import com.mvc.mock_project.entities.Account;
import java.util.List;

public interface AccountService {
    List<Account> findAll();
    Account findById(Integer id);
    void save(AccountFormDTO form);
    void update(Integer id, AccountFormDTO form);
    void deleteById(Integer id);
}
