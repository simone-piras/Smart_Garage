package cli;

import boundary.UserBoundary;

import java.util.Scanner;


/*
 Utilizza System.out per l'interazione utente in ambiente CLI.
 In un'applicazione CLI, System.out è lo standard per l'output utente.
 L'uso di logger sarebbe inappropriato per l'interazione diretta con l'utente.
 */
@SuppressWarnings("java:S106")
public class LoginViewCLI {
    private final Scanner scanner = new Scanner(System.in);
    private final UserBoundary userBoundary = new UserBoundary();

    public void start() {
        System.out.println("\n--- LOGIN LOCALE ---");

        System.out.print("Username: ");
        String username = scanner.nextLine();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        boolean success = userBoundary.loginUser(username, password);

        if (success) {
            System.out.println("Login riuscito. Benvenuto/a " + username + "!");
            GarageManagerViewCLI.start(username);
        } else {
            System.out.println("Credenziali non valide. Riprova.");
        }
    }
}