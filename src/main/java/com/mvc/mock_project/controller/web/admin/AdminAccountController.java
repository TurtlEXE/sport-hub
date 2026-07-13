package com.mvc.mock_project.controller.web.admin;

import com.mvc.mock_project.dto.request.AccountFormDTO;
import com.mvc.mock_project.entities.Account;
import com.mvc.mock_project.entities.enums.Role;
import com.mvc.mock_project.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/accounts")
@RequiredArgsConstructor
public class AdminAccountController {

    private final AccountService accountService;

    @GetMapping
    public String listAccounts(Model model) {
        model.addAttribute("accounts", accountService.findAll());
        return "admin/account/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("accountForm", new AccountFormDTO());
        model.addAttribute("roles", Role.values());
        return "admin/account/form";
    }

    @PostMapping("/create")
    public String processCreateForm(@Valid @ModelAttribute("accountForm") AccountFormDTO accountForm, 
                                    BindingResult bindingResult, 
                                    Model model, 
                                    RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", Role.values());
            return "admin/account/form";
        }
        
        try {
            accountService.save(accountForm);
            redirectAttributes.addFlashAttribute("successMessage", "Account created successfully!");
            return "redirect:/admin/accounts";
        } catch (Exception e) {
            model.addAttribute("roles", Role.values());
            model.addAttribute("errorMessage", "Error creating account: " + e.getMessage());
            return "admin/account/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Account account = accountService.findById(id);
        
        AccountFormDTO formDTO = new AccountFormDTO();
        formDTO.setId(account.getId());
        formDTO.setFullName(account.getFullName());
        formDTO.setEmail(account.getEmail());
        formDTO.setPhone(account.getPhone());
        formDTO.setRole(account.getRole());
        formDTO.setIsActive(account.getIsActive());
        // password is left null, meaning it won't be updated unless provided
        
        model.addAttribute("accountForm", formDTO);
        model.addAttribute("roles", Role.values());
        return "admin/account/form";
    }

    @PostMapping("/edit/{id}")
    public String processEditForm(@PathVariable Integer id, 
                                  @Valid @ModelAttribute("accountForm") AccountFormDTO accountForm, 
                                  BindingResult bindingResult, 
                                  Model model, 
                                  RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", Role.values());
            return "admin/account/form";
        }
        
        try {
            accountService.update(id, accountForm);
            redirectAttributes.addFlashAttribute("successMessage", "Account updated successfully!");
            return "redirect:/admin/accounts";
        } catch (Exception e) {
            model.addAttribute("roles", Role.values());
            model.addAttribute("errorMessage", "Error updating account: " + e.getMessage());
            return "admin/account/form";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteAccount(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            accountService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Account deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting account: " + e.getMessage());
        }
        return "redirect:/admin/accounts";
    }
}
