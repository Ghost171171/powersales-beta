package com.repository;

import com.model.enums.AuthRole;
import com.model.user.User;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class UserRepository {
    private final String dbURL = "jdbc:sqlite:pois.db";

    //Singleton-Instanz
    private static UserRepository instance;

    //Instanziiere das Repository
    private UserRepository() {
        try (Connection conn = DriverManager.getConnection(dbURL);
             Statement stmt = conn.createStatement()) {

            // Tabelle erstellen, falls sie nicht existiert
            stmt.executeUpdate("""
                  CREATE TABLE IF NOT EXISTS users (
                           id TEXT PRIMARY KEY,
                           username TEXT NOT NULL,
                           password TEXT NOT NULL,
                           role TEXT NOT NULL
                  );
                  """);

        } catch (SQLException e) {
            System.out.println("SQLException, when trying to initialize or get access to the Databank: " + e.getMessage());
        }
    }

    // Singleton Zugriff
    public static synchronized UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }
        return instance;
    }

    //ADD USER
    public void addUser(User user) {
        //sicherstellen das id gesetzt
        if (user.getId() == null) {
            user.setId(UUID.randomUUID());
        }
        String sql = """
                INSERT INTO users (id, username, password, role)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conn = DriverManager.getConnection(dbURL)) {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            prepareStatement(pstmt, user);
            System.out.println("Inserted User " + user.getId().toString() + " into DB");

        } catch (SQLException e) {
            System.out.println("SQLException, when trying to initialize or get access to the DB: " + e.getMessage());
        }
    }

    //UPDATE USER DATA
    public void updateUser(User updatedUser) {
        String sql = """
                INSERT INTO users(
                    id, username, password, role) VALUES (?, ?, ?, ?)
                    ON CONFLICT (id) DO UPDATE SET
                    username = excluded.username,
                    password = excluded.password,
                    role = excluded.role
                """;

        try (Connection conn = DriverManager.getConnection(dbURL)) {
            PreparedStatement stmt = conn.prepareStatement(sql);

            prepareStatement(stmt, updatedUser);
            System.out.println("Updating User..." + updatedUser.getId().toString());
        } catch (SQLException e) {
            System.out.println("SQLException, when trying to initialize or get access to the DB: " + e.getMessage());
        }
    }

    //DELETE USER
    public void deleteUser(UUID id) {
        String sql = """
                DELETE FROM users WHERE id = ?
                """;
        try (Connection conn = DriverManager.getConnection(dbURL)){
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, id.toString());

            //check if deleted
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Failed to delete User. No rows affected.");
            }
            System.out.println("Deleted User " + id);

        } catch (SQLException e) {
            System.out.println("SQLException, when trying to initialize or get access to the DB: " + e.getMessage());
        }
    }

    //GET USER
    public Optional<User> getUser(UUID id) {
        String sql = """
                SELECT * FROM users WHERE id = ?
                """;

        try (Connection conn = DriverManager.getConnection(dbURL)) {
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setString(1, id.toString());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                System.out.println("Found User. ID: " + id);
                return Optional.of(setUserFromResultSet(rs));
            }
            System.out.println("No User of id: " + id + " found.");
        } catch (SQLException e) {
            System.out.println("SQLException in UserRepository, when trying to initialize or get access to the DB: " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<User> getUserByName(String username) {
        String sql = """
                SELECT * FROM users WHERE username = ?
                """;

        try (Connection conn = DriverManager.getConnection(dbURL)) {
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                System.out.println("Found User. Username: " + username);
                return Optional.of(setUserFromResultSet(rs));
            }
            System.out.println("No User of username: " + username + " found.");
        } catch (SQLException e) {
            System.out.println("SQLException in UserRepository, when trying to initialize or get access to the DB: " + e.getMessage());
        }
        return Optional.empty();
    }

    //GET ALL USERS
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = """
                SELECT * FROM users;
                """;

        try(Connection conn = DriverManager.getConnection(dbURL)) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                users.add(setUserFromResultSet(rs));
                System.out.println("Found User. ID: " + rs.getString("id"));
            }
        } catch ( SQLException e ) {
            System.out.println("SQLException in UserRepository, when trying to initialize or get access to the DB: " + e.getMessage());
        }
        return users;
    }

    //HELPER
    //PREPARE A STATEMENT FOR USER
    private void prepareStatement(PreparedStatement pstmt, User rawUser) throws SQLException {

        pstmt.setString(1, rawUser.getId().toString());
        pstmt.setString(2, rawUser.getUsername());
        pstmt.setString(3, rawUser.getPassword());
        pstmt.setString(4, rawUser.getRole().toString());

        pstmt.executeUpdate();
    }
    //SET RESULT SET FOR USER
    private User setUserFromResultSet(ResultSet rs) throws SQLException {
        String roleString = rs.getString("role");
        if (roleString == null || roleString.isEmpty()) {
            throw new SQLException("Failed to set user role");
        }
        return new User(
                UUID.fromString(rs.getString("id")),
                rs.getString("username"),
                rs.getString("password"),
                AuthRole.valueOf(roleString)
        );
    }
}
