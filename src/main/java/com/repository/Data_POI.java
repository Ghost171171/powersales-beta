package com.repository;

import com.model.poi.Address;
import com.model.poi.POI;
import com.model.enums.ContractStatus;
import com.model.enums.VisitStatus;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

//WORK IN PROGRESS

/*
 * Die Data_POI Klasse ist unser Repository bzw. Speicher für POIs.
 * Es speichert alle POIs intern in einer Liste, welche in einer .json Datei geschrieben werden und aus dieser
 * gelesen werden.
 * Des Weiteren ermöglicht diese Klasse die Operation, ein POI Objekt zu modifizieren, die Liste zurückzugeben,
 * eine POI anhand seiner ID zu finden, eine POI zu entfernen anhand der ID und eine POI hinzuzufügen zur Liste
 *
 */

public class Data_POI {

    private final String dbURL = "jdbc:sqlite:pois.db";

    // Singleton-Instanz
    private static Data_POI instance;

    //Instanziiere das Repository
    private Data_POI() {
        try (Connection conn = DriverManager.getConnection(dbURL);
             Statement stmt = conn.createStatement()) {

            // Tabelle erstellen, falls sie nicht existiert
            stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS poi (
                        id TEXT PRIMARY KEY,
                        street TEXT,
                        houseNumber TEXT,
                        plz TEXT,
                        location TEXT,
                        latitude REAL NOT NULL,
                        longitude REAL NOT NULL,
                        lastVisit TEXT,
                        visitStatus TEXT,
                        contractStatus TEXT,
                        note TEXT
                    );
                    """);

        } catch (SQLException e) {
            System.out.println("SQLException, when trying to initialize or get access to the Databank: " + e.getMessage());
        }
    }

    // Singleton Zugriff
    public static synchronized Data_POI getInstance() {
        if (instance == null) {
            instance = new Data_POI();
        }
        return instance;
    }

    // Lade nur POIs in Bounds
    public List<POI> getPOIsInBounds(double minLat, double maxLat, double minLon, double maxLon, int limit) {
        List<POI> pois = new ArrayList<>();
        String sql = """
                SELECT * FROM poi
                WHERE latitude BETWEEN ? AND ?
                AND longitude BETWEEN ? AND ?
                LIMIT ?
                """;

        try (Connection conn = DriverManager.getConnection(dbURL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, minLat);
            stmt.setDouble(2, maxLat);
            stmt.setDouble(3, minLon);
            stmt.setDouble(4, maxLon);
            stmt.setInt(5, limit);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                pois.add(setPOIFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.out.println("SQLException, trying to access all elements: " + e.getMessage());
        }
        return pois;
    }

    // POI speichern / updaten
    public void updatePOI(POI poi) {
        String sql = """
                INSERT INTO poi(
                    id, street, houseNumber, plz, location, latitude, longitude, lastVisit, visitStatus, contractStatus, note
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(id) DO UPDATE SET
                    street = excluded.street,
                    houseNumber = excluded.houseNumber,
                    location = excluded.location,
                    plz = excluded.plz,
                    latitude = excluded.latitude,
                    longitude = excluded.longitude,
                    lastVisit = excluded.lastVisit,
                    visitStatus = excluded.visitStatus,
                    contractStatus = excluded.contractStatus,
                    note = excluded.note;
                """;

        try (Connection conn = DriverManager.getConnection(dbURL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            preparedStatement(stmt, poi);
            System.out.println("Updated POI: " + poi.getId().toString());

        } catch (SQLException e) {
            System.out.println("SQLException, trying to update a single element: " + e.getMessage());
        }
    }

    //Bekomme eine einzige POI anhand der ID der POI
    public Optional<POI> getPOI(UUID id) {
        String sql = "SELECT * FROM poi WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(dbURL)) {
            PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, id.toString());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.println("Found POI: " + id);
                return Optional.of(setPOIFromResultSet(rs));
            }
            System.out.println("No poi found with id: " + id);
        } catch (SQLException e) {
            System.out.println("SQLException, trying to access a element by id: " + e.getMessage());
        }
        return Optional.empty(); //if something went wrong we return null
    }

    public Optional<POI> getPOIByName(String street, String houseNumber, String plz) {
        String sql = "SELECT * FROM poi WHERE street = ? AND houseNumber = ? AND plz = ?";

        try (Connection conn = DriverManager.getConnection(dbURL)) {
            PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, street);
            pstmt.setString(2, houseNumber);
            pstmt.setString(3, plz);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.println("Found poi by Address!");
                return Optional.of(setPOIFromResultSet(rs));
            }
            System.out.println("No poi found with address!");
        } catch (SQLException e) {
            System.out.println("SQLException, trying to access a element by name: " + e.getMessage());
        }
        return Optional.empty();
    }

    //Füge eine POI einer Datenbank hinzu
    public void addPOI(POI poi) {
        String sql = """
                INSERT INTO poi(id, street, houseNumber, location, plz, latitude, longitude, lastVisit, visitStatus, contractStatus, note)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DriverManager.getConnection(dbURL)) {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            preparedStatement(pstmt, poi);
            System.out.println("Added POI: " + poi.getId().toString());
        } catch (SQLException e) {
            System.out.println("SQLException, trying to adding a element to pois: " + e.getMessage());
        }
    }

    //create a poi from the data bank by using the result set the data bank returns
    private POI setPOIFromResultSet(ResultSet rs) throws SQLException {
        POI poi = new POI(
                new Address(
                        rs.getString("street"),
                        rs.getString("houseNumber"),
                        rs.getString("plz"),
                        rs.getString("location"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude")
                ),
                UUID.fromString(rs.getString("id"))
        );
        String lastVisitStr = rs.getString("lastVisit");
        if (lastVisitStr != null && !lastVisitStr.isEmpty()) {
            poi.setLastVisit(LocalDate.parse(lastVisitStr));
        }
        poi.setVisitStatus(VisitStatus.valueOf(rs.getString("visitStatus")));
        poi.setContractStatus(ContractStatus.valueOf(rs.getString("contractStatus")));
        poi.setNote(rs.getString("note"));
        return poi;
    }
    //Gebe ein Statement für SQL bereit, um ein Element zu bearbeiten oder hinzuzufügen
    private void preparedStatement(PreparedStatement pstmt ,POI poi) throws SQLException {
        pstmt.setString(1, poi.getId().toString());
        pstmt.setString(2, poi.getAddress().getStreet());
        pstmt.setString(3, poi.getAddress().getHouseNumber());
        pstmt.setString(4, poi.getAddress().getPlz());
        pstmt.setString(5, poi.getAddress().getLocation());
        pstmt.setDouble(6, poi.getAddress().getLatitude());
        pstmt.setDouble(7, poi.getAddress().getLongitude());
        pstmt.setString(8, poi.getLastVisit() != null ? poi.getLastVisit().toString() : null);
        pstmt.setString(9, poi.getVisitStatus().name());
        pstmt.setString(10, poi.getContractStatus().name());
        pstmt.setString(11, poi.getNotes());

        pstmt.executeUpdate();
    }
}
