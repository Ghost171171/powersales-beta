package com.service;

import com.mapper.ContractMapper;
import com.model.contract.Contract;
import com.model.contract.Contract_DTO;
import com.model.enums.ContractType;
import com.repository.ContractRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ContractService {
    private final ContractRepository contractRepository;
    private final static Logger log = LoggerFactory.getLogger(ContractService.class);

    public ContractService(ContractRepository contractRepository) {
        this.contractRepository = contractRepository;
    }

    public void addContract(UUID id, String rawContractNotes) {
        if (rawContractNotes.isEmpty()) {
            log.info("Received Contract Notes from user of id {} but it's empty", id);
            return;
        }

        List<Contract> contracts = parseContracts(rawContractNotes, id);

        if (contracts != null) {
            addMultipleContracts(contracts);
            log.info("Added Contract from user of id {} ", id);
        } else {
            log.info("No valid contract format or contract found from user of id {} !", id);
        }
    }

    //Füge mehrere Contracts hinzu
    private void addMultipleContracts(List<Contract> contracts) {
        for (Contract contract : contracts) {
            contractRepository.addContract(contract);
        }
    }

    public Contract getContractById(String id) {
        return contractRepository.getContract(id).orElse(null);
    }

    public List<Contract_DTO> getContractsByUserId(UUID id) {
        return ContractMapper.toContractDTOList(contractRepository.getContractsByUserId(id));
    }

    public List<Contract_DTO> getContracts() {
        return ContractMapper.toContractDTOList(contractRepository.getAllContracts());
    }

    public void deleteContract(UUID id) {
        contractRepository.deleteContract(id);
    }

    public void updateContract(Contract contract) {
        contractRepository.updateContract(contract);
    }


    //parse multiple or a single contract by raw notes
    /*FORMAT: PROVISION_NUM1;CONTRACT_NUM1\nPROVISION_NUM2;CONTRACT_NUM2\n....
    */
    public List<Contract> parseContracts(String rawNotes, UUID userId) {
        if (rawNotes == null || rawNotes.isBlank()) {
            return null;
        }

        List<Contract> contracts = new ArrayList<>();

        String[] lines = rawNotes.split("\\r?\\n");

        for (String line : lines) {
            line = line.trim();

            if (line.isEmpty()) continue;

            Contract contract = validateNotes(line, userId);

            if (contract != null) {
                contracts.add(contract);
            } else {
                log.warn("Failed to parse contract line: {}", line);
                return null;
            }
        }

        log.info("Parsed {} contracts from input", contracts.size());

        return contracts;
    }

    private Contract validateNotes(String contractNotes, UUID userId) {
        if (contractNotes == null) return null;

        contractNotes = contractNotes.trim();
        if (contractNotes.length() < 3) return null;

        char typeChar = contractNotes.charAt(0);

        ContractType contractType;
        if (typeChar == '1') {
            contractType = ContractType.TYPE_1;
        } else if (typeChar == '2') {
            contractType = ContractType.TYPE_2;
        } else {
            return null;
        }

        if (contractNotes.charAt(1) != ';') return null;

        String contractNumber = contractNotes.substring(2).trim();

        return new Contract(contractNumber, contractType, userId);
    }
}
