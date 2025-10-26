package cli;

import bean.UserBean;
import boundary.UserBoundary;
import controller.GoogleLoginManager;

public class GoogleLoginViewCLI {
    private final UserBoundary userBoundary = new UserBoundary();

    public void start() {
        System.out.println("\n--- LOGIN CON GOOGLE ---");

        try {
            GoogleLoginManager googleLoginManager = new GoogleLoginManager();
            String email = googleLoginManager.getEmailFromGoogle();
            String username = email.split("@")[0];

            UserBean user = userBoundary.getUser(username);

            if (user == null) {
                System.out.println("Utente non registrato. Effettua prima la registrazione con Google.");
                return;
            }

            System.out.println("Login con Google riuscito. Benvenuto " + username + "!");
            GarageManagerViewCLI.start(username);

        } catch (Exception e) {
            System.out.println("Errore durante il login con Google: " + e.getMessage());
        }
    }
}
