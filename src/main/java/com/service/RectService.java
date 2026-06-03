package com.service;

import com.model.rect.BoundsRect;
import com.repository.RectRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

@Service
public class RectService {
    private final RectRepository rectRepository;

    public RectService(RectRepository rectRepository) {
        this.rectRepository = rectRepository;
    }

    public void addRect(BoundsRect rect) {
        rectRepository.addRect(rect);
    }

    public void updateRect(BoundsRect rect) {
        rect.setCreatedAt(getCreatedAtFromDB(rect.getId()));
        rectRepository.updateDrawnRect(rect);
    }

    public void deleteRect(UUID id) {
        rectRepository.deleteDrawnRect(id);
    }

    public ArrayList<BoundsRect> getAllRect() {
        return new ArrayList<>(rectRepository.getAllDrawnRect());
    }

    public BoundsRect getRect(UUID id) {
        return rectRepository.getDrawnRect(id).orElseThrow();
    }

    public ArrayList<BoundsRect> getRectsByUserId(UUID assignedUserId) {
        return new ArrayList<>(rectRepository.getDrawnRectsByUserId(assignedUserId));
    }

    private LocalDateTime getCreatedAtFromDB(UUID rectId) {
        return rectRepository.getCreatedAt(rectId).orElseThrow(
                () -> new IllegalStateException("createdAt missing for rect: " + rectId)
        );
    }
}
