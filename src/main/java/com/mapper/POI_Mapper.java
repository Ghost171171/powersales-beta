package com.mapper;

import com.model.poi.Address;
import com.model.poi.POI;
import com.model.poi.POI_DTO;
import com.model.enums.ContractStatus;
import com.model.enums.VisitStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Übersetzung von Frontend Logik und Backend Logik. In diesem Fall gegenseitige Übersetzung von POI und POI_DTO.
 */

public class POI_Mapper {
    private POI_Mapper() {}

    //Konvertiere eine POI in eine POI_DTO
    public static POI_DTO toDTO(POI poi) {
        return new POI_DTO(poi);
    }

    //Konvertiere eine DTO zu einer POI
    public static POI toPOI(POI_DTO dto) {
        UUID id = UUID.fromString(dto.getId());
        Address address = new Address(dto.getStreet(), dto.getHouseNumber(), dto.getPlz(), dto.getLocation(), dto.getLat(), dto.getLon());
        LocalDate lastVisit = LocalDate.parse(dto.getLastVisit());
        VisitStatus visitStatus = parseVisitStatus(dto.getVisitStatus());
        ContractStatus contractStatus = parseContractStatus(dto.getContractStatus());

        POI poi = new POI(address, id);
        poi.setLastVisit(lastVisit);
        poi.setVisitStatus(visitStatus);
        poi.setContractStatus(contractStatus);
        poi.setNote(dto.getNote());
        return poi;
    }

    //Konvertiere bzw. Übersetze einen String zu einem VisitStatus Enum
    private static VisitStatus parseVisitStatus(String visitStatus) {
        if (visitStatus == null) {
            throw new IllegalArgumentException("visitStatus cannot be null");
        }
        try {
            return VisitStatus.valueOf(visitStatus.toUpperCase());
        //wenn ein ungültiger Wert eingegeben wird, gebe eine IllegalArgumentException
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("visitStatus: '" + visitStatus + "' is not a valid visit status", e);
        }
    }
    //Konvertiere bzw. Übersetze einen String zu einem ContractStatus Enum
    private static ContractStatus parseContractStatus(String contractStatus) {
        if (contractStatus == null) {
            throw new IllegalArgumentException("contractStatus cannot be null");
        }
        try {
            return ContractStatus.valueOf(contractStatus.toUpperCase());
            //wenn ein ungültiger Wert eingegeben wird, gebe eine IllegalArgumentException
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("contractStatus: '" + contractStatus + "' is not a valid contract status", e);
        }
    }

    //Erstelle aus einer Liste von POIs eine Liste von POI_DTOs
    public static List<POI_DTO> toDTOList(List<POI> pois) {
        return pois.stream()
                .map(POI_Mapper::toDTO)
                .toList();
    }

    //Erstelle aus einer Liste von DTOs eine Liste von POIs
    public static List<POI> toPOIList(List<POI_DTO> dtos) {
        return dtos.stream()
                .map(POI_Mapper::toPOI)
                .toList();
    }
}
