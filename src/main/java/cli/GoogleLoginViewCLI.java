package cli;

import boundary.UserBoundary;
import controller.GoogleLoginManager;


@SuppressWarnings("java:S106")
public class GoogleLoginViewCLI {
    private final UserBoundary userBoundary = new UserBoundary();

    public void start() {
        System.out.println("\n--- LOGIN CON GOOGLE ---");

        try {
            GoogleLoginManager googleLoginManager = new GoogleLoginManager();
            String email = googleLoginManager.getEmailFromGoogle();


            boolean success = userBoundary.loginWithGoogle(email);

            if (success) {
                String username = email.split("@")[0];
                System.out.println("Login con Google riuscito. Benvenuto " + username + "!");
                GarageManagerViewCLI.start(username);
            } else {
                System.out.println("Utente non registrato. Effettua prima la registrazione con Google.");
            }

        } catch (Exception e) {
            System.out.println("Errore durante il login con Google: " + e.getMessage());
        }
    }
}