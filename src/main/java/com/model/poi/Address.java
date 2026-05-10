package com.model.poi;

/*
 * Eine Adresse ist ein Objekt, welches die geografischen bzw. logistischen Daten einer POI enthält.
 * Diese ist wichtig damit die Mitarbeiter bzw. Menschen die verschieden POIs unterscheiden können.
 * Eine Adresse besteht im System aus:
 *  Straßenname, Hausnummer, Postleitzahl, Ort und Koordinate (wichtig für die Map).
 * Unsere Operationen können diese Informationen abfragen oder modifizieren.
 */

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter@Setter
public class Address {
    private String street;
    private String houseNumber;
    private String plz;
    private String location; //location ist gleich zu stellen mit Ort bzw. Stadt
    private double latitude;
    private double longitude;

    //leerer Konstruktor für Jackson
    public Address () {}

    public Address(String street, String houseNumber, String PLZ, String location) {
        if (street == null || street.isEmpty()) {
            throw new IllegalArgumentException("Street must be defined!");
        }
        if (houseNumber == null || houseNumber.isEmpty()) {
            throw new IllegalArgumentException("HouseNumber must be defined!");
        }
        if (PLZ == null || PLZ.isEmpty()) {
            throw new IllegalArgumentException("PLZ must be defined!");
        }
        if (location == null || location.isEmpty()) {
            throw new IllegalArgumentException("Location must be defined!");
        }

        this.street = street;
        this.houseNumber = houseNumber;
        this.plz = PLZ;
        this.location = location;
        this.latitude = 0.0;
        this.longitude = 0.0;
    }

    public Address(String street, String houseNumber, String plz, String location, double latitude, double longitude) {
        if (street == null || street.isEmpty()) {
            throw new IllegalArgumentException("Street must be defined!");
        }
        if (houseNumber == null || houseNumber.isEmpty()) {
            throw new IllegalArgumentException("HouseNumber must be defined!");
        }
        if (plz == null || plz.isEmpty()) {
            throw new IllegalArgumentException("PLZ must be defined!");
        }
        if (location == null || location.isEmpty()) {
            throw new IllegalArgumentException("Location must be defined!");
        }

        this.street = street;
        this.houseNumber = houseNumber;
        this.plz = plz;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    //vergleiche zwei Adressen miteinander
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof Address address) {
            return street.equals(address.street) && houseNumber.equals(address.houseNumber)
                    && plz.equals(address.plz) && location.equals(address.location);
        } else {
            return false;
        }
    }

    //hash eine Adresse
    @Override
    public int hashCode() {
        return Objects.hash(street, houseNumber, plz, location);
    }

    //Bekomme alle Adressen die mit einem Präfix p starten, beachte dabei keine Groß - und Kleinschreibung
    public boolean startsWithIgnoreCase(String prefix) {
        String p = prefix.toLowerCase();

        return street.toLowerCase().startsWith(p)
                || houseNumber.toLowerCase().startsWith(p)
                || plz.toLowerCase().startsWith(p)
                || location.toLowerCase().startsWith(p);
     }

    @Override
    public String toString() {
        return street + " " + houseNumber + " " + plz + " " + location;
    }
}
