package com.repository;

import com.model.contract.Contract;
import com.model.enums.ContractProcessStatus;
import com.model.enums.ContractType;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ContractRepository {
    private final String dbURL = "jdbc:sqlite:pois.db";

    //Singleton-Instanz
    private static ContractRepository instance;

    //Instanziiere das Repository
    private ContractRepository() {
        try (Connection conn = DriverManager.getConnection(dbURL);
             Statement stmt = conn.createStatement()) {

            // Tabelle erstellen, falls sie nicht existiert
            stmt.executeUpdate("""
                  CREATE TABLE IF NOT EXISTS contracts (
                          id TEXT PRIMARY KEY,
                          contractValue DOUBLE NOT NULL,
                          userId TEXT NOT NULL,
                          contractCompletedDateTime TEXT NOT NULL,
                          contractProcessStatus VARCHAR(20) NOT NULL
                  );
                  """);

        } catch (SQLException e) {
            System.out.println("SQLException, when trying to initialize or get access to the Databank: " + e.getMessage());
        }
    }

    // Singleton Zugriff
    public static synchronized ContractRepository getInstance() {
        if (instance == null) {
            instance = new ContractRepository();
        }
        return instance;
    }

    //ADD CONTRACT
    public void addContract(Contract contract) {
        //sicherstellen das id gesetzt
        if (contract.getId() == null) {
            contract.setId("");
        }
        String sql = """
                INSERT INTO contracts (id, contractValue, userId, contractCompletedDateTime, contractProcessStatus) VALUES (?, ?, ?, ?, ?);
                """;

        try (Connection conn = DriverManager.getConnection(dbURL)) {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            prepareStatement(pstmt, contract);
            System.out.println("Inserted Contract " + contract.getId() + " into DB");

        } catch (SQLException e) {
            System.out.println("SQLException, when trying to initialize or get access to the DB: " + e.getMessage());
        }
    }

    //UPDATE USER DATA
    public void updateContract(Contract updatedContract) {
        String sql = """
                INSERT INTO contracts(
                    id, contractValue, userId, contractCompletedDateTime, contractProcessStatus) VALUES (?, ?, ?, ?, ?)
                    ON CONFLICT (id) DO UPDATE SET
                    contractValue = excluded.contractValue,
                    userId = excluded.userId,
                    contractProcessStatus = excluded.contractProcessStatus;
                """;

        try (Connection conn = DriverManager.getConnection(dbURL)) {
            PreparedStatement stmt = conn.prepareStatement(sql);

            prepareStatementUpdate(stmt, updatedContract);
        } catch (SQLException e) {
            System.out.println("SQLException, when trying to initialize or get access to the DB: " + e.getMessage());
        }
    }

    //DELETE USER
    public void deleteContract(UUID id) {
        String sql = """
                DELETE FROM contracts WHERE id = ?;
                """;
        try (Connection conn = DriverManager.getConnection(dbURL)){
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, id.toString());

            //check if deleted
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Failed to delete Contract. No rows affected.");
            }
            System.out.println("Deleted Contract:" + id);

        } catch (SQLException e) {
            System.out.println("SQLException, when trying to initialize or get access to the DB: " + e.getMessage());
        }
    }

    //GET CONTRACT
    public Optional<Contract> getContract(String id) {
        String sql = """
                SELECT * FROM contracts WHERE id = ?;
                """;

        try (Connection conn = DriverManager.getConnection(dbURL)) {
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                System.out.println("Found Contract. ID: " + id);
                return Optional.of(setContractFromResultSet(rs));
            }
            System.out.println("No Contract of id: " + id + " found.");
        } catch (SQLException e) {
            System.out.println("SQLException in UserRepository, when trying to initialize or get access to the DB: " + e.getMessage());
        }
        return Optional.empty();
    }

    //GET ALL CONTRACTS BY USER ID
    public List<Contract> getContractsByUserId(UUID id) {
        List<Contract> contracts = new ArrayList<>();
        String sql = """
                SELECT * FROM contracts WHERE userId = ?;
                """;

        try(Connection conn = DriverManager.getConnection(dbURL)) {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, id.toString());

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                contracts.add(setContractFromResultSet(rs));
                System.out.println("Found Contract. ID: " + rs.getString("id"));
            }
        } catch ( SQLException e ) {
            System.out.println("SQLException in UserRepository, when trying to initialize or get access to the DB: " + e.getMessage());
        }
        return contracts;
    }

    //GET ALL CONTRACTS
    public List<Contract> getAllContracts() {
        List<Contract> contracts = new ArrayList<>();
        String sql = """
                SELECT * FROM contracts;
                """;

        try(Connection conn = DriverManager.getConnection(dbURL)) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                contracts.add(setContractFromResultSet(rs));
                System.out.println("Found Contract. ID: " + rs.getString("id"));
            }
        } catch ( SQLException e ) {
            System.out.println("SQLException in UserRepository, when trying to initialize or get access to the DB: " + e.getMessage());
        }
        return contracts;
    }

    //HELPER
    //PREPARE A STATEMENT FOR USER
    private void prepareStatement(PreparedStatement pstmt, Contract rawContract) throws SQLException {

        pstmt.setString(1, rawContract.getId());
        pstmt.setDouble(2, rawContract.getContractType().getValue());
        pstmt.setString(3, rawContract.getUserId().toString());
        pstmt.setString(4, rawContract.getContractCompletedDateTime().toString());
        pstmt.setString(5, rawContract.getContractProcessStatus().toString());

        pstmt.executeUpdate();
    }
    private void prepareStatementUpdate(PreparedStatement pstmt, Contract rawContract) throws SQLException {

        pstmt.setString(1, rawContract.getId());
        pstmt.setDouble(2, rawContract.getContractType().getValue());
        pstmt.setString(3, rawContract.getUserId().toString());
        pstmt.setString(5, rawContract.getContractProcessStatus().toString());

        pstmt.executeUpdate();
    }

    //SET RESULT SET FOR USER
    private Contract setContractFromResultSet(ResultSet rs) throws SQLException {
        double valueContract = rs.getDouble("contractValue");
        ContractType contractType = ContractType.fromValue(valueContract);
        String contractCompletedDateTimeStr = rs.getString("contractCompletedDateTime");
        if (contractCompletedDateTimeStr == null || contractCompletedDateTimeStr.isEmpty()) {
            throw new SQLException("Failed to set contract completed date");
        }
        //Für Legacy Verträge
        ContractProcessStatus cPS;
        if (rs.getString("contractProcessStatus") == null) {
            cPS = ContractProcessStatus.IN_PROGRESS;
        } else {
            cPS = ContractProcessStatus.valueOf(rs.getString("contractProcessStatus"));
        }

        return new Contract(
                rs.getString("id"),
                contractType,
                UUID.fromString(rs.getString("userId")),
                cPS,
                LocalDateTime.parse(rs.getString("contractCompletedDateTime"))
        );
    }
}
