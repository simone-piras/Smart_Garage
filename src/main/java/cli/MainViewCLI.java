package cli;
import java.util.Scanner;

/*
 Utilizza System.out per l'interazione utente in ambiente CLI.
 In un'applicazione CLI, System.out è lo standard per l'output utente.
 L'uso di logger sarebbe inappropriato per l'interazione diretta con l'utente.
 */
@SuppressWarnings("java:S106")
public class MainViewCLI {
    private final Scanner scanner = new Scanner(System.in);

    //COSTANTI PER RISOLVERE GLI ISSUE
    private static final String PROMPT_SCELTA = "Scelta: ";
    private static final String MSG_SCELTA_NON_VALIDA = "Scelta non valida.";

    public void start() {
        while (true) {
            System.out.println("\n=== SMART GARAGE ===");
            System.out.println("1. Login");
            System.out.println("2. Registrazione");
            System.out.println("0. Esci");
            System.out.print(PROMPT_SCELTA);

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
                    System.out.println(MSG_SCELTA_NON_VALIDA + " Riprova.");
            }
        }
    }

    private void showLoginOptions() {
        System.out.println("\n--- LOGIN ---");
        System.out.println("1. Login con Google");
        System.out.println("2. Login Locale");
        System.out.println("0. Indietro");
        System.out.print(PROMPT_SCELTA);
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
                System.out.println(MSG_SCELTA_NON_VALIDA);
        }
    }

    private void showRegistrationOptions() {
        System.out.println("\n--- REGISTRAZIONE ---");
        System.out.println("1. Registrazione con Google");
        System.out.println("2. Registrazione Locale");
        System.out.println("0. Indietro");
        System.out.print(PROMPT_SCELTA);
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
                System.out.println(MSG_SCELTA_NON_VALIDA);
        }
    }
}