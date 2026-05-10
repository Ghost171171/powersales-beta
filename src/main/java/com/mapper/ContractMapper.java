package com.mapper;

import com.model.contract.Contract;
import com.model.contract.Contract_DTO;
import com.model.user.User;
import com.repository.UserRepository;
import com.service.UserService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ContractMapper {
    public ContractMapper() {}

    public static Contract_DTO toContractDTO(Contract contract) {
        UserService uService = new UserService(UserRepository.getInstance());

        String contractId = contract.getId();
        User user = uService.getUserById(contract.getUserId());
        String userName = user.getUsername();
        double cVal = contract.getContractType().getValue();
        String cPString = contract.getContractProcessStatus().toString();
        String localDate = formatLocalDateTime(contract.getContractCompletedDateTime());

        return new Contract_DTO(contractId, userName, cVal, cPString, localDate);
    }

    public static List<Contract_DTO> toContractDTOList(List<Contract> contracts) {
        List<Contract_DTO> contractDTOList = new ArrayList<>();
        contracts.forEach(contract -> contractDTOList.add(toContractDTO(contract)));
        return contractDTOList;
    }

    private static String formatLocalDateTime(LocalDateTime localDateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        return localDateTime.format(formatter);
    }
}
