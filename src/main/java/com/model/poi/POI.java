package com.model.poi;

import com.model.enums.ContractStatus;
import com.model.enums.VisitStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

/*
 * Die POI Klasse beschreibt alle Orte, die von uns von Interesse sind.
 * Es enthält:
 *  eine eindeutige ID, um jeden Ort genaustens identifizieren zu können im System
 *  eine Adresse für die Geolokation bzw. die menschliche Identifizierung
 *  das Datum an dem eine POI das letzte Mal besucht wurde von Mitarbeitern oder anderen
 *  eine Statusabfrage für das Besuchen und den Vertrag
 *  Notizen zu den einzelnen Ortschaften von Mitarbeitern ausgefüllt
 *
 * Eine POI kann nach ihren Attributen abgefragt werden und diese ausgeben, des Weiteren können die Attribute über
 * Operationen modifiziert werden
 */

//TODO Koordinaten Logik muss verändert werden

@Getter
@Setter
public class POI {
    private UUID id; //Identifier für eine POI

    private Address address;
    private LocalDate lastVisit;

    private VisitStatus visitStatus; //Zustand, ob Ort besucht oder nicht besucht
    private ContractStatus contractStatus; //Zustand, ob ein Vertrag abgeschlossen wurde oder nicht

    private String notes; //Zusätzliche Notizen

    //leerer Konstruktor für Jackson
    public POI() {}

    public POI (Address address) {
        this.id = UUID.randomUUID(); //generate a random ID
        this.address = address;
        this.lastVisit = LocalDate.MIN; //default Initialisierung
        this.visitStatus = VisitStatus.UNVISITED; //Setze erstmal den Status auf falsch
        this.contractStatus = ContractStatus.NONE; //Setzen den Status zunächst auf "kein Vertrag"
        this.notes = ""; //Wir haben noch keine Notiz bis eine hinzugefügt wird
    }

    public POI (Address address, UUID id) {
        this.id = id;
        this.address = address;
        this.lastVisit = LocalDate.MIN; //default Initialisierung
        this.visitStatus = VisitStatus.UNVISITED; //Setze erstmal den Status auf falsch
        this.contractStatus = ContractStatus.NONE; //Setzen den Status zunächst auf "kein Vertrag"
        this.notes = ""; //Wir haben noch keine Notiz bis eine hinzugefügt wird
    }

    //Füge neue Notizen hinzu, TODO Etwas läuft hier falsch
    public void setNote(String note) {
        //Wenn noch keine Notizen vorhanden sind, füge neue Notiz hinzu
        notes = note;
    }

    @Override
    public String toString() {
        return id + ": " + address + ", " + lastVisit + ", " + visitStatus + ", " + contractStatus + ", " + notes;
    }
}
