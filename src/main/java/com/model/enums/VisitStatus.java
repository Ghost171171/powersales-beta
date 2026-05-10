package com.model.enums;

import lombok.Getter;

@Getter
public enum VisitStatus {
    UNVISITED("Nicht besucht", "red"),
    VISITED("Besucht", "green"),
    NOT_REACHED("Kein Kontakt", "yellow");

    private final String label;
    private final String color;

    VisitStatus(String label, String color) {
        this.label = label;
        this.color = color;
    }
}
