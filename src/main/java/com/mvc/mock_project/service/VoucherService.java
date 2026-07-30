package com.mvc.mock_project.service;

import com.mvc.mock_project.dto.request.VoucherFormDTO;
import com.mvc.mock_project.entities.Voucher;

import java.util.List;

import com.mvc.mock_project.entities.Account;
import com.mvc.mock_project.entities.enums.IssuerType;

public interface VoucherService {
    List<Voucher> findAll();
    Voucher findById(Integer id);
    void save(VoucherFormDTO form, IssuerType issuerType, Account issuerAccount);
    void update(Integer id, VoucherFormDTO form, IssuerType issuerType, Account issuerAccount);
    void deleteById(Integer id);
}
