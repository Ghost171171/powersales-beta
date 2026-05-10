package com.model.poi;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * POI_DTO (Data Transfer Object) dient als Übertragungsobjekt zwischen Backend und Frontend.
 * -
 * Die Klasse kapselt ein POI-Objekt aus dem Backend und transformiert dessen Daten
 * in frontend-kompatible, serialisierbare Typen (z. B. Enums → String,
 * Koordinaten → primitive Datentypen).
 * -
 * POI_DTO enthält keinerlei Geschäftslogik und wird ausschließlich für die
 * Schnittstellenkommunikation (API / JSON) verwendet.
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter@Setter
public class POI_DTO {
    @NotNull
    private String id; //this is an uuid
    @NotNull
    private String street;
    @NotNull
    private String houseNumber;
    @NotNull
    private String plz;
    @NotNull
    private String location;
    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private double lat; //latitude = Breitengrad im Koordinatensystem
    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    private double lon; //longitude = Längengrad im Koordinatensystem
    private String lastVisit;
    @NotNull
    private String visitStatus;
    @NotNull
    private String contractStatus;
    private String note;

    public POI_DTO() {}

    public POI_DTO(POI poi) {
        this.id = poi.getId().toString();

        this.street = poi.getAddress().getStreet();
        this.houseNumber = poi.getAddress().getHouseNumber();
        this.plz = poi.getAddress().getPlz();
        this.location = poi.getAddress().getLocation();

        //Coordinate ist ein String wir müssen also unsere beiden Parameter vom Typ Double zu einem String konvertieren
        this.lat = poi.getAddress().getLatitude();
        this.lon = poi.getAddress().getLongitude();

        if (poi.getLastVisit() != null) {
            String lastVisited = poi.getLastVisit().toString();
            if (!lastVisited.isEmpty()) {
                this.lastVisit = poi.getLastVisit().toString();
            } else {
                this.lastVisit = "";
            }
        } else {
            this.lastVisit = "";
        }


        this.visitStatus = poi.getVisitStatus() != null ? poi.getVisitStatus().name() : "NONE"; //prüfe ob visitStatus auf null gesetzt
        this.contractStatus = poi.getContractStatus() != null ? poi.getContractStatus().name() : "NONE"; //prüfe ob contractStatus auf null gesetzt
        this.note = poi.getNotes();
    }

    @Override
    public String toString() {
        return id + " of name " + street + " " + houseNumber;
    }
}
