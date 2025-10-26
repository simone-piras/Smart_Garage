package cli;

import boundary.UserBoundary;

import java.util.Scanner;

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
