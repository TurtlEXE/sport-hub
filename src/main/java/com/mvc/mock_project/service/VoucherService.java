package com.mvc.mock_project.service;

import com.mvc.mock_project.dto.request.VoucherFormDTO;
import com.mvc.mock_project.entities.Voucher;

import java.util.List;

public interface VoucherService {
    List<Voucher> findAll();
    Voucher findById(Integer id);
    void save(VoucherFormDTO form);
    void update(Integer id, VoucherFormDTO form);
    void deleteById(Integer id);

    List<Voucher> findByIssuerAccountId(Integer accountId);
    Voucher findByIdAndIssuerAccountId(Integer id, Integer accountId);
    void createOwnerVoucher(Integer ownerAccountId, VoucherFormDTO form);
    void updateOwnerVoucher(Integer ownerAccountId, Integer voucherId, VoucherFormDTO form);
    void deleteOwnerVoucher(Integer ownerAccountId, Integer voucherId);
}
