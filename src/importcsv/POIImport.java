package importcsv;


import com.model.poi.Address;
import com.model.poi.POI;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

//Klasse die eine CSV Datei in unsere POI Logik importieren soll bzw. uminterpretieren soll
public class
POIImport {
    public static List<POI> importFromCSV(String pathFile) throws IOException {
        List<POI> pois = new ArrayList<>();

        try (Reader reader = new FileReader(pathFile)) {
            CSVFormat format = CSVFormat.DEFAULT
                    .withFirstRecordAsHeader()   // Header überspringen
                    .withDelimiter('\t')         // Tab als Trennzeichen
                    .withIgnoreSurroundingSpaces();

            Iterable<CSVRecord> records = format.parse(reader);

            for (CSVRecord record : records) {
                try {
                    POI poi = new POI(new Address(
                            record.get(2),               // street
                            record.get(3),               // houseNumber
                            record.get(4),               // plz
                            record.get(5),               // city
                            Double.parseDouble(record.get(0)), // latitude
                            Double.parseDouble(record.get(1))  // longitude
                    ));
                    pois.add(poi);
                } catch (Exception e) {
                    System.out.println("Ungültige Zeile übersprungen: " + record);
                }
            }
        }
        return pois;
    }

    private static POI generatePOIFromCSV(String[] line) {
        double latitude = Double.parseDouble(line[0]);
        double longitude = Double.parseDouble(line[1]);
        String street = line[2].trim();
        String houseNumber = line[3].trim();
        String plz = line[4].trim();
        String city = line[5].trim();

        Address address = new Address(street, houseNumber, plz, city, latitude, longitude);
        return new POI(address);
    }

    public static void main(String[] args) throws IOException {
        String csvFile = "/Users/ghost/Desktop/Ordner/Projekte/BerAlgSim/src/importcsv/interpreter.csv"; // Pfad zu deiner CSV-Datei
        List<POI> pois;

        try {
            pois = importFromCSV(csvFile);
            System.out.println("Gelesene POIs: " + pois.size());
            for (POI poi : pois) {
                System.out.println(poi);
            }
        } catch (Exception e) {
            System.err.println("Fehler beim Einlesen der CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
