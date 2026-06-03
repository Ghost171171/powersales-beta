package com.repository;

import com.model.rect.BoundsRect;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class RectRepository {
    private final String dbURL = "jdbc:sqlite:pois.db";

    //Singleton-Instanz
    private static RectRepository instance;

    //Instanziiere das Repository
    private RectRepository() {
        try (Connection conn = DriverManager.getConnection(dbURL);
             Statement stmt = conn.createStatement()) {

            // Tabelle erstellen, falls sie nicht existiert
            stmt.executeUpdate("""
                  CREATE TABLE IF NOT EXISTS drawnRect (
                           id TEXT PRIMARY KEY,
                           minLat REAL,
                           minLon REAL,
                           maxLat REAL,
                           maxLon REAL,
                           createdAt TEXT DEFAULT (datetime('now')),
                           updatedAt TEXT,
                           assignedUserId TEXT,
                           color TEXT DEFAULT '#3388ff'
                  );
                  """);

        } catch (SQLException e) {
            System.out.println("SQLException in RectRepo, when trying to initialize or get access to the Databank: " + e.getMessage());
        }
    }

    // Singleton Zugriff
    public static synchronized RectRepository getInstance() {
        if (instance == null) {
            instance = new RectRepository();
        }
        return instance;
    }

    //Add rect to table
    public void addRect(BoundsRect rect) {
        //sicherstellen das id und createdAt gesetzt
        if (rect.getId() == null) {
            rect.setId(UUID.randomUUID());
        }
        if (rect.getCreatedAt() == null) {
            rect.setCreatedAt(LocalDateTime.now());
        }
        if (rect.getAssignedUserId() == null) {
            rect.setAssignedUserId(null);
        }
        if (rect.getColor() == null) {
            rect.setColor(null);
        }
        String sql = """
                INSERT INTO drawnRect (id, minLat, minLon, maxLat, maxLon, createdAt, updatedAt, assignedUserId, color)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DriverManager.getConnection(dbURL)) {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            prepareStatementAdd(pstmt, rect);
            System.out.println("Inserted Rect " + rect.getId().toString() + " into DB");

        } catch (SQLException e) {
            System.out.println("SQLException in RectRepo, when trying to initialize or get access to the DB: " + e.getMessage());
        }
    }

    private void prepareStatementAdd(PreparedStatement pstmt, BoundsRect rect) throws SQLException {
        pstmt.setString(1, rect.getId().toString());
        pstmt.setDouble(2, rect.getMinLat());
        pstmt.setDouble(3, rect.getMinLon());
        pstmt.setDouble(4, rect.getMaxLat());
        pstmt.setDouble(5, rect.getMaxLon());
        pstmt.setString(6, rect.getCreatedAt().toString());
        pstmt.setString(7, null);
        pstmt.setString(8, rect.getAssignedUserId() != null ? rect.getAssignedUserId().toString() : null);
        pstmt.setString(9, rect.getColor());

        pstmt.executeUpdate();
    }

    //Update Position of rect
    public void updateDrawnRect(BoundsRect updatetRect) {
        String sql = """
                INSERT INTO drawnRect(
                    id, minLat, minLon, maxLat, maxLon, createdAt, updatedAt, assignedUserId, color
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    ON CONFLICT (id) DO UPDATE SET
                    minLat = excluded.minLat,
                    minLon = excluded.minLon,
                    maxLat = excluded.maxLat,
                    maxLon = excluded.maxLon,
                    updatedAt = excluded.updatedAt,
                    assignedUserId = excluded.assignedUserId,
                    color = excluded.color;
                """;

        try (Connection conn = DriverManager.getConnection(dbURL)) {
            PreparedStatement stmt = conn.prepareStatement(sql);

            prepareStatement(stmt, updatetRect);
            System.out.println("Updating Drawn Rect..." + updatetRect.getId().toString());
        } catch (SQLException e) {
            System.out.println("SQLException in RectRepo, when trying to initialize or get access to the DB: " + e.getMessage());
        }
    }

    //Prepared Statement for updatedRect
    private void prepareStatement(PreparedStatement pstmt, BoundsRect updatedRect) throws SQLException {
        LocalDateTime now = LocalDateTime.now();

        pstmt.setString(1, updatedRect.getId().toString());
        pstmt.setDouble(2, updatedRect.getMinLat());
        pstmt.setDouble(3, updatedRect.getMinLon());
        pstmt.setDouble(4, updatedRect.getMaxLat());
        pstmt.setDouble(5, updatedRect.getMaxLon());
        pstmt.setString(6, updatedRect.getCreatedAt().toString());
        pstmt.setString(7, now.toString());
        pstmt.setString(8, updatedRect.getAssignedUserId() != null ? updatedRect.getAssignedUserId().toString() : null);
        pstmt.setString(9, updatedRect.getColor() != null ? updatedRect.getColor() : null);

        pstmt.executeUpdate();
    }

    //Delete Rect from table
    public void deleteDrawnRect(UUID id) {
        String sql = """
                DELETE FROM drawnRect WHERE id = ?
                """;
        try (Connection conn = DriverManager.getConnection(dbURL)){
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, id.toString());

            //check if deleted
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Failed to delete Drawn Rect. No rows affected.");
            }
            System.out.println("Deleted Drawn Rect " + id);

        } catch (SQLException e) {
            System.out.println("SQLException in RectRepo, when trying to initialize or get access to the DB: " + e.getMessage());
        }
    }

    //Get Rect from table
    public Optional<BoundsRect> getDrawnRect(UUID id) {
        String sql = """
                SELECT * FROM drawnRect WHERE id = ?
                """;

        try (Connection conn = DriverManager.getConnection(dbURL)) {
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setString(1, id.toString());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                System.out.println("Found Drawn Rect. ID: " + id);
                return Optional.of(setPOIFromResultSet(rs));
            }
            System.out.println("No rect of id: " + id + " found.");
        } catch (SQLException e) {
            System.out.println("SQLException in RectRepo, when trying to initialize or get access to the DB: " + e.getMessage());
        }
        return Optional.empty();
    }

    //Get all drawn rects assigned to user id
    public List<BoundsRect> getDrawnRectsByUserId(UUID assignedUserId) {
        List<BoundsRect> rects = new ArrayList<>();
        String sql = """
                SELECT * FROM drawnRect WHERE assignedUserId = ?
                """;
        try(Connection conn = DriverManager.getConnection(dbURL)) {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, assignedUserId.toString());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                rects.add(setPOIFromResultSet(rs));
                System.out.println("Found Drawn Rect by id: " + rs.getString("id") + " from user: " + assignedUserId);
            }
        } catch (SQLException e) {
            System.out.println("SQLException in RectRepo, when trying to initialize or get access to the DB: " + e.getMessage());
        }
        return rects;
    }

    //Get all rect from table
    public List<BoundsRect> getAllDrawnRect() {
        List<BoundsRect> rects = new ArrayList<>();
        String sql = """
                SELECT * FROM drawnRect;
                """;

        try(Connection conn = DriverManager.getConnection(dbURL)) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                rects.add(setPOIFromResultSet(rs));
                System.out.println("Found Drawn Rect. ID: " + rs.getString("id"));
            }
        } catch ( SQLException e ) {
            System.out.println("SQLException in RectRepo, when trying to initialize or get access to the DB: " + e.getMessage());
        }
        return rects;
    }

    //Helper for result set in getDraw  nRect
    private BoundsRect setPOIFromResultSet(ResultSet rs) throws SQLException {
        String createdAtStr = rs.getString("createdAt");
        String updatedAtStr = rs.getString("updatedAt");
        LocalDateTime createdAt = LocalDateTime.parse(createdAtStr.replace(" ", "T"));
        LocalDateTime updatedAt = null;
        if (updatedAtStr != null) {
            updatedAt = LocalDateTime.parse(updatedAtStr.replace(" ", "T"));
        }

        String assignedUserIdString = rs.getString("assignedUserId");
        UUID assignedUserId = (assignedUserIdString != null && !assignedUserIdString.isEmpty()) ? UUID.fromString(assignedUserIdString) : null;

        return new BoundsRect(
                UUID.fromString(rs.getString("id")),
                rs.getDouble("minLat"),
                rs.getDouble("minLon"),
                rs.getDouble("maxLat"),
                rs.getDouble("maxLon"),
                createdAt,
                updatedAt,
                assignedUserId,
                rs.getString("color")
        );
    }

    public Optional<LocalDateTime> getCreatedAt(UUID id) {
        String sql = "SELECT createdAt FROM drawnRect WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(dbURL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id.toString());

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String createdAtStr = rs.getString("createdAt");

                if (createdAtStr == null) {
                    return Optional.empty();
                }

                LocalDateTime createdAt =
                        LocalDateTime.parse(createdAtStr.replace(" ", "T"));

                return Optional.of(createdAt);
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
