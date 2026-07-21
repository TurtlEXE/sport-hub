package com.mvc.mock_project.controller.web.owner;

import com.mvc.mock_project.dto.request.VoucherFormDTO;
import com.mvc.mock_project.entities.Facility;
import com.mvc.mock_project.entities.Voucher;
import com.mvc.mock_project.entities.enums.DiscountType;
import com.mvc.mock_project.repository.FacilityRepository;
import com.mvc.mock_project.security.CustomUserDetails;
import com.mvc.mock_project.service.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/owner/vouchers")
@RequiredArgsConstructor
public class OwnerVoucherController {

    private final VoucherService voucherService;
    private final FacilityRepository facilityRepository;

    @GetMapping
    public String listVouchers(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Integer ownerId = userDetails.getAccount().getId();
        model.addAttribute("vouchers", voucherService.findByIssuerAccountId(ownerId));
        return "owner/voucher/list";
    }

    @GetMapping("/create")
    public String showCreateForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Integer ownerId = userDetails.getAccount().getId();
        List<Facility> myFacilities = facilityRepository.findByOwner_IdOrderByCreatedAtDesc(ownerId);
        
        model.addAttribute("voucherForm", new VoucherFormDTO());
        model.addAttribute("discountTypes", DiscountType.values());
        model.addAttribute("myFacilities", myFacilities);
        return "owner/voucher/form";
    }

    @PostMapping("/create")
    public String processCreateForm(@AuthenticationPrincipal CustomUserDetails userDetails,
                                    @Valid @ModelAttribute("voucherForm") VoucherFormDTO voucherForm,
                                    BindingResult bindingResult,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        Integer ownerId = userDetails.getAccount().getId();
        if (bindingResult.hasErrors()) {
            List<Facility> myFacilities = facilityRepository.findByOwner_IdOrderByCreatedAtDesc(ownerId);
            model.addAttribute("discountTypes", DiscountType.values());
            model.addAttribute("myFacilities", myFacilities);
            return "owner/voucher/form";
        }

        try {
            voucherService.createOwnerVoucher(ownerId, voucherForm);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo mã giảm giá (Voucher) thành công!");
            return "redirect:/owner/vouchers";
        } catch (Exception e) {
            List<Facility> myFacilities = facilityRepository.findByOwner_IdOrderByCreatedAtDesc(ownerId);
            model.addAttribute("discountTypes", DiscountType.values());
            model.addAttribute("myFacilities", myFacilities);
            model.addAttribute("errorMessage", "Lỗi tạo voucher: " + e.getMessage());
            return "owner/voucher/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@AuthenticationPrincipal CustomUserDetails userDetails,
                               @PathVariable Integer id,
                               Model model) {
        Integer ownerId = userDetails.getAccount().getId();
        Voucher voucher = voucherService.findByIdAndIssuerAccountId(id, ownerId);
        List<Facility> myFacilities = facilityRepository.findByOwner_IdOrderByCreatedAtDesc(ownerId);

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
            formDTO.setFacilityIds(voucher.getApplicableFacilities().stream().map(Facility::getId).collect(Collectors.toList()));
        }

        model.addAttribute("voucherForm", formDTO);
        model.addAttribute("discountTypes", DiscountType.values());
        model.addAttribute("myFacilities", myFacilities);
        return "owner/voucher/form";
    }

    @PostMapping("/edit/{id}")
    public String processEditForm(@AuthenticationPrincipal CustomUserDetails userDetails,
                                  @PathVariable Integer id,
                                  @Valid @ModelAttribute("voucherForm") VoucherFormDTO voucherForm,
                                  BindingResult bindingResult,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        Integer ownerId = userDetails.getAccount().getId();
        if (bindingResult.hasErrors()) {
            List<Facility> myFacilities = facilityRepository.findByOwner_IdOrderByCreatedAtDesc(ownerId);
            model.addAttribute("discountTypes", DiscountType.values());
            model.addAttribute("myFacilities", myFacilities);
            return "owner/voucher/form";
        }

        try {
            voucherService.updateOwnerVoucher(ownerId, id, voucherForm);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật voucher thành công!");
            return "redirect:/owner/vouchers";
        } catch (Exception e) {
            List<Facility> myFacilities = facilityRepository.findByOwner_IdOrderByCreatedAtDesc(ownerId);
            model.addAttribute("discountTypes", DiscountType.values());
            model.addAttribute("myFacilities", myFacilities);
            model.addAttribute("errorMessage", "Lỗi cập nhật voucher: " + e.getMessage());
            return "owner/voucher/form";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteVoucher(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @PathVariable Integer id,
                                RedirectAttributes redirectAttributes) {
        Integer ownerId = userDetails.getAccount().getId();
        try {
            voucherService.deleteOwnerVoucher(ownerId, id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa voucher thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi xóa voucher: " + e.getMessage());
        }
        return "redirect:/owner/vouchers";
    }
}
