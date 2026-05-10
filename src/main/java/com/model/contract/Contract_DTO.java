package com.model.contract;

import com.model.enums.ContractProcessStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter@Setter
public class Contract_DTO {
    //wir brauchen hier, nicht die ID, sondern den Nutzer Namen direkt, wir wollen den DateTime String im richtigen Format direkt übertragen
    //des Weiteren wollen wir den Contract Type in seinen Wert direkt umwandeln, die Contract ID brauchen wir auch
    @NotNull
    private String id;
    @NotNull
    private String userName;
    @DecimalMin(value = "0.0")
    private double contractProv;
    @NotNull
    private String contractProcessStatus;
    @NotNull
    private String contractTimeDate;

    public Contract_DTO() {}

    public Contract_DTO(String id, String userName, double contractProv) {
        this.id = id;
        this.userName = userName;
        this.contractProv = contractProv;
        this.contractProcessStatus = ContractProcessStatus.IN_PROGRESS.toString();
        this.contractTimeDate = LocalDateTime.now().toString();
    }

    public Contract_DTO(String id, String userName, double contractProv, String cPS, String contractTimeDate) {
        this.id = id;
        this.userName = userName;
        this.contractProv = contractProv;
        this.contractProcessStatus = cPS;
        this.contractTimeDate = contractTimeDate;
    }

}
