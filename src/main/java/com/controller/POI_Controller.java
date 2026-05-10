package com.controller;

import com.mapper.POI_Mapper;
import com.model.rect.BoundsRect;
import com.model.user.SessionUser;
import com.service.POI_Service;
import com.model.enums.ContractStatus;
import com.model.poi.POI;
import com.model.poi.POI_DTO;
import com.model.enums.VisitStatus;
import com.service.RectService;
import com.util.AuthUtil;
import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Schnittstelle zwischen Front und Backend, sorgt für die Kommunikation der Map UI und der Datenbank.
 * Es sollen POI und POI_DTO Instanzen konvertiert werden (in beide Richtungen zu Frontend und Backend),
 * ins Besondere sollen Daten vom Frontend entgegengenommen werden und Daten vom Backend dem Frontend entgegen gegeben werden.
 * Dabei sollen auch die Daten in Form der Liste transformiert werden und ganz wichtig übernimmt keine Logik, es ist nur eine Art Mittelmann/ Übersetzer.
 */

@RestController
@RequestMapping("/pois")
public class POI_Controller {
    private final RectService rectService;
    private final static Logger log = LoggerFactory.getLogger(POI_Controller.class);
    POI_Service service;

    public POI_Controller(POI_Service service, RectService rectService) {
        this.service = service;
        this.rectService = rectService;
    }

    // --- Backend -> Frontend ---

    @GetMapping
    public ResponseEntity<String> poisRootDisabled() {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("Use /pois/bounds with minLat, maxLat, minLon, maxLon");
    }

    //Get pois in bounds of either rects or point of view in browser
    @GetMapping("/bounds")
    public List<POI_DTO> getPOIsInBound(
            @RequestParam double minLat,
            @RequestParam double maxLat,
            @RequestParam double minLon,
            @RequestParam double maxLon,
            HttpSession session) {

        SessionUser sessionUser = AuthUtil.requireUser(session);

        //Hole über Service die gefragte Liste aller POIs in der Grenze
        List<POI> visible = service.getPOIsInBounds(minLat, maxLat, minLon, maxLon);

        //Prüfe, ob Admin, wenn Admin übertrage alle POIs
        if (AuthUtil.isAdmin(sessionUser)) {
            log.info("Successful authorization by user {} as Admin, sending all POIs ...", sessionUser.getId());
            //Mappe zu DTOs
            return POI_Mapper.toDTOList(visible);
        }

        //lade rects von user
        List<BoundsRect> userRect = rectService.getRectsByUserId(sessionUser.getId());

        //filtere POIs
        List<POI> filtered = visible.stream().filter(poi -> isContainedInRect(poi, userRect)).toList();
        log.info("Successful authorization by user {} as User ,sending allocated POIs ...", sessionUser.getId());
        return POI_Mapper.toDTOList(filtered);
    }

    //Get coordinates of poi by name
    @GetMapping("/search")
    public Point getPOICoordinates(@RequestParam String query, HttpSession session) {
        AuthUtil.requireUser(session);
        AddressData aD = new AddressData(query);
        POI poi = service.getPOIByName(aD.getStreet(), aD.getHouseNumber(), aD.getPlz());
        if (poi == null) {
            log.warn("POI with name {} not found!", aD.getStreet() + aD.getHouseNumber() + aD.getPlz());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Poi with name " + query + " not found!");
        }
        log.info("Successfully found POI with name {} ", poi.getAddress().getStreet() + poi.getAddress().getHouseNumber()
                + poi.getAddress().getPlz());
        return new Point(poi.getAddress().getLatitude(), poi.getAddress().getLongitude());
    }

    //HELPER RECORD, ein Tupel welches Longitude und Latitude enthält
    public record Point(double lat, double lon) {}
    //HELPER: prüfe ob POI in Rect enthalten
    private boolean isContainedInRect(POI poi, List<BoundsRect> userRect) {
        for (BoundsRect rect : userRect) {
            if (poi.getAddress().getLatitude() >= rect.getMinLat()
                    && poi.getAddress().getLatitude() <= rect.getMaxLat()
                        && poi.getAddress().getLongitude() >= rect.getMinLon()
                            && poi.getAddress().getLongitude() <= rect.getMaxLon()) {
                return true;
            }
        }
        return false;
    }

    // --- Frontend -> Backend ---

    @PutMapping("/{id}")
    //update eine POI und gebe die veränderte POI als Transferable Object zurück
    public ResponseEntity<POI_DTO> updatePOI(@PathVariable String id, @RequestBody POI_DTO dto) {
        try {
            //build poi from dto
            POI updated = service.getPOI(UUID.fromString(id));
            updated.setVisitStatus(VisitStatus.valueOf(dto.getVisitStatus()));
            updated.setContractStatus(ContractStatus.valueOf(dto.getContractStatus()));
            updated.setNote(dto.getNote());

            //update this poi internally
            service.updatePOI(updated);

            log.info("Successfully updated POI with id {} ", id);

            return ResponseEntity.ok(POI_Mapper.toDTO(updated));
        } catch (Exception e) {
            log.error("POI couldn't be updatet: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

//Parse String um Straße, Hausnummer und PLZ zu extrahieren
@Getter@Setter
class AddressData {
    String street;
    String houseNumber;
    String plz;

    public AddressData(String poiName) {
        //split poiName into two Parts ,we expect the format (Street HouseNumber, Plz)
        String[] splits = poiName.split(",");

        //we know that if length is not equal to 2, the user did not adhere to the format, we throw an illegal exception
        if (splits.length != 2) {
            throw new IllegalArgumentException("invalid Address Format!");
        }

        //delete all white spaces, tabs, ...
        String streetNumParts = splits[0].trim().replaceAll("\\s+", " ");
        plz = splits[1].trim().replaceAll("\\s+", " ");

        //split house number and street name
        String[] streetTokens = streetNumParts.split(" ");
        if (streetTokens.length < 2) {
            throw new IllegalArgumentException("Invalid Street and House Number Format!");
        }
        houseNumber = streetTokens[streetTokens.length - 1];

        street = String.join(" ",
                Arrays.copyOf(streetTokens, streetTokens.length - 1));

        /* TODO Maybe implement later
        String[] cityTokens = cityPart.split(" ");
        String postalCode = cityTokens[0];
        String city = cityTokens[1];
        */
    }
}
