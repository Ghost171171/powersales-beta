package com.controller;

import com.mapper.POI_Mapper;
import com.model.poi.POI;
import com.model.poi.POI_DTO;
import com.model.rect.BoundsRect;
import com.model.user.SessionUser;
import com.service.POI_Service;
import com.service.RectService;
import com.util.AuthUtil;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/rects")
public class RectController {
    private final RectService rectService;
    private final POI_Service poiService;
    private final static Logger log = LoggerFactory.getLogger(RectController.class);


    public RectController(RectService rectService, POI_Service poiService) {
        this.rectService = rectService;
        this.poiService = poiService;
    }

    // GET /rects -> alle Rechtecke || GET /rects/{userid} -> nur alle Rechtecke die eine ID zugewiesen sind
    @GetMapping()
    public List<BoundsRect> getRects(HttpSession session) {
        SessionUser sessionUser = (SessionUser) session.getAttribute("user");
        if (sessionUser == null) {
            log.warn("Unauthorized access attempt to /rects without valid session by {}", session.getId());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,  "No session found");
        }
        if (AuthUtil.isAdmin(sessionUser)) {
            log.info("Successfully validated Admin {} , sending all rects to {}", sessionUser.getId(), session.getId());
            return rectService.getAllRect();
        }
        log.info("Successfully validated User {}, sending all rects to {}", sessionUser.getId() ,session.getId());
        List<BoundsRect> rects = rectService.getRectsByUserId(sessionUser.getId());
        log.info("Sending: ... {}", rects.toString());
        return rects;
    }

    // GET /rects/{id} -> einzelnes Rechteck
    @GetMapping("/{id}")
    public BoundsRect getRect(@PathVariable UUID id) {
        log.info("Successfully sent rect of id {}", id);
        return rectService.getRect(id);
    }

    // GET /rects/{id}/pois
    @GetMapping("/{id}/pois")
    public List<POI_DTO> getPoiInRect(@PathVariable UUID id) {
        BoundsRect rect = rectService.getRect(id);
        List<POI> pois = poiService.getPOIsInBounds(rect.getMinLat(), rect.getMaxLat(), rect.getMinLon(), rect.getMaxLon());
        log.info("Successfully sent all pois in rect of id {} ", id);
        return POI_Mapper.toDTOList(pois);
    }

    // POST /rects -> füge Rechteck hinzu
    @PostMapping()
    public BoundsRect addRect(@RequestBody BoundsRect rect) {
        rectService.addRect(rect);
        log.info("Successfully added rect: {}", rect.getId());
        return rect;
    }

    // PUT /rects/{id}  -> Rechteck aktualisieren
    @PutMapping("/{id}")
    public BoundsRect updateRect(@PathVariable UUID id, @RequestBody BoundsRect bounds) {
        bounds.setId(id);
        if (!bounds.getId().equals(id)) {
            log.warn("RECT ID mismatch, couldn't update rect of id {}", id);
            throw new IllegalArgumentException("id does not match");
        }
        rectService.updateRect(bounds);
        log.info("Successfully updated rect of id {}", id);
        return rectService.getRect(id);
    }

    // PUT /rects/{id}/color -> Farbe von Rechteck aktualisieren
    @PutMapping("/{id}/color")
    public BoundsRect updateRectColor(@PathVariable UUID id, @RequestParam String color) {
        BoundsRect bounds = rectService.getRect(id);
        bounds.setColor(color);
        rectService.updateRect(bounds);
        log.info("Successfully updated color of rect with id {}", id);
        return bounds;
    }

    // DELETE /rects/{id} -> Rechteck löschen
    @DeleteMapping("/{id}")
    public void deleteRect(@PathVariable UUID id) {
        rectService.deleteRect(id);
        log.info("Successfully deleted rect with id {}", id);
    }
}
