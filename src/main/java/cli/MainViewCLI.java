package cli;
import java.util.Scanner;

public class MainViewCLI {
    private final Scanner scanner = new Scanner(System.in);

    public void start() {
        while (true) {
            System.out.println("\n=== SMART GARAGE ===");
            System.out.println("1. Login");
            System.out.println("2. Registrazione");
            System.out.println("0. Esci");
            System.out.print("Scelta: ");

            String input = scanner.nextLine();

            switch (input) {
                case "1":
                    showLoginOptions();
                    break;
                case "2":
                    showRegistrationOptions();
                    break;
                case "0":
                    System.out.println("Uscita dall'applicazione. Arrivederci!");
                    return;
                default:
                    System.out.println("Scelta non valida. Riprova.");
            }
        }
    }

    private void showLoginOptions() {
        System.out.println("\n--- LOGIN ---");
        System.out.println("1. Login con Google");
        System.out.println("2. Login Locale");
        System.out.println("0. Indietro");
        System.out.print("Scelta: ");
        String choice = scanner.nextLine();

        switch (choice) {
            case "1":
                new GoogleLoginViewCLI().start();
                break;
            case "2":
                new LoginViewCLI().start();
                break;
            case "0":
                return;
            default:
                System.out.println("Scelta non valida.");
        }
    }

    private void showRegistrationOptions() {
        System.out.println("\n--- REGISTRAZIONE ---");
        System.out.println("1. Registrazione con Google");
        System.out.println("2. Registrazione Locale");
        System.out.println("0. Indietro");
        System.out.print("Scelta: ");
        String choice = scanner.nextLine();

        switch (choice) {
            case "1":
                new GoogleRegistrationViewCLI().start();
                break;
            case "2":
                new RegistrationViewCLI().start();
                break;
            case "0":
                return;
            default:
                System.out.println("Scelta non valida.");
        }
    }
}
