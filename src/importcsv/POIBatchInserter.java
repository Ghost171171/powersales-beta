package importcsv;

import com.model.poi.Address;
import com.model.poi.POI;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

//Hier wird unser uminterpretierte Batch aus der CSV in die Sql Datenbank uminterpretiert

class POIBatchInserter {
    private final DataSource dataSource;

    POIBatchInserter(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insertBatch(List<POI> pois) {
        String sql = """
                INSERT INTO poi (id, street, houseNumber, plz, location, latitude, longitude, lastVisit, visitStatus, contractStatus, note)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement psmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            for (POI poi : pois) {
                psmt.setString(1, poi.getId().toString());

                Address address = poi.getAddress();
                psmt.setString(2, address.getStreet());
                psmt.setString(3, address.getHouseNumber());
                psmt.setString(4, address.getPlz());
                psmt.setString(5, address.getLocation());
                psmt.setDouble(6, address.getLatitude());
                psmt.setDouble(7, address.getLongitude());

                psmt.setString(8, poi.getLastVisit().toString());
                psmt.setString(9, poi.getVisitStatus().toString());
                psmt.setString(10, poi.getContractStatus().toString());
                psmt.setString(11, poi.getNotes());

                psmt.addBatch();
            }
            psmt.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
