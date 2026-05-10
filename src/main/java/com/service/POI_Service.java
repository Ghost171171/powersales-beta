package com.service;

import com.model.poi.POI;
import com.repository.Data_POI;

import java.util.List;
import java.util.UUID;

/**
 * POI Service implementiert logische Operationen unseres Repositorys in unserer Schnittstelle.
 */

public class POI_Service {
    private final Data_POI repository;
    private final int MAX_POI = 1000;

    public POI_Service(Data_POI repository) {
        this.repository = repository;
    }

    //Gebe nur die POIs die innerhalb der Grenzen von Latitude und Longitude sind
    public List<POI> getPOIsInBounds(double minLat, double maxLat, double minLon, double maxLon) {
        return repository.getPOIsInBounds(minLat, maxLat, minLon, maxLon, MAX_POI);
    }

    //Rufe POI ab nach der ID
    public POI getPOI(UUID id) {
        return repository.getPOI(id).orElseThrow();
    }

    //Erhalte POI über Name
    public POI getPOIByName(String street, String houseNumber, String plz) {
        return repository.getPOIByName(street, houseNumber, plz).orElseThrow();
    }

    //POI hinzufügen
    public void addPOI(POI poi) {
        repository.addPOI(poi);
    }

    //POI aktualisieren
    public void updatePOI(POI poi) {
        repository.updatePOI(poi);
    }
}
