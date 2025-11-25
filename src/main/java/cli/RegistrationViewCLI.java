package cli;

import boundary.UserBoundary;
import exception.DuplicateUsernameException;

import java.util.Scanner;

/*
 Utilizza System.out per l'interazione utente in ambiente CLI.
 In un'applicazione CLI, System.out è lo standard per l'output utente.
 L'uso di logger sarebbe inappropriato per l'interazione diretta con l'utente.
 */
@SuppressWarnings("java:S106")
public class RegistrationViewCLI {
    private final Scanner scanner = new Scanner(System.in);
    private final UserBoundary userBoundary = new UserBoundary();

    public void start() {
        System.out.println("\n--- REGISTRAZIONE LOCALE ---");

        System.out.print("Scegli un username: ");
        String username = scanner.nextLine();

        System.out.print("Inserisci una password: ");
        String password = scanner.nextLine();

        System.out.print("Inserisci la tua email: ");
        String email = scanner.nextLine();
        try{
            userBoundary.registerUser(username, password, email);
            System.out.println("Registrazione avvenuta con successo. Ora puoi effettuare il login.");
        } catch (DuplicateUsernameException e) {
            System.out.println("Errore: " + e.getMessage());

        }
    }
}
