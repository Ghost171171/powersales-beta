package CreateUserSystem;

import com.model.enums.AuthRole;
import com.model.user.User;
import com.repository.UserRepository;
import com.service.UserService;

import java.util.Scanner;

//Kevin_Trost;WF09r63j0++C , David_Zagajnov;Kl:-R4o++63s

//CREATE USER SYSTEM short CUS
public class CUS {
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        UserService userService = new UserService(UserRepository.getInstance());

        System.out.println("============ Create User System ============\nErstelle Nutzer für das Hauptsystem dem ___");
        System.out.println("Um das Programm zu starten, gebe 'Ja' ein, um das Programm zu schließen 'Nein': ");
        boolean start;
        String startStr = scanner.nextLine();
        if (startStr.equals("Ja")) {
            System.out.println("Starte Programm, viel Spaß!");
            start = true;
            while (start) {
                System.out.println("(1. Erstelle Nutzer /2.Aktualisier Nutzer /3. Lösche Nutzer /4. Schließe Programm)\nGebe eine gültige Zahl von 1 bis 4 ein: ");
                int switchCase = scanner.nextInt();

                switch (switchCase) {
                    case 1:
                        createUser(userService);
                        break;
                    case 2:
                        updateUser(userService);
                        break;
                    case 3:
                        deleteUser(userService);
                        break;
                    case 4:
                        System.out.println("============ Schließe Programm, vielen Dank für die Nutzung des CUS. ============");
                        start = false;
                        break;
                    default:
                        System.out.println("Gebe eine gültige Eingabe ein!");
                        break;
                }
            }
        } else {
            System.out.println("============ Schließe Programm, vielen Dank für die Nutzung des CUS. ============");
        }
    }

    static void createUser(UserService userService) {
        scanner.nextLine();
        System.out.println("Gebe den Namen des Nutzers ein(statt Leerzeichen, nutze _): ");
        String username = scanner.nextLine();
        System.out.println("Gebe das Passwort des Nutzers ein: ");
        String password = scanner.nextLine();
        System.out.println("Gebe de Rolle des Nutzers ein (User/Admin): ");
        String role = scanner.nextLine();
        if (role.equals("User")) {
            role = "USER";
        }
        else if (role.equals("Admin")) {
            role = "ADMIN";
        }
        else {
            System.out.println("Keine gültige Eingabe breche Erstellung des Nutzers ab!");
            return;
        }
        AuthRole authRole = AuthRole.valueOf(role);
        System.out.println("Erstelle Nutzer mit Namen: " + username + " der Rolle " + authRole + " !");
        User newUser = new User(username, password, authRole);
        userService.addUser(newUser);
        System.out.println("Erfolgreich gespeichert!");
    }
    static void updateUser(UserService userService) {
        System.out.println("Noch nicht verfügbar!");
    }
    static void deleteUser(UserService userService) {
        System.out.println("Gebe den Namen des Nutzers ein: ");
        String username = scanner.nextLine();
        User foundUser = userService.getUserByUsername(username);
        if (foundUser == null) {
            System.out.println("Etwas ging schief!");
            return;
        }
        System.out.println("Bist du sicher, das du " + username + " aus der Datenbank entfernen willst?\n Wenn ja, gebe 'Ja' ein, wenn nicht 'nein':");
        String answer = scanner.nextLine();
        if (answer.equals("Ja")) {
            userService.deleteUser(foundUser.getId());
            System.out.println("Erfolgreich " + username + " gelöscht!");
        } else {
            System.out.println("Breche Vorgang ab!");
        }
    }
}
