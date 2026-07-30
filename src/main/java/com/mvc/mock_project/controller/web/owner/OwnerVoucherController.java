package com.mvc.mock_project.controller.web.owner;

import com.mvc.mock_project.dto.request.VoucherFormDTO;
import com.mvc.mock_project.entities.Account;
import com.mvc.mock_project.entities.Facility;
import com.mvc.mock_project.entities.Voucher;
import com.mvc.mock_project.entities.enums.IssuerType;
import com.mvc.mock_project.repository.AccountRepository;
import com.mvc.mock_project.repository.FacilityRepository;
import com.mvc.mock_project.repository.VoucherRepository;
import com.mvc.mock_project.service.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/owner/vouchers")
@RequiredArgsConstructor
public class OwnerVoucherController {

    private final VoucherService voucherService;
    private final VoucherRepository voucherRepository;
    private final FacilityRepository facilityRepository;
    private final AccountRepository accountRepository;

    private Account getLoggedInAccount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof com.mvc.mock_project.security.CustomUserDetails) {
                return ((com.mvc.mock_project.security.CustomUserDetails) principal).getAccount();
            } else if (principal instanceof com.mvc.mock_project.security.CustomOAuth2User) {
                return ((com.mvc.mock_project.security.CustomOAuth2User) principal).getAccount();
            } else if (principal instanceof UserDetails) {
                String username = ((UserDetails) principal).getUsername();
                return accountRepository.findByEmail(username).orElse(null);
            }
        }
        return null;
    }

    @GetMapping
    public String listVouchers(Model model) {
        Account owner = getLoggedInAccount();
        if (owner == null) return "redirect:/login";

        List<Voucher> vouchers = voucherRepository.findByIssuerAccountId(owner.getId());
        model.addAttribute("vouchers", vouchers);
        return "owner/voucher/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        Account owner = getLoggedInAccount();
        if (owner == null) return "redirect:/login";

        model.addAttribute("voucherForm", new VoucherFormDTO());
        model.addAttribute("facilities", facilityRepository.findByOwner_IdOrderByCreatedAtDesc(owner.getId()));
        return "owner/voucher/form";
    }

    @PostMapping("/create")
    public String processCreateForm(@Valid @ModelAttribute("voucherForm") VoucherFormDTO voucherForm, 
                                  BindingResult result,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        Account owner = getLoggedInAccount();
        if (owner == null) return "redirect:/login";

        if (result.hasErrors()) {
            model.addAttribute("facilities", facilityRepository.findByOwner_IdOrderByCreatedAtDesc(owner.getId()));
            return "owner/voucher/form";
        }
        
        try {
            voucherService.save(voucherForm, IssuerType.OWNER, owner);
            redirectAttributes.addFlashAttribute("successMessage", "Voucher created successfully!");
            return "redirect:/owner/vouchers";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error creating voucher: " + e.getMessage());
            model.addAttribute("facilities", facilityRepository.findByOwner_IdOrderByCreatedAtDesc(owner.getId()));
            return "owner/voucher/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Account owner = getLoggedInAccount();
        if (owner == null) return "redirect:/login";

        Voucher voucher = voucherService.findById(id);
        
        // Security check: Only allow if it's the owner's voucher
        if (voucher.getIssuerAccount() == null || !voucher.getIssuerAccount().getId().equals(owner.getId())) {
            return "redirect:/owner/vouchers";
        }

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
        
        if (voucher.getApplicableFacilities() != null) {
            formDTO.setFacilityIds(voucher.getApplicableFacilities().stream().map(Facility::getId).toList());
        }

        model.addAttribute("voucherForm", formDTO);
        model.addAttribute("facilities", facilityRepository.findByOwner_IdOrderByCreatedAtDesc(owner.getId()));
        return "owner/voucher/form";
    }

    @PostMapping("/edit/{id}")
    public String processEditForm(@PathVariable Integer id,
                                  @Valid @ModelAttribute("voucherForm") VoucherFormDTO voucherForm, 
                                  BindingResult result,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        Account owner = getLoggedInAccount();
        if (owner == null) return "redirect:/login";

        if (result.hasErrors()) {
            model.addAttribute("facilities", facilityRepository.findByOwner_IdOrderByCreatedAtDesc(owner.getId()));
            return "owner/voucher/form";
        }
        
        try {
            // Security check
            Voucher voucher = voucherService.findById(id);
            if (voucher.getIssuerAccount() == null || !voucher.getIssuerAccount().getId().equals(owner.getId())) {
                return "redirect:/owner/vouchers";
            }

            voucherService.update(id, voucherForm, IssuerType.OWNER, owner);
            redirectAttributes.addFlashAttribute("successMessage", "Voucher updated successfully!");
            return "redirect:/owner/vouchers";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error updating voucher: " + e.getMessage());
            model.addAttribute("facilities", facilityRepository.findByOwner_IdOrderByCreatedAtDesc(owner.getId()));
            return "owner/voucher/form";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteVoucher(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        Account owner = getLoggedInAccount();
        if (owner == null) return "redirect:/login";

        try {
            Voucher voucher = voucherService.findById(id);
            if (voucher.getIssuerAccount() != null && voucher.getIssuerAccount().getId().equals(owner.getId())) {
                voucherService.deleteById(id);
                redirectAttributes.addFlashAttribute("successMessage", "Voucher deleted successfully!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "You are not authorized to delete this voucher.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting voucher: " + e.getMessage());
        }
        return "redirect:/owner/vouchers";
    }
}
