package com.mvc.mock_project.controller.web.admin;

import com.mvc.mock_project.dto.request.VoucherFormDTO;
import com.mvc.mock_project.entities.Voucher;
import com.mvc.mock_project.entities.enums.DiscountType;
import com.mvc.mock_project.service.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/vouchers")
@RequiredArgsConstructor
public class AdminVoucherController {

    private final VoucherService voucherService;

    @GetMapping
    public String listVouchers(Model model) {
        model.addAttribute("vouchers", voucherService.findAll());
        return "admin/voucher/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("voucherForm", new VoucherFormDTO());
        model.addAttribute("discountTypes", DiscountType.values());
        return "admin/voucher/form";
    }

    @PostMapping("/create")
    public String processCreateForm(@Valid @ModelAttribute("voucherForm") VoucherFormDTO voucherForm, 
                                    BindingResult bindingResult, 
                                    Model model, 
                                    RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("discountTypes", DiscountType.values());
            return "admin/voucher/form";
        }
        
        try {
            voucherService.save(voucherForm);
            redirectAttributes.addFlashAttribute("successMessage", "Voucher created successfully!");
            return "redirect:/admin/vouchers";
        } catch (Exception e) {
            model.addAttribute("discountTypes", DiscountType.values());
            model.addAttribute("errorMessage", "Error creating voucher: " + e.getMessage());
            return "admin/voucher/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Voucher voucher = voucherService.findById(id);
        
        VoucherFormDTO formDTO = new VoucherFormDTO();
        formDTO.setId(voucher.getId());
        formDTO.setCode(voucher.getCode());
        formDTO.setName(voucher.getName());
        formDTO.setDescription(voucher.getDescription());
        formDTO.setDiscountType(voucher.getDiscountType());
        formDTO.setDiscountValue(voucher.getDiscountValue());
        formDTO.setValidFrom(voucher.getValidFrom());
        formDTO.setValidTo(voucher.getValidTo());
        formDTO.setMinOrderAmount(voucher.getMinOrderAmount());
        formDTO.setMaxDiscountAmount(voucher.getMaxDiscountAmount());
        formDTO.setUsageLimit(voucher.getUsageLimit());
        formDTO.setPerUserLimit(voucher.getPerUserLimit());
        formDTO.setIsActive(voucher.getIsActive());
        
        model.addAttribute("voucherForm", formDTO);
        model.addAttribute("discountTypes", DiscountType.values());
        return "admin/voucher/form";
    }

    @PostMapping("/edit/{id}")
    public String processEditForm(@PathVariable Integer id, 
                                  @Valid @ModelAttribute("voucherForm") VoucherFormDTO voucherForm, 
                                  BindingResult bindingResult, 
                                  Model model, 
                                  RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("discountTypes", DiscountType.values());
            return "admin/voucher/form";
        }
        
        try {
            voucherService.update(id, voucherForm);
            redirectAttributes.addFlashAttribute("successMessage", "Voucher updated successfully!");
            return "redirect:/admin/vouchers";
        } catch (Exception e) {
            model.addAttribute("discountTypes", DiscountType.values());
            model.addAttribute("errorMessage", "Error updating voucher: " + e.getMessage());
            return "admin/voucher/form";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteVoucher(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            voucherService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Voucher deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting voucher: " + e.getMessage());
        }
        return "redirect:/admin/vouchers";
    }
}
