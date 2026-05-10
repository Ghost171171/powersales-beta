package com.controller;

import com.model.contract.Contract;
import com.model.contract.Contract_DTO;
import com.model.enums.ContractProcessStatus;
import com.model.user.SessionUser;
import com.service.ContractService;
import com.util.AuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/contracts")
public class ContractController {
    private final ContractService contractService;
    private final static Logger log = LoggerFactory.getLogger(ContractController.class);

    public ContractController(ContractService contractService) {
        this.contractService = contractService;
    }

    public record ContractRequest(String rawContractNotes) {}

    @PostMapping("/rawContractNotes")
    public void addContract(HttpServletRequest request, @RequestBody ContractRequest requestBody) {
        SessionUser sessionUser = AuthUtil.getSessionUser(request);
        if (sessionUser == null) {
            log.warn("Unauthorized access attempt to /contracts/rawContractNotes without valid session by {}", request.getRequestURI());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not logged in");
        }
        contractService.addContract(sessionUser.getId(), requestBody.rawContractNotes());
    }

    @PutMapping("/{id}/status")
    public void updateContractStatus(@PathVariable String id, @RequestBody ContractProcessStatus status, HttpSession session) {
        SessionUser sessionUser = AuthUtil.requireUser(session);
        AuthUtil.requireAdmin(sessionUser);
        Contract update = contractService.getContractById(id);
        if (update == null) {
            log.warn("Contract with id {} does not exist", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Contract not found");
        }
        update.setContractProcessStatus(status != null ? status : ContractProcessStatus.IN_PROGRESS);
        log.info("Status of Contract with id {} has been updated to {}", id, status);
        contractService.updateContract(update);
    }

    @GetMapping()
    public List<Contract_DTO> getContractsByUserId(HttpServletRequest request) {
        SessionUser user = AuthUtil.getSessionUser(request);
        if (user == null) {
            log.warn("Unauthorized access attempt to /contracts without valid session by {}", request.getRequestURI());
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "No active session found"
            );
        }
        log.info("User {} successfully accessed /contracts with valid session", user.getUsername());
        return contractService.getContractsByUserId(user.getId());
    }

    @GetMapping("/all")
    public List<Contract_DTO> getContractsByAdmin(HttpServletRequest request) {
        SessionUser user = AuthUtil.getSessionUser(request);
        if (user == null) {
            log.warn("Unauthorized access attempt to /contracts/all without valid session by {}", request.getRequestURI());
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "No active session found"
            );
        }
        AuthUtil.requireAdmin(user);
        return  contractService.getContracts();
    }
}
