package cli;

import boundary.UserBoundary;
import controller.GoogleLoginManager;

public class GoogleRegistrationViewCLI {
    private final UserBoundary userBoundary = new UserBoundary();

    public void start() {
        System.out.println("\n--- REGISTRAZIONE CON GOOGLE ---");

        try {
            GoogleLoginManager googleLoginManager = new GoogleLoginManager();
            String email = googleLoginManager.getEmailFromGoogle();
            String username = email.split("@")[0];

            userBoundary.registerGoogleUser(username, email);
            System.out.println("Registrazione con Google completata con successo, ora puoi effettuare il login con Google.");
        } catch (Exception ex) {
            System.out.println("Errore: " + ex.getMessage());
        }




    }
}