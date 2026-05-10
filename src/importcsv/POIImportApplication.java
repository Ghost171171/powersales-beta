package importcsv;

import com.model.poi.POI;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.List;



/// WICHTIG NICHT STARTEN NUR ADMIN
public class POIImportApplication {
    private static final int BATCH_SIZE = 1000;

    public static void main(String[] args) {
        String filePath = "/Users/ghost/Desktop/Ordner/Projekte/BerAlgSim/src/importcsv/interpreter.csv";
        //1. Erzeuge eine Liste mit dem POIs aus der CSV-Datei
        List<POI> pois;
        try {
            pois = POIImport.importFromCSV(filePath);
            System.out.println("Gelesene POI: " + pois.size());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //2. Erzeuge eine SQLite Datasource
        DataSource ds = createDataSource();

        //3. Erstelle den inserter anhand der datasource
        POIBatchInserter inserter = new POIBatchInserter(ds);

        //4. Füge die Batches in die Datenbank ein
        for (int i = 0; i < pois.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, pois.size());
            List<POI> batch = pois.subList(i, end);

            inserter.insertBatch(batch);
            System.out.println("Eingefügter batch: " + i + " - " + end);
        }

        System.out.println("Import abgeschlossen!");
    }

    private static DataSource createDataSource() {
        org.sqlite.SQLiteDataSource ds = new org.sqlite.SQLiteDataSource();
        ds.setUrl("jdbc:sqlite:pois.db");
        return ds;
    }
}
