package com.model.contract;

import com.model.enums.ContractProcessStatus;
import com.model.enums.ContractType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

//Zur Klassifizierung eines Vertrages brauchen wir eine ContractId, den Typen des Vertrages und den Nutzer, der den Vertrag abgeschlossen hat,
//wir nutzen die Id des Nutzers

@Getter@Setter
public class Contract {
    @NotNull
    private String id;
    @NotNull
    private ContractType contractType;
    @NotNull
    private UUID userId;
    @NotNull
    private ContractProcessStatus contractProcessStatus;
    private LocalDateTime contractCompletedDateTime;

    public Contract() {}

    public Contract(String contractId, ContractType contractType, UUID userId) {
        this.id = contractId;
        this.contractType = contractType;
        this.userId = userId;
        this.contractProcessStatus = ContractProcessStatus.IN_PROGRESS;
        this.contractCompletedDateTime = LocalDateTime.now();
    }

    public Contract(String contractId, ContractType contractType, UUID userId, ContractProcessStatus cPS, LocalDateTime contractCompletedDateTime) {
        this.id = contractId;
        this.contractType = contractType;
        this.userId = userId;
        this.contractProcessStatus = cPS;
        this.contractCompletedDateTime = contractCompletedDateTime;
    }
}
