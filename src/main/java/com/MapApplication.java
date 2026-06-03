package com;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//Verträge löschen, Clear All Taste für Rechtecke entfernen, Rechteck Menü soll Datum auch zeigen und beim Hovern, Editieren des Datums
//Tabellenrand bei der Vertrags-Seite editieren

@SpringBootApplication
public class MapApplication {
    public static void main(String[] args) {
        SpringApplication.run(MapApplication.class, args);
        System.out.println("http://localhost:8080");
    }
}
